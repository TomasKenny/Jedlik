package com.jedlik.restaurants;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.jedlik.CMeal;
import com.jedlik.IRestaurant;
import com.jedlik.MainActivity;
import com.jedlik.Utils;

import java.util.List;

/**
 * Created by Tom on 25.7.2016.
 */
public abstract class CRestaurantBase
    extends AsyncTask<Void, Integer, List<CMeal>>
    implements IRestaurant
{
    protected Activity m_context;
    public String m_name;
    public int m_resid;

    @Override
    public String GetName() {
        return m_name;
    }

    @Override
    public int GetResId() {
        return m_resid;
    }

    @Override
    public void LoadData(){
        Utils.StartMyTask(this);
    }

    @Override
    protected void onPostExecute(List<CMeal> result){
        MainActivity mainActivity = (MainActivity)m_context;
        mainActivity.FillMeals(result, GetResId());
    }
}
