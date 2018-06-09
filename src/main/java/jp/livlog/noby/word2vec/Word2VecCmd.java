package jp.livlog.noby.word2vec;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.EndingPreProcessor;

import jp.livlog.noby.word2vec.tokenization.tokenizerfactory.KuromojiIpadicTokenizerFactory;

public class Word2VecCmd {

    public static void main(final String[] args) throws Exception {

        final SentenceIterator iter = new BasicLineIterator(new File("morning.txt"));

        final EndingPreProcessor preProcessor = new EndingPreProcessor();
        final KuromojiIpadicTokenizerFactory tokenizer = new KuromojiIpadicTokenizerFactory();
        tokenizer.setTokenPreProcessor(token -> {
            token = token.toLowerCase();
            String base = preProcessor.preProcess(token);
            base = base.replaceAll("\\d", "__NUMBER__");
            return base;
        });
        // TokenizerFactory tokenizer = new DefaultTokenizerFactory();

        System.out.println("Build model...");
        final int batchSize = 1000;
        final int iterations = 5;
        final int layerSize = 150;

        final Word2Vec vec = new Word2Vec.Builder()
                .batchSize(batchSize)
                .minWordFrequency(5)
                .useAdaGrad(false)
                .layerSize(layerSize)
                .iterations(iterations)
                .seed(7485)
                .windowSize(5)
                .learningRate(0.025)
                .minLearningRate(1e-3)
                .negativeSample(10)
                .iterate(iter)
                .tokenizerFactory(tokenizer)
                .workers(6)
                .build();
        vec.fit();
        WordVectorSerializer.writeWordVectors(vec, "wordvectors.txt");
    }

}
