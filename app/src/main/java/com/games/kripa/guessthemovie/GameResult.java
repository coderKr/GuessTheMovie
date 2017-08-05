package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameResult extends AppCompatActivity implements View.OnClickListener{
    private static final int RC_MATCH_RESULT = 4001;
    private String status="";
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        Intent intent = getIntent();
        status = (intent.getStringExtra("Status")).toUpperCase();
        String posterUrl = intent.getStringExtra("Poster");
        Button goBack = (Button) findViewById(R.id.goback);
        goBack.setOnClickListener(this);
        TextView movieTitle = (TextView) findViewById(R.id.selected_movie_title);
        TextView movieReleaseDate =(TextView)findViewById(R.id.selected_movie_release_date);
        ImageView i = (ImageView) findViewById(R.id.bkgd);
        if(intent.getStringExtra("Movie")!="") {
            if(!posterUrl.equals("None") && !posterUrl.equals("null")) {
                posterUrl = "https://image.tmdb.org/t/p/w300/" + intent.getStringExtra("Poster");
                new DownloadMoviePoster(posterUrl, "moviePoster", i, new ProgressDialog(this));
                i.setVisibility(View.VISIBLE);
            }
            if (status.equals("WON")){
                findViewById(R.id.won).setVisibility(View.VISIBLE);
            }
            else{
                findViewById(R.id.lost).setVisibility(View.VISIBLE);
            }
            movieTitle.setText(intent.getStringExtra("Movie"));
            movieReleaseDate.setText(intent.getStringExtra("ReleaseDate"));
        }
        else {
            findViewById(R.id.lost).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
