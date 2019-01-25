package com.jedlik.restaurants;

import android.app.Activity;

import com.jedlik.IRestaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 29.8.2016.
 */
public class CRestaurantPool {
    public final static int ID_TESAR = 0x04505D00;
    public final static int ID_KOMETA = 0x04505D01;
    public final static int ID_MYFOOD = 0x04505D02;
    public final static int ID_SUZIES = 0x04505D03;
    public final static int ID_TUSTO = 0x04505D04;
    public final static int ID_REBIO = 0x04505D05;
    public final static int ID_IQ = 0x04505D06;
    public final static int ID_MAKALU = 0x04505D07;
    public final static int ID_EATOLOGY = 0x04505D08;
    public final static int ID_SNOPEK = 0x04505D09;
    public final static int ID_MORAVKA = 0x04505D10;
    public final static int ID_AVAST = 0x04505111;

    public static List<IRestaurant> GetRestaurants(Activity activity){
        List<IRestaurant> list = new ArrayList<>();
        list.add(new RestaurantTesar(activity));
        list.add(new RestaurantKometa(activity));
        list.add(new RestaurantMyFood(activity));
        list.add(new RestaurantSuzies(activity));
        list.add(new RestaurantTusto(activity));
        list.add(new RestaurantRebio(activity));
//        list.add(new RestaurantIq(activity));
        list.add(new RestaurantMakalu(activity));
        list.add(new RestaurantEatology(activity));
        list.add(new RestaurantSnopek(activity));
        list.add(new RestaurantMoravka(activity));
        //list.add(new RestaurantAvast(activity));
        return list;
    }
}
