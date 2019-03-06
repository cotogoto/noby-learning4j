package jp.livlog.noby.ngword;

import java.io.File;
import java.util.Collection;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

public class NgWordPredictCmd {

    public static void main(final String[] args) throws Exception {

        final WordVectors vec = WordVectorSerializer.loadTxtVectors(new File("wordvectors.txt"));

        Collection <String> lst = vec.wordsNearest("セックス", 10);
        System.out.println(lst);
        lst = vec.wordsNearest("撮影会", 10);
        System.out.println(lst);
        lst = vec.wordsNearest("性欲", 10);
        System.out.println(lst);
        lst = vec.wordsNearest("シコ", 10);
        System.out.println(lst);
        lst = vec.wordsNearest("ラブドール", 10);
        System.out.println(lst);

        // final MeansBuilder meansBuilder = new MeansBuilder(
        // (InMemoryLookupTable <VocabWord>) vec.getLookupTable(),
        // tokenizerFactory);
        // final LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
        // (InMemoryLookupTable <VocabWord>) paragraphVectors.getLookupTable());
        // final INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
        // final List <Pair <String, Double>> scores = seeker.getScores(documentAsCentroid);
        // log.info("Document '" + document.getLabels() + "' falls into the following categories: ");
        // for (Pair<String, Double> score: scores) {
        // log.info(" " + score.getFirst() + ": " + score.getSecond());
        // }

        // final List <String> lst2 = vec.similarWordsInVocabTo("ちんこ", 1);
        // System.out.println(lst2);
        // final double cosSim = vec.similarity("男", "女");
        // System.out.println(cosSim);
        // final double[] wordVector = vec.getWordVector("sex");
        // for (final double val : wordVector) {
        // System.out.println(val);
        // }
        // vec.
    }

}
