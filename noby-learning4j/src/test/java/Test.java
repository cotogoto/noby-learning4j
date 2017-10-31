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

    public static void main(String[] args) throws IOException {

        String src = "今日はいい天気ですね。";
        List <String> list = tokenize(src);
        System.out.println(list);
    }


    private static List <String> tokenize(Reader reader) {

        List <String> ret = new ArrayList <>();

        UserDictionary userDict = null;
        Mode mode = JapaneseTokenizer.Mode.NORMAL;
        CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
        Set <String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

        try (JapaneseAnalyzer analyzer = new JapaneseAnalyzer(userDict, mode, stopSet, stopTags);
                TokenStream tokenStream = analyzer.tokenStream("", reader)) {

            // BaseFormAttribute baseAttr = tokenStream.addAttribute(BaseFormAttribute.class);
            CharTermAttribute charAttr = tokenStream.addAttribute(CharTermAttribute.class);
            // PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);
            // ReadingAttribute readAttr = tokenStream.addAttribute(ReadingAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String text = charAttr.toString(); // 単語
                // String baseForm = baseAttr.getBaseForm(); // 原型
                // String reading = readAttr.getReading(); // 読み
                // String partOfSpeech = posAttr.getPartOfSpeech(); // 品詞

                // System.out.println(text + "\t|\t" + baseForm + "\t|\t" + reading + "\t|\t" + partOfSpeech);
                ret.add(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }


    private static List <String> tokenize(String src) {

        return tokenize(new StringReader(src));
    }
}
