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
 * Created by Erik on 29.1.2019.
 */

public class RestaurantHoveziPupek extends CRestaurantBase
{
    //constructor
    public RestaurantHoveziPupek(Activity c){
        m_context = c;
        m_name = "U hovězího pupku";
        m_resid = CRestaurantPool.ID_HOVEZIPUPEK;
    }


    private final Pattern m_mealNamePattern
            = Pattern.compile("^(.*[^\\d\\s,A])\\s*((A\\s?)?\\d+[,\\s?\\d+]*)");

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
            Document doc = Jsoup.connect("https://www.uhovezihopupku.cz/menu/").get();

            Elements todayMenu = doc.getElementsByClass("menu_dnes");
            if (todayMenu == null || todayMenu.isEmpty()) {
                return meals;
            }

            Elements todaySections = todayMenu.first().getElementsByClass("menu_den");
            for (int k=0; k<todaySections.size(); k++) {
                Elements todayMeals = todaySections.get(k).getElementsByTag("tr");
                for (int j=1; j<todayMeals.size(); j++) {
                    Elements couples = todayMeals.get(j).getElementsByTag("td");
                    CMeal meal = new CMeal("", 0);
                    for (int i=0; i<couples.size(); i++) {
                        Element element = couples.get(i);
                        if (element.hasClass("menu_jidlo_text"))
                            meal.m_mealName = RemoveAllergens(element.text());
                        if (element.hasClass("menu_jidlo_cena")) {
                            char nbsp = 0xA0;
                            String priceText = element.text().replace(nbsp, ' ').replace("Kč", "").trim();
                            if (!priceText.isEmpty())
                                meal.m_cost = Utils.MyStringToInt(priceText);
                        }
                    }
                    if (!meal.m_mealName.isEmpty())
                        meals.add(meal);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }

}
