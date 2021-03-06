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
import com.example.movietime.R;
import com.example.movietime.activities.MovieDetail;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RecommendedMoviesAdapter extends RecyclerView.Adapter<RecommendedMoviesAdapter.MyViewHolder> {

    List<JSONObject> jsonObjects;

    public RecommendedMoviesAdapter(List<JSONObject> jsonObjects) {
        this.jsonObjects = jsonObjects;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_recommended_movies;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedMoviesAdapter.MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return jsonObjects==null?0:jsonObjects.size();
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

        public MyViewHolder(View view) {
            super(view);

            imageView = itemView.findViewById(R.id.image_view);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {

            Log.d("position", String.valueOf(position));

            try {
                String url= AppUtility.getBaseImageUrl() +jsonObjects.get(position).getString("poster_path");
                Log.d("picasso",url);
                Picasso.get().load(url).into(imageView);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onClick(View v) {

            Intent intent=new Intent(v.getContext(), MovieDetail.class);
            try {
                intent.putExtra("title",jsonObjects.get(getAdapterPosition()).getString("title"));
                intent.putExtra("overview",jsonObjects.get(getAdapterPosition()).getString("overview"));
                intent.putExtra("poster_path",jsonObjects.get(getAdapterPosition()).getString("poster_path"));
                intent.putExtra("release_date",jsonObjects.get(getAdapterPosition()).getString("release_date"));
                intent.putExtra("backdrop_path",jsonObjects.get(getAdapterPosition()).getString("backdrop_path"));
                intent.putExtra("id",jsonObjects.get(getAdapterPosition()).getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            v.getContext().startActivity(intent);
        }
    }
}
