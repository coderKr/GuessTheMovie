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

    private MovieResult(Builder builder){
        backdropPath = builder.backdropPath;
        originalTitle = builder.originalTitle;
        id = builder.id;
        posterPath = builder.posterPath;
        title = builder.title;
    }

    public static class Builder {
        private String title;
        private String originalTitle;
        private String backdropPath;
        private int id;
        private String posterPath;

        public Builder(int id, String title) {
            this.id = id;
            this.title = title;
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
}
