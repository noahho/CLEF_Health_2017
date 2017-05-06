[cumt, topic_scorest, jt, topic_indst] = load_topic(topic, cum, topic_scores, j, topic_inds)

X = [0:jt-3];
wf = 1; % CA
wp = 2; % CP
% minimize the cost
gain = max(cumt)-cumt;
penalty = (cumt / max(cumt)).*(wp*(numel(X)-X'));
work = X'*wf;
cost = work + penalty;
figure()
plot(X, penalty ,'b', X, work, 'r', X, cost)
figure()
plot(1-topic_scorest, penalty ,'b', 1-topic_scorest, work, 'r', 1-topic_scorest, cost)

% maximize the utility
o95_rank = min(find(cumt < max(cumt)*0.05))
o95_score_thresh = topic_scorest(o95_rank)
o95_p_rank = o95_rank/size(cumt, 1)

[~, opt_rank] = min(cost)
opt_score = topic_scorest(opt_rank)
