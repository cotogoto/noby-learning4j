import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;

public class Test {

    public static void main(final String[] args) throws IOException {

        final String src = "今日はいい天気ですね。";
        final List <String> list = Test.tokenize(src);
        System.out.println(list);
    }


    private static List <String> tokenize(final Reader reader) {

        final List <String> ret = new ArrayList <>();

        final UserDictionary userDict = null;
        final Mode mode = JapaneseTokenizer.Mode.NORMAL;

        CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
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

        return Test.tokenize(new StringReader(src));
    }
}
