package com.example.movietime;

public class AppUtility {

    private static final String API_KEY="ba1cbd940b7f3b9597fa89a52e81f3a8";

    private static final String BASE_URL_UPCOMING="https://api.themoviedb.org/3/movie/upcoming";

    public static final String BASE_IMAGE_URL="https://image.tmdb.org/t/p/w185/";

    public static final String BASE_SIMILAR_URL="https://api.themoviedb.org/3/movie/506554/similar";

    public static String getBaseSimilarUrl() {
        return BASE_SIMILAR_URL;
    }

    public static String getBaseImageUrl() {
        return BASE_IMAGE_URL;
    }

    public static String st;

    public static String getSt() {
        return st;
    }

    public static void setSt(String st) {
        AppUtility.st = st;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getBaseUrlPopular() {
        return BASE_URL_UPCOMING;
    }



}
