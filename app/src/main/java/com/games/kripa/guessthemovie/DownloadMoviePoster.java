package com.games.kripa.guessthemovie;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kripa on 20/2/2016.
 */
public class DownloadMoviePoster  extends AsyncTask<String, Void, Drawable> {

    private String imageUrl , imageName;
    private ImageView showHere;

    public DownloadMoviePoster(String url, String file_name, ImageView showHere) {
        this.imageUrl = url;
        this.imageName = file_name;
        this.showHere = showHere;
        execute(imageUrl);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Drawable doInBackground(String... urls) {

        try {
            InputStream is = (InputStream) this.fetch(this.imageUrl);
            Drawable d = Drawable.createFromStream(is, this.imageName);
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    protected void onPostExecute(Drawable result) {
        super.onPostExecute(result);
        showHere.setImageDrawable(result);
        showHere.setImageAlpha(60);
    }
}
