package com.frostnerd.dnsserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.frostnerd.dnsserver.R;
import com.frostnerd.dnsserver.activities.MainActivity;
import com.frostnerd.dnsserver.database.DatabaseHelper;
import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.server.DNSServer;
import com.frostnerd.dnsserver.server.TCPServer;
import com.frostnerd.dnsserver.server.UDPServer;
import com.frostnerd.dnsserver.util.Util;
import com.frostnerd.utils.database.orm.parser.Column;
import com.frostnerd.utils.database.orm.statementoptions.queryoptions.WhereCondition;
import com.frostnerd.utils.services.NotificationService;

import java.util.HashMap;

/**
 * Copyright Daniel Wolf 2018
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class ServerService extends NotificationService {
    private NotificationCompat.Builder notificationBuilder;
    private HashMap<DNSServer, Thread> servers = new HashMap<>();
    public static final String COMMAND_START_SERVER = "start_server", COMMAND_STOP_SERVER = "stop_server", COMMAND_START_MULTIPLE_SERVERS = "start_servers";

    @Override
    public void onCreate() {
        super.onCreate();
        notificationBuilder = new NotificationCompat.Builder(this, Util.createNotificationChannel(this, false));
        notificationBuilder.setContentTitle(getString(R.string.app_name));
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 10, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT));
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra(COMMAND_START_SERVER)){
            DNSServerSetting settings = Util.getMainDatabase(this).getSQLHandler(DNSServerSetting.class).
                    selectFirstRow(Util.getMainDatabase(this), true,
                            WhereCondition.equal(Util.getMainDatabase(this).findColumn(DNSServerSetting.class, "name"), intent.getStringExtra(COMMAND_START_SERVER)));
            DNSServer server = settings.isUdp() ? new UDPServer(this, settings) : new TCPServer(this, settings);
            Thread thread = new Thread(server);
            servers.put(server, thread);
            thread.start();
        }else if(intent.hasExtra(COMMAND_STOP_SERVER)){
            DNSServerSetting settings = Util.getMainDatabase(this).getSQLHandler(DNSServerSetting.class).
                    selectFirstRow(Util.getMainDatabase(this), true,
                            WhereCondition.equal(Util.getMainDatabase(this).findColumn(DNSServerSetting.class, "name"), intent.getStringExtra(COMMAND_STOP_SERVER)));
            for(DNSServer server: servers.keySet()){
                if(server.getServerSetting().equals(settings)){
                    server.stop();
                    servers.remove(server);
                    break;
                }
            }
        }else if(intent.hasExtra(COMMAND_START_MULTIPLE_SERVERS)){
            DatabaseHelper db = Util.getMainDatabase(this);
            Column<DNSServerSetting> column = db.findColumn(DNSServerSetting.class, "name");
            for(String name: intent.getStringArrayListExtra(COMMAND_START_MULTIPLE_SERVERS)){
                DNSServerSetting settings = db.getSQLHandler(DNSServerSetting.class).
                        selectFirstRow(Util.getMainDatabase(this), true,
                                WhereCondition.equal(column, name));
                DNSServer server = settings.isUdp() ? new UDPServer(this, settings) : new TCPServer(this, settings);
                Thread thread = new Thread(server);
                servers.put(server, thread);
                thread.start();
            }
        }
        if(servers.size() == 0){
            stopSelf();
            return START_NOT_STICKY;
        }
        else {
            updateNotification();
            return START_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @NonNull
    @Override
    public Notification getNotification() {
        notificationBuilder.setContentText(getString(R.string.x_servers_running).replace("[x]", servers.size() + ""));
        return notificationBuilder.build();
    }

    @Override
    public boolean foregroundMode() {
        return true;
    }

    @Override
    public boolean removeNotificationOnTaskRemoval() {
        return false;
    }
}
