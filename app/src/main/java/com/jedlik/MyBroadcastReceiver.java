package com.jedlik;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {

    static private final String actionPopup = "popup";
    static public final int defaultHour = 10;
    static public final int defaultMinute = 0;

    boolean IsNotificationTurnedOn(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getResources().getString(R.string.show_notif_pref);
        return sharedPref.getBoolean(prefKey, true);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if(action == null) {
            return;
        }

        if(action.equals("android.intent.action.BOOT_COMPLETED")){
            TurnOnNotifications(context);
        }
        else if(action.equals(actionPopup)){
            switch(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
                case Calendar.SATURDAY:
                case Calendar.SUNDAY:
                    return;
                default: {
                    if(IsNotificationTurnedOn(context)) {
                        ShowNotification(context);
                    }
                }
            }

        }
    }

    static public void TurnOnNotifications(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String notifTimePrefKey = context.getResources().getString(R.string.notif_time_pref);
        int hour = defaultHour;
        int minute = defaultMinute;

        if(notifTimePrefKey != null) {
            long notifTimeMs = sharedPref.getLong(notifTimePrefKey, 0);
            if(notifTimeMs != 0) {
                Calendar setTime = Calendar.getInstance();
                setTime.setTimeInMillis(notifTimeMs);
                hour = setTime.get(Calendar.HOUR_OF_DAY);
                minute = setTime.get(Calendar.MINUTE);
            }
        }

        Calendar calendar = Calendar.getInstance();
        final long currentTimMs = System.currentTimeMillis();
        calendar.setTimeInMillis(currentTimMs);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, MyBroadcastReceiver.class);
        newIntent.setAction(actionPopup);
        alarmIntent = PendingIntent.getBroadcast(context, 0, newIntent, 0);

        long timeShift = (calendar.getTimeInMillis() <= currentTimMs) ? AlarmManager.INTERVAL_DAY : 0;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + timeShift,
                /*30 * 1000*/AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void ShowNotification(Context context){
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle(context.getResources().getString(R.string.notif_title))
                        .setContentText(context.getResources().getString(R.string.notif_text))
                        .setSmallIcon(R.drawable.notificon)
                        .setSound(soundUri)
                        .setDefaults(
                            Notification.DEFAULT_VIBRATE |
                            Notification.DEFAULT_SOUND |
                            Notification.FLAG_SHOW_LIGHTS)
                        .setAutoCancel(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }
}

