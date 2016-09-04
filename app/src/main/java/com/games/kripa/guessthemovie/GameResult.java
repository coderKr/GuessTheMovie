package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class GameResult extends AppCompatActivity implements View.OnClickListener{
    private static final int RC_MATCH_RESULT = 4001;
    private String status="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);
        Intent intent = getIntent();
        status = (intent.getStringExtra("Status")).toUpperCase();
        String posterUrl = intent.getStringExtra("Poster");
        String imdbId = intent.getStringExtra("MovieId");
        Button GoBack = (Button) findViewById(R.id.goback);
        GoBack.setOnClickListener(this);
        TextView movie = (TextView) findViewById(R.id.movieName);
        InputStream in = null;
        Bitmap bitmap = null;
        ImageView i = (ImageView) findViewById(R.id.bkgd);
        if(intent.getStringExtra("Movie")!="") {
            new DownloadMoviePoster(posterUrl, "moviePoster", i);
            if (status.equals("WON")){
                findViewById(R.id.won).setVisibility(View.VISIBLE);
            }
            else{
                findViewById(R.id.lost).setVisibility(View.VISIBLE);
            }

            movie.setText(intent.getStringExtra("Movie"));

            //Set the poster image from API
        }
        else {
            findViewById(R.id.error_result).setVisibility(View.VISIBLE);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Status",status);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
