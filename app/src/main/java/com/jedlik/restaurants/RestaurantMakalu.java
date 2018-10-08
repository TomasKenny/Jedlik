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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestaurantMakalu extends CRestaurantBase {
    //constructor
    public RestaurantMakalu(Activity c) {
        m_context = c;
        m_name = "Makalu";
        m_resid = CRestaurantPool.ID_MAKALU;
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0) {
        List<CMeal> meals = new ArrayList<>();

        int today = Utils.GetDayOfWeek();
        if ((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)) {
            return meals;
        }

        Document doc;
        try {
            doc = Jsoup.connect("http://www.nepalska-restaurace-makalu.cz").get();
            Element weekMenu = doc.getElementById("T_menu");
            if (weekMenu == null) {
                return meals;
            }
            Elements mealsParagraph = weekMenu.getElementsByTag("p");
            if (mealsParagraph == null) {
                return meals;
            }

            boolean foundDayStart = false;
            for (int i = 0; i < mealsParagraph.size(); i++) {
                if (!foundDayStart) {
                    String dayName = mealsParagraph.get(i).text().toLowerCase().trim();
                    foundDayStart = dayName.startsWith(Utils.daysInWeek[today]);
                    continue;
                }

                String [] mealsToday = mealsParagraph.get(i).html().split("<b>");
                AddMeals(mealsToday, meals);
                return meals;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }

    //1. 150g Chicken kormal<span class='cena'>95/111 Kč</span></b><br>
    //        Kuřecí kousky s jemnou omáčkou.<br>
    private final Pattern m_pattern = Pattern.compile("^\\d+\\.(.*)<.*>(\\d+)/\\d+.*<br>(.*)<br>(.*)");

    private void AddMeals(String [] mealsToday, List<CMeal> meals){
        for(String oneMealStr: mealsToday) {
            if(oneMealStr.contains("polévka")){
                String [] soupArray = oneMealStr.split("<br>");
                if(soupArray.length > 0){
                    String soupName = soupArray[soupArray.length - 1];
                    soupName = soupName.replaceAll("/.*", "");
                    meals.add(new CMeal(soupName, 0));
                }
                continue;
            }

            Matcher m = m_pattern.matcher(oneMealStr);
            if (!m.matches()) {
                continue;
            }
            String mealName = m.group(1) + " - " + m.group(3);
            mealName = mealName.replace("</b>", "");
            int cost = Integer.parseInt(m.group(2));
            meals.add(new CMeal(mealName, cost));
        }
    }
}
