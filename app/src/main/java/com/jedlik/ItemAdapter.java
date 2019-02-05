package com.jedlik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<Utils.RestaurantOrderItem, ItemAdapter.ViewHolder>
{

    private int mLayoutId;
    private int mGrabHandleId;

    public ItemAdapter(ArrayList<Utils.RestaurantOrderItem> list, int layoutId, int grabHandleId) {
        super();
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public long getUniqueItemId(int position){
        return mItemList.get(position).m_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).m_name;
        holder.mText.setText(text);
        holder.mCheckBox.setTag(new Integer(mItemList.get(position).m_id));
        holder.mCheckBox.setChecked(mItemList.get(position).m_isActive);
    }



    public class ViewHolder extends DragItemAdapter.ViewHolder
            implements CompoundButton.OnCheckedChangeListener
    {

        public TextView mText;
        public CheckBox mCheckBox;
        /*CompoundButton.OnCheckedChangeListener m_checkboxListener =
            new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    if(isChecked){
                        // perform logic
                    }

                }
            };*/

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, false);
            mText = (TextView) itemView.findViewById(R.id.text);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.restcheckbox);
            mCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onItemClicked(View view) {
            //Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            //Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton button, boolean isChecked){
            Integer id = (Integer)button.getTag();
            if(id == null){
                return;
            }
            for(int i = 0; i < mItemList.size(); i++){
                if(mItemList.get(i).m_id == id){
                    mItemList.get(i).m_isActive = isChecked;
                    return;
                }
            }
        }
    }
}