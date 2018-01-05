package com.frostnerd.dnsserver.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.frostnerd.dnsserver.R;
import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.services.ServerService;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright Daniel Wolf 2018
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ViewHolder> {
    private LayoutInflater layoutInflater;
    private List<DNSServerSetting> dnsServerSettings;
    private static final int serverNotRunningColor = Color.parseColor("#F44336"), serverRunningColor = Color.parseColor("#00FF00");
    private String portText;
    private Context context;

    public ServersAdapter(Context context, List<DNSServerSetting> serverSettings){
        layoutInflater = LayoutInflater.from(context);
        portText = context.getString(R.string.server_port_label);
        this.dnsServerSettings = serverSettings;
        this.context = context;
        Collections.sort(dnsServerSettings, new Comparator<DNSServerSetting>() {
            @Override
            public int compare(DNSServerSetting o1, DNSServerSetting o2) {
                if(o1.isServerRunning() && !o2.isServerRunning())return -1;
                else if(o2.isServerRunning() && !o1.isServerRunning())return 1;
                else return o1.getName().compareTo(o2.getName());
            }
        });
        for(DNSServerSetting s: dnsServerSettings) System.out.println(s.getName());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.row_server, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DNSServerSetting setting = dnsServerSettings.get(position);
        setting.clearServerStateListeners();
        ((TextView)holder.itemView.findViewById(R.id.server_name)).setText(setting.getName());
        ((TextView)holder.itemView.findViewById(R.id.server_port)).setText(portText.replace("[x]", "" + setting.getPort()));
        final ImageButton startStopButton = holder.itemView.findViewById(R.id.server_start_stop);
        final View indicatorView =  holder.itemView.findViewById(R.id.server_status_indicator);
        if(!setting.isServerRunning()){
            indicatorView.setBackgroundColor(serverNotRunningColor);
            startStopButton.setImageResource(R.drawable.ic_play);
        }else{
            indicatorView.setBackgroundColor(serverRunningColor);
            startStopButton.setImageResource(R.drawable.ic_stop);
        }
        setting.addServerStateListener(new DNSServerSetting.ServerStateListener() {
            @Override
            public void serverStarted() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        indicatorView.setBackgroundColor(Color.parseColor("#00FF00"));
                        startStopButton.setImageResource(R.drawable.ic_stop);
                    }
                });
            }

            @Override
            public void serverStopped() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        indicatorView.setBackgroundColor(serverNotRunningColor);
                        startStopButton.setImageResource(R.drawable.ic_play);
                    }
                });
            }
        });
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = setting.isServerRunning() ? ServerService.COMMAND_STOP_SERVER : ServerService.COMMAND_START_SERVER;
                Intent intent = new Intent(context, ServerService.class).putExtra(cmd, setting.getName());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)context.startForegroundService(intent);
                else context.startService(new Intent(context, ServerService.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dnsServerSettings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
