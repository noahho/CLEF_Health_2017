function plot_topic_scores(topic, cum, topic_scores, j, topic_inds, rel_inds)
    [cumt, topic_scorest, jt, topic_indst, rel_indst] = load_topic(topic, cum, topic_scores, j, topic_inds, rel_inds);

    [bincounts,binedges] = histcounts(topic_scorest,50);
    bincenters = binedges(1:end-1)+diff(binedges)/2;
    binwidth = binedges(2)-binedges(1); % Finds the width of each bin
    area = numel(topic_scorest) * binwidth;
    topic_scorest_rel = topic_scorest(find(topic_scorest .* ([diff(cumt); 0]*-1) ~= 0));
    bincounts_rel = histcounts(topic_scorest_rel,binedges);
    topic_scorest_nonrel = topic_scorest(find(topic_scorest .* ([diff(cumt); 0]*-1) == 0));
    bincounts_nonrel = histcounts(topic_scorest_nonrel,binedges);
    %figure()
    %plot(bincenters, bincounts_rel./bincounts)
    figure()
    plot(bincenters, bincounts_rel/sum(bincounts_rel), 'b', bincenters, bincounts_nonrel/sum(bincounts_nonrel), 'r', bincenters, bincounts/sum(bincounts), 'g')
end