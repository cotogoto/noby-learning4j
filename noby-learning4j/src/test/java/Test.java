import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;

public class Test {

    public static void main(String[] args) throws IOException {

        String src = "今日はいい天気ですね。";
        try (JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.DEFAULT_MODE)) {
            tokenizer.setReader(new StringReader(src));
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            PartOfSpeechAttribute partOfSpeech = tokenizer.addAttribute(PartOfSpeechAttribute.class);
            tokenizer.reset();

            while (tokenizer.incrementToken()) {
                System.out.println(term.toString() + "\t" + partOfSpeech.getPartOfSpeech());
            }
        }
    }

}
