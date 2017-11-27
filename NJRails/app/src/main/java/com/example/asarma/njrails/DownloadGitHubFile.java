package com.example.asarma.njrails;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Anil Sarma on 11/24/2017.
 */

public class DownloadGitHubFile extends AsyncTask<String, Integer, String> {
    String filename;
    Context context;
    String urlbase="https://raw.githubusercontent.com/anilsarma/android/master/NJRails/app/src/main/assets/";
String msg;

    public DownloadGitHubFile(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
       // mProgressDialog.show();
    }
    // GET THE LAST MODIFIED TIME
    public long getLastModifiedTime(String url) throws  IOException
    {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.connect();
        long date = con.getLastModified();


        return date;
    }
    @Override
    protected String doInBackground(String... sUrl) {
        //FileOutputStream outputStream = context.openFileOutput(path, Context.MODE_PRIVATE);
        File dir = new File(context.getCacheDir() + "/db/");
        try {
            dir.mkdir();
        } catch( Exception e ) {

        }

        File file = new File(context.getCacheDir() + "/db/" + filename);

        long lastModified = 0;
        if( file.exists() ) {
            lastModified = file.lastModified();
        }
        if( lastModified != 0 ) {
            try {
                if (lastModified < getLastModifiedTime(urlbase +  filename)) {
                    msg = "File " + filename + " exits  " + file.exists()  + " " + lastModified + " < " + getLastModifiedTime(urlbase + filename);
                    return "";
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        try {
            URL url = new URL(urlbase + filename);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            // this will be useful so that you can show a typical 0-100% progress bar
            if(lastModified != 0) {
                connection.setIfModifiedSince(lastModified);
            }
            connection.setRequestMethod("GET");
            connection.connect();
            int fileLength = connection.getContentLength();
            if(connection.getResponseCode() == 304) {
                System.out.println(file+ " : already downloaded");
                msg = "File " + filename + " exits  " + file.exists()  + " " + lastModified + " < " + getLastModifiedTime(urlbase + filename);
                return "";
            } else {
                // Download the content again and store the image again
            }


            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        try {
            msg = "File " + filename + " download complete " + file.exists() + " " + lastModified + " < " + getLastModifiedTime(urlbase + filename) + " sz=" + file.length();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
       // mProgressDialog.dismiss();

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
       // mProgressDialog.setProgress(progress[0]);
    }
}
