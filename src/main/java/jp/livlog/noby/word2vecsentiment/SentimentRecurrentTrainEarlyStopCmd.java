package jp.livlog.noby.word2vecsentiment;


import java.io.File;

import org.apache.log4j.Logger;
import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingModelSaver;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.earlystopping.trainer.IEarlyStoppingTrainer;
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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;


public class SentimentRecurrentTrainEarlyStopCmd {

  private static final Logger LOG = Logger.getLogger(SentimentRecurrentTrainEarlyStopCmd.class);

  /**
   * args[0] input: word2vecファイル名
   * args[1] input: train/test親フォルダ名
   * args[2] output: 出力ディレクトリ名
   *
   * @param args
   * @throws Exception
   */
  public static void main (final String[] args) throws Exception {
    if (args[0]==null || args[1]==null || args[2]==null)
      System.exit(1);

    WordVectors wvec = WordVectorSerializer.loadTxtVectors(new File(args[0]));
    int numInputs   = wvec.lookupTable().layerSize();
    int numOutputs  = 2; // FIXME positive or negative
    int batchSize   = 16;//100;
    int testBatch   = 64;
    int nEpochs     = 5000;
    int thresEpochs = 10;
    double minImprovement = 1e-5;
    int listenfreq  = 10;

    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .seed(7485)
        //.updater(Updater.RMSPROP)
        .updater(Updater.ADADELTA)
        //.learningRate(0.001) //RMSPROP
        //.rmsDecay(0.90) //RMSPROP
        .rho(0.95) //ADADELTA
        .epsilon(1e-5) //1e-8 //ALL
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .weightInit(WeightInit.XAVIER)
        .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
        .gradientNormalizationThreshold(1.0)
        //.regularization(true)
        //.l2(1e-5)
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
    model.setListeners(new ScoreIterationListener(listenfreq));
    //model.setListeners(new HistogramIterationListener(listenfreq)); //FIXME error occur


    LOG.info("Starting training");
    DataSetIterator train = new AsyncDataSetIterator(
        new SentimentRecurrentIterator(args[1],wvec,batchSize,300,true),2);
    DataSetIterator test = new AsyncDataSetIterator(
        new SentimentRecurrentIterator(args[1],wvec,testBatch,300,false),2);

    EarlyStoppingModelSaver<MultiLayerNetwork> saver = new LocalFileModelSaver(args[2]);//new InMemoryModelSaver<>();
    EarlyStoppingConfiguration<MultiLayerNetwork> esConf = new EarlyStoppingConfiguration.Builder<MultiLayerNetwork>()
        .epochTerminationConditions(
            new MaxEpochsTerminationCondition(nEpochs),
            new ScoreImprovementEpochTerminationCondition(thresEpochs,minImprovement))
        .scoreCalculator(new DataSetLossCalculator(test, true))
        .modelSaver(saver)
        .build();

    IEarlyStoppingTrainer<MultiLayerNetwork> trainer = new EarlyStoppingTrainer(esConf,model,train);
    EarlyStoppingResult<MultiLayerNetwork> result = trainer.fit();
    LOG.info("Termination reason: " + result.getTerminationReason());
    LOG.info("Termination details: " + result.getTerminationDetails());
    LOG.info("Total epochs: " + result.getTotalEpochs());
    LOG.info("Best epoch number: " + result.getBestModelEpoch());
    LOG.info("Score at best epoch: " + result.getBestModelScore());

    //LOG.info("Save model");
    //MultiLayerNetwork best = result.getBestModel();
    //ModelSerializer.writeModel(best, new FileOutputStream(args[2]+"/sentiment.rnn.es.model"), true);

  }
}
