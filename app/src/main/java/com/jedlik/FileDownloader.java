package com.jedlik;

/**
 * Created by Tom on 14.6.2016.
 */
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownloader {

    public enum TDownloadResult{
        ERROR_UNKNOWN,
        ERROR_OK,
        ERROR_NO_CONNECTION,
        ERROR_NO_CARD,
        ERROR_BAD_URL
    }

    /*public boolean TestConnection(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    */

    public static TDownloadResult DownloadFromUrl(String DownloadUrl, String fileDir, String fileName){
        //helps to distinguish between connection error and sd card error
        boolean passedConnect = false;

        try{
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            URL url = new URL(DownloadUrl);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();


            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                urlConnection.setRequestProperty("Connection", "close");
            }
            //urlConnection.setDoOutput(true);

            //and connect!
            urlConnection.connect();
            passedConnect = true;

            File file = new File(fileDir, fileName);

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(file);

            //this will be used in reading the data from the internet
            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file
            //int totalSize = urlConnection.getContentLength();
            //variable to store total downloaded bytes
            //int downloadedSize = 0;

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                //downloadedSize += bufferLength;
                //this is where you would do something to report the progress, like this maybe
                //updateProgress(downloadedSize, totalSize);

            }
            //close the output stream when done
            fileOutput.close();
        }
        //catch some possible errors...
        catch(MalformedURLException e){
            e.printStackTrace();
            return TDownloadResult.ERROR_BAD_URL;
        }
        catch(IOException e){
            e.printStackTrace();
            if(passedConnect)
                return TDownloadResult.ERROR_NO_CARD;
            else
                return TDownloadResult.ERROR_BAD_URL;
        }
        catch(Exception e){
            e.printStackTrace();
            return TDownloadResult.ERROR_UNKNOWN;
        }
        return TDownloadResult.ERROR_OK;
    }


}
