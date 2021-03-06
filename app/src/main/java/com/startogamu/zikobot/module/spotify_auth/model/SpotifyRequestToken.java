package com.startogamu.zikobot.module.spotify_auth.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 *
 */
@Data
public class SpotifyRequestToken {
    @SerializedName("grant_type")
    private final String grantType;
    @SerializedName("code")
    private final String code;
    @SerializedName("redirect_uri")
    private final String redirectUri;
    @SerializedName("client_id")
    private final String cliendId;
    @SerializedName("client_secret")
    private final String clientSecret;
}
