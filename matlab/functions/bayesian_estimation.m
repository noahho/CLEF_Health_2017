[bincounts,binedges] = histcounts(topic_scorest,200);
bincenters = binedges(1:end-1)+diff(binedges)/2;
binwidth = binedges(2)-binedges(1); % Finds the width of each bin
area = numel(topic_scorest) * binwidth;

[y1, model, L] = mixGaussVb(topic_scorest',10); 
pd = gmdistribution.fit(topic_scorest,max(y1));
y = pdf(pd,bincenters');

pd =  fitdist(topic_scorest, 'gamma')
y = pdf(pd,bincenters');

areas_est = y * area;
diffs = bincounts - areas_est;

plot(bincenters, bincounts, bincenters, areas_est)