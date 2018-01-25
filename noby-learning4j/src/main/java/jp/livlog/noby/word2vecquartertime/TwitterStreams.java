package jp.livlog.noby.word2vecquartertime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterStreams {

    private static class Listener implements StatusListener {

        private final List <String>    tweetlist = new ArrayList <>();

        private final SimpleDateFormat sdf       = new SimpleDateFormat("yyyyMMdd-HHmmss");


        @Override
        public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {

            // ステータスの削除通知 (delete)
            // 通知に従って保存領域から該当ツイートを削除しなければならない
            System.out.println("!-----[onDeletionNotice]-----");
        }


        @Override
        public void onException(final Exception excptn) {

            // その他エラー
            System.out.println("!-----[onException]-----");
        }


        @Override
        public void onScrubGeo(final long lat, final long lng) {

            // 位置情報の削除通知 (scrub_geo)
            // 通知に従って保存領域から該当ツイートの位置情報を削除しなければならない
            System.out.println("!-----[onScrubGeo]-----");
        }


        @Override
        public void onStallWarning(final StallWarning warning) {

            // 速度低下警告 (warning)
            System.out.println("!-----[onStallWarning]-----");
        }


        @Override
        public void onStatus(final Status status) {

            String tweet = status.getText();
            tweet = tweet.replaceAll("[\r\n\t\\s]+", " ");
            final String id = String.valueOf(status.getId());
            if (tweet.length() < 20) {
                return;
            }

            if (status.isRetweeted() || tweet.startsWith("RT ")) {
                return;
            }

            if (tweet.contains("http")) {
                return;
            }

            final String tsv = String.format("%s\t%s\t%s", id, tweet, this.sdf.format(status.getCreatedAt()));
            this.tweetlist.add(tsv);
            System.out.println(tsv);

            if (this.tweetlist.size() > 1000) {
                this.write(this.tweetlist);
                this.tweetlist.clear();
            }
        }


        @Override
        public void onTrackLimitationNotice(final int i) {

            // 制限通知 (limit)
            // 速度制限の上限を超えたために取得できなかったツイートが存在する
            System.out.println("!-----[onTrackLimitationNotice]-----");
        }


        private void write(final List <String> list) {

            final String dir = "./data/";
            final String filebase = String.valueOf(System.currentTimeMillis());
            final String filename = String.format("%s.txt", filebase);

            try {
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
                Files.write(Paths.get(dir, filename), list, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            } catch (final IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
        }

    }


    public static void main(final String[] args) {

        // アプリケーションプロパティを取得
        final ResourceBundle application = ResourceBundle.getBundle("application");

        final ConfigurationBuilder conf = new ConfigurationBuilder();
        conf.setOAuthConsumerKey(application.getString("twitter.consumer.key"));
        conf.setOAuthConsumerSecret(application.getString("twitter.consumer.secret"));
        conf.setOAuthAccessToken(application.getString("twitter.access.token"));
        conf.setOAuthAccessTokenSecret(application.getString("twitter.access.token.secret"));

        TwitterStream twStream = null;
        twStream = new TwitterStreamFactory(conf.build()).getInstance();
        twStream.addListener(new Listener());
        twStream.sample("ja");
        // final FilterQuery filter = new FilterQuery();
        // filter.track(track);
        // twStream.filter(filter);
    }
}
