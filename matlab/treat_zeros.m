% Loads the presaved averages and replaces the zeros in current feats with
% the pretrained avgs

avgs = load('avgs');
avgs = avgs.avgs;

fileID_save = fopen(strcat(file, '_nozeros'),'w');

ind = 1;
abstract_feats = [1 4 9 10 13 14 15 19 20 21 30 31 32 41 42 47 50];
abstract_length_feat = 25;
mesh_feats = [5 6 11 12 45 46 49 52];
mesh_length_feat = 27;

non_mesh = find(~sum(mesh_feats == [1:numFeats]', 2));
non_abstract = find(~sum(mesh_feats == [1:numFeats]', 2));

C = A;
reb = 0;
for line = C
    if (line(abstract_length_feat+2) == 0)
        C(abstract_feats+2, ind) = avgs(abstract_feats);
    end
    if (line(mesh_length_feat+2) == 0)
        C(mesh_feats+2, ind) = avgs(mesh_feats);
    end
    ind = ind + 1;
end

fprintf(fileID_save,formatSpec,C);
fclose('all');