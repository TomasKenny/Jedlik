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

/**
 * Created by Tom on 25.7.2016.
 */
public class RestaurantTusto extends CRestaurantBase
{
    //constructor
    public RestaurantTusto(Activity c){
        m_context = c;
        m_name = "Tusto Titanium";
        m_resid = CRestaurantPool.ID_TUSTO;
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        List<CMeal> meals = new ArrayList<>();

        int today = Utils.GetDayOfWeek();
        if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
            return meals;
        }

        Document doc;
        try{
            doc = Jsoup.connect("http://titanium.tusto.cz/tydenni-menu").get();
            Element mealsThisWeekWrap = doc.getElementById("rccontent");
            if(mealsThisWeekWrap == null){
                return meals;
            }
            Elements mealsThisWeek = mealsThisWeekWrap.getElementsByTag("table");
            if(mealsThisWeek == null){
                return meals;
            }
            for(int i = 0; i < mealsThisWeek.size(); i++){
                Elements dayElement = mealsThisWeek.get(i).getElementsByTag("h2");
                if(dayElement == null || dayElement.size() == 0){
                    continue;
                }
                String dayName = dayElement.get(0).text().toLowerCase().trim();
                if(!dayName.startsWith(Utils.daysInWeek[today])){
                    continue;
                }

                Elements mealsToday = mealsThisWeek.get(i).getElementsByTag("tr");
                if(mealsToday == null){
                    return meals;
                }

                for(int j = 1; j < mealsToday.size(); j++){
                    GetOneMeal(meals, mealsToday.get(j));
                }
                //fix: return immediately after parsing one respective weekday
                //(there happen to be multiple the same weekdays, e.g. this Friday and Friday next week)
                return meals;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return meals;
    }

    private final Pattern m_pattern = Pattern.compile("^(\\d+),-(.*)");

    private void GetOneMeal(List<CMeal> meals, Element mealsToday){
        Elements items = mealsToday.getElementsByTag("td");
        if(items.size() != 3){  //jmeno + alergeny + cena
            return;
        }
        String mealName = items.get(0).text();
        if(mealName.length() > 2){
            if(Character.isDigit(mealName.charAt(0)) && (mealName.charAt(1) == ')')){
                mealName = mealName.substring(2).trim();
            }
        }
        String costStr =  items.get(2).text();
        int cost = 0;
        Matcher m = m_pattern.matcher(costStr);
        if(m.matches()){
            cost = Integer.parseInt(m.group(1));
        }
        CMeal meal = new CMeal(mealName, cost);
        meals.add(meal);
    }
}
