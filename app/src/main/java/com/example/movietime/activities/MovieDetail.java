package com.example.movietime.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.movietime.ApplicationClass;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.example.movietime.fragments.MovieDetailsFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

public class MovieDetail extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener{

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    TextView title_tv;
    JSONObject jsonObject;
    String title, overview, poster_path, backdrop_path, release_date, id;
    String jsonObjectString, loggedInEmailId;
    MovieObject movieObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar_layout);

        if(savedInstanceState!=null)
        {
            title=savedInstanceState.getString("title");
            overview=savedInstanceState.getString("overview");
            poster_path=savedInstanceState.getString("poster_path");
            release_date=savedInstanceState.getString("release_date");
            backdrop_path=savedInstanceState.getString("backdrop_path");
            id=savedInstanceState.getString("id");
            jsonObjectString=savedInstanceState.getString("jsonObjects");

        }
        else {
            Bundle extras = getIntent().getExtras();

            title = extras.getString("title");
            overview = extras.getString("overview");
            poster_path = extras.getString("poster_path");
            release_date = extras.getString("release_date");
            backdrop_path = extras.getString("backdrop_path");
            id = extras.getString("id");
            jsonObjectString = extras.getString("jsonObjects");
        }
        loggedInEmailId=((ApplicationClass)getApplication()).getApplicationEmail();



        movieObject=new MovieObject();

            movieObject.setTitle(title);
            movieObject.setOverview(overview);
            movieObject.setPoster_url(poster_path);
            movieObject.setRelease_date(release_date);
            movieObject.setBack_image_url(backdrop_path);
            movieObject.setId(id);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        MovieDetailsFragment movieDetailsFragment=new MovieDetailsFragment(movieObject,loggedInEmailId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.movie_detail_host, movieDetailsFragment);
        transaction.commit();
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.nav_home:  {
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("launch","home");
                startActivity(intent);
                break;
            }

            case R.id.nav_favorites:    {
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("launch","favorites");
                intent.putExtra("myemail",loggedInEmailId);
                intent.putExtra("jsonObjectsString",jsonObjectString);
                startActivity(intent);
                break;
            }



            case R.id.nav_details:  {

                Intent intent=new Intent(this,BookingDetailsActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonObjectString));
                intent.putExtra("email",loggedInEmailId);
                startActivity(intent);

                break;
            }

            case R.id.nav_cancel:   {
                Intent intent=new Intent(this, BookingDeleteActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonObjectString));
                intent.putExtra("email",loggedInEmailId);
                startActivity(intent);
                break;
            }




        }
        //close navigation drawer

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title",title);
        outState.putString("overview",overview);
        outState.putString("poster_path",poster_path);
        outState.putString("release_date",release_date);
        outState.putString("backdrop_path",backdrop_path);
        outState.putString("id",id);
        outState.putString("jsonObjects",jsonObjectString);

    }
}
