method = 1;
[pd_static_pos, static_pd_npos] = learn_threshold(train_topics, cum, topic_scores, j, topic_inds, rel_inds, method);
save pd_static_pos.mat pd_static_pos