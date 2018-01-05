package com.frostnerd.dnsserver.database.entities.main;


import android.content.Context;

import com.frostnerd.dnsserver.database.serializers.IPPortSerializer;
import com.frostnerd.dnsserver.server.DNSServer;
import com.frostnerd.utils.database.orm.SingletonEntity;
import com.frostnerd.utils.database.orm.annotations.Ignore;
import com.frostnerd.utils.database.orm.annotations.Named;
import com.frostnerd.utils.database.orm.annotations.NotNull;
import com.frostnerd.utils.database.orm.annotations.RowID;
import com.frostnerd.utils.database.orm.annotations.Serialized;
import com.frostnerd.utils.database.orm.annotations.Table;
import com.frostnerd.utils.database.orm.annotations.Unique;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
@Table(name = "DNSServerSetting")
public class DNSServerSetting extends SingletonEntity implements Serializable{
    @RowID
    private int rowID;
    @Named(name = "Name")
    @Unique
    @NotNull
    private String name;
    @Named(name = "Port")
    private int port;
    @Named(name = "ServerTimeout")
    private int serverTimeout;
    @Named(name = "UpstreamPrimary")
    @NotNull
    @Serialized(using = IPPortSerializer.class)
    private IPPortPair upstreamPrimary;
    @Named(name = "UpstreamSecondary")
    @Serialized(using = IPPortSerializer.class)
    private IPPortPair upstreamSecondary;
    @Named(name = "LogQueries")
    private boolean logQueries;
    @Named(name = "LogQueryResponses")
    private boolean logQueryResponses ;
    @Named(name = "UpstreamUDP")
    private boolean upstreamUDP;
    @Named(name = "ResolveLocal")
    private boolean resolveLocal;
    @Named(name = "UDP")
    private boolean udp = true;

    @Ignore
    private boolean serverRunning;
    @Ignore
    private DNSServer.ErrorListener errorListener;
    @Ignore
    private DNSServer.QueryListener queryListener;
    @Ignore
    private List<ServerStateListener> serverStateListeners = new ArrayList<>();

    public DNSServerSetting(){

    }

    public DNSServerSetting(String name, int port, int serverTimeout, IPPortPair upstreamPrimary, IPPortPair upstreamSecondary, boolean logQueries, boolean logQueryResponses, boolean upstreamUDP, boolean resolveLocal){
        this.name = name;
        this.port = port;
        this.serverTimeout = serverTimeout;
        this.upstreamPrimary = upstreamPrimary;
        this.upstreamSecondary = upstreamSecondary;
        this.logQueries = logQueries;
        this.logQueryResponses = logQueryResponses;
        this.upstreamUDP = upstreamUDP;
        this.resolveLocal = resolveLocal;
    }

    public boolean isServerRunning(){
        return serverRunning;
    }

    public void setServerRunning(boolean serverRunning) {
        this.serverRunning = serverRunning;
        if(serverRunning)for(ServerStateListener listener: serverStateListeners)listener.serverStarted();
        else for(ServerStateListener listener: serverStateListeners)listener.serverStopped();
    }

    public boolean isUdp() {
        return udp;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getServerTimeout() {
        return serverTimeout;
    }

    public IPPortPair getUpstreamPrimary() {
        return upstreamPrimary;
    }

    public IPPortPair getUpstreamSecondary() {
        return upstreamSecondary;
    }

    public boolean isQueryLoggingEnabled() {
        return logQueries;
    }

    public boolean isQueryResponseLoggingEnabled() {
        return logQueryResponses;
    }

    public boolean isUpstreamUDP() {
        return upstreamUDP;
    }

    public DNSServer.ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(DNSServer.ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public DNSServer.QueryListener getQueryListener() {
        return queryListener;
    }

    public void setQueryListener(DNSServer.QueryListener queryListener) {
        this.queryListener = queryListener;
    }

    public boolean shouldResolveLocal(){
        return resolveLocal;
    }

    public void addServerStateListener(ServerStateListener listener){
        serverStateListeners.add(listener);
    }

    public void removeServerStateListener(ServerStateListener listener){
        serverStateListeners.remove(listener);
    }

    public void clearServerStateListeners(){
        serverStateListeners.clear();
    }

    public interface ServerStateListener{
        public void serverStarted();
        public void serverStopped();
    }
}
