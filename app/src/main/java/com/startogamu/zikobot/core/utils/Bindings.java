package com.startogamu.zikobot.core.utils;

import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.startogamu.zikobot.R;

import es.claucookie.miniequalizerlibrary.EqualizerView;

/**
 * Created by josh on 26/03/16.
 */
public class Bindings {
    private static final int ROTATION = 2;

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_picture_loading)
                .into(view);
    }

    @BindingAdapter({"imageRes"})
    public static void loadImage(ImageView view, @DrawableRes int imageRes) {
        view.setImageResource(imageRes);
    }

    @BindingAdapter({"animatePlay"})
    public static void animate(EqualizerView equalizer, boolean isPlaying) {
        if (isPlaying)
            equalizer.animateBars(); // Whenever you want to tart the animation
        else equalizer.stopBars(); // When you want equalizer stops animating
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setBottomMargin(View view, float bottomMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, (int)bottomMargin);
        view.setLayoutParams(layoutParams);
    }
}
