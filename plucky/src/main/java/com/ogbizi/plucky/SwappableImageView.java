package com.ogbizi.plucky;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * Custom view to handle animating between image sources
 */
public class SwappableImageView extends RelativeLayout {

    private boolean isReversing = false;
    private boolean shouldLoop = false;
    private int currentIndex = -1;
    private ImageView primary;
    private ImageView secondary;
    private final List<Integer> mDrawables = new LinkedList<>();
    private ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    private Behavior mBehaviour;

    public SwappableImageView(Context context) {
        this(context, null);
    }

    public SwappableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwappableImageView(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Complete view setup
     *
     * @param context the view context
     * @param attrs the style attributes from xml
     */
    private void init(Context context, AttributeSet attrs) {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        if (attrs != null) initAttributes(context, attrs);
        initListeners();
        initViews(context);
    }

    /**
     * Set up the initial config passes in from xml
     *
     * @param context the view context
     * @param attrs the style attributes
     */
    protected void initAttributes(Context context,
                                  @NonNull AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                                                                 R.styleable.SwappableImageView,
                                                                 0, 0);
        try {
            setNext(a.getResourceId(R.styleable.SwappableImageView_src, 0));
            setPrevious(
                    a.getResourceId(R.styleable.SwappableImageView_prevSrc, 0));
            setNext(a.getResourceId(R.styleable.SwappableImageView_nextSrc, 0));
            shouldLoop = a.getBoolean(R.styleable.SwappableImageView_loop,
                                      false);
            Timber.d("looping: %s", shouldLoop);
        } finally {
            a.recycle();
        }
    }

    /**
     * Set up the animation listeners
     */
    protected void initListeners() {
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatch(MESSAGE.START);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dispatch(MESSAGE.END);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                dispatch(MESSAGE.CANCEL);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                throw new UnsupportedOperationException("animator repeat");
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dispatch(MESSAGE.UPDATE);
            }
        });
    }

    /**
     * Set up the views used for swapping behaviour
     *
     * @param context the view context
     */
    protected void initViews(Context context) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                                                     LayoutParams.MATCH_PARENT);
        primary = new ImageView(context);
        secondary = new ImageView(context);
        addView(primary, layoutParams);
        addView(secondary, layoutParams);
        setBehavior(new SwappableImageBehavior());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Timber.i("on layout");
        super.onLayout(changed, l, t, r, b);
        mBehaviour.onReset(primary, secondary);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Timber.i("on measure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Set the swapping behaviour
     *
     * @param callback the callback used to implement behaviour
     */
    public void setBehavior(Behavior callback) {
        mBehaviour = callback;
        dispatch(MESSAGE.ATTACH);
    }

    /**
     * Notify callbacks of the messages defined
     *
     * @param message the message passed
     */
    private void dispatch(MESSAGE message) {
        switch (message) {
            case END:
                mBehaviour.onEnd(isReversing, primary, secondary);
                break;
            case START:
                mBehaviour.onStart(isReversing, primary, secondary);
                break;
            case CANCEL:
                mBehaviour.onCancel(primary, secondary);
                break;
            case RESET:
                mBehaviour.onReset(primary, secondary);
                break;
            case UPDATE:
                mBehaviour.onUpdate(animator.getAnimatedFraction(), isReversing,
                                    primary, secondary);
                break;
            case ATTACH:
                mBehaviour.onAttach(this);
                break;
        }
    }

    /**
     * Set the ordered list of drawables used for selecting next and previous
     *
     * @param index the position to reset to
     * @param drawables the list of drawables use
     */
    public void setDrawables(int index, @DrawableRes Integer... drawables) {
        mDrawables.clear();
        mDrawables.addAll(Arrays.asList(drawables));
        setCurrentIndex(index);
    }

    /**
     * Get the list of drawable resource ids used in swapping
     *
     * @return the array list of drawables
     */
    public List getDrawables() {
        return mDrawables;
    }

    /**
     * Set the next image view drawable to show
     *
     * @param drawableRes the drawable to show
     */
    public void setNext(@DrawableRes int drawableRes) {
        Timber.i("next: %s", drawableRes);
        if (drawableRes != 0) {
            mDrawables.add(currentIndex + 1, drawableRes);
            currentIndex = Math.max(0, currentIndex);
            Timber.d("current: %s => %s", currentIndex, mDrawables);
        }
    }

    /**
     * Set the previous image view drawable to show from the current index
     *
     * @param drawableRes the drawable to set as previous
     */
    public void setPrevious(@DrawableRes int drawableRes) {
        Timber.i("previous: %s", drawableRes);
        if (drawableRes != 0) {
            mDrawables.add(Math.max(0, currentIndex), drawableRes);
            currentIndex += 1;
            Timber.d("current: %s => %s", currentIndex, mDrawables);
        }
    }

    /**
     * Set if swapping should loop over list of drawables
     *
     * @param loop true to wrap around end of list
     */
    public void setLooping(boolean loop) {
        shouldLoop = loop;
    }

    /**
     * Get if swapping should loop over list of drawables
     *
     * @return true if the looping is turned on
     */
    public boolean isLooping() {
        return shouldLoop;
    }

    /**
     * Limit value to bound range.
     * Example #bound(-4, 0, 10) = 0
     *
     * @param value the value to limit
     * @param min the minimum value allowed
     * @param max the maximum value allowed
     * @return the range limited value
     */
    private static int bound(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Wraps an integer value around a range
     * Example #wrap(-4, 0, 10) = 10
     *
     * @param value the value to wrap
     * @param min the minimum value allowed
     * @param max the maximum value allowed
     * @return the range limited value
     */
    private static int wrap(int value, int min, int max) {
        return value < min ? max : value > max ? min : value;
    }

    /**
     * Get the current index of images
     *
     * @return the current displayed image index
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Set the current image to be displayed by index
     *
     * @param index the drawable index
     */
    public void setCurrentIndex(int index) {
        currentIndex = bound(index, 0, mDrawables.size() - 1);
        dispatch(MESSAGE.RESET);
    }

    /**
     * Get the index of the next drawable
     *
     * @return the currentIndex + 1 or wrap around max if looping
     */
    public int getNextIndex() {
        Timber.i("get next: looping=%s", shouldLoop);
        int min = 0;
        int max = mDrawables.size() - 1;
        int index = currentIndex + 1;
        int nextIndex = isLooping() ? wrap(index, min, max) : bound(index, min,
                                                                    max);
        Timber.d("previous: %s, current: %s", nextIndex, currentIndex);
        return nextIndex;
    }

    /**
     * Get the index of the previous drawable
     *
     * @return the currentIndex - 1 or wrap around min if looping
     */
    public int getPreviousIndex() {
        Timber.i("get previous: looping=%s", shouldLoop);
        int min = 0;
        int max = mDrawables.size() - 1;
        int index = currentIndex - 1;
        int prevIndex = isLooping() ? wrap(index, min, max) : bound(index, min,
                                                                    max);
        Timber.d("previous: %s, current: %s", prevIndex, currentIndex);
        return prevIndex;
    }

    /**
     * Start the act of showing the next drawable
     *
     * @param force true to force a start
     */
    public void showNext(boolean force) {
        Timber.i("show next: force=%s", force);
        if (!animator.isStarted() || force) {
            int nextIndex = getNextIndex();
            if (nextIndex != currentIndex) {
                isReversing = false;
                primary.setImageResource(mDrawables.get(currentIndex));
                secondary.setImageResource(mDrawables.get(nextIndex));
                animator.start();
            }
        }
    }

    /**
     * Start the reverse action of showing the previous drawable
     *
     * @param force true to force an animation start
     */
    public void showPrevious(boolean force) {
        Timber.i("show previous: force=%s", force);
        if (!animator.isStarted() || force) {
            int prevIndex = getPreviousIndex();
            if (prevIndex != currentIndex) {
                isReversing = true;
                primary.setImageResource(mDrawables.get(currentIndex));
                secondary.setImageResource(mDrawables.get(prevIndex));
                animator.reverse();
            }
        }
    }

    /**
     * The swappable image view behaviour
     */
    public interface Behavior {
        /**
         * Called when behaviour is attached to a view
         *
         * @param view the swappable image view attached
         */
        void onAttach(SwappableImageView view);

        /**
         * Reset the state of the swap image views. Should be called when layout
         * changes occur.
         *
         * @param primary the current image view
         * @param secondary the image view used for swapping
         */
        void onReset(ImageView primary, ImageView secondary);

        /**
         * Called at the start of the swap
         *
         * @param isReverse if the swap is happening in reverse
         * @param primary the image view currently displayed
         * @param secondary the image view to swap in
         */
        void onStart(boolean isReverse, ImageView primary, ImageView secondary);

        /**
         * Update the swapping process with progress
         *
         * @param progress the current progress of the swap as float between 0..1
         * @param isReverse if true then the swap is playing from completion to onStart
         * @param primary the image view currently displayed
         * @param secondary the image view swapping in
         */
        void onUpdate(float progress, boolean isReverse, ImageView primary,
                      ImageView secondary);

        /**
         * Called when the swapping is completed
         *
         * @param isReverse if the swap just happened in reverse as with #showPrevious
         * @param primary the image view just removed from view
         * @param secondary the image view just swapped into view
         */
        void onEnd(boolean isReverse, ImageView primary, ImageView secondary);

        /**
         * Called when the swap is cancelled
         *
         * @param primary the image view that was currently displayed
         * @param secondary the image view that was swapping in
         */
        void onCancel(ImageView primary, ImageView secondary);
    }

    /**
     * Swap behaviour callback message
     */
    private enum MESSAGE {
        ATTACH, START, END, RESET, CANCEL, UPDATE,
    }
}
