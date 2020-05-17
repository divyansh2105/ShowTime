package com.example.movietime.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.AppUtility;
import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.CinemaObject;
import com.example.movietime.R;
import com.example.movietime.activities.ReceiptFromBookingDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class BookingDetailsAdapter extends RecyclerView.Adapter<BookingDetailsAdapter.MyViewHolder>{

    List<BookingObject> bookingObjects;
    List<JSONObject> jsonObjects;
    Context context;
    String movie_url_clicked;
    JSONObject my_jsonObject;
    int position;

    public BookingDetailsAdapter(List<BookingObject> bookingObjects,List<JSONObject> jsonObjects) {
//        notifyDataSetChanged();
        this.bookingObjects = bookingObjects;
        this.jsonObjects=jsonObjects;
    }

    @NonNull
    @Override
    public BookingDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.recyclerview_booking_details;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookingDetailsAdapter.MyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return bookingObjects==null?0:bookingObjects.size();
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

        TextView cinema_address_tv, movie_title_tv, seats_tv, amount_tv, date_tv, time_tv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cinema_address_tv=itemView.findViewById(R.id.cinema_address_tv);
            movie_title_tv=itemView.findViewById(R.id.booking_title_tv);
            seats_tv=itemView.findViewById(R.id.booking_seats_tv);
            amount_tv=itemView.findViewById(R.id.booking_amount_tv);
            date_tv=itemView.findViewById(R.id.booking_date_tv);
            time_tv=itemView.findViewById(R.id.booking_time_tv);

            itemView.setOnClickListener(this);
        }

        public void bind(int position)
        {
            CinemaObject cinemaObject=new CinemaObject();
            String addresses[]=cinemaObject.getCinema_addresses();
            int cinema_index= Integer.parseInt(bookingObjects.get(position).getCinemaId());
            cinema_address_tv.setText(addresses[cinema_index]);

            movie_title_tv.setText(bookingObjects.get(position).getMovieTitle());
            seats_tv.setText("Seat(s): "+bookingObjects.get(position).getSeat());

            amount_tv.setText("Rs. "+bookingObjects.get(position).getPrice());
            date_tv.setText(bookingObjects.get(position).getDateOfShow());
            time_tv.setText(bookingObjects.get(position).getTimeOfShow());
        }
        @Override
        public void onClick(View v) {
//            Toast.makeText(v.getContext(), "clicked"+getAdapterPosition(), Toast.LENGTH_SHORT).show();

//            JSONObject jsonObject=jsonObjects.get(getAdapterPosition());
//            MovieObject movieObject=new MovieObject();
//            try {
//                movieObject.setTitle(jsonObject.getString("title"));
//                movieObject.setOverview(jsonObject.getString("overview"));
//                movieObject.setPoster_url(jsonObject.getString("poster_path"));
//                movieObject.setRelease_date(jsonObject.getString("release_date"));
//                movieObject.setBack_image_url(jsonObject.getString("backdrop_path"));
//                movieObject.setId(jsonObject.getString("id"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
            String movieId=bookingObjects.get(getAdapterPosition()).getMovieId();
            movie_url_clicked= "https://api.themoviedb.org/3/movie/"+movieId+"?api_key="+ AppUtility.getApiKey();
            position=getAdapterPosition();

            new FetchInfo().execute();


        }
    }


    public Object getHttpResponse() {
        OkHttpClient httpClient = new OkHttpClient();


        URL url = null;
        try {
            url = new URL(movie_url_clicked);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(String.valueOf(url))
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "error in getting response get request okhttp");
        }
        return null;
    }

    class FetchInfo extends AsyncTask<String,Void, JSONObject>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected JSONObject doInBackground(String... strings) {

            String response_string="";
            JSONObject jsonObject = null;

                response_string = (String) getHttpResponse();
                Log.d("home_upcoming_url", response_string);


                try {
                    jsonObject = new JSONObject(response_string);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            return jsonObject;

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            my_jsonObject=jsonObject;

            String setDate=bookingObjects.get(position).getDateOfShow();
            String selectedInterval=bookingObjects.get(position).getTimeOfShow();
//            int cinemaIndex=Integer.parseInt(bookingObjects.get(getAdapterPosition()).getCinemaId());

            String phone_no=bookingObjects.get(position).getPhone();

            if(!phone_no.equals(((ApplicationClass)context.getApplicationContext()).getApplicationPhone()))
            {
                return;
            }

            Intent intent=new Intent(context, ReceiptFromBookingDetailsActivity.class);

            CinemaObject cinemaObject=new CinemaObject();
            String addresses[]=cinemaObject.getCinema_addresses();
            int cinema_index= Integer.parseInt(bookingObjects.get(position).getCinemaId());
            intent.putExtra("address",addresses[cinema_index]);

            intent.putExtra("selected_date",setDate);
            intent.putExtra("selected_interval",selectedInterval);

            try {
                intent.putExtra("poster_url",my_jsonObject.getString("poster_path"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            intent.putExtra("movieTitle",bookingObjects.get(position).getMovieTitle());

            intent.putExtra("amount",bookingObjects.get(position).getPrice());

            intent.putExtra("phone_no",phone_no);
            intent.putExtra("seats",bookingObjects.get(position).getSeat());

            intent.putExtra("booking_id",bookingObjects.get(position).getDatabaseEntryId());

            context.startActivity(intent);

        }
    }
}
