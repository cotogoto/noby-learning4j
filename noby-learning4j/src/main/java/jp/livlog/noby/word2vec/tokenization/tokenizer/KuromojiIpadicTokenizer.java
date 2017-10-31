package jp.livlog.noby.word2vec.tokenization.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;

public class KuromojiIpadicTokenizer implements Tokenizer {

    private static List <String> tokenize(final Reader reader) {

        final List <String> ret = new ArrayList <>();

        final UserDictionary userDict = null;
        final Mode mode = JapaneseTokenizer.Mode.NORMAL;
        final CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
        final Set <String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

        try (JapaneseAnalyzer analyzer = new JapaneseAnalyzer(userDict, mode, stopSet, stopTags);
                TokenStream tokenStream = analyzer.tokenStream("", reader)) {

            // BaseFormAttribute baseAttr = tokenStream.addAttribute(BaseFormAttribute.class);
            final CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
            // PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);
            // ReadingAttribute readAttr = tokenStream.addAttribute(ReadingAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                final String text = charAttr.toString(); // 単語
                // String baseForm = baseAttr.getBaseForm(); // 原型
                // String reading = readAttr.getReading(); // 読み
                // String partOfSpeech = posAttr.getPartOfSpeech(); // 品詞

                // System.out.println(text + "\t|\t" + baseForm + "\t|\t" + reading + "\t|\t" + partOfSpeech);
                ret.add(text);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static List <String> tokenize(final String src) {

        return KuromojiIpadicTokenizer.tokenize(new StringReader(src));
    }

    private List <String>   tokens = null;


    private int             index;


    private TokenPreProcess preProcess;


    public KuromojiIpadicTokenizer(final String toTokenize) {

        this.tokens = KuromojiIpadicTokenizer.tokenize(toTokenize);
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
}
