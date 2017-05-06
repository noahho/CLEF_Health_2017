topics = [8643, 10632, 10771, 009323, 008691, 009944, 011548];

fileID = fopen('../data/original/qrel_abs_train','r');
sizeA = [3 Inf];
qrels = fscanf(fileID,'CD%d\t0\t%d\t%d\t',sizeA);
fileID = fopen('../data/trec','r');
sizeA = [4 Inf];
scores = fscanf(fileID,'CD%d\t0\t%d\t%d\t%f\t0\t',sizeA);
scores = fliplr(scores);
num_topics = 7;
rel_inds = zeros(size(scores, 2)+1, num_topics);
cum = zeros(size(scores, 2)+1, num_topics);
topic_scores = zeros(size(scores, 2)+1, num_topics);
topic_inds = zeros(size(scores, 2)+1, num_topics);
j = ones(num_topics, 1) * 2;
last_rel = ones(num_topics, 1) * 0;
k = 1;
for i = scores
    pid = i(2);
    topic = i(1);
    
    topicid = find(topic == topics);
    
    [~, I] = find(qrels(2, :) == i);
    qrel = qrels(:, I);
    
    for tid = topicid
        ind = qrel(1, :) == topics(tid);
        rel = qrel(3, ind);
    
        if rel == 1
            if last_rel == 0
                last_rel(tid) = pid;
            end
        else
            rel = 0;
        end
    
        cum(j(tid), tid) = cum(j(tid)-1, tid) + rel;
        rel_inds(j(tid), tid) = rel;
        topic_scores(j(tid), tid) = i(4);
        topic_inds(j(tid), tid) = pid;
        j(tid) = j(tid) + 1;
    end
    k = k +1;
end