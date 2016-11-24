package com.example.dayre.bobprice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static final String site[]={
        "http://www.dns-shop.ru/catalog/markdown/?3[00000000-0000-0000-0000-000000000010]=1&offset=0",
                "http://www.dns-shop.ru/catalog/markdown/?3[00000000-0000-0000-0000-000000000140]=1&offset=0",
                "http://www.dns-shop.ru/catalog/markdown/?3[00000000-0000-0000-0000-000000000117]=1&offset=0"};

    public static final String categoryName[]={"Планшеты","Ноутбуки","Смартфоны"};

    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, AlarmService.class);
        // Add extras to the bundle
       // i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleAlarm();
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000*60*3, pIntent);//every 3 minute
    }
}
