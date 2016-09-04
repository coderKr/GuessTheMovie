package com.games.kripa.guessthemovie;

import android.provider.BaseColumns;

/**
 * Created by Kripa on 27/2/2016.
 */
public class ScoreContract {
        // To prevent someone from accidentally instantiating the contract class,
        // give it an empty constructor.
        public ScoreContract() {}

        /* Inner class that defines the table contents */
        public static abstract class ScoreEntry implements BaseColumns {
            public static final String TABLE_NAME = "score";
            public static final String COLUMN_NAME_PLAYER_ID = "PlayerId";
            public static final String COLUMN_NAME_SCORE = "Score";
            public static final String COLUMN_NAME_TOTAL_MATCHES = "Matches";
            public static final String COLUMN_NAME_LOST_MATCHES = "LostMatches";
        }


}

