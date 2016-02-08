package com.games.kripa.guessthemovie;

import com.google.android.gms.common.api.GoogleApiClient;
/**
 * Created by Kripa on 30/1/2016.
 */
public class GoogleAPIClient_Singleton {
    private static final String TAG = "GoogleAPIClient_Singleton";
    private static GoogleAPIClient_Singleton instance = null;

    private static GoogleApiClient mGoogleApiClient = null;

    protected GoogleAPIClient_Singleton() {

    }

    public static GoogleAPIClient_Singleton getInstance(GoogleApiClient aGoogleApiClient) {
        if(instance == null) {
            instance = new GoogleAPIClient_Singleton();
            if (mGoogleApiClient == null)
                mGoogleApiClient = aGoogleApiClient;
        }
        return instance;
    }

    public GoogleApiClient get_GoogleApiClient(){
        return mGoogleApiClient;
    }
}