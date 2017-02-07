package com.games.kripa.guessthemovie;

import java.util.ArrayList;

/**
 * Created by Kripa on 7/2/2017.
 */

public class TmdbConfig {
    private final String ImageBaseUrl;
    private final ArrayList<String> posterSizes;

    private TmdbConfig(ConfigBuilder builder){
        ImageBaseUrl = builder.ImageBaseUrl;
        posterSizes = builder.posterSizes;
    }

    public static class ConfigBuilder {
        private String ImageBaseUrl;
        private ArrayList<String> posterSizes;

        public ConfigBuilder setImageBaseUrl(String imgUrl) {
            this.ImageBaseUrl = imgUrl;
            return this;
        }

        public ConfigBuilder setPosterSizes(ArrayList<String> sizes){
            this.posterSizes = sizes;
            return this;
        }

        public TmdbConfig build() {
            return new TmdbConfig(this);
        }
    }

    public static ConfigBuilder newBuilder() {
        return new ConfigBuilder();
    }

    public String getImageBaseUrl(){
        return this.ImageBaseUrl;
    }

    public ArrayList<String> getPosterSizes() {
        return this.posterSizes;
    }

}
