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
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

public class ProgressFragment extends Fragment {

    public int countItemTypeSearch=0;
    String site[]={
        "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000010&shops[]=0",
        "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000117&shops[]=0",
        "http://www.dns-shop.ru/catalog/markdown/?order=1&groups[]=00000000-0000-0000-0000-000000000140&shops[]=0"};

    TextView contentView;
    String contentText = null;

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
//        if(contentText!=null)
//            contentView.setText(contentText);
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
                content = getContent(site[countItemTypeSearch]);
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
//            contentText=content;
            contentView.setText(contentView.getText()+content);
            countItemTypeSearch++;
            if ( countItemTypeSearch<site.length){
                new ProgressTask().execute();
            }
            Toast.makeText(getActivity(), "Данные загружены", Toast.LENGTH_SHORT)
                    .show();
        }

        public BasicCookieStore getCookieStore(String cookies, String domain) {
            String[] cookieValues = cookies.split(";");
            BasicCookieStore cs = new BasicCookieStore();

            BasicClientCookie cookie;
            for (int i = 0; i < cookieValues.length; i++) {
                String[] split = cookieValues[i].split("=");
                if (split.length == 2)
                    cookie = new BasicClientCookie(split[0], split[1]);
                else
                    cookie = new BasicClientCookie(split[0], null);

                cookie.setDomain(domain);
                cs.addCookie(cookie);
            }
            return cs;
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
               //Log.i("BobPrice", line);
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
                    //
                    //total+="Обновления цен:"+"\n";
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
                Log.i("BobPriceMain","new item = "+String.valueOf(count_new));
                Log.i("BobPriceMain","refresh item = "+String.valueOf(count_refresh));

                total+="-------СТАРЫЕ-------"+"\n"+total2;
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