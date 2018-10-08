package com.jedlik.restaurants;

import android.app.Activity;

import com.jedlik.CMeal;
import com.jedlik.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Tom on 9.7.2016.
 */
public class RestaurantIq extends CRestaurantBase
{
    //constructor
    public RestaurantIq(Activity c){
        m_context = c;
        m_name = "IQ Restaurant";
        m_resid = CRestaurantPool.ID_IQ;
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        List<CMeal> meals = new ArrayList<>();

        try{
            int today = Utils.GetDayOfWeek();
            if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
                return meals;
            }
            int todayIndex = today - Calendar.MONDAY;
            //int todayIndex = 1; ////DEBUGGGGGGGGGGG

            Document doc = Jsoup.connect("http://www.iqrestaurant.cz/brno/getData.svc?type=brnoMenuHTML2").get();
            Elements dayItems = doc.getElementsByClass("menuDayItems");
            if(dayItems.size() < 2 *(todayIndex + 1)){
                return meals;
            }
            for(int i = 0; i < 2; i++) {// 2 = normal menu + week menu
                Elements todayMeals = dayItems.get(2 * todayIndex + i).children();
                String foodName = "";
                for (int j = 0; j < todayMeals.size(); j++) {
                    Element temp = todayMeals.get(j);
                    if (temp.tagName() == "dt") {
                        Elements menuNumber = temp.getElementsByClass("menuNumber");
                        Elements weekNumber = temp.getElementsByClass("weekNumber");
                        String prefix = "";
                        if(menuNumber.size() > 0){
                           prefix = menuNumber.get(0).text();
                        }
                        else if(weekNumber.size() > 0){
                            prefix = weekNumber.get(0).text();
                        }
                        foodName = temp.text();
                        if(foodName.startsWith(prefix)){
                            foodName = foodName.substring(prefix.length());
                        }
                    }
                    else if (temp.tagName() == "dd") {
                        String priceStr = temp.text();
                        int delim = priceStr.indexOf(",-");
                        if (delim != -1) {
                            priceStr = priceStr.substring(0, delim);
                        }
                        if (foodName != "") {
                            CMeal meal = new CMeal(foodName, Utils.MyStringToInt(priceStr));
                            meals.add(meal);
                            foodName = "";
                        }
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }
}
