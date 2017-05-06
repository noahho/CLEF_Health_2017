function cost = estimate_thresh_mod_3(scores, relevances, static_pd_npos, static_pd_pos, query_hardness)
    cost = ones(numel(scores), 1);
    cost(ceil(numel(scores)*(0.25 + query_hardness))) = 0;
end
