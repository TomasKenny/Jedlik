package com.quoteplayer;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


public class MyListPreference extends ListPreference{

	public MyListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyListPreference(Context context) {
		super(context);
	}
	
	@Override
    protected void onBindView(View view)
    {       
        super.onBindView(view);

        TextView summary = (TextView)view.findViewById(android.R.id.summary);
        if(summary != null){
        	summary.setMaxLines(5);
        	summary.setSingleLine(false);
        }
    }    
	

}
