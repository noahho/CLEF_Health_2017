#python example to infer document vectors from trained doc2vec model
import gensim.models as g
import codecs
import gensim
import os
import collections
import smart_open
import random
import numpy
from gensim import corpora, models, similarities

global testSet
testSet = ["CD008643", "CD010632", "CD010771", "CD009323", "CD008691", "CD009944", "CD011548"]

training = True

def read_corpus(fname, tokens_only=False):
    with smart_open.smart_open(fname, encoding="iso-8859-1") as f:
        for i, line in enumerate(f):
            t = line.split(':', 3)
            yield gensim.models.doc2vec.TaggedDocument(gensim.utils.simple_preprocess(t[3]), [t[2], t[0], t[1]])

def read_topic_corpus(fname, tokens_only=False):
    with smart_open.smart_open(fname, encoding="iso-8859-1") as f:
        for i, line in enumerate(f):
            t = line.split(':', 3)
            yield gensim.models.doc2vec.TaggedDocument(gensim.utils.simple_preprocess(t[1]), [t[0]])

#infer test vectors

def handle_articles(input, dir, relevance_output_file):
    train_corpus = list(read_corpus(input))
    topic_vectors_pos = {}
    topic_vectors_neg = {}
    topic_vectors_pos_count = {}
    topic_vectors_neg_count = {}
    i = 0
    for line in train_corpus:
        fname = dir+"/"+line.tags[0]
        inferred_vector = m_doc.infer_vector(line.words)
        if not os.path.isfile(fname) :
            output = open(fname, "w")
            output.write(line.tags[0]+":"+str(inferred_vector)+"\n")
            output.flush()
            output.close()
        if line.tags[2] != "CD009593" and line.tags[2] != "CD007427" and line.tags[2] != "CD007394" and line.tags[2] != "CD008686":
            continue
        if line.tags[1] == "1":
            if not line.tags[2] in topic_vectors_pos:
                topic_vectors_pos.update({line.tags[2]: inferred_vector})
                topic_vectors_pos_count.update({line.tags[2]: 1})
            else:
                topic_vectors_pos[line.tags[2]] += inferred_vector
                topic_vectors_pos_count[line.tags[2]] += 1
        else:
            if not line.tags[2] in topic_vectors_neg:
                topic_vectors_neg.update({line.tags[2]: inferred_vector})
                topic_vectors_neg_count.update({line.tags[2]: 1})
            else:
                topic_vectors_neg[line.tags[2]] += inferred_vector
                topic_vectors_neg_count[line.tags[2]] += 1
        i += 1
        if i % 5000 == 0:
            print(str(i)+"\n")

    topic_vectors = {}
    relevant_count = 0
    for topic in topic_vectors_neg:
        if not (topic in topic_vectors_pos_count and topic in topic_vectors_neg_count):
            continue

        v = (topic_vectors_pos[topic]/topic_vectors_pos_count[topic])-(topic_vectors_neg[topic]/topic_vectors_neg_count[topic])
        v2 = (topic_vectors_pos[topic]/topic_vectors_pos_count[topic])
        v3 = (topic_vectors_neg[topic]/topic_vectors_neg_count[topic])
        topic_vectors.update({topic: v})
        relevant_count += 1
        if 'relevant_vector' in locals():
            relevant_vector += v
            relevant_vector_2 += v2
            non_relevant_vector += v3
        else:
            relevant_vector = v
            relevant_vector_2 = v2
            non_relevant_vector = v3

    relevant_vector = relevant_vector/relevant_count
    relevant_vector_2 = relevant_vector_2/relevant_count
    if not training:
        return;
    relevance_output = open(relevance_output_file, "w")
    relevance_output.write(str(relevant_vector)+"\n")
    relevance_output.write(str(relevant_vector_2))
    relevance_output.write(str(m_doc.most_similar([relevant_vector], topn=50)))
    relevance_output.write(str(m_doc.most_similar([relevant_vector_2], topn=50)))
    relevance_output.write(str(m_doc.most_similar([non_relevant_vector], topn=50)))
    relevance_output.flush()
    relevance_output.close()
    print(str(relevant_vector))

def handle_topic(input, output_dir):
    train_corpus = list(read_topic_corpus(input))
    for line in train_corpus:
        output = open(output_dir+"/"+line.tags[0], "w")
        inferred_vector = m_doc.infer_vector(line.words)
        output.write(str(inferred_vector)+"\n")

    output.flush()
    output.close()

#parameters
dataDir = "../data"
gensimDir = dataDir + "/learning/gensim"
gensimDirInput = gensimDir + "/gensimInput"
gensimDirOutput = gensimDir + "/gensimOutput"

model_doc="enwiki_dbow/doc2vec.bin"
model_word="w2vec/word2vec.bin"

articles_titles=gensimDirInput+"/articles_titles.txt"
articles_abstracts=gensimDirInput+"/articles_abstracts.txt"
articles_mesh=gensimDirInput+"/articles_mesh.txt"
topics_titles=gensimDirInput+"/topics_titles.txt"
topics_queries=gensimDirInput+"/topics_queries.txt"

articles_abstract_output_dir= gensimDirOutput+"/articles_abstracts_vectors"
articles_titles_output_dir= gensimDirOutput+"/articles_titles_vectors/"
articles_mesh_output_dir= gensimDirOutput+"/articles_mesh_vectors"
topics_query_output_file = gensimDirOutput+"/topics_queries_vector.txt"
topics_title_output_file = gensimDirOutput+"/topics_titles_vector.txt"

relevance_title_output_file= gensimDirOutput+"/articles_titles_relevance_vector.txt"
relevance_abstract_output_file= gensimDirOutput+"/articles_abstracts_relevance_vector.txt"
relevance_mesh_output_file= gensimDirOutput+"/articles_mesh_relevance_vector.txt"

topics_titles_output_dir = gensimDirOutput+"/topics_titles"
topics_queries_output_dir = gensimDirOutput+"/topics_queries"

#inference hyper-parameters
start_alpha=0.01
infer_epoch=1000

#load model
m_doc = g.Doc2Vec.load(model_doc)
#m_word = g.Word2Vec.load(model_word)

handle_articles(articles_titles, articles_titles_output_dir, relevance_title_output_file)
#handle_articles(articles_abstracts, articles_abstract_output_dir, relevance_abstract_output_file)
#handle_articles(articles_mesh, articles_mesh_output_dir, relevance_mesh_output_file)

handle_topic(topics_titles, topics_titles_output_dir)
handle_topic(topics_queries, topics_queries_output_dir)
#inferred_vector = m_doc.infer_vector(train_corpus[doc_id].words)
#inferred_vector = topic_vectors['CD007394']
#inferred_vector_2 = topic_vectors['CD007427']
#cosine_similarity = numpy.dot(inferred_vector, inferred_vector_2)/(numpy.linalg.norm(inferred_vector)* numpy.linalg.norm(inferred_vector_2))
#print(m_doc.most_similar([relevant_vector], topn=100))
#print(cosine_similarity)
