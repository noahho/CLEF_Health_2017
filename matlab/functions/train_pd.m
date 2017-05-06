function [pd_pos, pd_neg] = train_pd(scores, rf_rels)
    rf_pos = rf_rels > 0;
    rf_neg = rf_rels == 0;
    rf_scores_pos = scores(rf_pos);
    rf_scores_neg = scores(rf_neg);
    
    if numel(rf_scores_neg) < 2
        pd_neg = makedist('Uniform');
    else
        % estimate the negatives samples by gamma distribution
        pd_neg = fitdist(rf_scores_neg, 'gamma');
    end
    
     if numel(rf_scores_pos) < 2
         pd_pos = makedist('Uniform');
     else
        % estimate the positive samples by mixed gaussian distribution
        [y1, ~, ~] = mixGaussVb(rf_scores_pos',4); 
        pd_pos = gmdistribution.fit(rf_scores_pos,max(y1));
     end
end