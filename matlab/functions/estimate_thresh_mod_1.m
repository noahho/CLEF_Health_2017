function cost = estimate_thresh_mod_1(scores, relevances, static_pd_npos, static_pd_pos, query_hardness)
    cost = zeros(numel(scores), 1);
    i = 1;
    while (i <= numel(scores))
        cost(i) = cost_model(i, scores, static_pd_pos);
        
        i = i + 1;
    end
end

function cost = cost_model(pos, scores, pd_pos)
    % what is the fraction of left relevant documents
    r_prob = cdf(pd_pos, scores(pos));
    % what is the fraction of left documents
    n_prob = 1-(pos / numel(scores));
    
    cost = r_prob * n_prob * 2 + (1 - n_prob);
end