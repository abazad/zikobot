package com.startogamu.zikobot.view.fragment.local;

import android.support.annotation.Nullable;

import com.f2prateek.dart.henson.Bundler;
import com.joxad.easydatabinding.fragment.FragmentBase;
import com.startogamu.zikobot.BR;
import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.utils.EXTRA;
import com.startogamu.zikobot.databinding.FragmentLocalAlbumsBinding;
import com.startogamu.zikobot.module.content_resolver.model.LocalArtist;
import com.startogamu.zikobot.viewmodel.fragment.local.FragmentLocalAlbumsVM;

import org.parceler.Parcels;

/**
 * Created by josh on 06/06/16.
 */
public class FragmentLocalAlbums extends FragmentBase<FragmentLocalAlbumsBinding, FragmentLocalAlbumsVM> {


    public static FragmentLocalAlbums newInstance(@Nullable LocalArtist artist) {
        FragmentLocalAlbums fragment = new FragmentLocalAlbums();
        fragment.setArguments(Bundler.create().put(EXTRA.LOCAL_ARTIST, Parcels.wrap(artist)).get());
        return fragment;
    }

    @Override
    public int data() {
        return BR.fragmentLocalAlbumsVM;
    }

    @Override
    public int layoutResources() {
        return R.layout.fragment_local_albums;
    }

    @Override
    public FragmentLocalAlbumsVM baseFragmentVM(FragmentLocalAlbumsBinding binding) {
        return new FragmentLocalAlbumsVM(this, binding);
    }

}
