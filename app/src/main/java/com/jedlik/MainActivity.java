package com.jedlik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jedlik.restaurants.CRestaurantPool;
import com.jedlik.restaurants.RestaurantKometa;
import com.jedlik.restaurants.RestaurantMyFood;
import com.jedlik.restaurants.RestaurantSuzies;
import com.jedlik.restaurants.RestaurantTesar;
import com.jedlik.restaurants.RestaurantTusto;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int REST_NAME_FONT_SIZE = 30;
    private final int MEAL_DESC_FONT_SIZE = 20;
    int m_displayWidth;
    int m_tableSideMargin;
    //List<IRestaurant> m_restaurants;

    SharedPreferences.OnSharedPreferenceChangeListener m_myPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    final String favoritePrefKey = getResources().getString(R.string.favorites_pref);
                    if(key.equals(favoritePrefKey)){
                        UpdateFavoriteMeals();
                    }
                }
            };

    /*
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(m_myPrefListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).
                unregisterOnSharedPreferenceChangeListener(m_myPrefListener);
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(m_myPrefListener);



        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        m_displayWidth = metrics.widthPixels;
        m_tableSideMargin = (int)((double)m_displayWidth * 0.075239);

        LinearLayout restList = (LinearLayout)findViewById(R.id.RestList);
        if(restList == null){
            return;
        }

        List<IRestaurant> restaurants = ChooseRestaurants();

        for(int i = 0; i < restaurants.size(); i++){

            TableRow firstRow = new TableRow(this);
            String name = restaurants.get(i).GetName();
            TextView nameView = new TextView(this);
            Typeface textFont = CustomFontsLoader.getTypeface(this, CustomFontsLoader.SCRIPT_FONT);
            nameView.setTypeface(textFont);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, REST_NAME_FONT_SIZE);
            nameView.setText(name);

            //nameView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
            firstRow.addView(nameView);

            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setIndeterminate(true);
            progressBar.animate();
            TableRow secondRow = new TableRow(this);
            secondRow.addView(progressBar);

            TableLayout table = new TableLayout(this);
            table.setShrinkAllColumns(true);
            table.addView(firstRow);
            table.addView(secondRow);
            table.setId(restaurants.get(i).GetResId());

            restList.addView(table);

            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)table.getLayoutParams();
            params2.setMargins(m_tableSideMargin, 0, m_tableSideMargin, 20);
            table.setLayoutParams(params2);

            restaurants.get(i).LoadData();
        }


        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        receiver.TurnOnNotifications(this);
    }

    public List<IRestaurant> ChooseRestaurants(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String restOrderKey = getResources().getString(R.string.choose_restaurants_pref);
        String restOrderStr = sharedPref.getString(restOrderKey, "");

        do{
            if(restOrderStr == ""){
                break;
            }
            List<Utils.RestaurantOrderItem> restOrder = Utils.StringToRestOrder(restOrderStr);
            if(restOrder == null || restOrder.size() == 0){
                break;
            }
            List<IRestaurant> chosenRests = new ArrayList<>();
            List<IRestaurant> restsAvailable = CRestaurantPool.GetRestaurants(this);
            for(int i = 0; i < restOrder.size(); i++){
                if(!restOrder.get(i).m_isActive){
                    continue;
                }
                for(int j = 0; j < restsAvailable.size(); j++){
                    if(restOrder.get(i).m_id == restsAvailable.get(j).GetResId()){
                        chosenRests.add(restsAvailable.get(j));
                        break;
                    }
                }

            }
            return chosenRests;

        }while(false);

        return CRestaurantPool.GetRestaurants(this);
    }

    public String [] GetFavoriteMeals(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String prefKey = getResources().getString(R.string.favorites_pref);
        String favoriteMealsStr = sharedPref.getString(prefKey, "");
        String [] favoriteMeals = favoriteMealsStr.split(",");

        if(favoriteMeals == null || favoriteMeals.length == 0){
            return favoriteMeals;
        }

        for(int i = 0; i < favoriteMeals.length; i++){
            favoriteMeals[i] = Utils.StripAccents(favoriteMeals[i].trim().toLowerCase());
        }
        return favoriteMeals;
    }


    public void UpdateFavoriteMeals(){
        String [] favoriteMeals = GetFavoriteMeals();
        if(favoriteMeals == null || favoriteMeals.length == 0) {
            return;
        }

        TextView t = new TextView(this);
        ColorStateList oldColors = t.getTextColors();

        int highlightTextColor = getResources().getColor(R.color.colorHighlightMeal);

        LinearLayout layout = (LinearLayout)findViewById(R.id.RestList);
        int children = layout.getChildCount();
        for(int i = 0; i < children; i++){
            View v = layout.getChildAt(i);
            if(!(v instanceof TableLayout)){
                continue;
            }

            TableLayout table = (TableLayout)v;
            int rows = table.getChildCount();
            for(int j = 0; j < rows; j++){
                v = table.getChildAt(j);
                if(!(v instanceof TableRow)){
                    continue;
                }
                TableRow row = (TableRow)v;
                v = row.getChildAt(0);
                int cols = row.getChildCount();
                if(cols != 2){  //meal name + price
                    continue;
                }
                String mealName = ((TextView)row.getChildAt(0)).getText().toString();
                mealName = Utils.StripAccents(mealName.toLowerCase());
                boolean found = false;
                for(int k = 0; k < favoriteMeals.length; k++){
                    if(favoriteMeals[k].isEmpty()){
                        continue;
                    }
                    if(mealName.contains(favoriteMeals[k])){
                        found = true;
                        break;
                    }
                }
                TextView name = (TextView)row.getChildAt(0);
                TextView price = (TextView)row.getChildAt(1);

                if(found){
                    name.setTextColor(highlightTextColor);
                    price.setTextColor(highlightTextColor);
                }
                else{
                    name.setTextColor(oldColors);
                    price.setTextColor(oldColors);
                }

            }
        }
    }

    public void FillMeals(List<CMeal> meals, int tableResId){
        if(meals == null){
            return;
        }

        TableLayout table = (TableLayout)findViewById(tableResId);
        if(table == null){
            return;
        }
        if(meals.size() == 0){
            String noMealStr = getResources().getString(R.string.no_meals_today);
            CMeal noMeal = new CMeal(noMealStr, 0);
            meals.add(noMeal);
        }
        //delete row with a progress bar
        table.removeViewAt(1);

        Typeface textFont = CustomFontsLoader.getTypeface(this, CustomFontsLoader.SCRIPT_FONT);
        Drawable shape = getResources().getDrawable(R.drawable.tablerowshape);

        String [] favoriteMeals = GetFavoriteMeals();

        for(int i = 0; i < meals.size(); i++) {
            if(i != 0){
                // add separator dotted line
                TableRow separatorRow = new TableRow(this);
                LayoutInflater inflater = LayoutInflater.from(this);
                View dottedLine = inflater.inflate(R.layout.dottedline, null);
                separatorRow.addView(dottedLine, new TableRow.LayoutParams(0, Utils.dpToPx(this, 4), 1.0f));
                //separatorRow.setBackgroundColor(getResources().getColor(R.color.colorrow1));
                table.addView(separatorRow);
            }

            TableRow row = new TableRow(this);
            //row.setBackgroundColor(getResources().getColor(R.color.colorrow2));

            TextView textMeal = new TextView(this);
            textMeal.setTypeface(textFont);
            textMeal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, MEAL_DESC_FONT_SIZE);
            textMeal.setText(meals.get(i).m_mealName);
            //textMeal.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            row.addView(textMeal, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.9f));

            TextView textCost = new TextView(this);
            if(meals.get(i).m_cost != 0) {
                textCost.setTypeface(textFont);
                textCost.setTextSize(TypedValue.COMPLEX_UNIT_DIP, MEAL_DESC_FONT_SIZE);
                textCost.setText(Integer.toString(meals.get(i).m_cost));
            }
            row.addView(textCost, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
            textCost.setGravity(Gravity.RIGHT);
            table.addView(row);


            //favorite meals
            if(favoriteMeals == null || favoriteMeals.length == 0){
                continue;
            }
            boolean found = false;
            for(int j = 0; j < favoriteMeals.length; j++){
                if(favoriteMeals[j].isEmpty()){
                    continue;
                }
                String mealName = Utils.StripAccents(meals.get(i).m_mealName.toLowerCase());
                if(mealName.contains(favoriteMeals[j])){
                    found = true;
                    break;
                }
            }
            if(found){
                int highlightColor = getResources().getColor(R.color.colorHighlightMeal);
                textMeal.setTextColor(highlightColor);
                textCost.setTextColor(highlightColor);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        return true;
    }


}
