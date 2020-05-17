package com.example.movietime.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.movietime.AppUtility;
import com.example.movietime.R;
import com.squareup.picasso.Picasso;

public class ReceiptFromBookingDetailsActivity extends AppCompatActivity {

    String cinema_address;
    String date;
    String time;
    String poster_url;
    String movie_title;
    String seats;
    String amount;
    String phone;
    TextView cinema_tv, show_date, show_time, movie_name, seats_tv, price_tv,phone_no_tv, title_tv,bookingId_tv;
    ImageView poster_imageview;
    String bookingId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(" ");

        Window window = this.getWindow();


        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Bundle extras=getIntent().getExtras();
        cinema_address=extras.getString("address");
        date=extras.getString("selected_date");
        time=extras.getString("selected_interval");
        poster_url=extras.getString("poster_url");
        movie_title=extras.getString("movieTitle");
        phone=extras.getString("phone_no");
        amount=extras.getString("amount");
        seats=extras.getString("seats");
        bookingId=extras.getString("booking_id");

        cinema_tv=findViewById(R.id.cinema_address_tv);

        bookingId_tv=findViewById(R.id.bookingId_tv);
        bookingId_tv.setText(bookingId);

        show_date=findViewById(R.id.show_date_tv);
        show_time=findViewById(R.id.show_time_tv);

        price_tv=findViewById(R.id.price_tv);
        title_tv=findViewById(R.id.movie_name_tv);


        phone_no_tv=findViewById(R.id.phone_no_tv);


        movie_name=findViewById(R.id.movie_name_tv);
        seats_tv=findViewById(R.id.seats_tv);
        poster_imageview=findViewById(R.id.poster_image);
        String url= AppUtility.getBaseImageUrl() +poster_url;
        Log.d("picasso",url);
        Picasso.get().load(url).into(poster_imageview);

        cinema_tv.setText(cinema_address);
        show_date.setText(date);
        show_time.setText(time);
        price_tv.setText(amount);
        seats_tv.append(seats);
        phone_no_tv.append(phone);
        title_tv.setText(movie_title);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
