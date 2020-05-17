package com.example.movietime.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.movietime.BookingObject;
import com.example.movietime.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookingDeleteActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference databaseReference;
    EditText edittext_databaseId;
    String databaseId;

    List<BookingObject> bookingObjects;
    Button delete_button;

    DrawerLayout drawer;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    TextView title_tv;
    String loggedInEmailId;
    private AppBarConfiguration mAppBarConfiguration;
    List<JSONObject> jsonObjects2;
    String jsonString;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_delete);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(3).setChecked(true);

        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar_layout);

        title_tv=findViewById(R.id.title_tv);

        appBarLayout=findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
//                    toolbar.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(200, 100));
                    title_tv.setText("Booking Cancel");
//                    title_tv.setGravity(Gravity.TOP);
//                    Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_SHORT).show();
                }else //if(!toolbar.getTitle().equals(mExpandedTitle))
                {
                    title_tv.setText("Booking Cancel");
//                         title_tv.setGravity(Gravity.BOTTOM);
//                         Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_SHORT).show();
                }

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        if(savedInstanceState!=null)
        {
            jsonString=savedInstanceState.getString("jsonString");
            loggedInEmailId=savedInstanceState.getString("email");
        }

        JSONArray jsonArray;
        Bundle extras=getIntent().getExtras();
        if(extras!=null) {
            loggedInEmailId = extras.getString("email");
            jsonString = extras.getString("jsonObjects");
            jsonObjects2 = new ArrayList<>();
            try {
                jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObjects2.add(jsonArray.getJSONObject(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        edittext_databaseId=findViewById(R.id.edittext_databaseId);

        delete_button=findViewById(R.id.delete_button);

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseId=edittext_databaseId.getText().toString();
                bookingObjects=new ArrayList<>();
                if(databaseId.equals(""))
                {
                    return;
                }
                showAlert();

            }
        });



    }

    public void showAlert()
    {
        builder = new AlertDialog.Builder(this);

        //Setting message manually and performing action on button click
        builder.setMessage("Do you want to cancel your Booking")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings").child(databaseId);
                        databaseReference.removeValue();
                        Toast.makeText(getApplicationContext(),"Booking Cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Confirmation");
        alert.show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

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
                intent.putExtra("jsonObjectsString",jsonString);
                startActivity(intent);
                break;
            }



            case R.id.nav_details:  {

                Intent intent=new Intent(this,BookingDetailsActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonObjects2));
                intent.putExtra("email",loggedInEmailId);
                startActivity(intent);

                break;
            }

            case R.id.nav_cancel:   {
                Intent intent=new Intent(this, BookingDeleteActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonString));
                intent.putExtra("email",loggedInEmailId);
                startActivity(intent);
                break;
            }
//            case R.id.nav_signout:  {
//                Intent intent=new Intent(this,MainActivity.class);
//                intent.putExtra("launch","signout");
//                startActivity(intent);
//            }



        }
        //close navigation drawer

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("jsonString", jsonString);
        outState.putString("email",loggedInEmailId);
    }


}
