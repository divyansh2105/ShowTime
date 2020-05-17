package com.example.movietime;

import java.io.Serializable;

public class FavoritesObject implements Serializable {

    private String databaseId;

    private MovieObject movieObject;

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public MovieObject getMovieObject() {
        return movieObject;
    }

    public void setMovieObject(MovieObject movieObject) {
        this.movieObject = movieObject;
    }
}
