package com.ogbizi.plucky;

import android.view.View;
import android.widget.ImageView;

import com.ogbizi.plucky.SwappableImageView.Behavior;

import java.util.List;

import timber.log.Timber;

/**
 * Sample implementation of swappable image behavior
 * Swaps images by sliding horizontally out of view
 */
public class HorizontalSwappableImageBehavior implements Behavior {
    private SwappableImageView mView;

    @Override
    public void onAttach(SwappableImageView view) {
        mView = view;
    }

    @Override
    public void onReset(ImageView primary, ImageView secondary) {
        Timber.i("behaviour reset");
        secondary.setTranslationX(0);
        secondary.setTranslationY(mView.getMeasuredWidth());
        List drawables = mView.getDrawables();
        int index = mView.getCurrentIndex();
        primary.setImageResource((Integer) drawables.get(index));
        primary.setTranslationX(0);
        primary.setTranslationY(0);
    }

    @Override
    public void onStart(boolean isReverse, ImageView primary, ImageView secondary) {
        Timber.i("behaviour start: reverse=%s", isReverse);
        onReset(primary, secondary);
        View parent = (View) secondary.getParent();
        int sX = isReverse ? parent.getMeasuredWidth() : -secondary.getMeasuredWidth();
        int sY = 0;
        secondary.setTranslationX(sX);
        secondary.setTranslationY(sY);
    }

    @Override
    public void onUpdate(float progress, boolean isReverse, ImageView primary, ImageView secondary) {
        Timber.i("behaviour update: progress=%s", progress);
        float realProgress = isReverse ? 1 - progress : progress;
        Timber.d("real progress: %s", realProgress * 100);
        int x0 = secondary.getMeasuredWidth() * (isReverse ? -1 : 1);
        int x1 = 0;
        int x2 = primary.getMeasuredWidth() * (isReverse ? 1 : -1);
        primary.setTranslationX(x1 + realProgress * (x2 - x1));
        primary.setTranslationY(0);
        secondary.setTranslationX(x0 + realProgress * (x1 - x0));
        secondary.setTranslationY(0);
    }

    @Override
    public void onEnd(boolean isReverse, ImageView primary, ImageView secondary) {
        Timber.i("behaviour end: reverse=%s", isReverse);
        int prevIdx = mView.getPreviousIndex();
        int nextIdx = mView.getNextIndex();
        mView.setCurrentIndex(isReverse ? prevIdx : nextIdx);
        onReset(primary, secondary);
    }

    @Override
    public void onCancel(ImageView primary, ImageView secondary) {
        Timber.i("behaviour cancel");
    }
}
