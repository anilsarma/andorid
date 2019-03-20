package com.smartdeviceny.workinprogress;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class RailServiceAlertParser {
    ;

    public static String getRouteCode(String long_name) {
        switch (long_name) {
            case "Northeast Corridor":
                return "NEC";
            case "North Jersey Coast":
                return "NJCL";
            case "Raritan Valley":
                return "RARV";
            case "Morristown Line":
                return "MNE";
            case "Main/Bergen-Port Jervis Line":
                return "MNBN";
            case "Montclair-Boonton":
                return "BNTN";
            case "Pascack Valley":
                return "PASC";
            case "Atlantic City":
                return "ATLC";
            case "Hudson-Bergen Light Rail":
                return "HBLR";
            case "Newark Light Rail":
                return "NLR";
            case "River Line":
                return "RVR";
        }
        return long_name;
    }

    public HashMap<String, String> scrapeForAlertText(Document doc) {
        HashMap<String, String> alerts = new HashMap<>();
        Elements scripts = doc.getElementsByTag("script");
        for (Element n : scripts) {
            if (n.data().contains("function showAlertText")) {
                String msg = n.data().replace("\n", "").replace("\r", "").replace("\t", " ");
                Matcher m = Pattern.compile("case \'(.+?)\' *: *alerttext *= *\"(.+?)\"").matcher(msg);
                while (m.find()) {
                    String code = m.group(1);
                    String alerttext = m.group(2);
                    if (alerttext.equals("null")) {
                        continue;
                    }
                    alerts.put(code, alerttext);
                }
            }
        }
        return alerts;
    }

    public HashMap<String, RailAlertDetails> scrapeForRouteStatus(Document doc) {
        HashMap<String, String> alerts = scrapeForAlertText(doc);

        HashMap<String, RailAlertDetails> status = new HashMap<>();
        Element element = doc.getElementById("metro-status-tab1");
        if (element != null) {
            // inner table
            element = element.select("table").first();
            Element tbody = element.select("tbody").first();
            for (Element tr = tbody.select("tr").first();
                 tr != null; tr = tr.nextElementSibling()) {
                Elements cols = tr.select("td");
                if (cols.size() < 4) {
                    continue;
                }
                Elements spanRoute = cols.get(1).select("span");
                if (spanRoute == null) {
                    continue;
                }
                String route = spanRoute.first().childNodes().get(0).toString();
                String tstatus = cols.get(3).childNodes().get(0).toString();
                route = Jsoup.parse(route).text();
                tstatus = Jsoup.parse(tstatus).text();
                String short_code = getRouteCode(route);
                boolean hasAlert = alerts.containsKey(short_code)?true:false;
                String alertText = hasAlert?alerts.get(short_code):"";
                status.put(route, new RailAlertDetails(route, short_code, hasAlert, alertText));
            }

        }
        return status;
    }

}
