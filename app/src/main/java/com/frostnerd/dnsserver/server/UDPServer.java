package com.frostnerd.dnsserver.server;

import android.content.Context;

import com.frostnerd.dnsserver.util.DNSResolver;
import com.frostnerd.dnsserver.util.IPPortPair;
import com.frostnerd.utils.general.SortUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.measite.minidns.Record;
import de.measite.minidns.record.Data;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class UDPServer extends DNSServer{
    private boolean stop = false;
    private DatagramSocket serverSocket;
    private HashMap<ExpiredPacket, Query> futureAnswers = new HashMap<>();

    public UDPServer(Context context, int port, IPPortPair upstreamServer, Configuration configuration) {
        super(context, port, upstreamServer, configuration);
    }

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(port);
            //System.out.println("Listening on: " + serverSocket.getLocalSocketAddress());
            byte[] data = new byte[32767];
            DatagramPacket packet;
            DNSResolver.DNSResolveResult result;
            while(!stop){
                packet = new DatagramPacket(data, data.length);
                //System.out.println("Waiting for packet.");
                serverSocket.receive(packet);
                //System.out.println("Received a packet from " + packet.getAddress().getHostAddress());
                result = resolver.handlePossiblePacket(data, configuration.shouldResolveLocal());
                if(result != null){
                    if(result.isUpstreamAnswer()){
                        //System.out.println("Received upstream answer");
                        if(configuration.shouldLogUpstreamQueryResults()){
                            for(Record<? extends Data> record: result.getMessage().answerSection){
                                queryLogger.logUpstreamQueryResult(result.getQuery(),
                                        record.getPayload().toString(),record.type);
                            }
                        }
                        byte[] dnsData = result.getMessage().toArray();
                        Map.Entry<ExpiredPacket, Query> entry;
                        for(Iterator<Map.Entry<ExpiredPacket, Query>> iterator = futureAnswers.entrySet().iterator(); iterator.hasNext();){
                            entry = iterator.next();
                            //System.out.println("Someone waits for " + entry.getValue().getQuery() +
                              //      ", we have " + result.getQuery() + " equals: " + entry.getValue().getQuery().equalsIgnoreCase(result.getQuery()));
                            if(entry.getValue().getQuery().equalsIgnoreCase(result.getQuery()) &&
                                    containsType(result.getMessage().answerSection, entry.getValue().getType())){
                                //System.out.println("Answering to " + entry.getKey().getAddress());
                                serverSocket.send(new DatagramPacket(dnsData, dnsData.length,
                                        entry.getKey().getAddress(), entry.getKey().getPort()));
                                iterator.remove();
                            }
                        }
                    }else if(result.shouldForwardQuery()){
                        queryListener.queryReceived(packet.getAddress().getHostAddress(), result.getQuery(), result.getType(), true);
                        //System.out.println("Forwarding question for " + result.getQuery() + " of type: " + result.getType());
                        futureAnswers.put(new ExpiredPacket(packet), new Query(result));
                        serverSocket.send(new DatagramPacket(packet.getData(), packet.getLength(), upstreamServerAddress, upstreamServer.getPort()));
                    }else{
                        queryListener.queryReceived(packet.getAddress().getHostAddress(), result.getQuery(), result.getType(), false);
                        byte[] dnsData = result.getMessage().toArray();
                        packet = new DatagramPacket(dnsData, dnsData.length, packet.getAddress(), packet.getPort());
                        serverSocket.send(packet);
                    }
                }else System.out.println("RESULT NULL");
            }
        } catch (Exception e) {
            if(configuration.getErrorListener() != null)configuration.getErrorListener().onError(e);
            e.printStackTrace();
        }
    }

    private boolean containsType(List<Record<? extends Data>> records, Record.TYPE type){
        for(Record<? extends Data> record: records){
            //System.out.println("Waiting for type: " + record.getPayload().getType() + ", given: " + type);
            if(record.getPayload().getType() == type)return true;
        }
        return false;
    }

    @Override
    public void stopServer() {
        stop = true;
        serverSocket.close();
        futureAnswers.clear();
        serverSocket = null;
        upstreamServerAddress = null;
        futureAnswers = null;
    }

    private class ExpiredPacket{
        private InetAddress address;
        private int port;

        public ExpiredPacket(DatagramPacket packet){
            this.address = packet.getAddress();
            this.port = packet.getPort();
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return address + ":" + port;
        }
    }

    private class Query{
        private Record.TYPE type;
        private String query;

        public Query(DNSResolver.DNSResolveResult result){
            this(result.getType(), result.getQuery());
        }

        public Query(Record.TYPE type, String query) {
            this.type = type;
            this.query = query;
        }

        public Record.TYPE getType() {
            return type;
        }

        public String getQuery() {
            return query;
        }
    }
}
