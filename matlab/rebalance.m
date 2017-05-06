relation = 5;

fileID_save = fopen(strcat(file, '_rebalanced'),'w');
zero = find(A(1, :) == 0);
nonzero = find(A(1, :) > 0);
zero_lines = A(:, zero);
non_zero_lines = A(:, nonzero);
idx=randperm(size(zero_lines, 2));
picked_lines = zero_lines(:, idx);
picked_lines = picked_lines(:, [1:size(nonzero, 2)*relation]);
output = [non_zero_lines,picked_lines];
idx=randperm(size(output, 2));
output = output(:, idx);

idx=randperm(size(output, 2));
B = output(:, idx);
B = sortrows(B',2)';

fprintf(fileID_save,formatSpec,B);
fclose('all')