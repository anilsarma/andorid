package com.smartdeviceny.workinprogress;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RailServiceAlertParserTest {

    @Test
    public void checkNJTrainStatusScrapingCode() {
        try {
            InputStream is =  this.getClass().getClassLoader().getResourceAsStream("RailServiceStatus.html");
            Document doc = Jsoup.parse(is, null, "https://www.njtransit.com/hp/hp_servlet.srv?hdnPageAction=HomePageTo");
            RailServiceAlertParser parser = new RailServiceAlertParser();

            HashMap<String, RailAlertDetails> rtStatus = parser.scrapeForRouteStatus(doc);
            for (Map.Entry<String, RailAlertDetails> info : rtStatus.entrySet()) {
                System.out.println(info.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e, null);
        }
    }

    @Test
    public void liveCheckNJTrainStatusScrapingCode() {
        try {
            URL url = new URL("https://www.njtransit.com/hp/hp_servlet.srv?hdnPageAction=HomePageTo");
            InputStream is = url.openStream();  // throws an IOException
//            InputStream is =  this.getClass().getClassLoader().getResourceAsStream("RailServiceStatus.html");
            Document doc = Jsoup.parse(is, null, "https://www.njtransit.com/hp/hp_servlet.srv?hdnPageAction=HomePageTo");
            RailServiceAlertParser parser = new RailServiceAlertParser();

            HashMap<String, RailAlertDetails> rtStatus = parser.scrapeForRouteStatus(doc);
            for (Map.Entry<String, RailAlertDetails> info : rtStatus.entrySet()) {
                System.out.println(info.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(e, null);
        }
    }
}
