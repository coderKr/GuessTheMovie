package com.games.kripa.guessthemovie;

/**
 * Created by Kripa on 3/2/2017.
 */

public class MovieResult {
    private final String title;
    private final String originalTitle;
    private final String backdropPath;
    private final int id;
    private final String posterPath;
    private final String language;
    private final String description;
    private final String releaseDate;
    private final Boolean showVowels;
    private final String hint;

    private MovieResult(Builder builder){
        backdropPath = builder.backdropPath;
        originalTitle = builder.originalTitle;
        id = builder.id;
        posterPath = builder.posterPath;
        title = builder.title;
        language = builder.language;
        description = builder.description;
        releaseDate = builder.releaseDate;
        showVowels = builder.showVowels;
        hint = builder.hint;
    }

    public static class Builder {
        private String title;
        private String originalTitle;
        private String backdropPath;
        private int id;
        private String posterPath;
        private String language;
        private String description;
        private String releaseDate;
        private Boolean showVowels;
        private String hint;

        public Builder(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public Builder setLanguage(String language){
            this.language = language;
            return this;
        }

        public Builder setDescription(String desc){
            this.description = desc;
            return this;
        }

        public Builder setReleaseDate(String date){
            this.releaseDate = date;
            return this;
        }

        public Builder setBackdropPath(String backdropPath) {
            this.backdropPath = backdropPath;
            return this;
        }

        public Builder setOriginalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }


        public Builder setPosterPath(String posterPath) {
            this.posterPath = posterPath;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setShowVowels(Boolean showVowels){
            this.showVowels = showVowels;
            return this;
        }
        public Builder setHint(String hint){
            this.hint = hint;
            return this;
        }

        public MovieResult build() {
            return new MovieResult(this);
        }

    }

    public static Builder newBuilder(int id, String title) {
        return new Builder(id, title);
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public int getId() {
        return id;
    }


    public String getPosterPath() {
        return posterPath;
    }


    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public String getLanguage(){ return language; }

    public String getDescription() { return description;}

    public String getReleaseDate() { return releaseDate;}

    public String getHint(){ return hint;}

    public Boolean getShowVowels() {
        return showVowels;
    }
}
