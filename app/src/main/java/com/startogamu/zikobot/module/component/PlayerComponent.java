package com.startogamu.zikobot.module.component;

import com.startogamu.zikobot.core.receiver.StopReceiver;
import com.startogamu.zikobot.module.music.PlayerBaseComponent;
import com.startogamu.zikobot.module.music.PlayerModule;
import com.startogamu.zikobot.viewmodel.activity.ActivityWakeUpVM;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by josh on 11/04/16.
 */
@Singleton
@Component(modules = PlayerModule.class)
public interface PlayerComponent extends PlayerBaseComponent{

    void inject(ActivityWakeUpVM alarmService);

    void inject(StopReceiver stopReceiver);
}