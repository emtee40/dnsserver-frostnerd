package com.frostnerd.dnsserver.database.entities.main;

import com.frostnerd.dnsserver.util.Util;
import com.frostnerd.utils.database.orm.Entity;
import com.frostnerd.utils.database.orm.annotations.AutoIncrement;
import com.frostnerd.utils.database.orm.annotations.Named;
import com.frostnerd.utils.database.orm.annotations.PrimaryKey;
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
@Table(name = "IPPortPair")
public class IPPortPair extends Entity implements Serializable {
    @Named(name = "IP")
    private String ip;
    @Named(name = "Port")
    private int port;
    @Named(name = "Ipv6")
    private boolean ipv6;
    public static final IPPortPair EMPTY = new IPPortPair("", -1, false);


    @PrimaryKey
    @AutoIncrement
    private int id;

    public IPPortPair(){

    }

    public IPPortPair(String ip, int port, boolean IPv6) {
        this.ip = ip;
        this.port = port;
        this.ipv6 = IPv6;
    }

    public static IPPortPair wrap(String s){
        return Util.validateInput(s, s.contains("[") || s.matches("[a-fA-F0-9:]+"), true, true);
    }

    public static IPPortPair wrap(String s, int defPort){
        return Util.validateInput(s, s.contains("[") || s.matches("[a-fA-F0-9:]+"), true, defPort);
    }

    public String getAddress() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean port){
        if(isEmpty())return "";
        if(port)return ipv6 ? "[" + getAddress() + "]:" + getPort() : getAddress() + ":" + getPort();
        else return getAddress();
    }

    public String formatForTextfield(boolean customPorts){
        if(ip.equals(""))return "";
        if(ipv6){
            return customPorts ? "[" + ip + "]:" + port : ip;
        }else{
            return customPorts ? ip + ":" + port : ip;
        }
    }

    public boolean isEmpty(){
        return getAddress().equals("");
    }

}