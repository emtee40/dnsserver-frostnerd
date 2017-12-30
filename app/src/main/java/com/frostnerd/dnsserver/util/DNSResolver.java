package com.frostnerd.dnsserver.util;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.frostnerd.dnsserver.database.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Arrays;

import de.measite.minidns.DNSMessage;
import de.measite.minidns.Record;
import de.measite.minidns.record.A;
import de.measite.minidns.record.AAAA;
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
public class DNSResolver {
    private static final String WILDCARD_QUERY_RANDOM =
            "SELECT Target FROM DNSRules WHERE IPv6=? AND Wildcard=1 AND ? REGEXP Domain ORDER BY RANDOM() LIMIT 1";
    private static final String WILDCARD_QUERY_FIRST =
            "SELECT Target FROM DNSRules WHERE IPv6=? AND Wildcard=1 AND ? REGEXP Domain LIMIT 1";
    private static final String NON_WILDCARD_QUERY = "SELECT Target FROM DNSRules WHERE Domain=? AND IPv6=? AND Wildcard=0";
    private static final String SUM_WILDCARD_QUERY = "SELECT SUM(Wildcard) FROM DNSRules";
    private DatabaseHelper db;
    private int wildcardCount;

    public DNSResolver(Context context) {
        db = Util.getDatabaseHelper(context);
        Cursor cursor = db.getReadableDatabase().rawQuery(SUM_WILDCARD_QUERY, null);
        if (cursor.moveToFirst()) {
            wildcardCount = cursor.getInt(0);
        }
        cursor.close();
    }

    public void cleanup(){
        db.close();
        db = null;
    }

    public String resolve(String host, boolean ipv6) {
        return resolve(host, ipv6, true);
    }

    public String resolve(String host, boolean ipv6, boolean allowWildcard) {
        String res;
        res = resolveNonWildcard(host, ipv6);
        if (res == null && allowWildcard && wildcardCount != 0) {
            res = resolveWildcard(host, ipv6, false);
        }
        return res;
    }

    public String resolveNonWildcard(String host, boolean ipv6) {
        String result = null;
        Cursor cursor = db.getReadableDatabase().rawQuery(NON_WILDCARD_QUERY,
                new String[]{host, ipv6 ? "1" : "0"});
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public String resolveWildcard(String host, boolean ipv6, boolean matchFirst) {
        String result = null;
        Cursor cursor = db.getReadableDatabase().rawQuery(matchFirst ? WILDCARD_QUERY_FIRST : WILDCARD_QUERY_RANDOM,
                new String[]{ipv6 ? "1" : "0", host});
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    // TODO Reject non-DNS packets (currently throws exception)
    public DNSResolveResult handlePossiblePacket(byte[] packetBytes, boolean resolveLocal) throws IOException {
        DNSMessage dnsMsg = new DNSMessage(packetBytes);
        if(dnsMsg.answerSection != null && dnsMsg.answerSection.size() != 0)return new DNSResolveResult(dnsMsg, true, null);
        if(dnsMsg.getQuestion() == null)return null; //The sent packet most likely isn't a DNS packet. Ignoring.
        if(!resolveLocal)return new DNSResolveResult(dnsMsg.getQuestion().name.toString(), dnsMsg.getQuestion().type);
        String query = dnsMsg.getQuestion().name.toString(), target;
        if(!TextUtils.isEmpty(query)){
            target = resolve(query, dnsMsg.getQuestion().type == Record.TYPE.AAAA, true);
            if(target == null){
                return new DNSResolveResult(query, dnsMsg.getQuestion().type);
            }else{
                DNSMessage.Builder builder = null;
                if(dnsMsg.getQuestion().type == Record.TYPE.A){
                    builder = dnsMsg.asBuilder().setQrFlag(true).addAnswer(
                            new Record<Data>(query, Record.TYPE.A, 1, 64, new A(Inet4Address.getByName(target).getAddress())));
                }else if(dnsMsg.getQuestion().type == Record.TYPE.AAAA){
                    builder = dnsMsg.asBuilder().setQrFlag(true).addAnswer(
                            new Record<Data>(query, Record.TYPE.A, 1, 64, new AAAA(Inet6Address.getByName(target).getAddress())));
                }
                if(builder != null)return new DNSResolveResult(builder.build(), false, dnsMsg.getQuestion().type);
                return new DNSResolveResult(null, dnsMsg.getQuestion().type); //Not an A entry, but rather CNAME, SRV, TXT... All of which should be forwarded
            }
        }
        return null;
    }

    public DNSResolveResult handlePossiblePacket(InputStream inputStream, byte[] packetBytes, boolean resolveLocal) throws IOException {
        return handlePossiblePacket(Arrays.copyOfRange(packetBytes, 0, inputStream.read(packetBytes)), resolveLocal);
    }

    public static class DNSResolveResult{
        private DNSMessage message;
        private String query;
        private boolean upstreamAnswer;
        private Record.TYPE type;

        private DNSResolveResult(DNSMessage message, boolean isAnswer, Record.TYPE type) {
            this.message = message;
            upstreamAnswer = isAnswer;
            if(upstreamAnswer)query = message.getQuestion().name.toString();
            this.type = type;
        }

        private DNSResolveResult(String query, Record.TYPE type){
            message = null;
            this.query = query;
            this.type = type;
        }

        public boolean shouldForwardQuery(){
            return message == null;
        }

        public boolean isUpstreamAnswer(){
            return upstreamAnswer;
        }

        public Record.TYPE getType() {
            return type;
        }

        private boolean isIPv6(){
            return type == Record.TYPE.AAAA;
        }

        public DNSMessage getMessage() {
            return message;
        }

        public String getQuery() {
            return query;
        }
    }
}
