package jp.livlog.noby.word2vecquartertime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

public class TimeAllocation {

    /**  */
    private final static Logger LOG        = Logger.getLogger(TimeAllocation.class);

    // /** 1日始. */
    // private final static int DAY_ST = 0;
    //
    // /** 1日終. */
    // private final static int DAY_EN = 235959;

    /** 朝始. */
    private final static int    MORNING_ST = 50000;

    /** 朝終. */
    private final static int    MORNING_EN = 105959;

    /** 昼始. */
    private final static int    NOON_ST    = 110000;

    /** 昼終. */
    private final static int    NOON_EN    = 165959;

    /** 夜始. */
    private final static int    NIGHT_ST   = 170000;

    /** 夜終. */
    private final static int    NIGHT_EN   = 225959;

    // /** 深夜始. */
    // private final static int LATE_NIGHT_ST = 230000;
    //
    // /** 深夜終. */
    // private final static int LATE_NIGHT_EN = 45959;


    public static void main(final String[] args) throws Exception {

        System.out.println("Start.");
        LOG.info("Start.");

        final String dir = "./data/";

        final List <Path> files = Files.list(Paths.get(dir)).collect(Collectors.toList());
        for (final Path path : files) {
            final List <String> lines = Files.readAllLines(path);
            for (final String line : lines) {
                System.out.println(line);
                final String[] array = line.split("\t");
                final String[] datetime = array[2].split("-");
                final int time = Integer.parseInt(datetime[1]);

                if (MORNING_ST <= time && time <= MORNING_EN) {
                    write(textEditing(array[1]), "morning.txt");
                } else if (NOON_ST <= time && time <= NOON_EN) {
                    write(textEditing(array[1]), "noon.txt");
                } else if (NIGHT_ST <= time && time <= NIGHT_EN) {
                    write(textEditing(array[1]), "night.txt");
                } else {
                    write(textEditing(array[1]), "late_night.txt");
                }
            }
            // Files.delete(path);
        }

        System.out.println("End.");
        LOG.info("End.");

    }


    private static void write(final String text, final String filename) throws IOException {

        final String dir = "./data/";

        // ディレクトリが存在するかのチェック
        if (!Files.isDirectory(Paths.get(dir))) {
            // ディレクトリが存在しない場合ディレクトリを作成
            Files.createDirectory(Paths.get(dir));
        }
        // ファイルが存在するかのチェック
        if (!Files.exists(Paths.get(dir, filename))) {
            // ファイルが存在しない場合ファイルを作成
            Files.createFile(Paths.get(dir, filename));
        }

        // Filesクラスを使用してファイルに書き込み
        // StandardOpenOption.APPEND = 追記で書き込み
        final List <String> list = new ArrayList <>();
        list.add(text);
        Files.write(Paths.get(dir, filename), list, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

    }


    private static String textEditing(String text) {

        int beginIndex = text.indexOf("@");
        int endIndex = text.indexOf(" ");

        if (beginIndex == -1 || beginIndex == -1) {
            return text;
        }

        String ret = null;
        try {
            String userName = text.substring(beginIndex, endIndex);
            ret = text.replace(userName, "");
        } catch (StringIndexOutOfBoundsException e) {
            return text;
        }

        return textEditing(ret.trim());
    }
}
