package com.smartdeviceny.workinprogress;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssFeedCategorise {
    static DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

    class Category {
        ArrayList<RailAlertDetails> train = new ArrayList<>();
        ArrayList<RailAlertDetails> construction = new ArrayList<>();
        ArrayList<RailAlertDetails> service = new ArrayList<>();

        public void sort() {
            train.sort(Comparator.comparingLong(RailAlertDetails::getTime));
            construction.sort(Comparator.comparingLong(RailAlertDetails::getTime));
            service.sort(Comparator.comparingLong(RailAlertDetails::getTime));
        }
    }

    ;

    public Category categorize(Feed feed, long cutoffTime) {
        Category category = new Category();

        for (FeedMessage msg : feed.getMessages()) {
            //TravelAlertsTo&rel=Rail&selLine=MNE#RailTab
            long msgTime = 0;
            try {
                msgTime = dateFormat.parse(msg.pubDate).getTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (msg.link.contains("TravelAlertsTo")) {
                Matcher matcher = Pattern.compile("=Rail&selLine=(.+?)#RailTab").matcher(msg.link);
                if (matcher.find()) {
                    String long_name = matcher.group(1);
                    String short_code = matcher.group(1);
                    RailAlertDetails alert = new RailAlertDetails(long_name, short_code, true, msg.description);
                    alert.setTime(msgTime);
                    category.train.add(alert);
                }
            } else if (msg.link.contains("ConstructionAdvisoryTo")) {
                RailAlertDetails alert = new RailAlertDetails("", "", true, msg.description);
                alert.setTime(msgTime);
                category.construction.add(alert);
            } else if (msg.link.contains("ServiceAdjustmentTo")) {
                RailAlertDetails alert = new RailAlertDetails("", "", true, msg.description);
                alert.setTime(msgTime);
                category.service.add(alert);
            } else {
                System.out.println(msg.link);
            }
        }
        System.out.println(category.train);
        return category;
    }
}
