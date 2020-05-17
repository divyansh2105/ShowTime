package com.example.movietime.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.BookingWidget;
import com.example.movietime.BuildConfig;
import com.example.movietime.ContactObject;
import com.example.movietime.R;
import com.example.movietime.adapters.BookingDetailsAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookingDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference databaseReference;
    List<BookingObject> bookingObjects;


    String loggedInEmailId;

    RadioButton radioButton;
    RadioGroup radioGroup;

    int radiobutton_checked=R.id.radio_phone;
    EditText phone_edittext;
    TextView email_tv;
    List<ContactObject> contactObjects;

    RecyclerView recyclerView_bookingDetails;
    BookingDetailsAdapter bookingDetailsAdapter;

    Button go_button;
    List<JSONObject> jsonObjects;
    JSONArray jsonArray;

    String jsonString;

    DrawerLayout drawer;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    TextView title_tv;
    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        final Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout2);
        NavigationView navigationView = findViewById(R.id.nav_view2);


        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(2).setChecked(true);

        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar_layout2);

        title_tv=findViewById(R.id.title_tv2);

        appBarLayout=findViewById(R.id.app_bar_layout2);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
//                    toolbar.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(200, 100));
                    title_tv.setText("Booking Details");
//                    title_tv.setGravity(Gravity.TOP);
//                    Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_SHORT).show();
                }else //if(!toolbar.getTitle().equals(mExpandedTitle))
                {
                    title_tv.setText("ShowTime");
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
            jsonString=savedInstanceState.getString("jsonObjects");
            loggedInEmailId=savedInstanceState.getString("email");
        }
        else {
            Bundle extras = getIntent().getExtras();
            jsonString = extras.getString("jsonObjects");
            jsonObjects = new ArrayList<>();
            try {
                jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObjects.add(jsonArray.getJSONObject(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loggedInEmailId = extras.getString("email");
        }

        phone_edittext=findViewById(R.id.editText_phone);
        email_tv=findViewById(R.id.email_display_tv);
        radioGroup=(RadioGroup)findViewById(R.id.radio_group);
        go_button=findViewById(R.id.go_button);

        contactObjects=((ApplicationClass) this.getApplication()).getContactsListFromCursor();

        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");

        recyclerView_bookingDetails =findViewById(R.id.recyclerview_booking_details);
        recyclerView_bookingDetails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radiobutton_checked=checkedId;
                if(checkedId==R.id.radio_phone)
                {
//                    email_tv.setVisibility(View.GONE);
                    phone_edittext.setVisibility(View.VISIBLE);
                    email_tv.setText("Search all GLOBAL bookings of your contacts");
                    email_tv.setTextSize(15);
//                    searchByPhoneInfo_tv.setVisibility(View.VISIBLE);
                }
                else if(checkedId==R.id.radio_email)
                {
                    phone_edittext.setVisibility(View.INVISIBLE);
//                    email_tv.setVisibility(View.VISIBLE);
                    email_tv.setText(loggedInEmailId);
                    email_tv.setTextSize(18);
//                    searchByPhoneInfo_tv.setVisibility(View.INVISIBLE);
                }
            }
        });

        bookingObjects = new ArrayList<>();
        go_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                retrieveDatabase();

            }
        });

    }

    public void retrieveDatabase()
    {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                    BookingObject bookingObject = iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }

                if(radiobutton_checked==R.id.radio_phone)
                {
                    populateThroughPhone();
                }
                else if(radiobutton_checked==R.id.radio_email)
                {
                    populateThroughEmail();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void populateThroughPhone()
    {

        List<BookingObject> bookingObjects_byPhone=new ArrayList<>();
        for(int i=0;i<bookingObjects.size();i++)
        {
            String newPhone=phone_edittext.getText().toString();
            String newphone2 = "";
            for(int j=0;j<newPhone.length();j++)
            {
                if(newPhone.charAt(j)>=48 && newPhone.charAt(j)<=57)
                {
                    newphone2+=newPhone.charAt(j);
                }
                else
                {
                    newphone2+="";
                }
            }
            if(newphone2.length()>10)
            {
                newphone2=newphone2.substring(newphone2.length()-10);
            }

            if(bookingObjects.get(i).getPhone().equals(newphone2) && bookingObjects.get(i).getGlobal().equals("true"))
            {
                for(int j=0;j<contactObjects.size();j++)
                {
                    if(contactObjects.get(j).getContactPhoneNo().equals(newphone2))
                    {
                        bookingObjects_byPhone.add(bookingObjects.get(i));
                        break;
                    }
                }
            }


        }
        bookingDetailsAdapter=new BookingDetailsAdapter(bookingObjects_byPhone,jsonObjects);
        recyclerView_bookingDetails.setAdapter(bookingDetailsAdapter);



    }

    public void populateThroughEmail()
    {
        List<BookingObject> bookingObjects_byEmail=new ArrayList<>();
        for(int i=0;i<bookingObjects.size();i++)
        {

            if(bookingObjects.get(i).getEmail().equals(loggedInEmailId))
            {
                bookingObjects_byEmail.add(bookingObjects.get(i));
            }
        }
        bookingDetailsAdapter=new BookingDetailsAdapter(bookingObjects_byEmail,jsonObjects);
        recyclerView_bookingDetails.setAdapter(bookingDetailsAdapter);


    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if (sharedPreferences.getString("ID", "") .equals (((ApplicationClass)getApplication()).getApplicationEmail())){
            menu.findItem(R.id.mi_action_widget).setIcon(R.drawable.ic_star_white_48dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mi_action_widget){
            boolean isRecipeInWidget = (sharedPreferences.getString("ID", "") .equals(((ApplicationClass)getApplication()).getApplicationEmail()));

            // If recipe already in widget, remove it
            if (isRecipeInWidget){
                sharedPreferences.edit()
                        .remove("ID")
                        .remove("PHONE")
                        .remove("CONTENT")
                        .apply();

                item.setIcon(R.drawable.ic_star_border_white_48dp);
                Toast.makeText(this, "Widget Removed",Toast.LENGTH_SHORT).show();

                ComponentName provider = new ComponentName(this, BookingWidget.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                int[] ids = appWidgetManager.getAppWidgetIds(provider);
                BookingWidget bakingWidgetProvider = new BookingWidget();
                bakingWidgetProvider.onUpdate(this, appWidgetManager, ids);
            }
            // if recipe not in widget, then add it
            else{
                getWidgetContent();


                item.setIcon(R.drawable.ic_star_white_48dp);
                Toast.makeText(this, "Widget Added",Toast.LENGTH_SHORT).show();
            }

            // Put changes on the Widget

        }

        return super.onOptionsItemSelected(item);
    }

    public  void getWidgetContent()
    {
        final List<BookingObject> bookingObjects_widget=new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                    BookingObject bookingObject = iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }

                for(int i=0;i<bookingObjects.size();i++)
                {

                    if(bookingObjects.get(i).getEmail().equals(loggedInEmailId))
                    {
                        bookingObjects_widget.add(bookingObjects.get(i));
                    }
                }

                String content_string="";
                for(int i=0;i<bookingObjects_widget.size();i++)
                {
                    if(i>4)
                    {
                        content_string+="\t..........";
                        break;
                    }
                    content_string+=bookingObjects_widget.get(i).getMovieTitle()+"\n Date: "+bookingObjects_widget.get(i).getDateOfShow()+"\t Time: "+
                            bookingObjects_widget.get(i).getTimeOfShow()+"\n";
                }

                sharedPreferences
                        .edit()
                        .putString("ID", ((ApplicationClass)getApplication()).getApplicationEmail())
                        .putString("PHONE",((ApplicationClass)getApplication()).getApplicationPhone())
                        .putString("CONTENT", content_string)
                        .apply();

                ComponentName provider = new ComponentName(getBaseContext(), BookingWidget.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
                int[] ids = appWidgetManager.getAppWidgetIds(provider);
                BookingWidget bakingWidgetProvider = new BookingWidget();
                bakingWidgetProvider.onUpdate(getBaseContext(), appWidgetManager, ids);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                intent.putExtra("jsonObjectsString", String.valueOf(jsonString));
                startActivity(intent);
                break;
            }



            case R.id.nav_details:  {

                Intent intent=new Intent(this,BookingDetailsActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonString));
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




        }
        //close navigation drawer

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("jsonObjects",jsonString);
        outState.putString("email",loggedInEmailId);
    }
}
