function [dynamic_pd_pos, dynamic_pd_neg, dynamic_npos, confidence] = estimate_pds_it(scores, rf_rels)
    % most likely probability of a positive document
    %static_npos = static_pd_npos.mu;
    
    dynamic_pd_npos = fitdist(rf_rels(rf_rels > -1),'Normal');
    dynamic_npos = dynamic_pd_npos.mu;
    
    %confidence_static_npos = 1-(static_pd_npos.sigma)^2;
    %confidence_dynamic_npos = 1-(dynamic_pd_npos.sigma)^2;
    %npos = (confidence_static_npos * static_npos + confidence_dynamic_npos * dynamic_npos) / (confidence_dynamic_npos + confidence_static_npos);
    
    [dynamic_pd_pos, dynamic_pd_neg] = train_pd(scores, rf_rels);
    
    confidence = 0;
end