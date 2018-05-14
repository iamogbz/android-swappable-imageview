package com.ogbizi.formable;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Custom view to handle animating between image sources
 */
public class SwappableImageView extends RelativeLayout {

    private boolean isReversing = false;
    private boolean shouldLoop = false;
    private int currentIndex = -1;
    private ImageView toHide;
    private ImageView toShow;
    private final List<Integer> mDrawables = new LinkedList<>();
    private ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

    private Behavior mBehaviour = new Behavior() {
        @Override
        public void onReset(ImageView toHide, ImageView toShow) {
            System.out.println("behaviour reset");
            System.out.println(String.format("%s", mDrawables));
            toHide.setImageResource(mDrawables.get(getPreviousIndex()));
            toShow.setImageResource(mDrawables.get(currentIndex));
            toHide.setTranslationX(-getMeasuredWidth());
            toShow.setTranslationX(0);
        }

        @Override
        public void onStart(boolean isReverse, ImageView toHide, ImageView toShow) {
            System.out.println("behaviour start");
        }

        @Override
        public void onUpdate(float progress, boolean isReverse, ImageView toHide, ImageView toShow) {
            System.out.println("behaviour update");
        }

        @Override
        public void onEnd(boolean isReverse, ImageView toHide, ImageView toShow) {
            System.out.println("behaviour end");
        }

        @Override
        public void onCancel(ImageView toHide, ImageView toShow) {
            System.out.println("behaviour cancel");
        }
    };

    public SwappableImageView(Context context) {
        this(context, null);
    }

    public SwappableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwappableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context, attrs);
    }

    private void setUp(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwappableImageView, 0, 0);
        try {
            setNext(a.getResourceId(R.styleable.SwappableImageView_src, 0));
            setPrevious(a.getResourceId(R.styleable.SwappableImageView_prevSrc, 0));
            setNext(a.getResourceId(R.styleable.SwappableImageView_nextSrc, 0));
            shouldLoop = a.getBoolean(R.styleable.SwappableImageView_loop, false);
        } finally {
            a.recycle();
        }
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
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        toHide = new ImageView(context);
        toShow = new ImageView(context);
        addView(toHide, layoutParams);
        addView(toShow, layoutParams);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        System.out.println("on layout");
        super.onLayout(changed, l, t, r, b);
        mBehaviour.onReset(toHide, toShow);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("on measure");
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
    }

    /**
     * Notify callbacks of the messages defined
     *
     * @param message the message passed
     */
    private void dispatch(MESSAGE message) {
        switch (message) {
            case END:
                mBehaviour.onEnd(isReversing, toHide, toShow);
                break;
            case START:
                mBehaviour.onStart(isReversing, toHide, toShow);
                break;
            case CANCEL:
                mBehaviour.onCancel(toHide, toShow);
                break;
            case RESET:
                mBehaviour.onReset(toHide, toShow);
                break;
            case UPDATE:
                mBehaviour.onUpdate(animator.getAnimatedFraction(), isReversing, toHide, toShow);
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
        currentIndex = Math.max(0, Math.min(index, mDrawables.size() - 1));
        dispatch(MESSAGE.RESET);
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
     * Get the index of the next drawable
     *
     * @return the currentIndex + 1 or wrap around max if looping
     */
    private int getNextIndex() {
        int nextIndex = currentIndex + 1;
        if (nextIndex < mDrawables.size()) {
            return nextIndex;
        } else if (isLooping()) {
            return 0;
        }
        return currentIndex;
    }

    /**
     * Get the index of the previous drawable
     *
     * @return the currentIndex - 1 or wrap around min if looping
     */
    private int getPreviousIndex() {
        int prevIndex = currentIndex - 1;
        if (prevIndex >= 0) {
            return prevIndex;
        } else if (isLooping()) {
            return mDrawables.size() - 1;
        }
        return currentIndex;
    }

    /**
     * Set the next image view drawable to show
     *
     * @param drawableRes the drawable to show
     */
    public void setNext(@DrawableRes int drawableRes) {
        System.out.println(String.format("next: %s", drawableRes));
        if (drawableRes != 0) {
            mDrawables.add(currentIndex + 1, drawableRes);
            currentIndex = Math.max(0, currentIndex);
            System.out.println(String.format("current: %s => %s", currentIndex, mDrawables));
        }
    }

    /**
     * Start the act of showing the next drawable
     *
     * @param force true to force a start
     */
    public void showNext(boolean force) {
        if (animator.isStarted() || force) {
            int nextIndex = getNextIndex();
            if (nextIndex != currentIndex) {
                isReversing = false;
                toHide.setImageResource(mDrawables.get(currentIndex));
                toShow.setImageResource(mDrawables.get(nextIndex));
                animator.start();
            }
        }
    }

    /**
     * Helper function to set the next and start the swap animation
     *
     * @param drawableRes the drawable to show next
     * @param start pass true to immediately start the animation
     */
    public void next(@DrawableRes int drawableRes, boolean start) {
        setNext(drawableRes);
        if (start) showNext(false);
    }

    /**
     * Set the previous image view drawable to show from the current index
     *
     * @param drawableRes the drawable to set as previous
     */
    public void setPrevious(@DrawableRes int drawableRes) {
        System.out.println(String.format("previous: %s", drawableRes));
        if (drawableRes != 0) {
            mDrawables.add(Math.max(0, currentIndex), drawableRes);
            currentIndex += 1;
            System.out.println(String.format("current: %s => %s", currentIndex, mDrawables));
        }
    }

    /**
     * Start the reverse action of showing the previous drawable
     *
     * @param force true to force an animation start
     */
    public void showPrevious(boolean force) {
        if (animator.isStarted() || force) {
            int prevIndex = getPreviousIndex();
            if (prevIndex != currentIndex) {
                isReversing = true;
                toHide.setImageResource(mDrawables.get(currentIndex));
                toShow.setImageResource(mDrawables.get(currentIndex - 1));
                animator.reverse();
                previous(currentIndex - 1, true);
            }
        }
    }

    /**
     * Helper function to combine setting previous and start the animation
     *
     * @param drawableRes the drawable to show previously
     * @param start pass true to immediately start the animation
     */
    public void previous(@DrawableRes int drawableRes, boolean start) {
        setPrevious(drawableRes);
        if (start) showPrevious(false);
    }

    /**
     * The swappable image view behaviour
     */
    public interface Behavior {
        /**
         * Reset the state of the swap image views. Should be called when layout
         * changes occur.
         *
         * @param toHide the image view to replace
         * @param toShow the image view to display
         */
        void onReset(ImageView toHide, ImageView toShow);

        /**
         * Called at the start of the swap
         *
         * @param isReverse if the swap is happening in reverse
         * @param toHide the image view being replaced
         * @param toShow the image view to be displayed
         */
        void onStart(boolean isReverse, ImageView toHide, ImageView toShow);

        /**
         * Update the swapping process with progress
         *
         * @param progress the current progress of the swap as float between 0..1
         * @param isReverse if true then the swap is playing from completion to onStart
         * @param toHide the image view to replace
         * @param toShow the image view to display
         */
        void onUpdate(float progress, boolean isReverse, ImageView toHide, ImageView toShow);

        /**
         * Called when the swapping is completed
         *
         * @param isReverse if the swap just happened in reverse as with #showPrevious
         * @param toHide the image view just replaced
         * @param toShow the image view now displayed
         */
        void onEnd(boolean isReverse, ImageView toHide, ImageView toShow);

        /**
         * Called when the swap is cancelled
         *
         * @param toHide the image view being replaced
         * @param toShow the image view being displayed
         */
        void onCancel(ImageView toHide, ImageView toShow);
    }

    /**
     * Swap behaviour callback message
     */
    private enum MESSAGE {
        START, END, CANCEL, RESET, UPDATE
    }
}
