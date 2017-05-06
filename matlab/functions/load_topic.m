function [cumt, topic_scorest, jt, topic_indst, rel_indst] = load_topic(topic, cum, topic_scores, j, topic_inds, rel_inds, method)
    cumt = cum(:, topic);
    topic_scorest = topic_scores(:, topic);
    jt = j(topic);
    topic_indst = topic_inds(:, topic);
    rel_indst = rel_inds(:, topic);

    cumt = flipud(cumt(2:jt-1));
    topic_indst = flipud(topic_indst(2:jt-1));
    topic_scorest = flipud(topic_scorest(2:jt-1));
    rel_indst = flipud(rel_indst(2:jt-1));
    if (method == 4)
        topic_scorest = (numel(topic_scorest):-1:1)';
    end
    topic_scorest = topic_scorest - min(topic_scorest);
    topic_scorest = topic_scorest ./ max(topic_scorest);
    topic_scorest = topic_scorest + 1;
end