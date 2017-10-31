package jp.livlog.noby.word2vec.tokenization.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;

public class KuromojiIpadicTokenizer implements Tokenizer {

    private List <String>   tokens = new ArrayList <>();;

    private int             index;

    private TokenPreProcess preProcess;


    public KuromojiIpadicTokenizer(String toTokenize) {
        try (JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.DEFAULT_MODE)) {
            tokenizer.setReader(new StringReader(toTokenize));
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            // PartOfSpeechAttribute partOfSpeech = tokenizer.addAttribute(PartOfSpeechAttribute.class);
            tokenizer.reset();

            while (tokenizer.incrementToken()) {
                tokens.add(term.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int countTokens() {

        return tokens.size();
    }


    @Override
    public List <String> getTokens() {

        List <String> ret = new ArrayList <String>();
        while (hasMoreTokens()) {
            ret.add(nextToken());
        }
        return ret;
    }


    @Override
    public boolean hasMoreTokens() {

        if (index < 0)
            return false;
        else
            return index < tokens.size();
    }


    @Override
    public String nextToken() {

        if (index < 0)
            return null;

        String tok = tokens.get(index);
        index++;
        if (preProcess != null) {
            return preProcess.preProcess(tok);
        } else {
            return tok;
        }
    }


    @Override
    public void setTokenPreProcessor(TokenPreProcess preProcess) {

        this.preProcess = preProcess;
    }

}
