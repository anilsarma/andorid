package com.smartdeviceny.workinprogress;

import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

public class RssRailFeedParserTest {
    @Test
    public void testRssFeedParsing() {
        RssRailFeedParser parser = new RssRailFeedParser();
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("RailAdvisories_feed.xml");
            Feed feed = parser.parse(stream);
            RssFeedCategorise cat = new RssFeedCategorise();
            cat.categorize(feed,0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLiveRssFeedParsing() {
        RssRailFeedParser parser = new RssRailFeedParser();
        try {
            URL url = new URL("https://www.njtransit.com/rss/RailAdvisories_feed.xml");
            InputStream stream = url.openStream();  // throws an IOException

            Feed feed = parser.parse(stream);
            RssFeedCategorise cat = new RssFeedCategorise();
            cat.categorize(feed, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
