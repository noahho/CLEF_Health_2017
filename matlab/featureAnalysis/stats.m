%A = normc(A);
for n = 2:50
    figure()
    histogram(A(n, :),10)
end
for row = 1:size(A,2)
    if A(1, row) == 1
        bar(A(1, row))
    end
end
%corrplot( A(:, [1,3]),'type','Kendall','testR','on')