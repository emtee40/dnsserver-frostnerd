package com.frostnerd.dnsserver.util;

import android.content.Context;

import com.frostnerd.dnsserver.database.DatabaseHelper;
import com.frostnerd.dnsserver.database.entities.IPPortPair;
import com.frostnerd.utils.networking.NetworkUtil;

import java.util.regex.Pattern;

/**
 * Created by Daniel on 25.10.2017.
 */

public class Util {
    private static DatabaseHelper databaseHelper;
    private static DNSResolver dnsResolver;
    private static Pattern ipv6WithPort = Pattern.compile("(\\[[0-9a-f:]+\\]:[0-9]{1,5})|([0-9a-f:]+)");
    private static Pattern ipv4WithPort = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?");


    public static DatabaseHelper getDatabaseHelper(Context context){
        return databaseHelper == null ? databaseHelper = new DatabaseHelper(context) : databaseHelper;
    }

    public static DNSResolver getDnsResolver(Context context){
        return dnsResolver == null ? dnsResolver = new DNSResolver(context) : dnsResolver;
    }

    public static IPPortPair validateInput(String input, boolean iPv6, boolean allowEmpty, boolean allowLoopback) {
        if (allowEmpty && input.equals("")) return new IPPortPair("", -1, iPv6);
        if (iPv6) {
            if (ipv6WithPort.matcher(input).matches()) {
                if (input.contains("[")) {
                    int port = Integer.parseInt(input.split("]")[1].split(":")[1]);
                    String address = input.split("]")[0].replace("[", "");
                    boolean addressValid = (allowLoopback && NetworkUtil.isIP(address, true)) || NetworkUtil.isAssignableAddress(address, true);
                    return port <= 65535 && port >= 1 && addressValid ? new IPPortPair(address, port, true) : null;
                } else {
                    boolean addressValid = (allowLoopback && NetworkUtil.isIP(input, true)) || NetworkUtil.isAssignableAddress(input, true);
                    return addressValid ? new IPPortPair(input, -1, true) : null;
                }
            } else {
                return null;
            }
        } else {
            if (ipv4WithPort.matcher(input).matches()) {
                if (input.contains(":")) {
                    int port = Integer.parseInt(input.split(":")[1]);
                    String address = input.split(":")[0];
                    boolean addressValid = (allowLoopback && NetworkUtil.isIP(address, false)) || NetworkUtil.isAssignableAddress(address, false);
                    return port <= 65535 && port >= 1 && addressValid ? new IPPortPair(address, port, false) : null;
                } else {
                    boolean addressValid = (allowLoopback && NetworkUtil.isIP(input, false)) || NetworkUtil.isAssignableAddress(input, false);
                    return addressValid ? new IPPortPair(input, -1, false) : null;
                }
            } else {
                return null;
            }
        }
    }

    public static IPPortPair validateInput(String input, boolean iPv6, boolean allowEmpty, int defPort) {
        if (allowEmpty && input.equals("")) return new IPPortPair("", -1, iPv6);
        if (iPv6) {
            if (ipv6WithPort.matcher(input).matches()) {
                if (input.contains("[")) {
                    int port = Integer.parseInt(input.split("]")[1].split(":")[1]);
                    String address = input.split("]")[0].replace("[", "");
                    return NetworkUtil.isAssignableAddress(address, true) ? new IPPortPair(address,  port <= 65535 && port >= 1 ? port : defPort, true) : null;
                } else {
                    return NetworkUtil.isAssignableAddress(input, true) ? new IPPortPair(input, defPort, true) : null;
                }
            } else {
                return null;
            }
        } else {
            if (ipv4WithPort.matcher(input).matches()) {
                if (input.contains(":")) {
                    int port = Integer.parseInt(input.split(":")[1]);
                    String address = input.split(":")[0];
                    return NetworkUtil.isAssignableAddress(address, false) ? new IPPortPair(address, port <= 65535 && port >= 1 ? port : defPort, false) : null;
                } else {
                    return NetworkUtil.isAssignableAddress(input, false) ? new IPPortPair(input, defPort, false) : null;
                }
            } else {
                return null;
            }
        }
    }
}
