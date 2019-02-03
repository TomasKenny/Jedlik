package com.jedlik.restaurants;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jedlik.CMeal;
import com.jedlik.CMyPdfReader;
import com.jedlik.FileDownloader;
import com.jedlik.Utils;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tom on 6.7.2016.
 */
public class RestaurantAvast extends CRestaurantBase
{
    //constructor
    public RestaurantAvast(Activity c){
        m_context = c;
        m_name = "Avast Brno";
        m_resid = CRestaurantPool.ID_AVAST;
    }

    protected String GetLastSavedMenu(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String content = sharedPref.getString("avastmenutext", "");
        if(content.isEmpty()){
            return content;
        }
        int today = Utils.GetDayOfWeek();
        Calendar calendar = Calendar.getInstance();
        final long startOfWeekMs = System.currentTimeMillis() - (today - Calendar.MONDAY) * 86400 * 1000;
        calendar.setTimeInMillis(startOfWeekMs);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) - Calendar.JANUARY + 1;

        //look for "DD.M." in pdf content
        String pattern = String.format("%d.%d.", day, month);
        String contentCopy = content.replace(" ", "");
        if(contentCopy.contains(pattern)){
            return content;
        }
        return "";
    }

    @Override
    protected List<CMeal> doInBackground(Void... arg0){
        try{
            int today = Utils.GetDayOfWeek();
            if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
                return new ArrayList<>();
            }

            String content = GetLastSavedMenu();
            if(!content.isEmpty()){
                return ParseText(content);
            }

            FileDownloader.DownloadFromUrl(
                    "http://menu.perfectcanteen.cz/pdf/21/cz/a/a3",
                    m_context.getFilesDir().toString(),
                    "menu.pdf"
            );

            File f = new File(m_context.getFilesDir(), "menu.pdf");
            PDFBoxResourceLoader.init(m_context);
            content = CMyPdfReader.Read(f);

            //save the text for next time
            if(!content.isEmpty()) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("avastmenutext", content);
                editor.commit();
            }
            return ParseText(content);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ArrayList<CMeal>();
        }
    }


    private final Pattern m_pattern
            = Pattern.compile("^(\\d\\.)(\\s*)(.*)(\\s+)(\\d+)(\\D*)");


    public List<CMeal> ParseText(String text){
        List<CMeal> meals = new ArrayList<>();
        String textStr [] = text.split("\\r\\n|\\n|\\r");
        List<String> lines = new ArrayList<>(); //JoinSplitLines(textStr);
        int today = Utils.GetDayOfWeek();
        if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
            return meals;
        }

        /*
        for(int i = 0; i < lines.size(); i++){
            if(lines.get(i).toLowerCase().startsWith(Utils.daysInWeek[today])){
                i++;
                boolean firstItem = true;
                while(i < lines.size()){
                    CMeal meal;
                    if(firstItem){
                        //soup
                        meal = new CMeal(lines.get(i), 0);
                        meals.add(meal);
                        firstItem = false;
                    }
                    else{
                        Matcher m = m_pattern.matcher(lines.get(i));
                        if(!m.matches()){
                            gotoSpecialMeals = true;
                            break;
                        }
                        meal = new CMeal(m.group(3), Integer.parseInt(m.group(5)));
                        meals.add(meal);
                    }
                    i++;
                }

            }
        }
        */

        return meals;
    }
}

