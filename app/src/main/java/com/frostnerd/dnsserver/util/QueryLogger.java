package com.frostnerd.dnsserver.util;

import android.content.Context;

import com.frostnerd.dnsserver.database.DatabaseHelper;
import com.frostnerd.dnsserver.database.ServerDatabaseHelper;

import de.measite.minidns.Record;

/**
 * Created by Daniel on 26.10.2017.
 */

public class QueryLogger {
    private ServerDatabaseHelper serverDatabase;

    public QueryLogger(Context context, ServerDatabaseHelper serverDatabase){
        this.serverDatabase = serverDatabase;
    }

    public void logQuery(String host, String query, Record.TYPE type, boolean wasForwarded){

    }

    public void logUpstreamQueryResult(String query, String result, Record.TYPE type){

    }

    public void cleanup(){
        serverDatabase = null;
    }
}
