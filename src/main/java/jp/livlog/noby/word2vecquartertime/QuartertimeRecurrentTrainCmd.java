package jp.livlog.noby.word2vecquartertime;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class QuartertimeRecurrentTrainCmd {

    private static final Logger LOG = Logger.getLogger(QuartertimeRecurrentTrainCmd.class);


    /**
     * args[0] input: word2vecファイル名
     * args[1] input: train/test親フォルダ名
     * args[2] output: 学習モデル名
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {

        if (args[0] == null || args[1] == null || args[2] == null)
            System.exit(1);

        WordVectors wvec = WordVectorSerializer.loadTxtVectors(new File(args[0]));
        int numInputs = wvec.lookupTable().layerSize();
        int numOutputs = 2; // FIXME positive or negative
        int batchSize = 16;// 100;
        int testBatch = 64;
        int nEpochs = 5000;
        int listenfreq = 10;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(7485)
                .updater(Updater.RMSPROP) // ADADELTA
                .learningRate(0.001) // RMSPROP
                .rmsDecay(0.90) // RMSPROP
                // .rho(0.95) //ADADELTA
                .epsilon(1e-8) // ALL
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                // .regularization(true)
                // .l2(1e-5)
                .list()
                .layer(0, new GravesLSTM.Builder()
                        .nIn(numInputs).nOut(numInputs)
                        .activation("softsign")
                        .build())
                .layer(1, new RnnOutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .activation("softmax")
                        .nIn(numInputs).nOut(numOutputs)
                        .build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(listenfreq));

        LOG.info("Starting training");
        DataSetIterator train = new AsyncDataSetIterator(
                new QuartertimeRecurrentIterator(args[1], wvec, batchSize, 300, true), 2);
        DataSetIterator test = new AsyncDataSetIterator(
                new QuartertimeRecurrentIterator(args[1], wvec, testBatch, 300, false), 2);
        for (int i = 0; i < nEpochs; i++) {
            model.fit(train);
            train.reset();

            LOG.info("Epoch " + i + " complete. Starting evaluation:");
            Evaluation evaluation = new Evaluation();
            while (test.hasNext()) {
                DataSet t = test.next();
                INDArray features = t.getFeatures();
                INDArray lables = t.getLabels();
                INDArray inMask = t.getFeaturesMaskArray();
                INDArray outMask = t.getLabelsMaskArray();
                INDArray predicted = model.output(features, false, inMask, outMask);
                evaluation.evalTimeSeries(lables, predicted, outMask);
            }
            test.reset();
            LOG.info(evaluation.stats());

            LOG.info("Save model");
            ModelSerializer.writeModel(model, new FileOutputStream(args[2]), true);
        }
    }

}
