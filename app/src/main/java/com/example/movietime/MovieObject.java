package com.example.movietime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovieObject implements Serializable {

    private String title;
    private String release_date;
    private String ratings;
    private String poster_url;
    private String back_image_url;
    private String overview;
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getPoster_url() {
        return poster_url;
    }

    public void setPoster_url(String poster_url) {
        this.poster_url = poster_url;
    }

    public String getBack_image_url() {
        return back_image_url;
    }

    public void setBack_image_url(String back_image_url) {
        this.back_image_url = back_image_url;
    }

    public List<Integer> getCinemasfromId(String id_str)
    {
        int n=id_str.length();
        List<Integer> cinema_list=new ArrayList<>();
        int i=0;
        while(i<n)
        {
            Integer x= Character.getNumericValue(id_str.charAt(i));
            if(!checkInList(cinema_list,x))
            {
                cinema_list.add(x);
            }
            i++;
        }

        return cinema_list;
    }

    public boolean checkInList(List<Integer> list,int x)
    {
        if(list.size()==0)
        {
            return false;
        }
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i)==x)
            {
                return true;
            }
        }
        return false;
    }
}
