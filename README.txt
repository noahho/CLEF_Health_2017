SUBMISSION for trec eHealth Challenge 2017 Task2

Please get the following files before running the algorithm
	https://github.com/jhlau/doc2vec - Download english wikipedia dbow and unpack it into BachelorPython

Please do the following steps to run the algorithm
1. Put the two qrel files into folder data/original
2. Put the topic files into folder topics

3. Run the Jar with parameter download
4. Run the Python script BachelorPython/get_gensim.py
5. Run the Jar with parameter index
6. Run the Jar with parameter features
7. Run the Matlab script load_feats.m and then treat_zeros.m
8. Run the Jar with parameter predict
9. Run Matlab script load_qrels.m and then predict_threshold.m
10. The method used to predict the threshold can be one out of 4
	the best method using relevance feedback is 2, without relevance feedback use 1 or 3



In the file run on test data you can find the algorithms results on the topics 8643, 10632, 10771, 009323, 008691, 009944, 011548
