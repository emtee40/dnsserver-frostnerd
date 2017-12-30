package com.frostnerd.dnsserver.database.entities;


import com.frostnerd.dnsserver.server.DNSServer;
import com.frostnerd.utils.database.orm.Entity;
import com.frostnerd.utils.database.orm.annotations.Ignore;
import com.frostnerd.utils.database.orm.annotations.Named;
import com.frostnerd.utils.database.orm.annotations.NotNull;
import com.frostnerd.utils.database.orm.annotations.RowID;
import com.frostnerd.utils.database.orm.annotations.Table;

import java.io.Serializable;

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
public class DNSServerSetting extends Entity implements Serializable{
    @RowID
    private int rowID;
    @Named(name = "Name")
    @NotNull
    private String name;
    @Named(name = "Port")
    private int port;
    @Named(name = "ServerTimeout")
    private int serverTimeout;
    @Named(name = "UpstreamPrimary")
    @NotNull
    private IPPortPair upstreamPrimary;
    @Named(name = "UpstreamSecondary")
    private IPPortPair upstreamSecondary;
    @Named(name = "LogQueries")
    private boolean logQueries;
    @Named(name = "LogQueryResponses")
    private boolean logQueryResponses ;
    @Named(name = "UpstreamUDP")
    private boolean upstreamUDP;
    @Named(name = "ResolveLocal")
    private boolean resolveLocal;

    @Ignore
    private DNSServer.ErrorListener errorListener;
    @Ignore
    private DNSServer.QueryListener queryListener;

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
}
