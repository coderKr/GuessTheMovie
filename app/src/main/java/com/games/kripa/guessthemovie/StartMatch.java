package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

public class StartMatch extends AppCompatActivity implements
        View.OnClickListener {

    EditText movieText ;
    Button validate;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_match);
        View x = findViewById(R.id.start_match);
        movieText = (EditText)findViewById(R.id.enter_movie);
        Button send = (Button) x.findViewById(R.id.button_send);
        send.setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();

    }


    @Override
    public void onClick(View v) {
        String movie;


        movie = movieText.getText().toString();
        String mUrl = "http://api.cinemalytics.com/v1/movie/title/";
        String result="";


        try {
            String url = mUrl + "?value=" + URLEncoder.encode(movie, "UTF-8") + "&auth_token=B2B269D887D32D7E0E0643E007545328";

            result = new validateMovie().execute(url).get();
            JSONArray convertToJSON = new JSONArray(result);

            JSONObject first = convertToJSON.getJSONObject(0);

            result =(String) first.get("OriginalTitle");

            Intent returnIntent = new Intent();
            returnIntent.putExtra("MovieName",result);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
