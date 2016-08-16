package com.startogamu.zikobot.core.event;

import android.view.View;

import com.startogamu.zikobot.module.zikobot.model.Artist;

import lombok.Data;

/**
 * Created by josh on 06/06/16.
 */
@Data
public class EventShowArtistDetail {
    private final Artist artist;
    private final View view;
}