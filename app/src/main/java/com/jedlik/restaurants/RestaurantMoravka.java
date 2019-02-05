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
 * Created by Erik on 20.1.2019.
 */
public class RestaurantMoravka extends CRestaurantBase
{
    //private String m_pdfContent;

    //constructor
    public RestaurantMoravka(Activity c){
        m_context = c;
        m_name = "IQ Morávka";
        m_resid = CRestaurantPool.ID_MORAVKA;
    }

    private final String soupsKW = "polévky";
    private final String mainDishesKW = "hlavní jídla";
    private final String italianCuisineKW = "italská kuchyně";

    protected List<String> TrimLines(String textStr []){
        List<String> newList = new ArrayList<>();
        for(int i = 0; i < textStr.length; i++){
            String newLine = textStr[i].trim();
            if (!newLine.isEmpty())
                newList.add(newLine);
        }
        return newList;
    }

    protected String GetLastSavedMenu(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String content = sharedPref.getString("moravkamenutext", "");
        if(content.isEmpty()){
            return content;
        }
        int today = Utils.GetDayOfWeek();
        Calendar calendar = Calendar.getInstance();
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

            String todayFile = "0" + (today-1) + " " + Utils.daysInWeek[today].toUpperCase() + ".pdf";
            String todayUrl = "http://www.iqrestaurant.cz/moravka/" + todayFile;

            FileDownloader.DownloadFromUrl(
                    todayUrl,
                    m_context.getFilesDir().toString(),
                    todayFile
            );

            File f = new File(m_context.getFilesDir(), todayFile);
            PDFBoxResourceLoader.init(m_context);
            content = CMyPdfReader.Read(f);

            //save the text for next time
            if(!content.isEmpty()) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("moravkamenutext", content);
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

    //HOVĚZÍ S JÁTROVOU RÝŽÍ 1, 3,9 30,-
    private final Pattern m_soupLinePattern
            = Pattern.compile("^(.*\\D)((\\d+),-)");
    private final Pattern m_soupNamePattern
            = Pattern.compile("^(.*[^\\d\\s,])\\s*(\\d+[,\\s?\\d+]*)");

    // 300g ČOČKA NA KYSELO,SÁZENÉ VEJCE(2KS)/DEBRECÍNSKÉ PÁREČKY,91,-
    //    CHLÉB,OKUREK 1,3,7
    private final Pattern m_dishFirstLinePattern
            = Pattern.compile("^(\\d+\\s*g)\\s+(.*\\D)((\\d+),-)");
    private final Pattern m_dishLastLinePattern
            = Pattern.compile("^(.*[^\\d\\s,])?\\s*(\\d+[,\\s?\\d+]*)");


    public List<CMeal> ParseText(String text){
        List<CMeal> meals = new ArrayList<>();
        String textStr [] = text.split("\\r\\n|\\n|\\r");
        List<String> lines = TrimLines(textStr);

        boolean soupsParsed = false;
        for(int i = 0; i<lines.size(); i++){
            if (!soupsParsed) {
                if (lines.get(i).toLowerCase().startsWith(soupsKW)) {
                    // parse soups
                    while (i+1 < lines.size()) {
                        i++;
                        Matcher m = m_soupLinePattern.matcher(lines.get(i));
                        if (!m.matches()) {
                            soupsParsed = true;
                            break;
                        }
                        String mealName = m.group(1).trim();
                        String priceNumber = m.group(3);
                        m = m_soupNamePattern.matcher(mealName);
                        if (m.matches())
                            mealName = m.group(1);
                        CMeal meal = new CMeal(mealName, Integer.parseInt(priceNumber));
                        meals.add(meal);
                    }
                }
                continue;
            }
//            if (lines.get(i).toLowerCase().startsWith(mainDishesKW) ||
//                    lines.get(i).toLowerCase().startsWith(italianCuisineKW))
//                continue;

            // parse main dishes
            Matcher m1 = m_dishFirstLinePattern.matcher(lines.get(i));
            if (!m1.matches())
                continue;
            String mealName = m1.group(1) + " " + m1.group(2).trim();
            String priceNumber = m1.group(4);
            m1 = m_dishLastLinePattern.matcher(mealName);
            boolean allergensFound = m1.matches();
            if (allergensFound)
                mealName = m1.group(1);
            while (!allergensFound && i+1<lines.size()) {
                String otherLine = lines.get(i+1);
                Matcher m2 = m_dishLastLinePattern.matcher(otherLine);
                if (!m2.matches()) {
                    m1 = m_dishFirstLinePattern.matcher(otherLine); // next dish already?
                    if (m1.matches())   // sometimes they seem to forget specify allergens at the end
                        break;
                    mealName = mealName + " " + otherLine;
                }
                else {
                    if (m2.group(1)!=null && !m2.group(1).isEmpty())
                        mealName = mealName + " " + m2.group(1).trim();
                    allergensFound = true;
                }
                i++;
            }
            CMeal meal = new CMeal(mealName.replace(",", ", ").replace("  ", " "), Integer.parseInt(priceNumber));
            meals.add(meal);
        }

        return meals;
    }
}
