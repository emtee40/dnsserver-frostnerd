package com.frostnerd.dnsserver.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.frostnerd.dnsserver.R;
import com.frostnerd.dnsserver.database.DatabaseHelper;
import com.frostnerd.dnsserver.database.entities.main.IPPortPair;
import com.frostnerd.utils.networking.NetworkUtil;

import java.util.regex.Pattern;

/**
 * Created by Daniel on 25.10.2017.
 */

public final class Util {
    private static DatabaseHelper databaseHelper;
    private static DNSResolver dnsResolver;
    private static final Pattern ipv6WithPort = Pattern.compile("(\\[[0-9a-f:]+\\]:[0-9]{1,5})|([0-9a-f:]+)");
    private static final Pattern ipv4WithPort = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?");

    public static DatabaseHelper getMainDatabase(Context context){
        return databaseHelper == null ? databaseHelper = new DatabaseHelper(context) : databaseHelper;
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

    public static String createNotificationChannel(Context context, boolean allowIconHiding){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(allowIconHiding){
                NotificationChannel channel = new NotificationChannel("noIconChannel", context.getString(R.string.notification_channel_hiddenicon), NotificationManager.IMPORTANCE_MIN);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setDescription(context.getString(R.string.notification_channel_hiddenicon_description));
                notificationManager.createNotificationChannel(channel);
                return "noIconChannel";
            }else{
                NotificationChannel channel = new NotificationChannel("defaultchannel", context.getString(R.string.notification_channel_default), NotificationManager.IMPORTANCE_LOW);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setDescription(context.getString(R.string.notification_channel_default_description));
                notificationManager.createNotificationChannel(channel);
                return "defaultchannel";
            }
        }else{
            return "defaultchannel";
        }
    }
}
