package com.jedlik.restaurants;

/**
 * Created by Tom on 9.7.2016.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.jedlik.CMeal;
import com.jedlik.IRestaurant;
import com.jedlik.MainActivity;
import com.jedlik.R;
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
public class RestaurantMyFood extends CRestaurantBase
{
    //constructor
    public RestaurantMyFood(Activity c){
        m_context = c;
        m_name = "My Food Holandská";
        m_resid = CRestaurantPool.ID_MYFOOD;
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

            Document doc = Jsoup.connect("https://www.sklizeno.cz/o-nas/brno-holandska").get();
            Elements mealsDiv = doc.getElementsByClass("jidla");
            if(mealsDiv.size() == 0){
                return meals;
            }
            Elements mealsThisWeek = mealsDiv.get(0).children();
            if(today >= mealsThisWeek.size()){
                return meals;
            }

            Elements mealsToday = mealsThisWeek.get(today).getElementsByTag("li");

            for(Element oneMeal: mealsToday){
                Elements mealNameElem = oneMeal.getElementsByTag("span");
                if(mealNameElem.size() == 0){
                    continue;
                }
                String mealStr = mealNameElem.get(0).text().trim();

                int cost = 0;
                Elements costElement = oneMeal.getElementsByTag("small");
                if(costElement.size() > 0){
                    String costStr = costElement.get(0).text();
                    costStr = costStr.replaceAll("\\s+Kč", "");
                    cost = Utils.MyStringToInt(costStr);
                }
                CMeal meal = new CMeal(mealStr, cost);
                meals.add(meal);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }
}


