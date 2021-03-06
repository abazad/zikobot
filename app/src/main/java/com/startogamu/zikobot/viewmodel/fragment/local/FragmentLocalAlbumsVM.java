package com.startogamu.zikobot.viewmodel.fragment.local;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.joxad.easydatabinding.fragment.FragmentBaseVM;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.event.EventShowArtistDetail;
import com.startogamu.zikobot.core.utils.Constants;
import com.startogamu.zikobot.core.utils.EXTRA;
import com.startogamu.zikobot.core.viewutils.EndlessRecyclerViewScrollListener;
import com.startogamu.zikobot.databinding.FragmentLocalAlbumsBinding;
import com.startogamu.zikobot.module.component.Injector;
import com.startogamu.zikobot.module.content_resolver.model.LocalAlbum;
import com.startogamu.zikobot.module.content_resolver.model.LocalArtist;
import com.startogamu.zikobot.view.fragment.local.FragmentLocalAlbums;
import com.startogamu.zikobot.viewmodel.base.AlbumVM;

import org.greenrobot.eventbus.EventBus;

import me.tatarka.bindingcollectionadapter.ItemView;

/**
 * Created by josh on 06/06/16.
 */
public class FragmentLocalAlbumsVM extends FragmentBaseVM<FragmentLocalAlbums, FragmentLocalAlbumsBinding> {

    @Nullable
    @InjectExtra(EXTRA.LOCAL_ARTIST)
    LocalArtist localArtist;

    public static final String TAG = "FragmentLocalAlbumsVM";
    public ObservableBoolean showZmvMessage;

    public String zmvMessage;

    public ItemView itemView = ItemView.of(BR.albumVM, R.layout.item_album);
    public ObservableArrayList<AlbumVM> items;

    /***
     * @param fragment
     * @param binding
     */
    public FragmentLocalAlbumsVM(FragmentLocalAlbums fragment, FragmentLocalAlbumsBinding binding) {
        super(fragment, binding);
    }


    @Override
    public void init() {

        Dart.inject(this, fragment.getArguments());
        items = new ObservableArrayList<>();
        showZmvMessage = new ObservableBoolean(false);
        zmvMessage = "";
        Injector.INSTANCE.contentResolverComponent().init(this);
        binding.rv.setLayoutManager(new GridLayoutManager(fragment.getContext(),2));
        binding.rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.rv.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadLocalMusic(15, totalItemsCount);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            updateMessage(fragment.getString(R.string.permission_local));
            binding.zmv.setOnClickListener(v -> askPermission());
            return;
        }
        items.clear();
        loadLocalMusic(15, 0);
    }

    public void onClick(View view) {
        EventBus.getDefault().post(new EventShowArtistDetail(null, view));
    }


    /***
     * Load the local music
     */
    public void loadLocalMusic(int limit, int offset) {


        Injector.INSTANCE.contentResolverComponent().localMusicManager().getLocalAlbums(limit, offset, localArtist != null ? localArtist.getName() : null, null).subscribe(localAlbums -> {
            Log.d(TAG, "" + localAlbums.size());
            for (LocalAlbum localAlbum : localAlbums) {
                items.add(new AlbumVM(fragment.getContext(), localAlbum));
            }
            if (localAlbums.isEmpty() && offset == 0) {
                updateMessage(fragment.getString(R.string.no_music));
            } else {
                showZmvMessage.set(false);
            }
        }, throwable -> {
            if (offset==0)
            updateMessage(fragment.getString(R.string.no_music));

        });
    }

    /***
     * Update t
     *
     * @param string
     */
    private void updateMessage(String string) {
        showZmvMessage.set(true);
        zmvMessage = string;
        binding.zmv.setZmvMessage(zmvMessage);
    }

    /***
     * Method to ask storage perm
     */
    private void askPermission() {
        ActivityCompat.requestPermissions(fragment.getActivity(), new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_STORAGE_ALBUM);

    }

    @Override
    public void destroy() {

    }
}
