package com.example.dayre.bobprice;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlarmService extends IntentService {
    public int countItemTypeSearch;
    // Must create a default constructor
    public AlarmService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        Log.i("BobPrice/service:","triggered");
        countItemTypeSearch=0;
        doRefresh();
    }

    String site[]={
            "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000010&shops[]=0",
            "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000117&shops[]=0",
            "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000140&shops[]=0"};

    public String doRefresh() {
        String content;
        try{
            content = getContent(site[countItemTypeSearch]);
        }
        catch (IOException ex){
            content = ex.getMessage();
        }
        return content;
    }

    String getContent(String path) throws IOException {
        BobPriceDBAdapter db = new BobPriceDBAdapter(getApplicationContext());
        db.open();
        Log.i("BobPrice/Service",path);
        BufferedReader reader = null;

            URL url = new URL(path);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.connect();
            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            //Sort
            int start = 0;
            int long_name = 0;
            int long_price = 0;
            String product_code = "<div class=\"item-name\"><a href=\"/catalog/markdown/";
            String product_name = "data-product-param=\"name\">";
            String product_price = "data-product-param=\"price\" data-value=\"";
            String tmp;
            String _name;
            int _price = 0;
            String _code = "0";
            String total = "";
            String total2 = "";
            int count_new = 0;
            int count_refresh = 0;
            while (buf.indexOf(product_name, start) > 0) {
                start = buf.indexOf(product_code, start);
                start += product_code.length();
                long_name = buf.indexOf("/", start + 1);
                _code = buf.substring(start, long_name);
                start = buf.indexOf(product_name, start);
                start += product_name.length();
                long_name = buf.indexOf("<", start + 1);
                tmp = buf.substring(start, long_name);
                _name = tmp;
                if (_name.contains("Apple "))//We ignore Apple production.
                    continue;
                tmp += ", цена ";
                start = buf.indexOf(product_price, start);
                start += product_price.length();
                long_price = buf.indexOf("\"", start + 1);
                String a = buf.substring(start, long_price);
                a = a.substring(0, a.length() - 3);
                _price = Integer.parseInt(a);
                tmp += a;
                total2 += tmp + "\n";
                //
                //total+="Обновления цен:"+"\n";
                if (db.searchItemIsExist(_code)) {
                    int price_in_base = db.searchItemByCode(_code).getPrice();
                    if (price_in_base != _price) {
                        //new price
                        db.updateItem(new BobPriceItem(_name, _price, _code));
                        count_refresh++;
                        total += "Новая цена на " + _name + ", цена = " + _price + "\n";
                    }
                } else {
                    //new item
                    db.insertRecord(new BobPriceItem(_name, _price, _code));
                    count_new++;
                    total += "Новый товар " + _name + ", цена = " + _price + "\n";
                }
            }
//
            Log.i("BobPriceMain", "new item = " + String.valueOf(count_new));
            Log.i("BobPriceMain", "refresh item = " + String.valueOf(count_refresh));

            if (count_new!=0){sendNotif("Новые товары!");}
            if (count_refresh!=0){sendNotif("Обновление цен!");}
            db.close();
        countItemTypeSearch++;
        if ( countItemTypeSearch<site.length){
            getContent(site[countItemTypeSearch]);
        }
            return (total);
    }

    NotificationManager nm;

    void sendNotif(String text) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent callIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.bobprice)
                .setWhen(0)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(alarmSound)
                .setContentIntent(callIntent)
                .setOngoing(false)
                .setAutoCancel(false)
                .setContentTitle("BobPrice")
                .setContentText(text)
                .build();
       nm.notify(1, notification);
    }
}
