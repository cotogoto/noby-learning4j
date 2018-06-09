package jp.livlog.noby.word2vecsentiment;


import java.io.File;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;


public class SentimentRecurrentTestCmd {

  /**
   * args[0] input: word2vecファイル名
   * args[1] input: sentimentモデル名
   * args[2] input: test親フォルダ名
   *
   * @param args
   * @throws Exception
   */
  public static void main (final String[] args) throws Exception {
    if (args[0]==null || args[1]==null || args[2]==null)
      System.exit(1);

    WordVectors wvec = WordVectorSerializer.loadTxtVectors(new File(args[0]));
    MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(args[1],false);

    DataSetIterator test = new AsyncDataSetIterator(
        new SentimentRecurrentIterator(args[2],wvec,100,300,false),1);
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
    System.out.println(evaluation.stats());
  }

}
