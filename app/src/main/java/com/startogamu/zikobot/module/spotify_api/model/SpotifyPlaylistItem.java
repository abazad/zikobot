package com.startogamu.zikobot.module.spotify_api.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * Created by josh on 29/03/16.
 */
@Data
public class SpotifyPlaylistItem {

    @SerializedName("track")
    public SpotifyTrack track;
}
