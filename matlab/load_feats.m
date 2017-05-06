file = '../data/learning/features/feats_train_nozeros';

fileID = fopen(file,'r');
numFeats = 52;
pidFeat = numFeats+3;
FeatsSpec = '';
for i = 1:numFeats
    FeatsSpec = strcat(FeatsSpec, int2str(i),':%f', {' '});
end
formatSpec = string(strcat('%d qid:%d', {' '}, string(FeatsSpec), '# pid:%d\n'));
sizeA = [numFeats+3 Inf];
A = fscanf(fileID,formatSpec,sizeA);
feats = A(3:numFeats+2, :);

[~, I] = max(feats');
maxpids = A(pidFeat, I);
[~, I] = min(feats');
minpids = A(pidFeat, I);

maxs = max(feats');
mins = min(feats');
feats_norm = (feats - mins') ./ (maxs' - mins');
