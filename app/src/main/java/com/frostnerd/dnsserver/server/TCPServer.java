package com.frostnerd.dnsserver.server;

import android.content.Context;

import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;

/**
 * Created by Daniel on 25.10.2017.
 */

public class TCPServer extends DNSServer{

    public TCPServer(Context context, DNSServerSetting settings) {
        super(context, settings);
    }

    @Override
    protected void stopServer() {

    }

    @Override
    public void run() {

    }
}
