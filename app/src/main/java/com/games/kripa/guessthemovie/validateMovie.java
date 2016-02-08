package com.games.kripa.guessthemovie;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kripa on 14/1/2016.
 */
public class validateMovie extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
        InputStream is = null;
        String result = null;
        try {
            for(String url:urls) {
                URL urlCon = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlCon.openConnection();
                try {
                    urlConnection.setRequestProperty("Content-length", "0");
                    urlConnection.setUseCaches(false);
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    int status = urlConnection.getResponseCode();
                    InputStream in = urlConnection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(in);
                    result = convertStreamToString(in);

                }catch(IOException e){
                    e.printStackTrace();
                    return null;
                }
                finally {
                    urlConnection.disconnect();
                }
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

