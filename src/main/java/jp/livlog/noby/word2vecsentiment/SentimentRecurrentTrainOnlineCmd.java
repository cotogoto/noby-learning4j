package jp.livlog.noby.word2vecsentiment;


import java.io.File;
import java.io.FileOutputStream;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;


public class SentimentRecurrentTrainOnlineCmd {

  /**
   * args[0] input: word2vecファイル名
   * args[1] input: 学習モデル名
   * args[2] input: train/test親フォルダ名
   * args[3] output: 学習モデル名
   *
   * @param args
   * @throws Exception
   */
  public static void main (final String[] args) throws Exception {
    if (args[0]==null || args[1]==null || args[2]==null || args[3]==null)
      System.exit(1);

    WordVectors wvec = WordVectorSerializer.loadTxtVectors(new File(args[0]));
    MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(args[1],true);
    int batchSize   = 16;//100;
    int testBatch   = 64;
    int nEpochs     = 1;

    System.out.println("Starting online training");
    DataSetIterator train = new AsyncDataSetIterator(
        new SentimentRecurrentIterator(args[2],wvec,batchSize,300,true),2);
    DataSetIterator test = new AsyncDataSetIterator(
        new SentimentRecurrentIterator(args[2],wvec,testBatch,300,false),2);
    for( int i=0; i<nEpochs; i++ ){
      model.fit(train);
      train.reset();

      System.out.println("Epoch " + i + " complete. Starting evaluation:");
      Evaluation evaluation = new Evaluation();
      while(test.hasNext()) {
        DataSet t = test.next();
        INDArray features = t.getFeatures();
        INDArray lables = t.getLabels();
        INDArray inMask = t.getFeaturesMaskArray();
        INDArray outMask = t.getLabelsMaskArray();
        INDArray predicted = model.output(features,false,inMask,outMask);
        evaluation.evalTimeSeries(lables,predicted,outMask);
      }
      test.reset();
      System.out.println(evaluation.stats());

      System.out.println("Save model");
      ModelSerializer.writeModel(model, new FileOutputStream(args[3]), true);
    }
  }

}
