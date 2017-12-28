package com.frostnerd.dnsserver.server;

import android.content.Context;

import com.frostnerd.dnsserver.util.DNSResolver;
import com.frostnerd.dnsserver.util.IPPortPair;
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
    protected DNSResolver resolver;
    protected int port;
    protected Configuration configuration;
    protected IPPortPair upstreamServer;
    protected InetAddress upstreamServerAddress;
    protected QueryLogger queryLogger;
    protected QueryListener queryListener;

    public DNSServer(Context context, int port, IPPortPair upstreamServer, final Configuration configuration) {
        this.resolver = Util.getDnsResolver(context);
        this.port = port;
        this.configuration = configuration;
        this.upstreamServer = upstreamServer;
        try {
            this.upstreamServerAddress = InetAddress.getByName(upstreamServer.getAddress());
        } catch (UnknownHostException ignored) {}
        if(configuration.shouldLogQueries() || configuration.shouldLogForwardedQuery() ||
                configuration.shouldLogUpstreamQueryResults())
            queryLogger = new QueryLogger(context);

        if(configuration.shouldLogQueries() || configuration.shouldLogForwardedQuery() ||
                configuration.shouldLogUpstreamQueryResults() || configuration.getQueryListener() != null){
            if((configuration.shouldLogQueries() || configuration.shouldLogForwardedQuery()) && configuration.getQueryListener() != null){
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        if(configuration.shouldLogQueries() || configuration.shouldLogForwardedQuery())
                            queryLogger.logQuery(host,query, type, forwardedToUpstream);
                        configuration.getQueryListener().queryReceived(host, query, type, forwardedToUpstream);
                    }
                };
            }else if(configuration.getQueryListener() != null){
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        configuration.getQueryListener().queryReceived(host, query, type, forwardedToUpstream);
                    }
                };
            }else{
                queryListener = new QueryListener() {
                    @Override
                    public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                        if(configuration.shouldLogQueries() || configuration.shouldLogForwardedQuery())
                            queryLogger.logQuery(host,query, type, forwardedToUpstream);
                    }
                };
            }
        }else{
            queryListener = new QueryListener() {
                @Override
                public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream) {
                    // - Dust -
                }
            };
        }
        System.out.println("Listing on port " + port);
    }

    public abstract void stopServer();


    public void stop() {
        stopServer();
        resolver.cleanup();
        if(queryLogger != null)queryLogger.cleanup();
        resolver = null;
        queryLogger = null;
    }

    public static class Configuration{
        private final boolean logQueries, upstreamUDP, logForwardedQuery, logUpstreamQueryResults, resolveLocal;
        private final ErrorListener errorListener;
        private final QueryListener queryListener;

        private Configuration(boolean logQueries, ErrorListener errorListener, boolean upstreamUDP,
                              boolean logForwardedQuery, boolean logUpstreamQueryResults, QueryListener listener,
                              boolean resolveLocal){
            this.logQueries = logQueries;
            this.errorListener = errorListener;
            this.upstreamUDP = upstreamUDP;
            this.logForwardedQuery = logForwardedQuery;
            this.logUpstreamQueryResults = logUpstreamQueryResults;
            this.queryListener = listener;
            this.resolveLocal = resolveLocal;
        }

        public boolean shouldLogQueries() {
            return logQueries;
        }

        public boolean shouldLogForwardedQuery() {
            return logForwardedQuery;
        }

        public boolean shouldLogUpstreamQueryResults() {
            return logUpstreamQueryResults;
        }

        public boolean isUpstreamUDP() {
            return upstreamUDP;
        }

        public ErrorListener getErrorListener() {
            return errorListener;
        }

        public QueryListener getQueryListener() {
            return queryListener;
        }

        public boolean shouldResolveLocal() {
            return resolveLocal;
        }

        public static class Builder {
            private boolean logQueries;
            private DNSServer.ErrorListener errorListener;
            private boolean upstreamUDP, logForwardedQuery, logUpstreamQueryResults, resolveLocal;
            private QueryListener queryListener;

            public Builder setLogQueries(boolean logQueries) {
                this.logQueries = logQueries;
                return this;
            }

            public Builder setErrorListener(DNSServer.ErrorListener errorListener) {
                this.errorListener = errorListener;
                return this;
            }

            public Builder setUpstreamUDP(boolean upstreamUDP) {
                this.upstreamUDP = upstreamUDP;
                return this;
            }

            public Builder setLogForwardedQuery(boolean logForwardedQuery){
                this.logForwardedQuery = logForwardedQuery;
                return this;
            }

            public Builder setResolveLocal(boolean resolveLocal) {
                this.resolveLocal = resolveLocal;
                return this;
            }

            public Builder setLogUpstreamQueryResults(boolean logUpstreamQueryResults) {
                this.logUpstreamQueryResults = logUpstreamQueryResults;
                return this;
            }

            public Builder setQueryListener(QueryListener queryListener) {
                this.queryListener = queryListener;
                return this;
            }

            public DNSServer.Configuration build() {
                return new DNSServer.Configuration(logQueries, errorListener, upstreamUDP,
                        logForwardedQuery, logUpstreamQueryResults, queryListener, resolveLocal);
            }
        }
    }

    public interface ErrorListener{
        public void onError(Exception e);
    }

    public interface QueryListener{
        public void queryReceived(String host, String query, Record.TYPE type, boolean forwardedToUpstream);
    }
}
