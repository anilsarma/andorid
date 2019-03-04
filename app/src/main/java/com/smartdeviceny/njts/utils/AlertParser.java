package com.smartdeviceny.njts.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AlertParser {
    //https://www.njtransit.com/rss/RailAdvisories_feed.xml
    public static void maieeeen(String[] args) {
        //Jul 23, 2018 09:12:30 AM -- MMM d, yyyy HH:mm:ss a
        String format = "MMM d, yyyy HH:mm:ss a";
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        //ArrayList<Record> alerts = new ArrayList<Record>();
        try {
            File file = new File("input.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            NodeList nodes = document.getChildNodes();
            Node node = nodes.item(0).getChildNodes().item(0);
            nodes = node.getChildNodes();
            Date now = new Date();
            Calendar date = new GregorianCalendar();
// reset hour, minutes, seconds and millis
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            now = date.getTime();
            for(int i=0;i < nodes.getLength(); i ++ ) {
                //#System.out.println(nodes.item(i).getNodeName());
                if ( nodes.item(i).getNodeName() == "item") {
                    NodeList items = nodes.item(i).getChildNodes();
                    String descrition = null;
                    String title = null;
                    Date  dt =null;
                    for(int j=0; j < items.getLength();j ++ ) {
                        if(items.item(j).getNodeName()=="title") {
                            title = items.item(j).getFirstChild().getNodeValue();
                            dt = dateTimeFormat.parse(title);

                        } else  if(items.item(j).getNodeName()== "description") {
                            descrition = items.item(j).getFirstChild().getNodeValue();
                            //System.out.println(items.item(j).getFirstChild().getNodeValue());
                        }
                    }
                    if (dt.after(now)) {
                        //System.out.println(title + " " + descrition);
                        //alerts.add( new Record(dt, descrition));
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
