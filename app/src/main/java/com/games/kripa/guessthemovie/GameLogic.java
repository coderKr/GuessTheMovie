package com.games.kripa.guessthemovie;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameLogic extends AppCompatActivity implements View.OnClickListener {

    private List<String> guessedLetters = new ArrayList<String>();
    private int chancesLeft = 0;
    private String actualMovie;
    private String displayMovie;
    String MovieId, language;
    boolean win,lost;
    private int uniqueChar =0;
    public Intent returnIntent;
    List<String> wrongGuesses = new ArrayList<String>(Arrays.asList("B","O","L","L1","Y","W","O","O","D"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_logic);

        Intent intent = getIntent();
        actualMovie = (intent.getStringExtra("Movie")).toUpperCase();
        MovieId = intent.getStringExtra("MovieId");
        language = intent.getStringExtra("Language");
        if(language.equals("English")){
            ((TextView) findViewById(R.id.B)).setText("H");
        }
        displayMovie = actualMovie;
        displayMovie = getBlanks(displayMovie);
        TextView t = (TextView) findViewById(R.id.display);
        t.setText(displayMovie);
        String movieWithoutSpaces= actualMovie.replaceAll("\\s","");
        uniqueChar = countUniqueCharacters(movieWithoutSpaces);
        populateButtons();

        returnIntent = new Intent();
    }

    public void populateButtons() {

        GridView keyboard = (GridView) findViewById(R.id.grid);
		/* disable scrolling */
        keyboard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

		/* add buttons to gridview */
        Button cb = null;
        ArrayList<Button> mButtons = new ArrayList<Button>();
        for (char buttonChar = 'A'; buttonChar <= 'Z'; buttonChar++) {
            cb = new Button(this);
            cb.setText("" + buttonChar);
            cb.setPadding(0, 0, 0, 0);
            cb.setId(buttonChar);

            cb.setTextColor(Color.parseColor("white"));
            cb.setTextSize(25);
            cb.setOnClickListener(this);
            cb.setBackgroundColor(Color.parseColor("#D9411E"));
            if (guessedLetters.contains("" + buttonChar)) {
                cb.setBackgroundColor(Color.parseColor("#858585"));
                cb.setOnClickListener(null);
            }
            mButtons.add(cb);
        }

        keyboard.setAdapter(new KeyboardAdapter(mButtons));
    }

    @Override
    public void onClick(View v) {
            Button selection = (Button) v;
            selection.setBackgroundColor(Color.parseColor("#858585"));
            selection.setOnClickListener(null);
            nextGuess((String) selection.getText());
    }

    public void nextGuess(String letter){
        if(actualMovie.contains(letter)){
            guessedLetters.add(letter);
            String toShow = getBlanks(actualMovie);
            TextView t = (TextView) findViewById(R.id.display);
            t.setText(toShow);
            if(guessedLetters.size() == uniqueChar){
                win = true;
                Toast.makeText(GameLogic.this, "Won the Match!", Toast.LENGTH_SHORT).show();
                returnIntent.putExtra("Status", "WON");
                returnIntent.putExtra("Movie", actualMovie);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }

        }
        else{
            if(chancesLeft < 9){
                chancesLeft++;
                fade( findViewById(R.id.view_game));
            }
            else {
                lost = true;
                Toast.makeText(GameLogic.this, "Lost the Match!", Toast.LENGTH_SHORT).show();
                returnIntent.putExtra("Status", "LOST");
                returnIntent.putExtra("Movie", actualMovie);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }


    public int countUniqueCharacters(String s) {
        String lowerCase = s.toLowerCase();
        char characters[] = lowerCase.toCharArray();
        int countOfUniqueChars = s.length();
        for (int i = 0; i < characters.length; i++) {
            if (i != lowerCase.indexOf(characters[i])) {
                countOfUniqueChars--;
            }
        }
        return countOfUniqueChars;
    }

    public String getBlanks(String movie){
        String tempStr="";
        String vowels = "AEIOU";
        for(int i=0;i<movie.length();i++){
            if (!guessedLetters.contains("" + movie.charAt(i))) {
                if(movie.charAt(i) == ' '){
                    tempStr = tempStr + "/ ";
                    //movie.replace("" + movie.charAt(i), "/ ");
                }
                else{
                    if(vowels.indexOf(movie.charAt(i)) >= 0) {
                        tempStr = tempStr + movie.charAt(i) + " ";
                        guessedLetters.add(Character.toString(movie.charAt(i)));
                    }
                    else
                        tempStr = tempStr + "_ ";
                   // movie = movie.replace("" + movie.charAt(i),"_ ");
                }
            }
            else
            {
               // movie = movie.replace("" + actualMovie.charAt(i),
               //         actualMovie.charAt(i)+" ");
                tempStr = tempStr + movie.charAt(i)+" ";
            }

        }
        return tempStr;

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void fade(View view){
        ChancesLeft leftObj = (ChancesLeft) this.findViewById(R.id.ChanceB);
        switch(chancesLeft){
            case 1: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceB);
                    break;
            case 2: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceO1);
                break;
            case 3: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceL1);
                break;
            case 4: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceL2);
                break;
            case 5: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceY);
                break;
            case 6: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceW);
                break;
            case 7: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceO2);
                break;
            case 8:leftObj = (ChancesLeft) this.findViewById(R.id.ChanceO3);
                break;
            case 9: leftObj = (ChancesLeft) this.findViewById(R.id.ChanceD);
                break;
        }
        leftObj.drawStrike();

    }
}




