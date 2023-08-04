package main;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;


public class Old_Parser {
    public static String getNextPage(String currentPageUrl) {
        currentPageUrl = currentPageUrl.replace("page", "");
        int number = Integer.parseInt(currentPageUrl.substring(currentPageUrl.lastIndexOf('/') + 1)) + 1;
        String nextUrl = currentPageUrl.substring(0, currentPageUrl.lastIndexOf('/') + 1).concat("page");
        return nextUrl.concat(Integer.toString(number));
    }

    public static ArrayList<ArrayList<String>> getMusicFile() throws Exception {
        ArrayList<String> albums = new ArrayList<>();
        ArrayList<String> songs = new ArrayList<>();
        ArrayList<String> mp3 = new ArrayList<>();
        ArrayList<String> crutch = new ArrayList<>();
        String URLNext = "https://musify.club/release/page20";
        while (!URLNext.equals("https://musify.club/release/page100")) {
            Document pageArtistNext = Jsoup.connect(URLNext).get();
            Elements elementsArtistNext = pageArtistNext.select("body > main.main.main--alt > section.content.content--full > " +
                    "div.row.content__inner > div#bodyContent.content__inner--sm.col-md-12.col-lg-9 > " +
                    "div.card-deck > div.card.release-thumbnail > div.card-body > h4.card-subtitle > a");
            for (Element element : elementsArtistNext) {
                String href1Next = "https://musify.club/";
                albums.add(element.text());
                Document pageMusicNext = Jsoup.connect(href1Next.concat(element.attr("href".toString()))).get();
                Elements elementsMusicNext = pageMusicNext.select("body > main.main.main--alt > section.content.content--full > " +
                        "div.row.content__inner > div#bodyContent.content__inner--sm.col-md-12.col-lg-9 > " +
                        "div.card > div.card-body > div.playlist.playlist--hover > div");
                int i = -1;
                for (Element element1 : elementsMusicNext) {
                    i += 1;
                    String href2Next = "https://musify.club";
                    songs.add(element1.attr("data-name").toString());
                    mp3.add(href2Next.concat(element1.select("div[data-url]").attr("data-url").toString()));
                }
                crutch.add(Integer.toString(i));
            }
            URLNext = getNextPage(URLNext);
        }
        ArrayList<ArrayList<String>> unity = new ArrayList<ArrayList<String>>();
        unity.add(albums);
        unity.add(songs);
        unity.add(mp3);
        unity.add(crutch);
        return unity;
    }
}