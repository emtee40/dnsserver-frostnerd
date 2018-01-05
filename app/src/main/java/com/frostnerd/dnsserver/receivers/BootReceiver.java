package com.frostnerd.dnsserver.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.services.ServerService;
import com.frostnerd.dnsserver.util.Util;

import java.util.ArrayList;

/**
 * Copyright Daniel Wolf 2018
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null && intent.getAction() != null && (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED))){
            ArrayList<String> servers = new ArrayList<>();
            for(DNSServerSetting setting: Util.getMainDatabase(context).getAll(DNSServerSetting.class)){
                if(setting.shouldStartOnBoot())servers.add(setting.getName());
            }
            Intent serviceIntent = new Intent(context, ServerService.class).putExtra(ServerService.COMMAND_START_MULTIPLE_SERVERS, servers);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)context.startForegroundService(serviceIntent);
            else context.startService(serviceIntent);
        }
    }
}
