package com.example.movietime.ui.favorites;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.FavoritesObject;
import com.example.movietime.R;
import com.example.movietime.adapters.FavoriteMoviesAdapter;
import com.example.movietime.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference databaseFavorite;

    List<FavoritesObject> favoritesObjects;
    String loggedInEmailId;
    List<JSONObject> jsonObjects;

    public FavoritesFragment(String loggedInEmailId, List<JSONObject> jsonObjects) {
        this.loggedInEmailId = loggedInEmailId;
        this.jsonObjects=jsonObjects;
    }

    public interface FavoritesInteface
    {
        public void favoritesInterfaceSetText(String s);
    }

    FavoritesInteface favoritesInteface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            favoritesInteface = (FavoritesInteface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement favoritesInterface");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView=root.findViewById(R.id.recycler_view_favoritefragment);
        GridLayoutManager gridLayoutManager = null;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager= new GridLayoutManager(root.getContext(), 2, GridLayoutManager.VERTICAL, false);
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(root.getContext(), 3, GridLayoutManager.VERTICAL, false);
        }
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(true);

        favoritesObjects=new ArrayList<>();

        favoritesInteface.favoritesInterfaceSetText("Favorites");

        databaseFavorite= FirebaseDatabase.getInstance().getReference("Favorites");
        retrieveFavorites();


        return root;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FavoritesFragment favoritesFragment=new FavoritesFragment(loggedInEmailId, jsonObjects);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, favoritesFragment);
        transaction.commit();
//
    }

    public void retrieveFavorites()
    {

        databaseFavorite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                favoritesObjects.clear();

                for(DataSnapshot iterator: dataSnapshot.getChildren())
                {
                    FavoritesObject favoritesObject2=iterator.getValue(FavoritesObject.class);
                    if(favoritesObject2.getEmail().equals(loggedInEmailId)) {
                        favoritesObjects.add(favoritesObject2);
                    }
                }


                Log.d("retrieved_database", String.valueOf(favoritesObjects));

                FavoriteMoviesAdapter upcomingMoviesAdapter=new FavoriteMoviesAdapter(favoritesObjects,loggedInEmailId, jsonObjects);
                recyclerView.setAdapter(upcomingMoviesAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}