package com.example.movietime.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.CinemaObject;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.example.movietime.fragments.SeatMatrixFragment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CinemasMovieDetailsAdapter extends RecyclerView.Adapter<CinemasMovieDetailsAdapter.MyViewHolder> {

    String movieId;
    List<Integer> cinemas_index_list;
    MovieObject movieObject;
    Double current_lat, current_long;
    List<Double> lats;
    List<Double> longs;
    List<HashSet<String>> no_of_friends_watching;
    Double washingtonLat=38.9072;
    Double washingtonLong=77.0369;
    Context activityContext;

    public CinemasMovieDetailsAdapter(Context activityContext,MovieObject movieObject, Double current_lat, Double current_long, List<Double> lats ,List<Double> longs
                    ,List<HashSet<String>> no_of_friends_watching) {
        this.movieId = movieId;
        this.current_lat = current_lat;
        this.current_long = current_long;
        this.lats=lats;
        this.longs=longs;
        this.activityContext=activityContext;
        this.movieObject=movieObject;
        movieId=movieObject.getId();
        this.no_of_friends_watching=no_of_friends_watching;

        cinemas_index_list=movieObject.getCinemasfromId(movieId);
        Collections.sort(cinemas_index_list);
    }

//    public CinemasMovieDetailsAdapter(String movieId) {
//        this.movieId = movieId;
//        movieObject=new MovieObject();
////        movieObject.setId(movieId);
//        cinemas_index_list=movieObject.getCinemasfromId(movieId);
//    }

    public float distanceBetweenPoints (Double lat_a, Double lng_a, Double lat_b, Double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    @NonNull
    @Override
    public CinemasMovieDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_cinemas_movie_details;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CinemasMovieDetailsAdapter.MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {

        return lats.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView cinema_address_tv, friends_watching_tv,distance_cinema_tv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cinema_address_tv=itemView.findViewById(R.id.cinema_address_tv);
            friends_watching_tv=itemView.findViewById(R.id.no_of_friends_watching_tv);
            distance_cinema_tv=itemView.findViewById(R.id.distance_cinema_tv);
            itemView.setOnClickListener(this);;
        }

        public void bind(int position) {

            Log.d("position", String.valueOf(position));

            CinemaObject cinemaObject=new CinemaObject();

            String[] ar=cinemaObject.getCinema_addresses();
                Integer x1=cinemas_index_list.get(position);
                cinema_address_tv.setText(ar[x1]);

            if(activityContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                friends_watching_tv.setText(String.valueOf(no_of_friends_watching.get(cinemas_index_list.get(position)).size())+" contacts");
            }
            else {
                friends_watching_tv.setText(String.valueOf(no_of_friends_watching.get(cinemas_index_list.get(position)).size()) + " contacts watching");
            }
                float ans=distanceBetweenPoints(
                        current_lat,current_long,
//                        washingtonLat,washingtonLong,
                        lats.get(position),longs.get(position));
                ans/=1000;
                ans= (float) (Math.round(ans * 100.0) / 100.0);
            distance_cinema_tv.setText(String.valueOf(ans)+" km");

        }

        @Override
        public void onClick(View v) {

            SeatMatrixFragment seatMatrixFragment=new SeatMatrixFragment(movieObject,cinemas_index_list.get(getAdapterPosition()));
            FragmentManager manager = ((AppCompatActivity)activityContext).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction =  manager.beginTransaction();
            fragmentTransaction.replace(R.id.movie_detail_host, seatMatrixFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        }
    }
}
