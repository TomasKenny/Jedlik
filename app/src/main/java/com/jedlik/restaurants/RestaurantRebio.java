package com.jedlik.restaurants;

/**
 * Created by Tom on 9.7.2016.
 */

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
public class RestaurantRebio extends CRestaurantBase
{
    //constructor
    public RestaurantRebio(Activity c){
        m_context = c;
        m_name = "Rebio Holandská";
        m_resid = CRestaurantPool.ID_REBIO;
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        List<CMeal> meals = new ArrayList<>();
        try{
            int today = Utils.GetDayOfWeek();
            switch(today){
                case Calendar.MONDAY:
                    today = 0; break;
                case Calendar.TUESDAY:
                    today = 1; break;
                case Calendar.WEDNESDAY:
                    today = 2; break;
                case Calendar.THURSDAY:
                    today = 3; break;
                case Calendar.FRIDAY:
                    today = 4; break;
                default:
                    return meals;
            }

            Document doc = Jsoup.connect("http://www.rebio.cz/Holandska/Nase-nabidka/Jidelni-listek-ul-Holandska/dW-ei-ej.article.aspx").get();
            Elements mealsThisWeek = doc.getElementsByClass("padding");
            if(today >= mealsThisWeek.size()){
                return meals;
            }
            Elements mealsToday = mealsThisWeek.get(today).getElementsByTag("strong");

            for(Element oneMeal: mealsToday){
                String mealStr = oneMeal.text().trim();
                CMeal meal = new CMeal(mealStr, 0);
                meals.add(meal);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }
}


