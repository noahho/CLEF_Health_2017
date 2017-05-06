function [pd_static_pos, static_pd_npos] = learn_threshold(train_topics, cum, topic_scores, j, topic_inds, rel_inds, method)
    scores = zeros(0);
    rf = zeros(0);
    
    topic_mus = zeros(numel(train_topics), 1);
    k = 1;
    for topic = train_topics
        [cumt, topic_scorest, jt, topic_indst, rel_indst] = load_topic(topic, cum, topic_scores, j, topic_inds, rel_inds, method);
        rnd = randi([1 numel(topic_scorest)],1,200);
        topic_scorest = topic_scorest(rnd);
        pd = fitdist(topic_scorest, 'Normal');
        topic_mus(k) = pd.mu;
        scores = [scores; topic_scorest];
        rf = [rf; rel_indst];
        k = k + 1;
    end
    %[y1, ~, ~] = mixGaussVb(scores,5);
    %pd_static_pos = fitgmdist(scores,numel(train_topics))
    pd_static_pos = fitdist(topic_mus*1.2,'Normal');
    %pd_static_pos = fitdist(scores,'Normal');
    static_pd_npos = fitdist(rf,'Normal');
end