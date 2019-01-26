package com.jedlik;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jedlik.restaurants.CRestaurantPool;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 27.8.2016.
 */
public class SortingPreference extends DialogPreference {
    private DragListView m_sortView;
    private Context m_context;
    private ArrayList<Utils.RestaurantOrderItem> mItemArray;


    public SortingPreference(Context ctxt) {
        this(ctxt, null);
        m_context = ctxt;
    }

    public SortingPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
        m_context = ctxt;
    }

    public SortingPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
        m_context = ctxt;
        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            //dragView.setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }
    }

    public List<Utils.RestaurantOrderItem> GetRestaurantsOrder(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String restOrderKey = m_context.getResources().getString(R.string.choose_restaurants_pref);
        String restOrderStr = sharedPref.getString(restOrderKey, "");

        do{
            if(restOrderStr == ""){
                break;
            }
            List<Utils.RestaurantOrderItem> restOrder = Utils.StringToRestOrder(restOrderStr);
            if(restOrder == null || restOrder.size() == 0){
                break;
            }

            //get actual list of available restaurants to compare
            List<IRestaurant> restList = CRestaurantPool.GetRestaurants((Activity)m_context);

            //go through saved order list, remove obsolete restaurants, change name of renamed restaurants
            for (int i=0; i<restOrder.size(); i++) {
                boolean found = false;
                for (int j=0; j<restList.size(); j++) {
                    if (restOrder.get(i).m_id == restList.get(j).GetResId()) {
                        found = true;
                        restOrder.get(i).m_name = restList.get(j).GetName(); // update name
                        restList.remove(j);
                        break;
                    }
                }
                if (!found) {   // remove unavailable restaurant
                    restOrder.remove(i);
                    i--;
                }
            }
            //add restaurants which are new
            for (int k=0; k<restList.size(); k++) {
                restOrder.add(new Utils.RestaurantOrderItem(
                                restList.get(k).GetResId(),
                                restList.get(k).GetName(),
                                false
                        )
                );
            }

            return restOrder;
        }while(false);

        //if something goes wrong, get all restaurants
        List<IRestaurant> restList = CRestaurantPool.GetRestaurants((Activity)m_context);
        List<Utils.RestaurantOrderItem> allRests = new ArrayList<>();
        for(int i = 0; i < restList.size(); i++){
            allRests.add(new Utils.RestaurantOrderItem(
                    restList.get(i).GetResId(),
                    restList.get(i).GetName(),
                    true
            ));
        }
        return allRests;
    }

    @Override
    protected View onCreateDialogView() {
        Activity activity = (Activity)getContext();
        View view = activity.getLayoutInflater().inflate(R.layout.draglistview, null);
        m_sortView = (DragListView)view.findViewById(R.id.draglistview);
        m_sortView.getRecyclerView().setVerticalScrollBarEnabled(true);

        m_sortView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                }

            }
        });

        mItemArray = (ArrayList<Utils.RestaurantOrderItem>)GetRestaurantsOrder();
        m_sortView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.image, false);
        m_sortView.setAdapter(listAdapter, true);
        m_sortView.setCanDragHorizontally(false);
        m_sortView.setCustomDragItem(new MyDragItem(getContext(), R.layout.list_item));

        return view;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        //picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        //picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if(positiveResult){
            persistString(Utils.RestOrderToString(mItemArray));
            String message = m_context.getResources().getString(R.string.next_start);
            Toast toast = Toast.makeText(m_context, message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
