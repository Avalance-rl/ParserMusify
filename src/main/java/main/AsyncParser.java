package main;

import java.util.ArrayList;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class AsyncParser {
    private static final String RELEASESURL = "https://musify.club/release/page";
    private static final String RELEASESELECTOR = "body > main.main.main--alt > section.content.content--full > " +
            "div.row.content__inner > div#bodyContent.content__inner--sm.col-md-12.col-lg-9 > " +
            "div.card-deck > div.card.release-thumbnail > div.card-body > h4.card-subtitle > a";
    private static final String MUSICSELECTOR = "body > main.main.main--alt > section.content.content--full > " +
            "div.row.content__inner > div#bodyContent.content__inner--sm.col-md-12.col-lg-9 > " +
            "div.card > div.card-body > div.playlist.playlist--hover > div";

    public static class MusicFile {
        public String artist;
        public String song;
        public String mp3;

        public MusicFile(String artist, String song, String mp3) {
            this.artist = artist;
            this.song = song;
            this.mp3 = mp3;
        }

        public String getArtist() {
            return artist;
        }

        public String getSong() {
            return song;
        }

        public String getMp3() {
            return mp3;
        }
    }

    public static String getNextPage(String currentPageUrl) {
        currentPageUrl = currentPageUrl.replace("page", "");
        int number = Integer.
                parseInt(currentPageUrl.
                        substring(currentPageUrl.
                                lastIndexOf('/') + 1)) + 1;
        String nextUrl = currentPageUrl.substring(0, currentPageUrl.lastIndexOf('/') + 1).
                concat("page");
        return nextUrl.concat(Integer.toString(number));
    }

    public static CompletableFuture<ArrayList<MusicFile>> getMusicFilesAsync() {

        return CompletableFuture.supplyAsync(() -> {
            ArrayList<MusicFile> results = new ArrayList<>();
            String currentUrl = RELEASESURL.concat("2");
            int pageCount = 1;
            // выбираем здесь сколько страниц нам нужно пропарсить
            while (pageCount < 5000) {
                try {
                    Document page = Jsoup.connect(currentUrl).get();
                    Elements releaseLinks = page.select(RELEASESELECTOR);
                    ArrayList<CompletableFuture<Void>> releasesFutures = new ArrayList<>();
                    for (Element releaseLink : releaseLinks) {
                        String albumUrl = "https://musify.club/" + releaseLink.attr("href");
                        CompletableFuture<Void> albumFuture = CompletableFuture.supplyAsync(() -> {
                            ArrayList<MusicFile> songs = new ArrayList<>();
                            try {
                                Document album = Jsoup.connect(albumUrl).get();
                                Elements songElements = album.select(MUSICSELECTOR);
                                for (Element songElement : songElements) {
                                    String artistName = songElement.attr("data-artist");
                                    String songName = songElement.attr("data-name");
                                    String mp3Url = "https://musify.club".
                                            concat(songElement.
                                            select("div[data-url]").
                                            attr("data-url").
                                                    toString());
                                    songs.add(new MusicFile(artistName, songName, mp3Url));
                                }
                            } catch (IOException e) {
                                System.out.println("Error loading album page: " + albumUrl);
                                e.printStackTrace();
                            }
                            return songs;
                        }).thenAcceptAsync(results::addAll).exceptionally(e -> {
                            System.out.println("Error loading album page: " + albumUrl);
                            e.printStackTrace();
                            return null;
                        });
                        releasesFutures.add(albumFuture);
                    }
                    CompletableFuture.allOf(releasesFutures.toArray(new CompletableFuture[0])).join();
                    currentUrl = getNextPage(currentUrl);
                    System.out.println(pageCount);
                    pageCount++;
                } catch (IOException e) {
                    System.out.println("Error loading page: " + currentUrl);
                    e.printStackTrace();
                }
            }
            return results;
        });
    }
}
