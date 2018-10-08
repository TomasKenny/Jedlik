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

import org.apache.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tom on 6.7.2016.
 */
public class RestaurantTesar extends CRestaurantBase
{
    //private String m_pdfContent;

    //constructor
    public RestaurantTesar(Activity c){
        m_context = c;
        m_name = "Hostinec U Tesaře";
        m_resid = CRestaurantPool.ID_TESAR;
    }

    private final String [] specialMealNames = {
            "menu týdne", "vegetariánská specialita", "ryba týdne", "salát"
    };

    private final Pattern m_pattern2 = Pattern.compile("^(\\d\\.)(.*)");

    protected List<String> JoinSplitLines(String textStr []){
        List<String> newList = new ArrayList<>();
        for(int i = 0; i < textStr.length; i++){
            Matcher m = m_pattern2.matcher(textStr[i]);
            textStr[i] = textStr[i].trim();
            int joinedLinesCount = 0;

            //join only lines which begin with a number or these keywords
            if( (m.matches() ||
                textStr[i].toLowerCase().startsWith(specialMealNames[0]) ||
                textStr[i].toLowerCase().startsWith(specialMealNames[1]) ||
                textStr[i].toLowerCase().startsWith(specialMealNames[2]) ||
                textStr[i].toLowerCase().startsWith(specialMealNames[3]))
                    && !textStr[i].matches("(.*)(\\d{2,3}|(…,-))(.{0,5})"))
            {
                for(int j = i + 1; j < textStr.length; j++){
                    if(textStr[j].matches("(.*)(\\d{2,3}|(…,-))(.{0,5})")){
                        joinedLinesCount = j - i;
                        break;
                    }
                }
            }

            textStr[i] = textStr[i].replaceAll("\\s+", " ").trim();

            if(joinedLinesCount > 0){
                for(int k = 0; k < joinedLinesCount; k++){
                    textStr[i + k + 1] = textStr[i + k + 1].replaceAll("\\s+", " ").trim();
                    textStr[i] += " " + textStr[i + k + 1].trim();
                }
            }
            newList.add(textStr[i]);
            i += joinedLinesCount;    //skip joined lines
        }
        return newList;
    }

    protected String GetLastSavedMenu(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String content = sharedPref.getString("tesarmenutext", "");
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
                    "http://www.utesare.cz/Menu.pdf",
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
                editor.putString("tesarmenutext", content);
                editor.commit();
            }
            return ParseText(content);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ArrayList<CMeal>();
        }
    }

    /*@Override
    protected void onProgressUpdate(Integer... values){
    }*/

    private final Pattern m_pattern
            = Pattern.compile("^(\\d\\.)(\\s*)(.*)(\\s+)(\\d+)(\\D*)");

    private final Pattern m_specialMealPattern
            = Pattern.compile("^(.*)(\\s+)(\\d+|(…,-))(\\D*)");

    public List<CMeal> ParseText(String text){
        List<CMeal> meals = new ArrayList<>();
        String textStr [] = text.split("\\r\\n|\\n|\\r");
        List<String> lines = JoinSplitLines(textStr);
        int today = Utils.GetDayOfWeek();
        if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
            return meals;
        }

        boolean gotoSpecialMeals = false;

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
                if(gotoSpecialMeals){
                    break;
                }
            }
        }

        //special meals
        boolean [] counter = { true, true, true, true };
        for(int i = 0; i < lines.size(); i++){
            int mealIndex = -1;
            for(int j = 0; j < specialMealNames.length; j++) {
                if(lines.get(i).trim().toLowerCase().startsWith(specialMealNames[j]) && counter[j]){
                    mealIndex = j;
                    break;
                }
            }

            if(mealIndex < 0){
                continue;
            }

            Matcher m = m_specialMealPattern.matcher(lines.get(i));
            if(!m.matches()){
                continue;
            }
            CMeal meal = new CMeal(m.group(1), Utils.MyStringToInt(m.group(3)));
            meals.add(meal);
            counter[mealIndex] = false;
        }
        return meals;
    }
}

