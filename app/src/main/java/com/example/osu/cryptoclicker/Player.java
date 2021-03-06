package com.example.osu.cryptoclicker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aruef on 3/12/2018.
 */

public class Player {

    private final static double BASE_CLICK = .5;

    private Map<String, Double> currency;
    private int upgrade;
    private double mClickAmount;

    private SQLiteDatabase mDB;

    public Player(SQLiteDatabase db){
        currency = new HashMap<String, Double>();
        mDB = db;

        //User will have ID of 1
        Cursor cursor = db.query(ClickerContract.UserData.TABLE_NAME,
                new String[] {ClickerContract.UserData.COLUMN_UPGRADE,
                        ClickerContract.UserData.COLUMN_USD,
                        ClickerContract.UserData.COLUMN_BITCOIN},
                ClickerContract.UserData._ID + "=?",
                new String[] {"1"},
                null, null, null);

        cursor.moveToFirst();
        upgrade = cursor.getInt(0);
        currency.put(CoinBaseUtils.COINBASE_CURRENCY_USD, cursor.getDouble(1));
        currency.put(CoinBaseUtils.COINBASE_CURRENCY_BTC, cursor.getDouble(2));
        mClickAmount = calcClickAmount();
    }

    public double getCurrency(String reqCurrency){
        return currency.containsKey(reqCurrency) ? currency.get(reqCurrency) : 0.;
    }

    public void setCurrency(String reqCurrency, double value){
        currency.put(reqCurrency, value);

        ClickerDBHelper.updateCurrency(mDB, reqCurrency, value);
    }

    public int getUpgrade(){
        return upgrade;
    }

    public void setUpgrade(int upgrade){
        this.upgrade = upgrade;
        ClickerDBHelper.updateUpgrade(mDB, upgrade);
        mClickAmount = calcClickAmount();
    }

    private double calcClickAmount(){
        return BASE_CLICK*(1 + upgrade*(.5+.25*upgrade));
    }

    public double getClickAmount(){
        return mClickAmount;
    }

    public void click(){
        setCurrency(CoinBaseUtils.COINBASE_CURRENCY_USD,
                getCurrency(CoinBaseUtils.COINBASE_CURRENCY_USD) + mClickAmount);
    }

    public void updateFromDB()    {
        Cursor cursor = mDB.query(
                ClickerContract.UserData.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            currency.put(CoinBaseUtils.COINBASE_CURRENCY_USD, cursor.getDouble(cursor.getColumnIndex(ClickerContract.UserData.COLUMN_USD)));
            currency.put(CoinBaseUtils.COINBASE_CURRENCY_BTC, cursor.getDouble(cursor.getColumnIndex(ClickerContract.UserData.COLUMN_BITCOIN)));
            upgrade = cursor.getInt(cursor.getColumnIndex(ClickerContract.UserData.COLUMN_UPGRADE));
        }
        cursor.close();

        mClickAmount = calcClickAmount();
    }

    public boolean purchaseUpgrade(Upgrade upgrade){
         boolean success = false;

         if(upgrade.getCost() <= currency.get(CoinBaseUtils.COINBASE_CURRENCY_USD) && upgrade.getCount() == this.upgrade+1){
             setCurrency(CoinBaseUtils.COINBASE_CURRENCY_USD, getCurrency(CoinBaseUtils.COINBASE_CURRENCY_USD) - upgrade.getCost());
             setUpgrade(this.upgrade + 1);

             success = true;
         }

         return success;
    }
}
