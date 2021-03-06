/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;


/**
 * @author Kripa Agarwal
 */
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
        View.OnClickListener {

    public static final String TAG = "Main Activity";
    private static final int RC_REQUEST_LEADERBOARD = 3111;

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // Current turn-based match
    private TurnBasedMatch mTurnBasedMatch;

    //TurnBased Match for startGam
    private TurnBasedMatch mStartTurnBasedMatch;

    // Local convenience pointers
    public TextView mTurnTextView;

    private AlertDialog mAlertDialog;

    // For our intents
    private static final int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;
    private static final int RC_START_MATCH = 1001;
    private static final int RC_GUESS_MOVIE = 2001;
    private static final int RC_MATCH_RESULT = 4001;
    private  static final int RC_GAME_DESCRIPTION = 5001;

    private static final int RC_UNUSED = 3001;

    // How long to show toasts.
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;

    // Should I be showing the turn API?
    public boolean isDoingTurn = false;

    // This is the current match we're in; null if not loaded
    public TurnBasedMatch mMatch;

    // This is the current match data after being unpersisted.
    // Do not retain references to match data once you have
    // taken an action on the match, such as takeTurn()
    ScoreDbHelper scoreDb;
    GameTurn mTurnData;
    String movieName;
    String posterUrl ="";
    String releaseDate="";
    String language="";
    String hint = "";
    Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        scoreDb = new ScoreDbHelper(getApplicationContext());
        //Uncomment when you want to delete db
//        SQLiteDatabase db = scoreDb.getWritableDatabase();
//        scoreDb.onCreate(db);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Get latest movie posters
        getLatestMoviePosters();

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addScope(new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .build();

        // Setup signin and signout buttons
        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): Connecting to Google APIs");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): Disconnecting from Google APIs");
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected(): Connection successful");

        // Retrieve the TurnBasedMatch from the connectionHint
        if (connectionHint != null) {
            mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (mTurnBasedMatch != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(mTurnBasedMatch);
                return;
            }
        }

        setViewVisibility();

        // As a demonstration, we are registering this activity as a handler for
        // invitation and match events.

        // This is *NOT* required; if you do not register a handler for
        // invitation events, you will get standard notifications instead.
        // Standard notifications may be preferable behavior in many cases.
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        // Likewise, we are registering the optional MatchUpdateListener, which
        // will replace notifications you would get otherwise. You do *NOT* have
        // to register a MatchUpdateListener.
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
        mGoogleApiClient.connect();
        setViewVisibility();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            // Already resolving
            Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
            return;
        }

        // Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;

            mResolvingConnectionFailure = resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult, RC_SIGN_IN,
                    getString(R.string.signin_other_error));
        }

        setViewVisibility();
    }

    // Displays your inbox. You will get back onActivityResult where
    // you will need to figure out what you clicked on.
    public void onCheckGamesClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }

    public void onShowLeaderboardsRequested(View view) {
        if ((mGoogleApiClient != null) && (mGoogleApiClient.isConnected())) {
            updateScoresFromDb();
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getString(R.string.leadership_id)), RC_REQUEST_LEADERBOARD);
        } else {
            makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }

    // Open the create-game UI. You will get back an onActivityResult
    // and figure out what to do.
    public void onStartMatchClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
                1, 1, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    // Create a one-on-one automatch game.
    public void onQuickMatchClicked(View view) {

        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                1, 1, 0);

        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                .setAutoMatchCriteria(autoMatchCriteria).build();

        showSpinner();

        // Start the match
        ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                processResult(result);
            }
        };
        Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
    }

    // In-game controls

    // Cancel the game. Should possibly wait until the game is canceled before
    // giving up on the view.
    public void onCancelClicked(View view) {
        showSpinner();
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        setViewVisibility();
    }

    // Leave the game during your turn. Note that there is a separate
    // Games.TurnBasedMultiplayer.leaveMatch() if you want to leave NOT on your turn.
    public void onLeaveClicked(View view) {
        showSpinner();
        String nextParticipantId = getNextParticipantId();

        Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                        processResult(result);
                    }
                });
        setViewVisibility();
    }

    // Finish the game. Sometimes, this is your only choice.
    public void onFinishClicked(View view, String status) throws JSONException {
        showSpinner();

        if(status.equals("WON")){
            updateLeaderboards(true);
        }
        else{
            updateLeaderboards(false);
        }
        JSONObject endGameInfo = new JSONObject();
        endGameInfo.put("movieName", movieName);
        endGameInfo.put("posterUrl", posterUrl);
        endGameInfo.put("releaseDate", releaseDate);
        Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId(), mTurnData.persist(endGameInfo))
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });

        isDoingTurn = false;
        setViewVisibility();
    }

    public void endGame(String movie, String posterUrl, String releaseDate){
        Intent intent = new Intent(this, GameResult.class);
        String [] scoreInfo = new String[4];
        scoreInfo = readFromDb(getCurrentPlayerId());
        String status = scoreInfo[4].replaceAll("'","");
        intent.putExtra("Status", status);
        intent.putExtra("Movie", movie);
        intent.putExtra("Poster",posterUrl);
        intent.putExtra("ReleaseDate", releaseDate);
        startActivityForResult(intent, RC_MATCH_RESULT);
    }


    // Upload your new gamestate, then take a turn, and pass it on to the next
    // player.
    public void onDoneClicked(View view) {
        showSpinner();

        String nextParticipantId = getNextParticipantId();

        showSpinner();

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                mTurnData.persist(null), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });

        mTurnData = null;
    }

    // Sign-in, Sign out behavior

    // Update the visibility based on what state we're in.
    public void setViewVisibility() {
        boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());

        if (!isSignedIn) {
            findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);

            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            return;
        }

        findViewById(R.id.login_layout).setVisibility(View.GONE);

        //insert player id if he/she doesn't exist
        insertDb(getCurrentPlayerId());

        if (isDoingTurn) {
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.matchup_layout).setVisibility(View.VISIBLE);
        }
    }

    // Switch to gameplay view.
    public void setGameplayUI() {
        isDoingTurn = true;

        setViewVisibility();

        try{
            //String[] parts = mTurnData.data.split("--NEXT--");
            String movie = mTurnData.data.getString("movieName");
            String language = mTurnData.data.getString("language");
            Boolean showVowels = mTurnData.data.getBoolean("showVowels");
            String hint = mTurnData.data.getString("hint");
            posterUrl = mTurnData.data.getString("posterUrl");
            releaseDate = mTurnData.data.getString("releaseDate");

            Intent intent = new Intent(this, GameLogic.class);
            intent.putExtra("Movie", movie);
            intent.putExtra("Language", language);
            intent.putExtra("ShowVowels", showVowels);
            intent.putExtra("Hint", hint);
            startActivityForResult(intent, RC_GUESS_MOVIE);
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "This match is either finished or cancelled. Check completed matches for more information", Toast.LENGTH_SHORT).show();
            setViewVisibility();
        }

    }

    // Helpful dialogs

    public void showSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.GONE);
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    // Rematch dialog
    public void askForRematch() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage("Do you want a rematch?");

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rematch();
                            }
                        })
                .setNegativeButton("No.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

        alertDialogBuilder.show();
    }

    // This function is what gets called when you return from either the Play
    // Games built-in inbox, or else the create game built-in interface.
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (request == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (response == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                //BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
            }
        } else if (request == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            TurnBasedMatch match = data
                    .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) {
                updateMatch(match);
            }

            Log.d(TAG, "Match = " + match);
        } else if (request == RC_SELECT_PLAYERS) {
            // Returned from 'Select players to Invite' dialog


            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }
            showSpinner();
            // get the invitee list
            final ArrayList<String> invitees = data
                    .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();


            // Start the match
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            processResult(result);
                        }
                    });

        }
        else if(request == RC_START_MATCH){
            dismissSpinner();
            if(response == Activity.RESULT_OK) {
                movieName = data.getStringExtra("MovieName");
                posterUrl = data.getStringExtra("Poster");
                language = data.getStringExtra("Language");
                releaseDate = data.getStringExtra("ReleaseDate");
                Boolean showVowels = data.getBooleanExtra("ShowVowels", false);
                hint = data.getStringExtra("Hint");
                posterUrl = posterUrl.isEmpty()? "null" : posterUrl;
                releaseDate = releaseDate.isEmpty() ? "null" :  releaseDate;
                try {
                    startMatch(movieName, language, showVowels, hint, posterUrl, releaseDate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{

            }
        }
        else if(request == RC_GUESS_MOVIE){
            if(response == Activity.RESULT_OK) {
                String status = data.getStringExtra("Status");
                movieName = data.getStringExtra("Movie");
                try {
                    onFinishClicked(findViewById(R.id.matchup_layout), status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                endGame(movieName, posterUrl, releaseDate);
            } else {
                onCancelClicked(findViewById(android.R.id.content));
            }
        }
        else if(request == RC_MATCH_RESULT){
            if(response == Activity.RESULT_OK) {
            }
           setViewVisibility();
        } else if(request == RC_GAME_DESCRIPTION){
            if(response == Activity.RESULT_OK) {
            }
            setViewVisibility();
        }
    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(String movieName, String language, Boolean showVowels, String hint, String posterUrl, String releaseDate) throws JSONException {

       // mGoogleApiClient.connect();


        mTurnData = new GameTurn();
        JSONObject turnDataInfo = new JSONObject();
        turnDataInfo.put("movieName", movieName);
        turnDataInfo.put("language", language);
        turnDataInfo.put("showVowels", showVowels);
        turnDataInfo.put("posterUrl", posterUrl);
        turnDataInfo.put("releaseDate", releaseDate);
        turnDataInfo.put("hint", hint);

        mMatch = mStartTurnBasedMatch;

        String nextParticipantId = getNextParticipantId();

        showSpinner();

        Games.TurnBasedMultiplayer.takeTurn(this.mGoogleApiClient, mStartTurnBasedMatch.getMatchId(),
                mTurnData.persist(turnDataInfo), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        insertDb(getCurrentPlayerId());

        makeSimpleDialog(this, "Match Request has been send!").show();
    }

    // If you choose to rematch, then call it and wait for a response.
    public void rematch() {
        showSpinner();
        Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                });
        mMatch = null;
        isDoingTurn = false;
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }

    public String getNextPlayerId(){
        String nextParticipantId  = getNextParticipantId();
        return mMatch.getParticipant(nextParticipantId).getDisplayName();
    }

    public String getCurrentPlayerId(){
        String playerId = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
        return playerId;
    }

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                    mTurnData = GameTurn.unpersist(mMatch.getData());
                    try{
                            endGame(mTurnData.data.getString("movieName"), mTurnData.data.getString("posterUrl"),mTurnData.data.getString("releaseDate"));
                    }catch(Exception e){
                        Toast.makeText(MainActivity.this, "Match no longer exists!", Toast.LENGTH_SHORT).show();
                    }

                    showWarning(
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                return;

        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = GameTurn.unpersist(mMatch.getData());
                setGameplayUI();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;

        setViewVisibility();
    }

    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        dismissSpinner();

        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;

        showWarning("Match",
                "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        mStartTurnBasedMatch = match;

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }
        Intent intent = new Intent(this, StartMatch.class);
        startActivityForResult(intent, RC_START_MATCH);

    }


    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        showWarning("Left", "You've left this match.");
    }


    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.canRematch()) {
            askForRematch();
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }

    // Handle notification events.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), TOAST_DELAY)
                .show();


    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        Toast.makeText(this, "An invitation was removed.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        Toast.makeText(this, "A match was updated.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchRemoved(String matchId) {
        Toast.makeText(this, "A match was removed.", TOAST_DELAY).show();

    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
                                 int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        this,
                        "Stored action for later.  (Please remove this toast before release.)",
                        TOAST_DELAY).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }

        return false;
    }

    void updateLeaderboards(boolean won) {
        String [] scoreInfoPlayer1 = new String[4];
        scoreInfoPlayer1 = readFromDb(getCurrentPlayerId());
        int scoresPlayer1 =  Integer.parseInt(scoreInfoPlayer1[1].replaceAll("'",""));
        int totalMatchesPlayer1 =  Integer.parseInt(scoreInfoPlayer1[2].replaceAll("'",""));
        int lostMatchesPlayer1 =  Integer.parseInt(scoreInfoPlayer1[3].replaceAll("'",""));
        String [] scoreInfoPlayer2 = new String[4];
        scoreInfoPlayer2 = readFromDb(getNextPlayerId());
        int scoresPlayer2 =  Integer.parseInt(scoreInfoPlayer2[1].replaceAll("'",""));
        int totalMatchesPlayer2 =  Integer.parseInt(scoreInfoPlayer2[2].replaceAll("'",""));
        int lostMatchesPlayer2 =  Integer.parseInt(scoreInfoPlayer2[3].replaceAll("'",""));
        if(won) {
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leadership_id), scoresPlayer1+5);
            updateDb(getCurrentPlayerId(), scoresPlayer1+5, totalMatchesPlayer1+1, lostMatchesPlayer1, "won");
            updateDb(getNextPlayerId(), scoresPlayer2, totalMatchesPlayer2+1, lostMatchesPlayer2+1, "lost");
        }
        else {
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leadership_id), scoresPlayer1);
            updateDb(getCurrentPlayerId(), scoresPlayer1, totalMatchesPlayer1+1, lostMatchesPlayer1+1, "lost");
            updateDb(getNextPlayerId(), scoresPlayer2+5, totalMatchesPlayer2+1, lostMatchesPlayer2, "won");
        }
    }

    void updateScoresFromDb(){
        String [] scoreInfo = new String[4];
        scoreInfo = readFromDb(getCurrentPlayerId());
        int scores =  Integer.parseInt(scoreInfo[1].replaceAll("'",""));
        String status =  scoreInfo[4].replaceAll("'","");
        if(status.equals("won")){
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leadership_id), scores);
        }
    }

    public void insertDb(String playerId){
        // Gets the data repository in write mode
        SQLiteDatabase db = scoreDb.getWritableDatabase();
        String initialValue ="'0'";

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_PLAYER_ID,"'"+ playerId+"'");
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_SCORE, initialValue);
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_LOST_MATCHES, initialValue);
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_TOTAL_MATCHES, initialValue);
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_STATUS, "'lost'");

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ScoreContract.ScoreEntry.TABLE_NAME,
                null,
                values);
        db.close();
    }

    public void updateDb(String playerId, int score, int matches, int lost, String status){
        SQLiteDatabase db = scoreDb.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_SCORE, "'"+ score+"'");
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_TOTAL_MATCHES, "'"+matches+"'");
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_LOST_MATCHES, "'"+lost+"'");
        values.put(ScoreContract.ScoreEntry.COLUMN_NAME_STATUS, "'"+status+"'");


        // Which row to update, based on the ID
        String selection = ScoreContract.ScoreEntry.COLUMN_NAME_PLAYER_ID + " = ?";
        String[] selectionArgs = {"'"+ playerId +"'" };

        int count = db.update(
                ScoreContract.ScoreEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();
    }

    public String[] readFromDb(String playerId){
        SQLiteDatabase db = scoreDb.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {"*"
        };

        String[] results = {null, null, null, null, null};


        // Which row to update, based on the ID
        String selection = ScoreContract.ScoreEntry.COLUMN_NAME_PLAYER_ID + " = ?";
        String[] selectionArgs = { "'"+playerId+"'" };

        Cursor c = db.query(
                ScoreContract.ScoreEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );



        if(c.moveToFirst() || c.getCount() != 0){
            results[0] = playerId;
            results[1] = c.getString(c.getColumnIndex(ScoreContract.ScoreEntry.COLUMN_NAME_SCORE));
            results[2] = c.getString(c.getColumnIndex(ScoreContract.ScoreEntry.COLUMN_NAME_TOTAL_MATCHES));
            results[3] = c.getString(c.getColumnIndex(ScoreContract.ScoreEntry.COLUMN_NAME_LOST_MATCHES));
            results[4] = c.getString(c.getColumnIndex(ScoreContract.ScoreEntry.COLUMN_NAME_STATUS));
        }
        db.close();
        if(results[0] == null){
            insertDb(playerId);
            String[] initialValues = {playerId, "'0'", "'0'","'0'","'lost'"};
            return initialValues;
        } else {
            return results;
        }
    }

    public static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                mSignInClicked = true;
                mTurnBasedMatch = null;
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                mGoogleApiClient.connect();
                break;
        }
    }


    public static boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client, ConnectionResult result, int requestCode,
                                                   String fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                showAlert(activity, fallbackErrorMessage);
            }
            return false;
        }
    }

    public static void showAlert(Activity activity, String message) {
        (new AlertDialog.Builder(activity)).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    public void getLatestMoviePosters(){
        ProgressDialog progress = new ProgressDialog(MainActivity.this);
        new TmdbQuerySearch("", (SliderLayout) findViewById(R.id.slider), (PagerIndicator) findViewById(R.id.custom_indicator), getApplicationContext(), progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem signOutBtnVisible = menu.findItem(R.id.action_logout);
        if(mGoogleApiClient.isConnected())
        {
            signOutBtnVisible.setVisible(true);
        }
        else
        {
            signOutBtnVisible.setVisible(false);
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_game_rules:
                Intent intent = new Intent(this, GameDescription.class);
                startActivityForResult(intent, RC_GAME_DESCRIPTION);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logOut(){
        mSignInClicked = false;
        if (mGoogleApiClient.isConnected()) {
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
        setViewVisibility();
    }


}


