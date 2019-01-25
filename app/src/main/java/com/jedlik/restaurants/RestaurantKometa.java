package com.jedlik.restaurants;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.jedlik.CMeal;
import com.jedlik.IRestaurant;
import com.jedlik.MainActivity;
import com.jedlik.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tom on 9.7.2016.
 */
public class RestaurantKometa extends CRestaurantBase
{
    //constructor
    public RestaurantKometa(Activity c){
        m_context = c;
        m_name = "Kometa Pub Arena";
        m_resid = CRestaurantPool.ID_KOMETA;
    }


    private final Pattern m_mealNamePattern
            = Pattern.compile("^(.*[^\\d\\s,])\\s*(\\d+[,\\s?\\d+]*)");

    String RemoveAllergens(String mealName) {
        Matcher m = m_mealNamePattern.matcher(mealName);
        if (m.matches())
            return m.group(1);

        return mealName;
    }


    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        List<CMeal> meals = new ArrayList<>();

        try{
            int today = Utils.GetDayOfWeek();
            String elementName;
            switch(today){
                case Calendar.MONDAY:
                    elementName = "div1"; break;
                case Calendar.TUESDAY:
                    elementName = "div2"; break;
                case Calendar.WEDNESDAY:
                    elementName = "div3"; break;
                case Calendar.THURSDAY:
                    elementName = "div4"; break;
                case Calendar.FRIDAY:
                    elementName = "div5"; break;
                default:
                    return meals;
            }

            Document doc = Jsoup.connect("http://arena.kometapub.cz/tydenni-menu.php").get();
            Element content = doc.getElementById(elementName);
            Elements todayMeals = content.getElementsByTag("tr");

            for(int j = 1; j < todayMeals.size(); j++){
                Elements pairs = todayMeals.get(j).getElementsByTag("td");
                CMeal meal = new CMeal("", 0);
                for(int i = 0; i < pairs.size(); i++){
                    String pairItemText = pairs.get(i).text();
                    char nbsp = 0xA0;
                    pairItemText = pairItemText.replace(nbsp, ' ');
                    pairItemText = pairItemText.replace("POLÉVKA:", "");
                    pairItemText = pairItemText.trim();

                    if(i == 0){
                        meal.m_mealName = RemoveAllergens(pairItemText);
                    }
                    else{
                        pairItemText = pairItemText.replace(",-", "");
                        meal.m_cost = Utils.MyStringToInt(pairItemText); //Integer.valueOf(pairItemText);
                    }
                }
                meals.add(meal);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }

}
