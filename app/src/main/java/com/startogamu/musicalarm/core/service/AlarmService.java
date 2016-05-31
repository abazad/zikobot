package com.startogamu.musicalarm.core.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.startogamu.musicalarm.core.notification.MusicNotification;
import com.startogamu.musicalarm.core.receiver.StopReceiver;
import com.startogamu.musicalarm.core.utils.EXTRA;
import com.startogamu.musicalarm.module.alarm.manager.AlarmManager;
import com.startogamu.musicalarm.module.alarm.object.Alarm;
import com.startogamu.musicalarm.module.component.Injector;
import com.startogamu.musicalarm.view.Henson;

import java.io.UnsupportedEncodingException;

/**
 * Created by josh on 28/03/16.
 */
public class AlarmService extends Service {

    Alarm alarm;

    private static final String TAG = AlarmService.class.getSimpleName();
    private StopReceiver stopReceiver;

    /***
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        long alarmId = intent.getLongExtra(EXTRA.ALARM_ID, -1);
        AlarmManager.getAlarmById(alarmId).subscribe((alarm) -> {
            AlarmService.this.alarm = alarm;
            MusicNotification.show(alarm.getName());
            startActivity(Henson.with(getBaseContext()).gotoActivityWakeUp().alarm(alarm).build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        });

        stopReceiver = new StopReceiver(this::stop);
        registerReceiver(stopReceiver, new IntentFilter(StopReceiver.TAG));
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (stopReceiver != null) {
            unregisterReceiver(stopReceiver);
        }
        super.onDestroy();
    }


    /***
     *
     */
    private void stop() {
        Injector.INSTANCE.playerComponent().manager().stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
