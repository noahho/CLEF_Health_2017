addpath('functions')
addpath('VbGm')
addpath('EmGm')

topics = [1 2 3 4 5 6 7];
error_c = zeros(numel(topics), 1);
error_r = zeros(numel(topics), 1);
error_s = zeros(numel(topics), 1);

method = 3;

for topic = topics
    train_topics = topics(topics ~= topic);
    [pd_static_pos, ~] = learn_threshold(train_topics, cum, topic_scores, j, topic_inds, rel_inds, method);
    [error_c(topic), error_r(topic), error_s(topic), ind] = test_threshold(topic, cum, topic_scores, j, topic_inds, rel_inds, topic, method, pd_static_pos);
end
mean(error_c)