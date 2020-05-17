package com.example.movietime.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.R;

import java.util.List;

public class FriendsWatchingAdapter extends RecyclerView.Adapter<FriendsWatchingAdapter.MyViewHolder>{

    String name;
    List<String> names;

    public FriendsWatchingAdapter(List<String> names) {
        this.names = names;
    }


    @NonNull
    @Override
    public FriendsWatchingAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.recyclerview_friends_watching;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsWatchingAdapter.MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return names==null?0:names.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView friend_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            friend_name=itemView.findViewById(R.id.friend_name_tv);
        }

        public void bind(int position)
        {
            friend_name.setText(names.get(position));
        }

    }
}
