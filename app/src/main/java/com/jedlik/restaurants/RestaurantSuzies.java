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
import java.util.List;

/**
 * Created by Tom on 9.7.2016.
 */
public class RestaurantSuzies extends CRestaurantBase
{
    //constructor
    public RestaurantSuzies(Activity c){
        m_context = c;
        m_name = "Suzie's";
        m_resid = CRestaurantPool.ID_SUZIES;
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        List<CMeal> meals = new ArrayList<CMeal>();
        Document doc = null;
        try{
            doc = Jsoup.connect("https://www.zomato.com/widgets/daily_menu.php?entity_id=16506939&amp;width=100%25&amp;height=700&quot;").get();
            Elements mealsToday = doc.getElementsByClass("item");
            if(mealsToday.size() == 0){
                return meals;
            }

            for(int i = 0; i < mealsToday.size(); i++){
                Element oneMeal = mealsToday.get(i);
                Elements mealsNameElement = oneMeal.getElementsByClass("item-name");
                Elements priceElement = oneMeal.getElementsByClass("item-price");
                if(mealsNameElement.size() == 0 || priceElement.size() == 0){
                    continue;
                }
                String priceStr = priceElement.get(0).text();
                char nbsp = 0xA0;
                priceStr = priceStr.replace(nbsp, ' ');
                priceStr = priceStr.replace(" Kč", "");
                int cost = Utils.MyStringToInt(priceStr);
                String mealName = mealsNameElement.get(0).text().trim().toLowerCase();
                if(mealName.contains("menu s polévkou") || mealName.contains("víkendová nabídka")){
                    if(meals.isEmpty()){
                        continue;
                    }
                    break;  //this is meal from the next day
                }
                CMeal meal = new CMeal(mealsNameElement.get(0).text(), cost);
                meals.add(meal);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }
}


