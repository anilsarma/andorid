package com.smartdeviceny.njts;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by asarma on 11/3/2017.
 */
/*
https://m.njtransit.com/mo/mo_servlet.srv?hdnPageAction=DvTo
<tr onclick="javascript:window.open('train_stops.aspx?sid=NP&amp;train=3915');" style="color:white;background-color:red;overflow:hidden;">
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;"> 8:57 </td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 45%;overflow:hidden;white-Space:nowrap;">Trenton&nbsp;✈</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="center" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">4</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">Northeast Corrdr</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="right" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">3915</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="right" valign="middle" style="font-family:Arial;width: 15%;overflow:hidden;white-Space:nowrap;">in 11 Min</td>
11-03 08:54:57.811 21940-21958/? I/System.out:  </tr>
*/
public class DownloadDepartureVisionTask extends AsyncTask<String, Integer, Long> {

    IDownloadComple parent;
    View view;
    String msg;
    public DownloadDepartureVisionTask(View view, String msg, IDownloadComple parent)
    {
        this.parent = parent;
        this.view = view;
        this.msg = msg;
    }
    ArrayList<HashMap<String, Object>> result= new ArrayList<>();
    protected Long doInBackground(String... codes) {
        ArrayList<HashMap<String, Object>> result =  new ArrayList<HashMap<String, Object>>();
        String station = codes[1];
        try {
            String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station;
            System.out.println("Departure Vistion: " + "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station);
            Connection  conn =  Jsoup.connect(url).timeout(3000);
            Document doc = conn.get();
            Element table = doc.getElementById("GridView1");
            Node node = table;
            List<Node> child = node.childNodes().get(1).childNodes();
            // discard the frist 3
            //System.out.println("child ===================== Size:" + child.size());
            for (int i = 3; i < child.size(); i++) {
                Node tr = child.get(i);
                List<Node> td = tr.childNodes();
                if (td.size()< 4 ) {
                    continue;
                }
                HashMap<String, Object> data = new HashMap<>();
                String time = ((Element)td.get(1)).html().toString();
                String to =  ((Element)td.get(3)).html().toString();
                String track = ((Element)td.get(5)).html().toString();
                String line = ((Element)td.get(7)).html().toString();
                String train = ((Element)td.get(9)).html().toString();
                String status =  ((Element)td.get(11)).html().toString();;
                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station);
                result.add(data);
            }
            this.result = result;
            return new Long(0);
            // send the data to the main thread
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return new Long(-1);
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long status) {
        parent.updateAdapter(view, status, result);
        if( status == -1 ){
            // should really let the UI handle this.
            if ( parent.getContext()!= null ) {
                Toast.makeText(parent.getContext(), "Failed retrieving live train status ", Toast.LENGTH_LONG).show();
                ;
            }
        }
    }
}
