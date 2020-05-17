package com.example.movietime.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.AppUtility;
import com.example.movietime.FavoritesObject;
import com.example.movietime.R;
import com.example.movietime.activities.MovieDetail;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.MyViewHolder> {

    List<FavoritesObject> favoritesObjects;
    String loggedInEmailId;
    List<JSONObject> jsonObjects;

    public FavoriteMoviesAdapter(List<FavoritesObject> favoritesObjects, String loggedInEmailId, List<JSONObject> jsonObjects) {
        this.favoritesObjects = favoritesObjects;
        this.loggedInEmailId=loggedInEmailId;
        this.jsonObjects=jsonObjects;
    }

    @NonNull
    @Override
    public FavoriteMoviesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_upcoming_movies;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteMoviesAdapter.MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return favoritesObjects==null?0:favoritesObjects.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {

            Log.d("position", String.valueOf(position));


                String url= AppUtility.getBaseImageUrl() +favoritesObjects.get(getAdapterPosition()).getMovieObject().getPoster_url();
                Log.d("picasso",url);
                Picasso.get().load(url).into(imageView);


        }

        @Override
        public void onClick(View v) {

            Intent intent=new Intent(v.getContext(), MovieDetail.class);
            intent.putExtra("title",favoritesObjects.get(getAdapterPosition()).getMovieObject().getTitle());
            intent.putExtra("overview",favoritesObjects.get(getAdapterPosition()).getMovieObject().getOverview());
            intent.putExtra("poster_path",favoritesObjects.get(getAdapterPosition()).getMovieObject().getPoster_url());
            intent.putExtra("release_date",favoritesObjects.get(getAdapterPosition()).getMovieObject().getRelease_date());
            intent.putExtra("backdrop_path",favoritesObjects.get(getAdapterPosition()).getMovieObject().getBack_image_url());
            intent.putExtra("id",favoritesObjects.get(getAdapterPosition()).getMovieObject().getId());
            intent.putExtra("jsonObjects",jsonObjects.toString());
            intent.putExtra("email",loggedInEmailId);
            v.getContext().startActivity(intent);
        }
    }
}
