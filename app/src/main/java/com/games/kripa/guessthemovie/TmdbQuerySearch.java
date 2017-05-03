package com.games.kripa.guessthemovie;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

//public class TmdbQuerySearch {
//    ViewPager viewPager;
//    PagerAdapter pageAdapter;
//    String EXTRA_QUERY = "Query";
//    CirclePageIndicator mIndicator;
//    int [] moviePosters;
//    private ProgressDialog progressDialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tmdbquerysearch);
//        progressDialog = new ProgressDialog(TmdbQuerySearch.this);
//        Intent intent = getIntent();
//        String query = intent.getStringExtra(EXTRA_QUERY);
//
//        // Check if the NetworkConnection is active and connected.
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            new TMDBQueryManager().execute(query);
//        } else {
//            TextView textView = new TextView(this);
//            textView.setText("No network connection.");
//            setContentView(textView);
//        }
//    }

    public class TmdbQuerySearch extends AsyncTask implements BaseSliderView.OnSliderClickListener {

        private final String TMDB_API_KEY = "36073b90b4180c75881e92624e72ec3c"; //tmdb
        //private final String TMDB_API_KEY = "B2B269D887D32D7E0E0643E007545328"; //cinmealytics
        private static final String DEBUG_TAG = "TMDBQueryManager";
        private String IMG_BASE_URL = "";
        private String IMG_SIZE = "";
        private String query;
        private SliderLayout slider;
        private PagerIndicator pager;
        private ProgressDialog progressDialog;
        private Context appContext;

        public TmdbQuerySearch(String query, SliderLayout slider, PagerIndicator pager, Context appContext, ProgressDialog progress) {
            this.query = query;
            this.slider = slider;
            this.pager = pager;
            this.appContext = appContext;
            this.progressDialog = progress;
            execute(query);
        }


        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog.setMessage("Downloading Images");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();


        }

        private void getConfigDetailsFromTmdb() {
            TmdbConfig config = null;
            try {
                config = (TmdbConfig)searchIMDB("Config", "");
                IMG_BASE_URL = config.getImageBaseUrl();
                IMG_SIZE = config.getPosterSizes().get(5);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            @Override
        protected ArrayList<MovieResult> doInBackground(Object... params) {
            ArrayList<MovieResult> movies = new ArrayList<MovieResult>();
            try {
                getConfigDetailsFromTmdb();
                movies = (ArrayList<MovieResult>)searchIMDB("Movies", (String) params[0]);
                return movies;
            } catch (IOException e) {
                return null;
            }
        }


        private Object fetch(String address) throws MalformedURLException,IOException {
            URL url = new URL(address);
            Object content = url.getContent();
            return content;
        }

        @Override
        protected void onPostExecute(Object result) {
            progressDialog.dismiss();
            SliderLayout sliderShow = slider;
            HashMap<String,String> url_maps = new HashMap<String, String>();
            for(MovieResult movie: (ArrayList<MovieResult>)result){
                url_maps.put(movie.getTitle(), movie.getPosterPath());
                TextSliderView textSliderView = new TextSliderView(appContext);
                // initialize a SliderLayout
                textSliderView
                        .description(movie.getTitle())
                        .image(IMG_BASE_URL + IMG_SIZE + movie.getPosterPath())
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);
                sliderShow.addSlider(textSliderView);
            }
            sliderShow.setCustomIndicator((PagerIndicator) pager);
            sliderShow.setCustomAnimation(new DescriptionAnimation());
            sliderShow.setDuration(4000);

//            moviePosters = new int[] {R.drawable.poster1, R.drawable.poster2, R.drawable.poster3};
//            viewPager = (ViewPager) findViewById(R.id.pager);
//            pageAdapter = new ViewPagerAdapter(TmdbQuerySearch.this, moviePosters);
//            viewPager.setAdapter(pageAdapter);
//            mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
//            mIndicator.setViewPager(viewPager);
        };

        /**
         * Searches IMDBs API for the given query
         * @param query The query to search.
         * @return A list of all hits.
         */
        public Object searchIMDB(String type, String query) throws IOException {
            // Build URL
            String BASE_URL = "http://api.themoviedb.org/3/";
            StringBuilder stringBuilder = new StringBuilder();
            String result;
            URL url;
            switch(type) {
                case "Config":
                    stringBuilder.append(BASE_URL + "configuration" + "?api_key=" + TMDB_API_KEY);
                    url = new URL(stringBuilder.toString());
                    result = HTTPConnection(url);
                    return parseConfigResult(result);
                case "Movies":
                    stringBuilder.append(BASE_URL + "movie/upcoming" + "?api_key=" + TMDB_API_KEY);
                    //stringBuilder.append("https://api.cinemalytics.com/v1/movie/upcoming?auth_token=" + TMDB_API_KEY);
                    url = new URL(stringBuilder.toString());
                    result = HTTPConnection(url);
                    return parseMovieResult(result);
                default:
                    return null;

            }
        }

        private String HTTPConnection(URL url) throws IOException{
            InputStream stream = null;
            try {
                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json"); // Required to get TMDB to play nicely.
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

                stream = conn.getInputStream();
                return stringify(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private ArrayList<MovieResult> parseMovieResult(String result) {
            String streamAsString = result;
            ArrayList<MovieResult> results = new ArrayList<MovieResult>();
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);
                JSONArray array = (JSONArray) jsonObject.get("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    MovieResult.Builder movieBuilder = MovieResult.newBuilder(
                            Integer.parseInt(jsonMovieObject.getString("id")),
                            jsonMovieObject.getString("title"))
                            .setBackdropPath(jsonMovieObject.getString("backdrop_path"))
                            .setOriginalTitle(jsonMovieObject.getString("original_title"))
                            .setPosterPath(jsonMovieObject.getString("poster_path"));
                    results.add(movieBuilder.build());
                }
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: " + streamAsString);
            }
            return results;
        }

        private TmdbConfig parseConfigResult(String result){
            String streamAsString = result;
            TmdbConfig results = null;
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);
                JSONObject images = (JSONObject) jsonObject.get("images");
                JSONArray imagesListJsonArray = images.getJSONArray("poster_sizes");
                ArrayList<String> imagesList = new ArrayList<String>();
                for (int i=0; i<imagesListJsonArray.length(); i++) {
                    imagesList.add( imagesListJsonArray.getString(i));
                }
                TmdbConfig.ConfigBuilder configBuilder = TmdbConfig.newBuilder()
                                                                    .setImageBaseUrl(images.getString("base_url"))
                                                                    .setPosterSizes(imagesList);
                results = configBuilder.build();

            } catch (JSONException e) {
                System.err.println(e);
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: " + streamAsString);
            }
            return results;
        }


        public String stringify(InputStream stream) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }

        @Override
        public void onSliderClick(BaseSliderView slider) {

        }
    }

