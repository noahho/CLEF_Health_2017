addpath('functions')
addpath('VbGm')
addpath('EmGm')

topics = [1 2 3 4 5 6 7];
method = 1;
topics_ids = [8643, 10632, 10771, 009323, 008691, 009944, 011548];

pd_static_pos = load('pd_static_pos');
pd_static_pos = pd_static_pos.pd_static_pos;

ind = zeros(numel(topics));
final_scores = zeros(1,4);
for topic = topics
    [cumt, topic_scorest, jt, topic_indst, rel_indst] = load_topic(topic, cum, topic_scores, j, topic_inds, rel_inds, method);
    [~, ~, ~, ind(topic), rf] = test_threshold(topic, cum, topic_scores, j, topic_inds, rel_inds, topic, method, pd_static_pos);
    
    interaction = [rf(1:ind(topic)); zeros(numel(topic_indst) - ind(topic), 1)];
    score = [ones(numel(topic_indst), 1)*topics_ids(topic), interaction, topic_indst , topic_scorest];
    final_scores = [score; final_scores];
end

% SAVE TO TREC FILE
fileID = fopen('../data/trec_final','w');
fprintf(fileID,'CD%d\t%d\t%d\t0\t%f\t0\t\n',final_scores');