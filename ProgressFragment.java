package com.example.dayre.bobprice;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

public class ProgressFragment extends Fragment {

    public int countItemTypeSearch=0;

    TextView contentView;
    //String contentText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        contentView = (TextView) view.findViewById(R.id.content);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new ProgressTask().execute();
    }

    class ProgressTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... path) {
            String content;
            try{
                content = getContent(MainActivity.site[countItemTypeSearch]);
            }
            catch (IOException ex){
               content = ex.getMessage();
            }
            return content;
        }
        @Override
        protected void onProgressUpdate(Void... items) {
        }
        @Override
        protected void onPostExecute(String content) {
            contentView.setText(contentView.getText()+content);
            Toast.makeText(getActivity(), "Данные <" +MainActivity.categoryName[countItemTypeSearch]+"> загружены", Toast.LENGTH_SHORT)
                    .show();
            countItemTypeSearch++;
            if ( countItemTypeSearch<MainActivity.site.length){
                new ProgressTask().execute();
            }

        }

        private String getContent(String path) throws IOException {
            BobPriceDBAdapter db = new BobPriceDBAdapter(getActivity().getApplicationContext());
            db.open();

            BufferedReader reader=null;
            try {
                URL url=new URL(path);
                HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(10000);
                c.connect();
                reader= new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder buf=new StringBuilder();
                String line=null;
                while ((line=reader.readLine()) != null) {
                    buf.append(line + "\n");
                }
                //Sort
                int start=0;
                int long_name=0;
                int long_price=0;
                String product_code="<div class=\"item-name\"><a href=\"/catalog/markdown/";
                String product_name="data-product-param=\"name\">";
                String product_price="data-product-param=\"price\" data-value=\"";
                String tmp;
                String _name;
                int _price=0;
                String _code="0";
                String total="";
                String total2="";
                int count_new=0;
                int count_refresh=0;
                while (buf.indexOf(product_name,start)>0) {
                    start=buf.indexOf(product_code, start);
                    start+=product_code.length();
                    long_name=buf.indexOf("/",start+1);
                    _code=buf.substring(start,long_name);
                    start=buf.indexOf(product_name, start);
                    start+=product_name.length();
                    long_name=buf.indexOf("<",start+1);
                    tmp=buf.substring(start,long_name);
                    _name=tmp;
                    if (_name.contains("Apple "))//We ignore Apple production.
                        continue;
                    tmp+=", цена ";
                    start=buf.indexOf(product_price, start);
                    start+=product_price.length();
                    long_price=buf.indexOf("\"",start+1);
                    String a=buf.substring(start,long_price);
                    a=a.substring(0,a.length()-3);
                    _price=Integer.parseInt(a);
                    tmp+=a;
                    total2+=tmp+"\n";
                    if (db.searchItemIsExist(_code))
                    {
                        int price_in_base=db.searchItemByCode(_code).getPrice();
                        if (price_in_base!=_price)
                        {
                            //new price
                            db.updateItem(new BobPriceItem(_name,_price,_code));
                            count_refresh++;
                            total+="Новая цена на "+_name+", цена = "+_price+"\n";
                        }
                    }
                    else
                    {
                        //new item
                        db.insertRecord(new BobPriceItem(_name,_price,_code));
                        count_new++;
                        total+="Новый товар "+_name+", цена = "+_price+"\n";
                    }
                }
//
                String totalUp="";

                totalUp+="======"+MainActivity.categoryName[countItemTypeSearch]+"======\n";
                totalUp+="Новых товаров - "+String.valueOf(count_new)+"\n";
                totalUp+="Обновлено цен - "+String.valueOf(count_refresh)+"\n";

                total=totalUp+total+total2;
                Log.i("BobPriceMain","new item = "+String.valueOf(count_new));
                Log.i("BobPriceMain","refresh item = "+String.valueOf(count_refresh));

               // total+="-------СТАРЫЕ-------"+"\n"+total2;
                db.close();
                return(total);
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }
}