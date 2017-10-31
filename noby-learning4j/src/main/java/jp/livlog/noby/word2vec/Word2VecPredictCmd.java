package jp.livlog.noby.word2vec;


import java.io.File;
import java.util.Collection;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;


public class Word2VecPredictCmd {

    public static void main (final String[] args) throws Exception {

        final WordVectors vec = WordVectorSerializer.loadTxtVectors(new File("wordvectors.txt"));

        final Collection <String> lst = vec.wordsNearest("男", 10);
        System.out.println(lst);
        final double cosSim = vec.similarity("男", "女");
        System.out.println(cosSim);
        final double[] wordVector = vec.getWordVector("男");
        System.out.println(wordVector);
    }

}
