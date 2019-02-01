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
 * Created by Erik on 18.1.2019.
 */
public class RestaurantSnopek extends CRestaurantBase {
    //constructor
    public RestaurantSnopek(Activity c) {
        m_context = c;
        m_name = "Snopek";
        m_resid = CRestaurantPool.ID_SNOPEK;
    }

    //Polévka: Fazolová polévka (1,9)
    //1. Segedínský vepřový guláš, houskový knedlík (1,3,7)
    //Moučník 1: Lívance s povidlím a tvarohem – 2ks (1,3,7)
    private final Pattern m_pattern = Pattern.compile("^(Polévka\\s*:|\\d\\s*\\.|Moučník\\s*\\d\\s*:)\\s*(.*)\\s*(\\([\\d,\\s]+\\))\\s*(([\\d]+)\\s*Kč)?");

    private final String desertKW = "Moučník";

    @Override
    protected List<CMeal> doInBackground(Void... arg0) {
        List<CMeal> meals = new ArrayList<>();

        int today = Utils.GetDayOfWeek();
        if ((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)) {
            return meals;
        }

        // they seem to alternate these two URLs on weekly basis
        if (!LoadTodaysMeals(today, "http://www.stravovanisnopek.cz/jidelni-listek-od/", meals))
            LoadTodaysMeals(today, "http://www.stravovanisnopek.cz/jidelni-listek-od-4-12-2017-do-8-12-2017-2/", meals);

        return meals;
    }

    private boolean LoadTodaysMeals(int today, String url, List<CMeal> meals) {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();

            Elements weekMenu = doc.getElementsByClass("wpb_text_column");
            if (weekMenu == null || weekMenu.isEmpty()) {
                return false;
            }

            Elements mealsParagraph = weekMenu.first().getElementsByTag("p");
            if (mealsParagraph == null) {
                return false;
            }

            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year =  cal.get(Calendar.YEAR);

            boolean foundToday = false;
            for (int i = 0; i < mealsParagraph.size(); i++) {
                if (!foundToday) {
                    // look for actual day of week
                    String dayStr = mealsParagraph.get(i).text().toLowerCase().trim();
                    if (dayStr.startsWith(Utils.daysInWeek[today])) {
                        // verify that the date is really today   // Pátek : 25.1.2019
                        String dayParts[] = dayStr.split(":|\\.");
                        if ((dayParts.length == 4) &&
                                (Integer.parseInt(dayParts[1].trim()) == day) &&
                                (Integer.parseInt(dayParts[2].trim()) == month) &&
                                (Integer.parseInt(dayParts[3].trim()) == year)) {
                            foundToday = true;
                        }
                        else
                            return false;   // wrong week
                    }
                    continue;
                }

                // parse section of meals for today
                String mealStr = mealsParagraph.get(i).text().trim();
                Matcher m = m_pattern.matcher(mealStr);
                if (!m.matches())
                    break;  // reached end of section for today
                String mealName = m.group(2);
                if (m.group(1).toLowerCase().startsWith(desertKW.toLowerCase()))
                    mealName = desertKW + ": " + mealName;
                int mealPrice = 0;
                if (m.group(5) != null && !m.group(5).isEmpty())
                    mealPrice = Utils.MyStringToInt(m.group(5));
                CMeal meal = new CMeal(mealName, mealPrice);
                meals.add(meal);
            }
            return foundToday;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
