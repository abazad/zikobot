package com.startogamu.zikobot.module.music.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.player.PlayerWrapper;
import com.joxad.android_easy_spotify.SpotifyPlayerManager;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.event.EventShowMessage;
import com.startogamu.zikobot.core.event.TrackChangeEvent;
import com.startogamu.zikobot.core.event.player.EventAddTrackToCurrent;
import com.startogamu.zikobot.core.event.player.EventAddTrackToPlayer;
import com.startogamu.zikobot.core.event.player.EventPlayTrack;
import com.startogamu.zikobot.core.event.player.EventStopPlayer;
import com.startogamu.zikobot.core.notification.PlayerNotification;
import com.startogamu.zikobot.core.service.MediaPlayerService;
import com.startogamu.zikobot.core.utils.AppPrefs;
import com.startogamu.zikobot.module.deezer.manager.DeezerManager;
import com.startogamu.zikobot.module.deezer.manager.SimplifiedPlayerListener;
import com.startogamu.zikobot.module.zikobot.model.Alarm;
import com.startogamu.zikobot.module.zikobot.model.TYPE;
import com.startogamu.zikobot.module.zikobot.model.Track;
import com.startogamu.zikobot.viewmodel.base.TrackVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Singleton;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link PlayerMusicManager} will handle the change of track according to the type of alarm track that is used
 * If a spotify track is finished and the next one is a local one, it is his job to deal with it
 */
@Singleton
public class PlayerMusicManager {

    private static final String TAG = PlayerMusicManager.class.getSimpleName();
    private static Context context;
    static Handler handler = new Handler();
    private static boolean pauseToHandle = true;

    static boolean newTrack = true;

    private Alarm alarm = null;
    @Getter
    static int currentSong = 0;
    static int currentType = -1;
    static int currentPosition;
    private static ArrayList<TrackVM> tracks = new ArrayList<>();
    private static MediaPlayerService mediaPlayerService;
    private static boolean mediaPlayerServiceBound = false;
    //connect to the service
    public static boolean isPlaying = false;
    private static PlayerWrapper deezerPlayer;
    private static Handler trackPositionHandler;

    /***
     * @param context
     */
    public PlayerMusicManager(Context context) {
        PlayerMusicManager.context = context;
        initMediaPlayer(context);
        initSpotifyPlayer(context);
        initDeezerPlayer(context);
        trackPositionHandler = new Handler();
        EventBus.getDefault().register(this);
    }

    /***
     * Method called when the token is updated for spotify
     */
    public void refreshAccessTokenPlayer() {
        SpotifyPlayerManager.updateToken(AppPrefs.getSpotifyAccessToken());
    }

    /***
     * service connexion that will handle the binding with the {@link MediaPlayerService} service
     */
    private static ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaPlayerServiceBinder binder = (MediaPlayerService.MediaPlayerServiceBinder) service;
            //get service
            mediaPlayerService = binder.getService();
            mediaPlayerService.setOnCompletionListener(mp -> {
                next();
            });
            mediaPlayerService.setOnDisconnectListener(() -> {
                mediaPlayerService = null;
                mediaPlayerServiceBound = false;
            });
            //pass list
            mediaPlayerServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlayerService = null;
            mediaPlayerServiceBound = false;
        }
    };


    /***
     * @param context
     */
    private static void initMediaPlayer(Context context) {
        if (!mediaPlayerServiceBound || mediaPlayerService == null) {
            Intent playIntent = new Intent(context, MediaPlayerService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(playIntent);
        }
    }

    /***
     * Initialize the SpotifyPlayer according to the data found with accesstoken
     *
     * @param context
     */
    private void initSpotifyPlayer(Context context) {
        SpotifyPlayerManager.startPlayer(context, AppPrefs.getSpotifyAccessToken(), new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        }, new PlayerNotificationCallback() {
            @Override
            public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
                if (currentType == TYPE.SPOTIFY)
                    currentPosition = playerState.positionInMs;
                if (playerState.positionInMs >= playerState.durationInMs && playerState.durationInMs > 0 && pauseToHandle)
                    next();
                Log.d(TAG, String.format("Player state %s - activeDevice %s : current duration %d total duration %s", playerState.trackUri, playerState.activeDevice, playerState.positionInMs, playerState.durationInMs));
            }

            @Override
            public void onPlaybackError(ErrorType errorType, String s) {

            }
        }, null);
    }

    private void initDeezerPlayer(Context context) {
        DeezerManager.player().addPlayerListener(new SimplifiedPlayerListener() {
            @Override
            public void onTrackEnded(PlayableEntity playableEntity) {
                next();
            }
        });

    }


    /***
     * @param track
     */
    private static void playTrack(final TrackVM track) {
        initMediaPlayer(context);
        int i = 0;
        newTrack = true;
        for (TrackVM t : tracks) {
            if (t.getModel().getRef().equals(track.getModel().getRef())) {
                currentSong = i;
                newTrack = false;
                break;
            }
            i++;
        }
        if (newTrack) {
            currentSong = 0;
            tracks.clear();
            tracks.add(track);
        }
        Track model = track.getModel();
        currentType = model.getType();
        switch (model.getType()) {
            case TYPE.LOCAL:
                pauseToHandle = false;
                SpotifyPlayerManager.pause();
                SpotifyPlayerManager.clear();
                handler.postDelayed(() -> pauseToHandle = true, 500);
                mediaPlayerService.playSong(Uri.parse(model.getRef()));
                break;
            case TYPE.SPOTIFY:
                if (mediaPlayerService != null)
                    mediaPlayerService.stop();
                SpotifyPlayerManager.play(model.getRef());
                break;
            case TYPE.SOUNDCLOUD:
                SpotifyPlayerManager.pause();
                SpotifyPlayerManager.clear();
                mediaPlayerService.playUrlSong(model.getRef());
                break;
            case TYPE.DEEZER:
                SpotifyPlayerManager.pause();
                SpotifyPlayerManager.clear();
                mediaPlayerService.pause();
                DeezerManager.playTrack(Long.parseLong(model.getRef()));
                break;
        }
        isPlaying = true;
        observe();
        EventBus.getDefault().post(new TrackChangeEvent(model));

        if (playerStatusListener != null) {
            playerStatusListener.onUpdate(isPlaying);
        }
        PlayerNotification.show(model);

    }

    /***
     *
     */
    public void previous() {
        currentSong--;
        if (currentSong >= 0)
            playTrack(tracks.get(currentSong));
    }

    /***
     *
     */
    public static void next() {
        currentSong++;
        if (tracks.size() > currentSong) {
            playTrack(tracks.get(currentSong));
        } else {
            stop();
        }
    }


    /***
     * @param
     */
    public void playTracks() {
        currentSong = 0;
        playTrack(tracks.get(currentSong));

    }

    /***
     * @param alarm
     */
    public void startAlarm(Alarm alarm) {
        currentSong = 0;
        this.alarm = alarm;
        this.tracks.clear();
        for (Track track : alarm.getTracks()) {
            this.tracks.add(new TrackVM(context, track));
        }
        if (alarm.getRandomTrack() == 1) {
            Collections.shuffle(tracks);
        }
        playTrack(tracks.get(currentSong));
    }

    /***
     * Stop all the players
     */
    public static void stop() {
        SpotifyPlayerManager.pause();
        if (mediaPlayerService != null) {
            mediaPlayerService.stop();
        }
        currentSong = 0;

    }

    /***
     *
     */
    public void resume() {
        if (mediaPlayerService != null) {
            mediaPlayerService.resume();
        }
        SpotifyPlayerManager.resume();
        PlayerNotification.updatePlayStatus(false);
    }

    /**
     * pause all the players
     */
    public void pause() {
        if (mediaPlayerService != null) {
            mediaPlayerService.pause();
        }
        SpotifyPlayerManager.pause();
        PlayerNotification.updatePlayStatus(true);
    }

    public ArrayList<TrackVM> trackVMs() {
        return tracks;
    }


    @Subscribe
    public void onReceive(EventPlayTrack eventPlayTrack) {
        if (trackIsPlayable(eventPlayTrack.getTrack().getModel())) {
            playTrack(eventPlayTrack.getTrack());
        }
    }

    private boolean trackIsPlayable(Track model) {
        if (model.getType() == TYPE.SPOTIFY) {
            if (!AppPrefs.spotifyUser().getProduct().equalsIgnoreCase("premium")) {
                EventBus.getDefault().post(new EventShowMessage(context.getString(R.string.oops), context.getString(R.string.no_premium)));
                return false;
            }
        }
        if (model.getRef().contains(".m4a")) {
            EventBus.getDefault().post(new EventShowMessage(context.getString(R.string.oops), context.getString(R.string.no_m4a_support)));
            return false;
        }
        return true;
    }


    @Subscribe
    public void onReceive(EventAddTrackToPlayer eventAddTrackToPlayer) {
        tracks.clear();
        tracks.addAll(eventAddTrackToPlayer.getItems());
        for (int i = tracks.size() - 1; i >= 0; i--) {
            if (!trackIsPlayable(tracks.get(i).getModel())) {
                tracks.remove(i);
            }
        }
        isPlaying = true;
        playTracks();
    }


    @Subscribe(priority = 10)
    public void onReceive(EventAddTrackToCurrent eventAddTrackToPlayer) {
        tracks.add(eventAddTrackToPlayer.getTrackVM());
    }


    @Subscribe
    public void onReceive(EventStopPlayer eventStopPlayer) {
        tracks.clear();
        trackVMs().clear();
        PlayerNotification.cancel();
        stop();
    }

    public void playOrResume() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            resume();
        } else {
            pause();
        }
        if (playerStatusListener != null) {
            playerStatusListener.onUpdate(isPlaying);
        }
    }

    /***
     * move position
     *
     * @param progress
     */
    public void seek(int progress) {
        switch (currentType) {
            case TYPE.LOCAL:
                mediaPlayerService.seek(progress);
                break;
            case TYPE.SPOTIFY:
                SpotifyPlayerManager.player().seekToPosition(progress);
                break;
            case TYPE.SOUNDCLOUD:
                mediaPlayerService.seek(progress);
                break;
            case TYPE.DEEZER:
                deezerPlayer.seek(currentPosition);
                break;
        }
    }


    public TrackVM getCurrentTrackVM() {
        if (tracks.isEmpty()) {
            return new TrackVM(context, new Track());
        }
        return tracks.get(currentSong);
    }


    public static void observe() {
        trackPositionHandler.postDelayed(() -> {
            if (isPlaying) {
                switch (currentType) {
                    case TYPE.LOCAL:
                        currentPosition = mediaPlayerService.getCurrentPosition();
                        break;
                    case TYPE.SPOTIFY:
                        //already done in playbackevent
                        break;
                    case TYPE.SOUNDCLOUD:
                        currentPosition = mediaPlayerService.getCurrentPosition();
                        break;
                    case TYPE.DEEZER:
                        currentPosition = (int) deezerPlayer.getPosition();
                        break;
                }
                sendMsgToUI(currentPosition);
            }
            observe();

        }, 200);
    }


    @Setter
    private static DurationListener durationListener;

    public interface DurationListener {
        void onUpdate(int position);
    }

    @Setter
    private static PlayerStatusListener playerStatusListener;

    public interface PlayerStatusListener {
        void onUpdate(boolean isPlayinh);
    }

    //handles all the background threads things for you
    private static void sendMsgToUI(int position) {
        if (durationListener != null)
            durationListener.onUpdate(position);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }
}
