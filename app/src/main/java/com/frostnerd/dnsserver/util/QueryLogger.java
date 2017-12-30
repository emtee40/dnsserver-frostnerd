package com.frostnerd.dnsserver.util;

import android.content.Context;

import com.frostnerd.dnsserver.database.DatabaseHelper;

import de.measite.minidns.Record;

/**
 * Created by Daniel on 26.10.2017.
 */

public class QueryLogger {
    private DatabaseHelper databaseHelper;

    public QueryLogger(Context context){
        this.databaseHelper = Util.getDatabaseHelper(context);
    }

    public void logQuery(String host, String query, Record.TYPE type, boolean wasForwarded){

    }

    public void logUpstreamQueryResult(String query, String result, Record.TYPE type){

    }

    public void cleanup(){
        databaseHelper.close();
        databaseHelper = null;
    }
}
