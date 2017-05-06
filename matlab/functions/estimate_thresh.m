function [cost, cost_dynamic, cost_static] = estimate_thresh(scores, relevances, static_pd_npos, static_pd_pos, static_pd_neg)
    cost = zeros(numel(scores), 1);
    cost_dynamic = zeros(numel(scores), 1);
    cost_static = zeros(numel(scores), 1);
    
    rf = zeros(numel(scores), 1);
    
    p_pos = static_pd_npos.mu;
    
    rf_rels = ones(numel(scores), 1) * -1;
    samples = zeros(0);
    while sum(rf_rels(samples) == 1) < 3
        % Pick the number of samples such that with 95% probability there is
        % positive sample
        % (1-p_pos)*(1-p_pos)^n_rf = 0.05
        % n_rf = log_(1-p_pos)(0.05)
        n_rf = ceil(log(0.05)/log((1-p_pos)));

        % Pick random samples for relevance feedback
        %samples = randperm(numel(scores), n_rf);
        samples = 1:numel(scores);
        rf(samples) = 1;

        % estimate dynamic score distributions
        rf_rels(samples) = relevances(samples);
        
        dynamic_pd_npos = fitdist(rf_rels(rf_rels > -1),'Normal');
        dynamic_npos = dynamic_pd_npos.mu;
    end
    
    [pd_pos_dynamic, pd_neg_dynamic, ~, confidence] = estimate_pds_it(scores, rf_rels);
    
    p_pos_biased = pdf(pd_pos_dynamic,scores);
    % p_overall ~ p_neg
    pd_overall = fitdist(scores,'normal');
    p_overall = pdf(pd_overall,scores);
    pd_sample = fitdist(scores(rf == 1),'normal');
    p_sample = pdf(pd_sample,scores);
    p_pos = (p_overall)./(p_sample);
    %plot(scores, p_pos, scores, p_sample, scores, p_overall)
    plot(scores, p_pos_biased, 'r', scores, p_pos_biased .* p_pos)
    
    %plot the distributions
    figure()
    bincenters = [0:0.05:1];
    y1 = pdf(pd_pos_dynamic,bincenters');
    y2 = pdf(static_pd_pos,bincenters');
    plot(bincenters', y1, 'b', bincenters', y2, 'r');
    
    i = 1;    
    while i <= numel(scores)
        cost_static(i) = cost_model(i, scores, static_pd_pos, p_pos, rf);
        cost_dynamic(i) = cost_model(i, scores, pd_pos_dynamic, p_pos, rf);
        cost(i) = (cost_static(i) + cost_dynamic(i))/2;
        
        i = i + 1;
    end
end

function a = s_cdf(p, s)
    a = 0;
    for i = [2:numel(p)]
        a = a + (p(i)-p(i-1)) * s;
    end
end

function cost = cost_model_s(pos, scores, p_pos, rf)
    % what is the fraction of left relevant documents
    r_prob = s_cdf(p_pos, scores(pos));
    % what is the fraction of left documents
    n_prob = 1-(pos / numel(scores));
    % What is the cost of the relevance feedback?
    rf_cost = (2 * sum(rf) - sum(rf(1:pos)))/numel(scores);
    
    cost = r_prob * n_prob * 2 + (1 - n_prob) + rf_cost;
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