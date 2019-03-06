package jp.livlog.noby.ngword;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.livlog.helper.analysis.InputCharacters;

public class NgwordCleansing {

    /** Log. */
    private static Log               log             = LogFactory.getLog(NgwordCleansing.class);

    /** 合計件数. */
    private static int               cnt             = 0;

    /** 日本文字. */
    public static final Set <String> TARGET_JAPANESE = new HashSet <>();
    static {
        for (final char chr : InputCharacters.BT_ALPHA.toCharArray()) {
            TARGET_JAPANESE.add(String.valueOf(chr));
        }
        for (final char chr : InputCharacters.EM_HIRAGANA.toCharArray()) {
            TARGET_JAPANESE.add(String.valueOf(chr));
        }
        for (final char chr : InputCharacters.EM_KATAKANA.toCharArray()) {
            TARGET_JAPANESE.add(String.valueOf(chr));
        }
        for (final char chr : InputCharacters.EM_KANJI.toCharArray()) {
            TARGET_JAPANESE.add(String.valueOf(chr));
        }
        // for (final char chr : InputCharacters.EM_KUTOUTEN.toCharArray()) {
        // TARGET_JAPANESE.add(String.valueOf(chr));
        // }
    }


    public static void main(final String[] args) {

        final String dir = "./data/ngword/twitter";

        final Path p1 = Paths.get(dir);

        try {
            Files.walkFileTree(p1, new SimpleFileVisitor <Path>() {

                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs)
                        throws IOException {

                    if (Files.isRegularFile(path)) {
                        final List <String> readdata = Files.readAllLines(path, StandardCharsets.UTF_8);
                        for (String line : readdata) {
                            // System.out.println(line);
                            line = Normalizer.normalize(line, Normalizer.Form.NFKC);
                            final String[] lines = line.split("\t");
                            analysis(lines);

                            cnt++;
                        }
                    }

                    cnt++;

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        }

        log.info("合計:" + cnt);
    }


    /**
     * 解析.
     * @param talks 道路データ一覧
     */
    private static void analysis(final String[] lines) {

        if (lines.length >= 3) {
            final String talk = lines[2];
            final String[] talks = talk.split(" ");
            for (String text : talks) {
                if (!text.contains("#") && !text.contains("@")) {
                    text = onlyJapanese(text);
                    if (text.contains(lines[1])) {
                        try (FileWriter fw = new FileWriter("ngwords.txt", true)) {
                            fw.write(text + "\n");
                        } catch (final IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }


    /**
     * 日本語だけのテキストにする.
     *
     * @param text String
     * @return String
     */
    private static String onlyJapanese(final String text) {

        final String value = text;

        try {

            final StringBuilder sb = new StringBuilder();
            for (final char chr : value.toCharArray()) {
                if (TARGET_JAPANESE.contains(String.valueOf(chr))) {
                    sb.append(chr);
                }
            }
            return sb.toString();

        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        return value;
    }
}
