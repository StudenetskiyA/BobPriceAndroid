package com.example.dayre.bobprice;

public class BobPriceItem {

    private String itemName;
    private int itemPrice;
    private String itemCode;

    public String getName(){
        return itemName;
    }

    public int getPrice(){
        return itemPrice;
    }

    public String getCode(){
        return itemCode;
    }

    public void setName(String _name){
        itemName=_name;
    }
    public void setPrice(int _price){
        itemPrice=_price;
    }
    public void setCode(String _code){
        itemCode=_code;
    }


    BobPriceItem(String _name, int _price, String _code){
        itemName=_name;
        itemPrice=_price;
        itemCode=_code;
    }

}
