package jp.livlog.noby.tokenization.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;

public class SudachiIpadicTokenizer implements Tokenizer {

    // private static List <String> tokenize(final Reader reader) {
    //
    // try {
    // return SudachiIpadicTokenizer.tokenize(IOUtils.toString(reader));
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return Collections.emptyList();
    // }

    private List <String> tokenize(final String src) {

        final List <String> ret = new ArrayList <>();

        try (InputStream is = SudachiIpadicTokenizer.class.getResourceAsStream("/sudachi_fulldict.json")) {
            final String settings = this.readAll(is); // readAllはテキストファイルの中身を読み込むメソッド
            final Dictionary dict = new DictionaryFactory().create(settings);
            final com.worksap.nlp.sudachi.Tokenizer tokenizer = dict.create();
            // final String line = "コミニュケーションをシュミレーションしろ。";
            final List <Morpheme> tokens = tokenizer.tokenize(com.worksap.nlp.sudachi.Tokenizer.SplitMode.C, src);
            for (final Morpheme morpheme : tokens) {
                // System.out.println(morpheme.surface() + "\t" + morpheme.normalizedForm() + "\t" + morpheme.partOfSpeech());
                ret.add(morpheme.normalizedForm());
            }
            dict.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private List <String>   tokens = null;

    private int             index;

    private TokenPreProcess preProcess;


    public SudachiIpadicTokenizer(final String toTokenize) {

        this.tokens = this.tokenize(toTokenize);
    }


    @Override
    public int countTokens() {

        return this.tokens.size();
    }


    @Override
    public List <String> getTokens() {

        final List <String> ret = new ArrayList <>();
        while (this.hasMoreTokens()) {
            ret.add(this.nextToken());
        }
        return ret;
    }


    @Override
    public boolean hasMoreTokens() {

        if (this.index < 0) {
            return false;
        } else {
            return this.index < this.tokens.size();
        }
    }


    @Override
    public String nextToken() {

        if (this.index < 0) {
            return null;
        }

        final String tok = this.tokens.get(this.index);
        this.index++;
        if (this.preProcess != null) {
            return this.preProcess.preProcess(tok);
        } else {
            return tok;
        }
    }


    @Override
    public void setTokenPreProcessor(final TokenPreProcess preProcess) {

        this.preProcess = preProcess;
    }


    private String readAll(final InputStream input) throws IOException {

        try (InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isReader);) {
            final StringBuilder sb = new StringBuilder();
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
