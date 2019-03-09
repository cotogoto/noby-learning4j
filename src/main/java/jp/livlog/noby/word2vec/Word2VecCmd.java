package jp.livlog.noby.word2vec;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.EndingPreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.JapaneseTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

public class Word2VecCmd {

    public static void main(final String[] args) throws Exception {

        final SentenceIterator iter = new BasicLineIterator(new File("shisoutofuzoku.txt"));

        final EndingPreProcessor preProcessor = new EndingPreProcessor();
        final TokenizerFactory tokenizer = new JapaneseTokenizerFactory();
        tokenizer.setTokenPreProcessor(token -> {
            token = token.toLowerCase();
            String base = preProcessor.preProcess(token);
            base = base.replaceAll("\\d", "__NUMBER__");
            return base;
        });
        // TokenizerFactory tokenizer = new DefaultTokenizerFactory();

        System.out.println("Build model...");

        final Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(2)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(tokenizer)
                .workers(10)
                .build();
        vec.fit();
        WordVectorSerializer.writeWordVectors(vec, "wordvectors.txt");
    }

}
