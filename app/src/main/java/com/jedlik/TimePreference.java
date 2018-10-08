package com.jedlik;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;


public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getPersistedLong(GetDefaultNotifTime()));
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    protected long GetDefaultNotifTime(){
        Calendar calendar = Calendar.getInstance();
        final long currentTimMs = System.currentTimeMillis();
        calendar.setTimeInMillis(currentTimMs);
        calendar.set(Calendar.HOUR_OF_DAY, MyBroadcastReceiver.defaultHour);
        calendar.set(Calendar.MINUTE, MyBroadcastReceiver.defaultMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue){
            if(defaultValue == null){
                calendar.setTimeInMillis(getPersistedLong(GetDefaultNotifTime()));
            }
            else{
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        }
        else{
            if(defaultValue == null){
                calendar.setTimeInMillis(GetDefaultNotifTime());
            }
            else{
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if(calendar == null){
            return null;
        }
        CharSequence timeStr = DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
        String summary = getContext().getString(R.string.notif_time_summary);
        return String.format(summary, timeStr);
    }
}