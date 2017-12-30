package com.frostnerd.dnsserver.server;

import android.content.Context;

import com.frostnerd.dnsserver.database.ServerDatabaseHelper;
import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.util.DNSResolver;
import com.frostnerd.dnsserver.util.QueryLogger;
import com.frostnerd.dnsserver.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public abstract class DNSServer implements Runnable {
    DNSResolver resolver;
    DNSServerSetting settings;
    InetAddressWithPort primaryAddress, secondaryAddress;
    QueryLogger queryLogger;
    QueryListener queryListener;
    ServerDatabaseHelper serverDatabase;

    DNSServer(Context context, DNSServerSetting settings) {
        this.resolver = new DNSResolver(context, serverDatabase = new ServerDatabaseHelper(context, settings));
        this.settings = settings;
        try {
            this.primaryAddress = new InetAddressWithPort(InetAddress.getByName(settings.getUpstreamPrimary().getAddress()), settings.getUpstreamPrimary().getPort());
            this.secondaryAddress = new InetAddressWithPort(InetAddress.getByName(settings.getUpstreamSecondary().getAddress()), settings.getUpstreamSecondary().getPort());
        } catch (UnknownHostException ignored) {}
        if(settings.isQueryLoggingEnabled() || settings.isQueryResponseLoggingEnabled())
            queryLogger = new QueryLogger(context, serverDatabase);

            if(settings.isQueryLoggingEnabled() && settings.getQueryListener() != null){
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        queryLogger.logQuery(host,query, type, forwardedToUpstream);
                        DNSServer.this.settings.getQueryListener().queryReceived(host, query, type, forwardedToUpstream);
                    }
                };
            }else if(settings.getQueryListener() != null){
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        DNSServer.this.settings.getQueryListener().queryReceived(host, query, type, forwardedToUpstream);
                    }
                };
            }else if(settings.isQueryLoggingEnabled()){
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        queryLogger.logQuery(host, query, type, forwardedToUpstream);
                    }
                };
            }else{
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        // -Dust-
                    }
                };
            }
    }

    protected abstract void stopServer();

    public final void stop() {
        stopServer();
        resolver.cleanup();
        if(queryLogger != null)queryLogger.cleanup();
        serverDatabase.close();
        serverDatabase = null;
        resolver = null;
        queryLogger = null;
        primaryAddress = null;
        secondaryAddress = null;
        settings = null;
    }

    public interface ErrorListener{
        public void onError(Exception e);
    }

    public interface QueryListener{
        public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream);
    }

    protected final class InetAddressWithPort{
        InetAddress address;
        int port;

        public InetAddressWithPort(InetAddress address, int port){
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }
}
