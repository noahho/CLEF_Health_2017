function score = query_hardness_estimator(scores)
    gauss_score = gaussian_hardness_score(scores);
    
    % ESTIMATE FROM SCORES OF TOP DOCUMENTS
    n = round(numel(scores)/100);
    top_doc_score = mean(scores(1:n).*(n:-1:1)'/sum((n:-1:1)));
    score = top_doc_score/5 + gauss_score * 2;
end

function score = empty_score()
    % NUMBER OF EMPTY ABSTRACTS
    score = mean(feats(27, :) == 0) + mean(feats(29, :) == 0);
end

function score = clarity_score()
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
end

function score = gaussian_hardness_score(scores)
    [bincounts,binedges] = histcounts(scores,200);
    bincenters = binedges(1:end-1)+diff(binedges)/2;
    binwidth = binedges(2)-binedges(1); % Finds the width of each bin
    area = numel(scores) * binwidth;

    pd = fitdist(scores,'Normal');
    y = pdf(pd,bincenters);

    areas_est = y * area;
    diffs = bincounts - areas_est;
    
    score = sum(diffs/numel(scores))
end