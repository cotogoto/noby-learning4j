package jp.livlog.noby.paragraphvectors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.EndingPreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.JapaneseTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.livlog.noby.paragraphvectors.tools.LabelSeeker;
import jp.livlog.noby.paragraphvectors.tools.MeansBuilder;

/**
 * This is basic example for documents classification done with DL4j ParagraphVectors.
 * The overall idea is to use ParagraphVectors in the same way we use LDA:
 * topic space modelling.
 *
 * In this example we assume we have few labeled categories that we can use
 * for training, and few unlabeled documents. And our goal is to determine,
 * which category these unlabeled documents fall into
 *
 *
 * Please note: This example could be improved by using learning cascade
 * for higher accuracy, but that's beyond basic example paradigm.
 *
 * @author raver119@gmail.com
 */
public class ParagraphVectorsClassifier {

    // ParagraphVectors paragraphVectors;
    //
    // LabelAwareIterator iterator;
    //
    // TokenizerFactory tokenizerFactory;

    private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifier.class);


    public static void main(final String[] args) throws Exception {

        final ParagraphVectorsClassifier app = new ParagraphVectorsClassifier();
        // app.makeParagraphVectors();
        app.checkUnlabeledData();
        /*
                Your output should be like this:
        
                Document 'health' falls into the following categories:
                    health: 0.29721372296220205
                    science: 0.011684473733853906
                    finance: -0.14755302887323793
        
                Document 'finance' falls into the following categories:
                    health: -0.17290237675941766
                    science: -0.09579267574606627
                    finance: 0.4460859189453788
        
                    so,now we know categories for yet unseen documents
         */
    }


    void makeParagraphVectors() throws Exception {

        // final ClassPathResource resource = new ClassPathResource("paravec/labeled");
        final File folder = new File("paravec/labeled");

        // build a iterator for our dataset
        final LabelAwareIterator iterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(folder)
                .build();

        // this.tokenizerFactory = new DefaultTokenizerFactory();
        // this.tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        final EndingPreProcessor preProcessor = new EndingPreProcessor();
        final TokenizerFactory tokenizerFactory = new JapaneseTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(token -> {
            token = token.toLowerCase();
            String base = preProcessor.preProcess(token);
            base = base.replaceAll("\\d", "__NUMBER__");
            return base;
        });

        // ParagraphVectors training configuration
        final ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(20)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .build();

        // Start model training
        paragraphVectors.fit();
        WordVectorSerializer.writeParagraphVectors(paragraphVectors, "ngParagraphVectors.txt");
    }


    void checkUnlabeledData() throws IOException {

        // this.paragraphVectors= WordVectorSerializer.loadTxtVectors(new File("wordvectors.txt"));
        final ParagraphVectors paragraphVectors = WordVectorSerializer.readParagraphVectors(new File("ngParagraphVectors.txt"));

        // final EndingPreProcessor preProcessor = new EndingPreProcessor();
        // final TokenizerFactory tokenizerFactory = new JapaneseTokenizerFactory();
        // tokenizerFactory.setTokenPreProcessor(token -> {
        // token = token.toLowerCase();
        // String base = preProcessor.preProcess(token);
        // base = base.replaceAll("\\d", "__NUMBER__");
        // return base;
        // });
        final TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        /*
        At this point we assume that we have model built and we can check
        which categories our unlabeled document falls into.
        So we'll start loading our unlabeled documents and checking them
        */
        // final ClassPathResource unClassifiedResource = new ClassPathResource("paravec/unlabeled");
        final File folder = new File("paravec/unlabeled");
        final FileLabelAwareIterator unClassifiedIterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(folder)
                .build();

        // build a iterator for our dataset
        final File folder2 = new File("paravec/labeled");
        final LabelAwareIterator iterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(folder2)
                .build();

        /*
         Now we'll iterate over unlabeled data, and check which label it could be assigned to
         Please note: for many domains it's normal to have 1 document fall into few labels at once,
         with different "weight" for each.
        */
        final MeansBuilder meansBuilder = new MeansBuilder(
                (InMemoryLookupTable <VocabWord>) paragraphVectors.getLookupTable(),
                tokenizerFactory);
        final LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
                (InMemoryLookupTable <VocabWord>) paragraphVectors.getLookupTable());

        while (unClassifiedIterator.hasNextDocument()) {
            final LabelledDocument document = unClassifiedIterator.nextDocument();
            final INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
            final List <Pair <String, Double>> scores = seeker.getScores(documentAsCentroid);

            /*
             please note, document.getLabel() is used just to show which document we're looking at now,
             as a substitute for printing out the whole document name.
             So, labels on these two documents are used like titles,
             just to visualize our classification done properly
            */
            log.info("Document '" + document.getLabels() + "' falls into the following categories: ");
            for (final Pair <String, Double> score : scores) {
                log.info("        " + score.getFirst() + ": " + score.getSecond());
            }
        }

    }
}
