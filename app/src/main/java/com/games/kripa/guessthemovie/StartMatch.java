package com.games.kripa.guessthemovie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class StartMatch extends AppCompatActivity implements
        View.OnClickListener {

    EditText movieText ;
    Button validateBtn;
    Button sendBtn;
    private Intent intent;
    String title="";
    String posterUrl="";
    String language="";
    String releaseDate="";
    public DelayAutoCompleteTextView movieTitle = null;
    String movie;
    Toolbar mToolbar;
    RelativeLayout postValidation;
    Boolean showVowels = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_match);
        mToolbar = (Toolbar) findViewById(R.id.toolbarstartmatch);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        View sendMovieLayout = findViewById(R.id.start_match);
        sendBtn = (Button) sendMovieLayout.findViewById(R.id.button_send);
        sendBtn.setOnClickListener(this);
        validateBtn = (Button) sendMovieLayout.findViewById(R.id.button_check);
        validateBtn.setOnClickListener(this);
        postValidation = findViewById(R.id.postValidate);
        intent = getIntent();
        movieTitle = (DelayAutoCompleteTextView) findViewById(R.id.MovieSuggestionList);
        movieTitle.setThreshold(2);
        movieTitle.setAdapter(new MovieSuggestionAdapter(this)); // 'this' is Activity instance
        movieTitle.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));

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
        movie = movieTitle.getText().toString();
        try {
            switch (v.getId()) {
                case R.id.button_check:
                    findViewById(R.id.error_msg).setVisibility(v.GONE);
                    postValidation.setVisibility(v.GONE);
                    ArrayList<MovieResult> movies = new ArrayList<MovieResult>();
                    movies = (ArrayList<MovieResult>) new TmdbQuerySearch(movie, null, null,StartMatch.this,null).get();
                    if(movies!=null && !movies.isEmpty() && movies.size() > 0) {
                        title = movies.get(0).getTitle();
                        title = title.replaceAll("[-+.^:,]","");
                        posterUrl = movies.get(0).getBackdropPath();
                        language = movies.get(0).getLanguage();
                        releaseDate = movies.get(0).getReleaseDate();
                        TextView selectedMovieTitle =  findViewById(R.id.selected_movie_title);
                        selectedMovieTitle.setText(title);
                        TextView selectedMovieDesc = findViewById(R.id.selected_movie_description);
                        selectedMovieDesc.setText(movies.get(0).getDescription());
                        TextView selectedMovieLang = findViewById(R.id.selected_movie_language);
                        selectedMovieLang.setText(language);
                        TextView selectedMovieReleaseDate = findViewById(R.id.selected_movie_release_date);
                        selectedMovieReleaseDate.setText(releaseDate);
                        movieTitle.setText("");
                        postValidation.setVisibility(v.VISIBLE);
                    } else{
                        findViewById(R.id.error_msg).setVisibility(v.VISIBLE);
                    }

                    break;
                case R.id.button_send:
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("MovieName",title);
                    returnIntent.putExtra("Poster",posterUrl);
                    returnIntent.putExtra("Language", language);
                    returnIntent.putExtra("ReleaseDate", releaseDate);
                    returnIntent.putExtra("ShowVowels", showVowels);
                    returnIntent.putExtra("Hint", ((EditText)findViewById(R.id.enterHint)).getText().toString());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    break;
                case R.id.showVowel:
                    ((RadioButton) v).setChecked(!showVowels);
                    showVowels = ((RadioButton) v).isChecked();
                    break;
                default:
                    break;
        }
        } catch (InterruptedException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(v.VISIBLE);
        } catch (ExecutionException e) {
            e.printStackTrace();
            findViewById(R.id.error_msg).setVisibility(v.VISIBLE);
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
        ArrayList<MovieResult> movies = new ArrayList<MovieResult>();
        List<String> suggestedMovies = new ArrayList<String>();
        if(!movieTitle.isEmpty()) {
            movies = (ArrayList<MovieResult>) new TmdbQuerySearch(movieTitle, null, null, context, null).get();
            for (MovieResult movie : (ArrayList<MovieResult>) movies) {
                String name = movie.getTitle();
                if(movie.getLanguage().equals("hi") || movie.getLanguage().equals("en")) {
                    suggestedMovies.add(name);
                }
            }
        }
        return suggestedMovies;
    }

}

