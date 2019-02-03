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
 * Created by Erik on 13.1.2019.
 */
public class RestaurantEatology extends CRestaurantBase
{
    //private String m_pdfContent;

    //constructor
    public RestaurantEatology(Activity c){
        m_context = c;
        m_name = "Eatology";
        m_resid = CRestaurantPool.ID_EATOLOGY;
    }

    private final String dailyMenuKW = "polední nabídka";
    private final String weeklyMenuKW = "týdenní nabídka";
    private final String dailyMenuEnglishKW = "lunch menu";
    private final String weeklyMenuEnglishKW = "weekly offer";

    private final String [] dailyMenuSectionNames = {
            "polévky", "těstoviny", "pizza", "hlavní jídla", "eatology special"
    };
    private final String [] weeklyMenuSectionNames = {
            "wok", "hamburger", "gril"
    };

    // examples: 150 g řízek - 0,33 l vývar - ¼ kuře
    private final Pattern m_pattern2 = Pattern.compile("^((\\d+\\s+g)|(\\d+,\\d+\\s+l)|([\\xBC-\\xBE]))\\s(.*)");

    protected List<String> JoinSplitLines(String textStr []){
        List<String> newList = new ArrayList<>();
        for(int i = 0; i < textStr.length; i++){
            Matcher m = m_pattern2.matcher(textStr[i]);
            textStr[i] = textStr[i].trim();
            int joinedLinesCount = 0;

            //join only lines which begin with a number or these keywords
            if( (m.matches())
                    && !textStr[i].matches("(.*\\D)(\\d+)\\s+Kč\\s*"))
            {
                for(int j = i + 1; j < textStr.length; j++){
                    if (textStr[j].startsWith(dailyMenuKW) || textStr[j].startsWith(weeklyMenuKW)
                            || textStr[j].startsWith(dailyMenuEnglishKW) || textStr[j].startsWith(weeklyMenuEnglishKW))
                        break;
                    if(textStr[j].matches("(.*\\D)(\\d+)\\s+Kč\\s*")){
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
        String content = sharedPref.getString("eatologymenutext", "");
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
                    "http://www.iqrestaurant.cz/brno/menu.pdf",
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
                editor.putString("eatologymenutext", content);
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
            = Pattern.compile("^((\\d+\\s+g)|(\\d+,\\d+\\s+l)|([\\xBC-\\xBE]))\\s(.*\\D)(\\d+)\\sKč");

    public List<CMeal> ParseText(String text){
        List<CMeal> meals = new ArrayList<>();
        String textStr [] = text.split("\\r\\n|\\n|\\r");
        List<String> lines = JoinSplitLines(textStr);
        int today = Utils.GetDayOfWeek();
        if((today == Calendar.SATURDAY) || (today == Calendar.SUNDAY)){
            return meals;
        }

        String dailyMenuSectionName = dailyMenuKW + " " + Utils.daysInWeek[today];
        for(int i = 0; i < lines.size(); i++){
            if(lines.get(i).toLowerCase().startsWith(dailyMenuSectionName)){
                i++;
                boolean parsingWeeklyMenu = false;
                boolean parsingPizzaSection = false;
                while(i < lines.size()){
                    Matcher m = m_pattern.matcher(lines.get(i));
                    if(!m.matches()) {
                        if (parsingWeeklyMenu) {
                            if (lines.get(i).toLowerCase().startsWith(dailyMenuEnglishKW)) // end of day section reached
                                break;
                        }
                        parsingPizzaSection = (lines.get(i).toLowerCase().startsWith("pizza"));
                        if (lines.get(i).toLowerCase().startsWith(weeklyMenuKW))
                            parsingWeeklyMenu = true;
                        i++;
                        continue;
                    }
                    String mealName = m.group(5);
                    if (parsingPizzaSection && !mealName.toLowerCase().startsWith("pizza"))
                        mealName = "PIZZA " + mealName;
                    CMeal meal = new CMeal(m.group(1) + " " + mealName, Integer.parseInt(m.group(6)));
                    meals.add(meal);
                    i++;
                }
            }
        }

        return meals;
    }
}
