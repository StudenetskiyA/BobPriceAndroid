package com.example.dayre.bobprice;

        import java.util.ArrayList;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase.CursorFactory;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

public class BobPriceDBAdapter {
    private static final String APPLICATION_TAG = "BobPrice";
    private static final String DATABASE_NAME = "BobPrice.db";
    private static final String DATABASE_PATH = "/mnt/sdcard/BobPrice/";
    private static final String DATABASE_TABLE = "price";
    private static final int DATABASE_VERSION = 8;//Add when database changed
    static final int CODE_COLUMN=0;
    static final int NAME_COLUMN=1;
    static final int PRICE_COLUMN=2;
    private SQLiteDatabase db;
    private final Context context;
    private BobPriceDBOpenHelper dbHelper;


    public static final String KEY_CODE = "_code";
    public static final String KEY_NAME = "_name";
    public static final String KEY_PRICE = "price";
    public static final String ALL_FIELD[] = {KEY_CODE, KEY_NAME, KEY_PRICE};
    public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_CODE + " STRING PRIMARY KEY, " + KEY_NAME + " string, " + KEY_PRICE + " integer);";

    public BobPriceDBAdapter(Context _context) {
        this.context = _context;
        dbHelper = new BobPriceDBOpenHelper(context, DATABASE_PATH+DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void close() {
        db.close();
        dbHelper.close();
    }

    public void open() throws SQLiteException {
        try {db = dbHelper.getWritableDatabase();
        }
        catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
        }
    }

    public long insertRecord(BobPriceItem _day) {
//        Log.i(APPLICATION_TAG, "insertItem: song = " + _day.getName());
//        Log.i(APPLICATION_TAG, "tag to insert = " + _day.getTag());
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_CODE, _day.getCode());
        newValue.put(KEY_NAME, _day.getName());
        newValue.put(KEY_PRICE, _day.getPrice());
        return db.insert(DATABASE_TABLE, null, newValue);
    }

    public boolean removeItem(int _rowIndex) {
        return db.delete(DATABASE_TABLE, KEY_CODE + "=" + _rowIndex, null) > 0;
    }

    public boolean updateItem(BobPriceItem _day) {
        Log.i(APPLICATION_TAG, "updateItem = " + _day.toString());
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_NAME, _day.getName());
        newValue.put(KEY_PRICE, _day.getPrice());
        newValue.put(KEY_CODE, _day.getCode());
        return db.update(DATABASE_TABLE, newValue, KEY_CODE + "='" + _day.getCode() + "'", null) > 0;
    }

    public Cursor getAllItemsCursor() {
        String order= KEY_PRICE;
        return db.query(DATABASE_TABLE, ALL_FIELD, null, null, null, null, order);
    }

    public Cursor setCursorToItem(long _rowIndex) throws SQLException {
        Cursor result = db.query(true, DATABASE_TABLE,ALL_FIELD,KEY_NAME + "=" + _rowIndex, null, null, null,
                null, null, null);
        if ((result.getCount() == 0) || !result.moveToFirst()) {
            throw new SQLException("No to do items found for row: " + _rowIndex);
        }
        return result;
    }

    public ArrayList<BobPriceItem> searchAll() throws SQLException {
        int i;
        String name = "-1";
        int price = -1;
        String code="-1";
        ArrayList<BobPriceItem> result = new ArrayList<BobPriceItem>();
        Cursor cursor = getAllItemsCursor();

        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            Log.i(APPLICATION_TAG,"searchAll: no one item found in DB!");
            return result;
        }

        for (i=0; i<cursor.getCount(); i++){
            name = cursor.getString(NAME_COLUMN);
            price = Integer.parseInt(cursor.getString(PRICE_COLUMN));
            code = cursor.getString(CODE_COLUMN);
            result.add(new BobPriceItem(name, price, code));
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public boolean searchItemIsExist(String _code) throws SQLException {
        Cursor cursor = db.query(true, DATABASE_TABLE,ALL_FIELD,KEY_CODE + "='" + _code+"'", null, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
            //Log.i(APPLICATION_TAG,"DB: no item found for song: " + _name );
            return false;
        }
        //Log.i(APPLICATION_TAG,"DB: something found" );
        cursor.close();
        return true;
    }

    public BobPriceItem searchItemByCode(String _code) throws SQLException {
        String name = "-1";
        int price = -1;
        BobPriceItem result = new BobPriceItem(name, price,"0");
        Cursor cursor = db.query(true, DATABASE_TABLE,ALL_FIELD,KEY_CODE + "='" + _code+"'", null, null, null, null, null);
        if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
  //          Log.i(APPLICATION_TAG,"DB: no item found : " + _code );
            return result;
        }
//        Log.i(APPLICATION_TAG,"DB: something found, load  " + _code );
        name = cursor.getString(NAME_COLUMN);
        String tmp=cursor.getString(PRICE_COLUMN);
        price = Integer.parseInt(cursor.getString(PRICE_COLUMN));
        //Log.i(APPLICATION_TAG,"DB: load  " + _name );
        result.setName(name);
        result.setPrice(price);
        result.setCode(_code);
        cursor.close();
        return result;
    }

    public void clear(){
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        db.execSQL(DATABASE_CREATE);

    }
    private static class BobPriceDBOpenHelper extends SQLiteOpenHelper {

        public BobPriceDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            Log.i(APPLICATION_TAG, "Database created now.");
            _db.execSQL(DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            Log.i(APPLICATION_TAG, "Upgrading from version " +
                    _oldVersion + " to " +
                    _newVersion + ", which will destroy all old data");
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(_db);
        }
    }
}

