package jp.livlog.noby.word2vecquartertime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.EndingPreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import jp.livlog.noby.word2vec.tokenization.tokenizerfactory.KuromojiIpadicTokenizerFactory;

/**
 * これは、朝・昼・夜・深夜の分類で会話をカテゴライズして、その結果からいつの時間帯の会話を推測する。
 * テストデータはTwitterから取得し、1日分のデータを朝・昼・夜・深夜として判定する。
 *
 * @author H.Aoshima
 */
public class QuartertimeRecurrentIterator implements DataSetIterator {

    private static final long      serialVersionUID = 3720823704002518845L;

    private static final Logger    LOG              = Logger.getLogger(QuartertimeRecurrentIterator.class);

    private static final int       MODE_MORNING     = 1;

    private static final int       MODE_NOON        = 2;

    private static final int       MODE_NIGHT       = 3;

    private static final int       MODE_LATENIGHT   = 4;

    private final WordVectors      wordVectors;

    private final int              batchSize;

    private final int              vectorSize;

    private final int              truncateLength;

    private int                    cursor           = 0;

    private int                    morningCursor    = 0;

    private int                    noonCursor       = 0;

    private int                    nightCursor      = 0;

    private int                    lateNightCursor  = 0;

    private final File[]           morningFiles;

    private final File[]           noonFiles;

    private final File[]           nightFiles;

    private final File[]           lateNightFiles;

    private final int              numMorning;

    private final int              numNoon;

    private final int              numNight;

    private final int              numLateNight;

    private final int              numTotals;

    private final Random           rnd;

    private final TokenizerFactory tokenizerFactory;


    /**
     * @param dataDirectory IMDBレビューデータセットのディレクトリ
     * @param wordVectors WordVectors オブジェクト
     * @param batchSize トレーニング用の各ミニサイズのサイズ
     * @param truncateLength レビューが超過した場合
     * @param train trueの場合：トレーニングデータを返します。 falseの場合：テストデータを返します。
     */
    public QuartertimeRecurrentIterator(String dataDirectory, WordVectors wordVectors, int batchSize, int truncateLength, boolean train)
            throws IOException {

        this.batchSize = batchSize;
        this.vectorSize = wordVectors.lookupTable().layerSize();

        File morning = new File(FilenameUtils.concat(dataDirectory, (train ? "train" : "test") + "/morning/") + "/");
        File noon = new File(FilenameUtils.concat(dataDirectory, (train ? "train" : "test") + "/noon/") + "/");
        File night = new File(FilenameUtils.concat(dataDirectory, (train ? "train" : "test") + "/night/") + "/");
        File lateNight = new File(FilenameUtils.concat(dataDirectory, (train ? "train" : "test") + "/lateNight/") + "/");

        morningFiles = morning.listFiles();
        noonFiles = noon.listFiles();
        nightFiles = night.listFiles();
        lateNightFiles = lateNight.listFiles();
        numMorning = morningFiles.length;
        numNoon = noonFiles.length;
        numNight = nightFiles.length;
        numLateNight = lateNightFiles.length;
        numTotals = numMorning + numNoon + numNight + numLateNight;
        rnd = new Random(3);

        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;

        // tokenizerFactory = new DefaultTokenizerFactory();
        // tokenizerFactory.setTokenPreProcessor(new LowCasePreProcessor());
        final EndingPreProcessor preProcessor = new EndingPreProcessor();
        tokenizerFactory = new KuromojiIpadicTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(token -> {
            token = token.toLowerCase();
            String base = preProcessor.preProcess(token);
            base = base.replaceAll("\\d", "__NUMBER__");
            return base;
        });
    }


    @Override
    public DataSet next(int num) {

        long filesCount = morningFiles.length + noonFiles.length + nightFiles.length + lateNightFiles.length;

        if (cursor >= filesCount)
            throw new NoSuchElementException();
        try {
            return nextDataSet(num);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private DataSet nextDataSet(int num) throws IOException {

        // 1：レビューをStringにロードします。 朝・昼・夜・深夜のレビュー
        List <String> morningReviews = new ArrayList <>(num);
        List <String> noonReviews = new ArrayList <>(num);
        List <String> nightReviews = new ArrayList <>(num);
        List <String> lateNightReviews = new ArrayList <>(num);
        // boolean[] morning = new boolean[num];
        // boolean[] noon = new boolean[num];
        // boolean[] night = new boolean[num];
        // boolean[] lateNight = new boolean[num];
        String review = null;
        for (int i = 0; i < num && cursor < totalExamples(); i++) {
            int idx = rnd.nextInt(numTotals);
            int mode = modeJudge(idx);
            switch (mode) {
                case MODE_MORNING:
                    review = FileUtils.readFileToString(morningFiles[morningCursor]);
                    morningReviews.add(review);
                    // morning[i] = true;
                    morningCursor++;
                    break;
                case MODE_NOON:
                    review = FileUtils.readFileToString(noonFiles[noonCursor]);
                    noonReviews.add(review);
                    // noon[i] = true;
                    noonCursor++;
                    break;
                case MODE_NIGHT:
                    review = FileUtils.readFileToString(nightFiles[nightCursor]);
                    nightReviews.add(review);
                    // night[i] = true;
                    nightCursor++;
                    break;
                case MODE_LATENIGHT:
                    review = FileUtils.readFileToString(lateNightFiles[lateNightCursor]);
                    lateNightReviews.add(review);
                    // lateNight[i] = true;
                    lateNightCursor++;
                    break;
                default:
                    break;
            }

            cursor++;
        }

        // 2：レビューをトークン化し、未知語を除外する
        List <List <String>> morningTokens = new ArrayList <>(morningReviews.size());
        List <List <String>> noonTokens = new ArrayList <>(noonReviews.size());
        List <List <String>> nightTokens = new ArrayList <>(nightReviews.size());
        List <List <String>> lateNightTokens = new ArrayList <>(lateNightReviews.size());
        int morningMaxLength = 0;
        int noonMaxLength = 0;
        int nightMaxLength = 0;
        int lateNightMaxLength = 0;
        // 朝のトークン化
        for (String s : morningReviews) {
            List <String> tokens = tokenizerFactory.create(s).getTokens();
            List <String> tokensFiltered = new ArrayList <>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t))
                    tokensFiltered.add(t);
                // else LOG.info(t);
            }
            if (tokensFiltered.isEmpty())
                LOG.info("Invalid text: " + s);
            morningTokens.add(tokensFiltered);
            morningMaxLength = Math.max(morningMaxLength, tokensFiltered.size());
        }
        // 昼のトークン化
        for (String s : noonReviews) {
            List <String> tokens = tokenizerFactory.create(s).getTokens();
            List <String> tokensFiltered = new ArrayList <>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t))
                    tokensFiltered.add(t);
                // else LOG.info(t);
            }
            if (tokensFiltered.isEmpty())
                LOG.info("Invalid text: " + s);
            noonTokens.add(tokensFiltered);
            noonMaxLength = Math.max(noonMaxLength, tokensFiltered.size());
        }
        // 夜のトークン化
        for (String s : nightReviews) {
            List <String> tokens = tokenizerFactory.create(s).getTokens();
            List <String> tokensFiltered = new ArrayList <>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t))
                    tokensFiltered.add(t);
                // else LOG.info(t);
            }
            if (tokensFiltered.isEmpty())
                LOG.info("Invalid text: " + s);
            nightTokens.add(tokensFiltered);
            nightMaxLength = Math.max(nightMaxLength, tokensFiltered.size());
        }
        // 深夜のトークン化
        for (String s : lateNightReviews) {
            List <String> tokens = tokenizerFactory.create(s).getTokens();
            List <String> tokensFiltered = new ArrayList <>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t))
                    tokensFiltered.add(t);
                // else LOG.info(t);
            }
            if (tokensFiltered.isEmpty())
                LOG.info("Invalid text: " + s);
            lateNightTokens.add(tokensFiltered);
            lateNightMaxLength = Math.max(lateNightMaxLength, tokensFiltered.size());
        }

        // 最長レビューが 'truncateLength'を超えている場合：最初の 'truncateLength'単語だけを取る
        if (morningMaxLength > truncateLength)
            morningMaxLength = truncateLength;
        if (noonMaxLength > truncateLength)
            noonMaxLength = truncateLength;
        if (nightMaxLength > truncateLength)
            nightMaxLength = truncateLength;
        if (lateNightMaxLength > truncateLength)
            lateNightMaxLength = truncateLength;

        // トレーニング用のデータを作成する
        // ここでは、さまざまな長さのreviews.size()の例があります。
        int allSize = morningReviews.size() + noonReviews.size() + nightReviews.size() + lateNightReviews.size();
        int maxLength = morningMaxLength + noonMaxLength + nightMaxLength + lateNightMaxLength;
        INDArray features = Nd4j.create(allSize, vectorSize, maxLength);
        INDArray labels = Nd4j.create(allSize, 4, maxLength); // 4つのラベル：朝・昼・夜・深夜
        // 最終的なタイムステップでは異なる長さのレビューと1つの出力のみを処理するため、パディング配列を使用します
        // マスク配列は、その例のそのタイムステップでデータが存在する場合は1を、データがパディングの場合は0を含みます
        INDArray featuresMask = Nd4j.zeros(allSize, maxLength);
        INDArray labelsMask = Nd4j.zeros(allSize, maxLength);

        int[] temp = new int[2];
        for (int i = 0; i < morningReviews.size(); i++) {
            List <String> tokens = morningTokens.get(i);
            temp[0] = i;
            // レビュー中の各単語の単語ベクトルを取得し、それらをトレーニングデータに入れる
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[] { NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j) }, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0); // この例ではWordが存在します（パディングなし）。
            }

            int idx = 0;
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[] { i, idx, lastIdx - 1 }, 1.0); // セットラベル：負の場合は[0,1]、正の場合は[1,0]
            labelsMask.putScalar(new int[] { i, lastIdx - 1 }, 1.0); // この例の最後のタイムステップで出力が存在することを指定する
        }

        for (int i = 0; i < noonReviews.size(); i++) {
            List <String> tokens = noonTokens.get(i);
            temp[0] = i;
            // レビュー中の各単語の単語ベクトルを取得し、それらをトレーニングデータに入れる
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[] { NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j) }, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0); // この例ではWordが存在します（パディングなし）。
            }

            int idx = 1;
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[] { i, idx, lastIdx - 1 }, 1.0); // セットラベル：負の場合は[0,1]、正の場合は[1,0]
            labelsMask.putScalar(new int[] { i, lastIdx - 1 }, 1.0); // この例の最後のタイムステップで出力が存在することを指定する
        }

        for (int i = 0; i < nightReviews.size(); i++) {
            List <String> tokens = nightTokens.get(i);
            temp[0] = i;
            // レビュー中の各単語の単語ベクトルを取得し、それらをトレーニングデータに入れる
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[] { NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j) }, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0); // この例ではWordが存在します（パディングなし）。
            }

            int idx = 2;
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[] { i, idx, lastIdx - 1 }, 1.0); // セットラベル：負の場合は[0,1]、正の場合は[1,0]
            labelsMask.putScalar(new int[] { i, lastIdx - 1 }, 1.0); // この例の最後のタイムステップで出力が存在することを指定する
        }

        for (int i = 0; i < lateNightReviews.size(); i++) {
            List <String> tokens = lateNightTokens.get(i);
            temp[0] = i;
            // レビュー中の各単語の単語ベクトルを取得し、それらをトレーニングデータに入れる
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[] { NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j) }, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0); // この例ではWordが存在します（パディングなし）。
            }

            int idx = 3;
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[] { i, idx, lastIdx - 1 }, 1.0); // セットラベル：負の場合は[0,1]、正の場合は[1,0]
            labelsMask.putScalar(new int[] { i, lastIdx - 1 }, 1.0); // この例の最後のタイムステップで出力が存在することを指定する
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }


    private int modeJudge(int idx) {

        int a = numMorning;
        int b = a + numNoon;
        int c = b + numNight;
        // int d = c + numLateNight;

        if (0 <= idx && idx < a) {
            return MODE_MORNING;
        } else if (a <= idx && idx < b) {
            return MODE_NOON;
        } else if (b <= idx && idx < c) {
            return MODE_NIGHT;
        } else {
            return MODE_LATENIGHT;
        }

    }


    @Override
    public int totalExamples() {

        return morningFiles.length + noonFiles.length + nightFiles.length + lateNightFiles.length;
    }


    @Override
    public int inputColumns() {

        return vectorSize;
    }


    @Override
    public int totalOutcomes() {

        return 4;
    }


    @Override
    public void reset() {

        cursor = 0;
        morningCursor = 0;
        noonCursor = 0;
        nightCursor = 0;
        lateNightCursor = 0;
    }


    @Override
    public boolean resetSupported() {

        return true;
    }


    @Override
    public boolean asyncSupported() {

        return true;
    }


    @Override
    public int batch() {

        return batchSize;
    }


    @Override
    public int cursor() {

        return cursor;
    }


    @Override
    public int numExamples() {

        return totalExamples();
    }


    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {

        throw new UnsupportedOperationException();
    }


    @Override
    public List <String> getLabels() {

        return Arrays.asList("positive", "negative");
    }


    @Override
    public boolean hasNext() {

        return cursor < numExamples();
    }


    @Override
    public DataSet next() {

        return next(batchSize);
    }


    @Override
    public void remove() {

    }


    @Override
    public DataSetPreProcessor getPreProcessor() {

        throw new UnsupportedOperationException("Not implemented");
    }

}
