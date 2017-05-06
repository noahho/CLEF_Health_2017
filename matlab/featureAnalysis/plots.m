% How many feats are 0
figure
zero_feats = sum(feats(:, :)' == 0)/size(feats, 2);
plot(zero_feats)

% Average feats for pos and neg

nonzero = find(A(1, :) > 0);
pos = nansum(feats_norm(:, nonzero)')/size(nonzero, 2);

zero = find(A(1, :) == 0);
neg = nansum(feats_norm(:, zero)')/size(zero, 2);

figure
plot([1:numFeats],pos,'b',[1:numFeats],neg,'r')
