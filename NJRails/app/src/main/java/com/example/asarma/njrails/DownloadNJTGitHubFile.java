package com.example.asarma.njrails;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Anil Sarma on 11/24/2017.
 */
//https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt
public class DownloadNJTGitHubFile extends AsyncTask<String, Integer, String> {
    String filename;
    String destination;
    Context context;
    //String urlbase="https://raw.githubusercontent.com/anilsarma/android/master/NJRails/app/src/main/assets/";
    String urlbase = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/";
    //String urlbase = "https://raw.githubusercontent.com/anilsarma/android/master/NJRails/cache/";
    String msg;
    IGitHubDownloadComple main;

    public File getCacheDir(Object filename)
    {
        return new File(context.getCacheDir() + "/db/" + filename);
    }

    public File getDir( Object filename)
    {
        return new File(context.getDir("db",  context.MODE_PRIVATE|context.MODE_APPEND) + "/db/" + filename);
    }

    public FileOutputStream openFileOutput( String filename) throws  FileNotFoundException
    {
        return context.openFileOutput(filename,  context.MODE_PRIVATE|context.MODE_APPEND);
    }

    public DownloadNJTGitHubFile( Context context, String filename, String destination, IGitHubDownloadComple main) {
        this.main = main;
        this.context = context;
        this.filename = filename;
        this.destination = destination;
        if (this.destination == null) {
            this.destination = this.filename;
        }


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
        HttpURLConnection con = (HttpURLConnection) new URL(url ).openConnection();
        con.connect();
        long date = con.getLastModified();
        return date;
    }
    @Override
    protected String doInBackground(String... sUrl) {
        File dir = new File(context.getCacheDir() + "/db/");
        try {
            dir.mkdir();
        } catch( Exception e ) {

        }
        File file = new File(context.getCacheDir() + "/db/" + destination);
        file.delete();

        long lastModified = 0;
        if( file.exists() ) {
            lastModified = file.lastModified();
        }
        if( lastModified != 0 ) {
            try {
                if (lastModified < getLastModifiedTime(urlbase +  filename)) {
                    msg = "File " + filename + " exits  " + file.exists()  + " " + lastModified + " < " + getLastModifiedTime(urlbase + filename);
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
            msg = "File " + urlbase + " download complete " + file.exists() + " Modified:" + lastModified + " < webmodified " + getLastModifiedTime(urlbase + filename) + " sz=" + file.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // check the version files
        try {
            File file = new File(context.getCacheDir() + "/db/" + destination);
            main.onDownloadComplete( filename, new File(context.getCacheDir() + "/db"), file);
        }
        catch( Exception e) {
            e.printStackTrace();;
        }
        //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        // mProgressDialog.dismiss();

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }


    ArrayList<File> unzipfile(File fileZip, File directory) throws IOException
    {
        ArrayList<File> output = new ArrayList<>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while(zipEntry != null){
            String fileName = zipEntry.getName();
            File newFile = new File(directory + fileName);
            output.add(newFile);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return output;
    }

    public void writeFile(File output, String data) throws IOException
    {
        FileOutputStream ot = new FileOutputStream(output);
        ot.write(data.getBytes());
        ot.close();
    }

    public String readFile(File file)
    {
        if(!file.exists()) {
            return "";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String data = br.readLine();
            br.close();
            return data;
        }
        catch (IOException e) {
            return "";
        }
    }

    public void removeFiles(File dir)
    {
        for(File currentFile: dir.listFiles()){
            currentFile.delete();
        }
    }
}
