package com.frostnerd.dnsserver.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.frostnerd.dnsserver.R;
import com.frostnerd.dnsserver.server.DNSServer;
import com.frostnerd.dnsserver.server.UDPServer;
import com.frostnerd.dnsserver.util.IPPortPair;

import java.util.ArrayList;
import java.util.List;

import de.measite.minidns.Record;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class MainActivity extends AppCompatActivity {
    private ListView list;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.list);
        final List<String> obj = new ArrayList<>();
        list.setAdapter(adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, obj));
        DNSServer.Configuration configuration = new DNSServer.Configuration.Builder().setLogUpstreamQueryResults(false)
                .setQueryListener(new DNSServer.QueryListener() {
                    @Override
                    public void queryReceived(final String host, final String query, final Record.TYPE type, boolean forwardedToUpstream) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(host + " ---> " + query + "(" + type + ")");
                                System.out.println("Received Query: " + query + " from " + host + " (" + type + ")");
                            }
                        });
                    }
                }).build();
        new Thread(new UDPServer(this, 5301, new IPPortPair("8.8.8.8", 53, false), configuration)).start();
    }
}
