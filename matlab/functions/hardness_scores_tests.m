X = [1:jt-2];
effectiveness = gain'./X;
%figure()
%plot(X, effectiveness, 'r', X, topic_scorest)
% ESTIMATE HARDNESS
% GAUSSIAN IDEA
% TODO: I HAVE TO INCREASE THE HARDNESS FOR MORE DOCUMENTS, CHECK IF THIS
% IS DONE IN PAPER
%figure()
%h = histfit(topic_scorest)
[bincounts,binedges] = histcounts(topic_scorest,200);
bincenters = binedges(1:end-1)+diff(binedges)/2;
binwidth = binedges(2)-binedges(1); % Finds the width of each bin
area = numel(topic_scorest) * binwidth;

pd = fitdist(topic_scorest,'Normal');
y = pdf(pd,bincenters);

areas_est = y * area;
diffs = bincounts - areas_est;

plot(bincenters, bincounts, bincenters, areas_est)

gaussian_hardness = sum(diffs/numel(topic_scorest))
% CLARITY SCORE, HIGHER MEANS HARDER, NOT CORRECT
[~, feats_topic_ind] = find(A(pidFeat, :) == topic_indst);
feats_topic = A(:, feats_topic_ind);
gensim = zeros(size(feats_topic, 2), 301);
distances = zeros(size(feats_topic, 2), 1);
gensim_before = zeros(300, 1);
k = 1;
FeatsSpec = '%d:[';
for i = 1:301
    FeatsSpec = strcat(FeatsSpec, {' '}, '%f');
end
FeatsSpec = string(FeatsSpec);
for i = feats_topic
    filet = strcat('preprocessing/gensim/gensimOutput/articles_abstracts_vectors/', int2str(i(pidFeat)));
    fileID = fopen(filet, 'r');
    gensim(k, :) = fscanf(fileID,FeatsSpec,301);
    distances(k) = dot(gensim_before,gensim(k, 2:301))/(norm(gensim_before,2)*norm(gensim(k, 2:301),2));
    gensim_before = gensim_before + gensim(k, 2:301)'/k;
    k = k + 1;
    fclose(fileID);
end
clarity_score = mean(distances(2:end))

% NUMBER OF EMPTY ABSTRACTS
empty_score = mean(feats_topic(27, :) == 0) + mean(feats_topic(29, :) == 0)
% ESTIMATE FROM SCORES OF TOP DOCUMENTS
n = round(numel(topic_scorest)/100)
top_doc_score = mean(topic_scorest(1:n).*(n:-1:1)')
% ESTIMATE FROM EFFECTIVENESS
%effectiveness_score = effectiveness();
% ESTIMATE NUMBER OF RELEVANT FILES
% ESTIMATE BY RETRIEVED DOCS


% ESTIMATE FROM EFFECTIVENESS


% ESTIMATE FROM QUERY
num_docs_retrieved_score = numel(topic_indst)
