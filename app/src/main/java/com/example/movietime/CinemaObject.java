package com.example.movietime;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CinemaObject {

    private final String[] cinema_addresses=new String[]{
            "Select City Walk Mall, District Center, Saket, New Delhi",
            "DLF MOIN, 4th floor, C502, Plot No 3, M-Block, Sector 18, NOIDA - 201301, UP",
            "Logix City Centre BW-58, Sector 32, Noida, Uttar Pradesh 201301",
            "PVR YPCC Mall , Yashwat Palace Chanakyapuri , New Delhi",              //3

            "PVR LimitedPVR Sangam theatre,Complex,R.K.Puram New delhi. 110022",
            "61 Community Centre, Basant Lok Vasant Vihar, Delhi",
            "Ambience Mall, Nelson Mandela Road, Vasant Kunj, New Delhi",           //6
            "Regal Building, Baba kharag singh marg, Connaught place, New Delhi",
            "3rd Floor, Ambience Mall, NH-8, Gurgaon",
            "Sahara Mall, MG Road, Gurgoan"                //9

    };

    private final String[] intervals=new String[]{
            "9 to 11 a.m.",
            "1 to 3 p.m.",
            "5 to 7 p.m.",
            "7 to 9 p.m.",
    };

    private List<LatLng> latLngList;

    public CinemaObject() {
        setLatLongList();
    }

    public String[] getIntervals() {
        return Arrays.copyOf(intervals,intervals.length);
    }



    public void setLatLongList()
    {
        latLngList=new ArrayList<>();
        latLngList.add(new LatLng(28.5288503,77.21954079999999));
        latLngList.add(new LatLng(28.567339,77.3211513));
        latLngList.add(new LatLng(28.574297599999998,77.3536665));
        latLngList.add(new LatLng(28.5841472,77.1913536));                       //3

        latLngList.add(new LatLng(28.572625199999997,77.17348439999999));
        latLngList.add(new LatLng(28.5231423,77.20783209999999));
        latLngList.add(new LatLng(28.5412473,77.1551671));                          //6
        latLngList.add(new LatLng(28.6306339,77.2169638));
        latLngList.add(new LatLng(28.503650399999998,77.09733039999999));
        latLngList.add(new LatLng(28.480140,77.118960));                   //9

    }


    public List<LatLng> getLatLngList() {
        return latLngList;
    }



    public String[] getCinema_addresses() {
        return Arrays.copyOf(cinema_addresses,cinema_addresses.length);
    }
}
