package com.frostnerd.dnsserver.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.frostnerd.dnsserver.R;
import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.services.ServerService;
import com.frostnerd.utils.networking.NetworkUtil;
import com.frostnerd.utils.preferences.Preferences;

import java.io.IOException;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Copyright Daniel Wolf 2018
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class RootRequestDialog extends AlertDialog {
    private CheckBox dontAskAgain;

    public RootRequestDialog(final Context context, final DNSServerSetting server) {
        super(context);
        if(Preferences.getBoolean(context, "dont_ask_root", false)){
            doStuff(server, context);
        }else{
            setTitle(R.string.dialog_title_root_needed);
            setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel), (OnClickListener) null);
            setButton(BUTTON_POSITIVE, context.getString(R.string.ok), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(dontAskAgain.isChecked())Preferences.put(context, "dont_ask_root", true);
                    doStuff(server, context);
                }
            });
            View content = getLayoutInflater().inflate(R.layout.dialog_root_required, null, false);
            setView(content);
            ((TextView)content.findViewById(R.id.text)).setText(R.string.root_needed_explain);
            dontAskAgain = content.findViewById(R.id.dont_show_again);
            show();
        }
    }

    //Come on, do stuff!
    private void doStuff(DNSServerSetting server, Context context){
        int port = NetworkUtil.getFirstFreePort(server.isUdp(), 10000, 30000);
        String cmd = "iptables -t nat -A PREROUTING -i eth0 -p [trspt] --dport [src] -j REDIRECT --to-port [dst]";
        cmd = cmd.replace("[trsprt]", server.isUdp() ? "udp" : "tcp").
                replace("[src]", server.getPort() + "").
                replace("[dst]", port + "");
        server.setLocalRedirectPort(port);
        List<String> result = Shell.SU.run(cmd);
        if(result != null && result.size() == 0){
            Intent intent = new Intent(context, ServerService.class);
            intent.putExtra(ServerService.COMMAND_START_SERVER, server.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)context.startForegroundService(intent);
            else context.startService(intent);
        }
        dismiss();
    }

}
