package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class GameDescription extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_description);
        Intent intent = getIntent();
        Button goBack = (Button) findViewById(R.id.goback);
        goBack.setOnClickListener(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbarstartmatch);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void onClick(View v) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
