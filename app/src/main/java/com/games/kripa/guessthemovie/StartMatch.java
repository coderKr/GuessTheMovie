package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StartMatch extends AppCompatActivity implements
        View.OnClickListener {

    EditText movieText ;
    Button validate;
    private Intent intent;
    Button send;
    String result="";
    String posterUrl="";
    String movieId="";
    String language="";
    String mUrl = "http://www.omdbapi.com/";
    public DelayAutoCompleteTextView movieTitle = null;
    String movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_match);
        View x = findViewById(R.id.start_match);
        send = (Button) x.findViewById(R.id.button_send);
        send.setOnClickListener(this);
        Button check = (Button) x.findViewById(R.id.button_check);
        check.setOnClickListener(this);

        intent = getIntent();
        movieTitle = (DelayAutoCompleteTextView) findViewById(R.id.MovieSuggestionList);
        movieTitle.setThreshold(2);
        movieTitle.setAdapter(new MovieSuggestionAdapter(this)); // 'this' is Activity instance
        movieTitle.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
       /* movieTitle.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            onClick(findViewById(android.R.id.content).getRootView());
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });*/

        movieTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String movie = (String) adapterView.getItemAtPosition(position);
                movieTitle.setText(movie);
            }
        });

    }


    @Override
    public void onClick(View v) {
        findViewById(R.id.selected_movie).setVisibility(View.GONE);
        findViewById(R.id.error_msg).setVisibility(View.GONE);
        send.setVisibility(View.GONE);

        //movie = movieText.getText().toString();
        movie = movieTitle.getText().toString();

        try {
            String url = mUrl + "?t=" + URLEncoder.encode(movie, "UTF-8");

            switch (v.getId()) {
            case R.id.button_check:
                result = new validateMovie().execute(url).get();
                if(result!=null && result.length() > 0) {

                    JSONObject first = new JSONObject(result);

                    result = (String) first.getString("Title");
                    result = result.replaceAll("[-+.^:,]","");
                    posterUrl = (String)first.getString("Poster");
                    movieId = (String) first.getString("imdbID");
                    language = (String)first.getString("Language");


                   // if(language.equals("Hindi") || language.equals("English")) {
                        TextView selectedMovie = (TextView) findViewById(R.id.selected_movie);
                        selectedMovie.setText("The movie you have selected is " + result);
                        findViewById(R.id.selected_movie).setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                   // }else{
                   //     findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
                   // }

                } else{
                    findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
                }

                break;
            case R.id.button_send:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("MovieName",result);
                returnIntent.putExtra("Poster",posterUrl);
                returnIntent.putExtra("MovieId", movieId);
                returnIntent.putExtra("Language", language);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }

        } catch (InterruptedException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
        } catch (ExecutionException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(View.VISIBLE);
        }

    }
}

class MovieSuggestionAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<String> resultList = new ArrayList<String>();

    public MovieSuggestionAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_1line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position));
       // ((TextView) convertView..setText(getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<String> movies = null;
                    try {
                        movies = findMovies(mContext, constraint.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = movies;
                    filterResults.count = movies.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private List<String> findMovies(Context context, String movieTitle) throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        String mUrl = "http://www.omdbapi.com/";
        String url = mUrl + "?s=" + URLEncoder.encode(movieTitle, "UTF-8");
        List<String> suggestedMovies = new ArrayList<String>();
        String result = new validateMovie().execute(url).get();
        if (result != null && result.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                // Getting JSON Array node
                JSONArray moviesList = jsonObj.getJSONArray("Search");

                // looping through All Contacts
                for (int i = 0; i < moviesList.length(); i++) {
                    JSONObject c = moviesList.getJSONObject(i);
                    String name = c.getString("Title");
                    suggestedMovies.add(name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return suggestedMovies;
    }

}

class DelayAutoCompleteTextView extends AutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
    private ProgressBar mLoadingIndicator;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }
        super.onFilterComplete(count);
    }
}

