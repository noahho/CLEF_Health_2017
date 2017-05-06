function [error_c, error_r, error_s, ind_m, rf] = test_threshold(topic, cum, topic_scores, j, topic_inds, rel_inds, test_topic, method, pd_static_pos)
    static_pd_npos = 0;
    
    [cumt, topic_scorest, jt, topic_indst, rel_indst] = load_topic(test_topic, cum, topic_scores, j, topic_inds, rel_inds, method);

    query_hardness = query_hardness_estimator(topic_scorest);
    
    X = [0:jt-3];
    penalty = (cumt / max(cumt)).*(2 * (numel(X)-X'));
    work = X';
    cost = (work + penalty)/numel(work);
    
    if method == 1 | method == 4
        cost_model = estimate_thresh_mod_1(topic_scorest, rel_indst, static_pd_npos, pd_static_pos, query_hardness);
        rf = zeros(numel(topic_scorest),1);
        figure()
        plot(X, cost, 'b', X, cost_model, 'r')
    end
    if method == 2
        [cost_model, cost_dynamic, cost_static, rf] = estimate_thresh_mod_2(topic_scorest, rel_indst, static_pd_npos, pd_static_pos, query_hardness);
        figure()
        plot(X, cost, 'b', X, cost_model, 'r', X, cost_dynamic, X, cost_static)
    end
    if method == 3
        cost_model = estimate_thresh_mod_3(topic_scorest, rel_indst, static_pd_npos, pd_static_pos, query_hardness);
        rf = zeros(numel(topic_scorest),1);
        figure()
        plot(X, cost, 'b', X, cost_model, 'r')
    end
    
    [~, ind] = min(cost);
    [~, ind_m] = min(cost_model);
    error_c = abs(cost(ind) - cost(ind_m));
    error_r = abs(ind - ind_m);
    error_s = abs(topic_scorest(ind) - topic_scorest(ind_m));
end