package com.startogamu.musicalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.startogamu.musicalarm.model.Alarm;
import com.startogamu.musicalarm.service.AlarmService;
import com.startogamu.musicalarm.utils.EXTRA;

import org.parceler.Parcels;

/**
 * Created by josh on 28/03/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentAlarm = new Intent(context, AlarmService.class);
        long alarmId =intent.getLongExtra(EXTRA.ALARM_ID, -1);
        Log.d(AlarmReceiver.class.getSimpleName(), ""+alarmId);
        intentAlarm.putExtra(EXTRA.ALARM_ID, alarmId);
        context.startService(intentAlarm);
    }
}

