function [cost, cost_dynamic, cost_static, rf] = estimate_thresh_mod_2(scores, relevances, static_pd_npos, static_pd_pos, query_hardness)
    
    rf = zeros(numel(scores), 1);
    
    rf_rels = ones(numel(scores), 1) * -1;
    
    pd_overall = fitdist(scores,'normal');
    p_overall = pdf(pd_overall,scores);
    
    sampler_pd = makedist('Weibull','a',1,'b',0.2);
    p_sample = pdf(sampler_pd,(2-scores));
    
    % solange die ableitung der kosten nach der score negativ ist
    % wie viel besser wird mein modell 
    % wie viel spare ich durch ein besseres modell
    steps_from_last_pos = 0;
    steps_from_last_pos_d1 = 0;
    last_opt_cost = 1;
    updated = 0;
    while sum(rf)/numel(scores) < 0.05
        % APPLY RELEVANCE FEEDBACK
        % Pick random samples for relevance feedback
        rnd = random(sampler_pd);
        if (rnd > 1)
            continue;
        end
        
        % Update the relevance feedback
        [~, sample] = min(abs(scores-(2-rnd)));
        rf(sample) = 1;
        updated = rf_rels(sample) < 0;
        rf_rels(sample) = relevances(sample);
        
        if (rf_rels(sample) == 1 & updated)
            steps_from_last_pos_d1 = steps_from_last_pos;
            steps_from_last_pos = 0;
        else
            steps_from_last_pos = steps_from_last_pos + (2 - (rf_rels(sample) == 1)) * updated;
        end
    end
    
    [cost, cost_static, cost_dynamic] = estimate_model(scores, relevances, rf, rf_rels, p_sample, p_overall, static_pd_pos, query_hardness);
end

function [cost, cost_static, cost_dynamic] = estimate_model(scores, relevances, rf, rf_rels, p_sample, p_overall, static_pd_pos, query_hardness)
    cost = zeros(numel(scores), 1);
    cost_dynamic = zeros(numel(scores), 1);
    cost_static = zeros(numel(scores), 1);
    
    % ESTIMATE MODEL BASED ON RELEVANCE FEEDBACK
    pd_pos_correct = fitdist(scores(relevances > 0),'normal');
    pd_pos_biased = fitdist(scores(relevances > 0 & rf > 0),'normal');

    p_pos_biased = pdf(pd_pos_biased,scores);
    p_pos_correct = pdf(pd_pos_correct,scores);

    % Estmate unbiased mu
    p_pos = p_pos_biased .* (p_overall./(p_sample));
    p_pos = p_pos/trapz(2-scores, p_pos);

    [~, i] = max(p_pos);
    mu = scores(i);

    % Estimate unbiased sigma
    similarity_prev = 0;
    similarity = 0;
    sigma = pd_pos_biased.sigma;
    while (similarity >= similarity_prev)
        sigma = sigma * 1.1;
        pd_test = makedist('Normal','mu',mu,'sigma',sigma);
        similarity_prev = similarity;
        % Under my new sigma how probable are the sampled values
        similarity = model_similarity(pd_test, scores(rf_rels > 0), p_overall(rf_rels > 0));
    end
    
    pd_guessed = makedist('Normal','mu',mu,'sigma',sigma);
    p_guessed = pdf(pd_guessed, scores);
    
    simiarity_guessed = similarity;
    similarity_static = model_similarity(static_pd_pos, scores(rf_rels > 0), p_overall(rf_rels > 0));
    
    %plot(scores, p_pos, scores, p_sample, scores, p_overall)
    %plot(scores, p_pos_biased, 'r', scores, p_guessed, 'b', scores, p_pos_correct)
    
    % Estimate the cost for each cutoff according to our model
    i = 1;    
    while i <= numel(scores)
        w_static = similarity_static * (1-query_hardness);
        w_dynamic = simiarity_guessed + 2 * p_sample(i) * simiarity_guessed;
        w_static_n = w_static / (w_static + w_dynamic);
        w_dynamic_n = w_dynamic / (w_static + w_dynamic);
        if (w_dynamic == inf)
            w_dynamic_n = 1;
        end
        
        cost_static(i) = cost_model(i, scores, static_pd_pos, rf);
        cost_dynamic(i) = cost_model(i, scores, pd_guessed, rf);
        cost(i) = w_static_n * cost_static(i) + w_dynamic_n * cost_dynamic(i);
        
        i = i + 1;
    end
end

function similarity = model_similarity(pd, sampled, overall)
    similarity = sum(pdf(pd, sampled) .* overall);
end

function cost = cost_model(pos, scores, pd_pos, rf)
    % what is the fraction of left relevant documents
    r_prob = cdf(pd_pos, scores(pos));
    % what is the fraction of left documents
    n_prob = 1-(pos / numel(scores));
    % What is the cost of the relevance feedback?
    rf_cost = (2 * sum(rf) - sum(rf(1:pos)))/numel(scores);
    
    cost = r_prob * n_prob * 2 + (1 - n_prob) + rf_cost;
end