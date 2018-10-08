package com.jedlik;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Tom on 9.7.2016.
 */
public class Utils {

    public static final String [] daysInWeek = {
            "sobota", "neděle", "pondělí", "úterý", "středa", "čtvrtek", "pátek"
    };

    public static int GetDayOfWeek(){
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public static int MyStringToInt(String str){
        try{
            return Integer.parseInt(str);
        }
        catch(NumberFormatException e){
            return 0;
        }
    }

    public static String RemoveLastChar(String s){
        if(s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static <T> void StartMyTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

    public static class NoFoodException extends Exception {
        //Parameterless Constructor
        public NoFoodException(){}

        //Constructor that accepts a message
        public NoFoodException(String message){
            super(message);
        }
    }

    public static int dpToPx(Context c, int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(Context c, int px) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static String StripAccents(String s){
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static class RestaurantOrderItem{
        RestaurantOrderItem(int id, String name, boolean isActive){
            m_id = id;
            m_name = name;
            m_isActive = isActive;
        }
        public int m_id;
        public String m_name;
        public boolean m_isActive;
    }

    public static List<RestaurantOrderItem> StringToRestOrder(String str){
        List<RestaurantOrderItem> list = new ArrayList<>();
        String[] restaurants = str.split(";");
        if(restaurants == null || restaurants.length == 0){
            return list;
        }
        for(int i = 0; i < restaurants.length; i++){
            String[] items = restaurants[i].split(",");
            if(items.length != 3){
                continue;
            }
            RestaurantOrderItem rest = new RestaurantOrderItem(
                    MyStringToInt(items[0]), items[1], MyStringToInt(items[2]) != 0
            );
            list.add(rest);
        }

        return list;
    }

    public static String RestOrderToString(List<RestaurantOrderItem> restaurants){
        if(restaurants == null || restaurants.size() == 0){
            return "";
        }
        String str = "";
        for(int i = 0; i < restaurants.size(); i++){
            if(i > 0){
                str += ";";
            }
            str += "" + restaurants.get(i).m_id;
            str += ",";
            str += restaurants.get(i).m_name;
            str += ",";
            str += restaurants.get(i).m_isActive ? "1" : "0";
        }
        return str;
    }
}
