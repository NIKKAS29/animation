package cn.hacktons.animation;

import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Builder to make {@link LazyAnimationDrawable} instance;
 * <h2>Usage</h2>
 * <pre>
 *     {@code private static final int[] IMAGE_RESOURCES = {
 *              R.drawable.num_0,
 *              R.drawable.num_1,
 *              R.drawable.num_2,
 *              R.drawable.num_3,
 *              R.drawable.num_4,
 *              R.drawable.num_5,
 *              R.drawable.num_6,
 *              R.drawable.num_7,
 *              R.drawable.num_8,
 *              R.drawable.num_9,
 *              R.drawable.num_a,
 *              R.drawable.num_b,
 *              R.drawable.num_c,
 *              R.drawable.num_d,
 *              R.drawable.num_e,
 *              R.drawable.num_f,
 *       };
 *      LazyAnimationDrawable drawable = new AnimationBuilder()
 *          .frames(IMAGE_RESOURCES, 120)
 *          .cachePercent(0.4f)
 *          .oneShot(false)
 *          .into(imageView);
 *
 *     // start animation
 *     drawable.start();
 *     // stop animation
 *     drawable.stop();
 * }
 * </pre>
 * <p>
 * Created by chaobin on 9/11/17.
 */
public class AnimationBuilder {
    private int[] frames;
    private int duration = 1000 / 30;
    private int cacheSize = 0;
    private boolean oneShot = false;
    private float percent = 0.69f;

    /**
     * set animation frames with duration
     *
     * @param frames   animation frames
     * @param duration animation duration, duration should not be smaller than 1000/30
     * @return
     */
    public AnimationBuilder frames(@DrawableRes int[] frames, @IntRange(from = 1000 / 30) int
        duration) {
        this.frames = frames;
        this.duration = duration;
        return this;
    }

    /**
     * set cache size, the size is referred as bitmap count not byte size;<br>
     * you may also use {@link #cachePercent(float)} to calculate size automatically
     *
     * @param size cache should be at least 2
     * @return
     * @see #cachePercent(float)
     */
    public AnimationBuilder cacheSize(@IntRange(from = 2) int size) {
        this.cacheSize = size;
        return this;
    }

    /**
     * set cache percent for convenient, (cache size) = frames * percent
     *
     * @param percent recommend percent should be 0.4~1.0
     * @return
     * @see #cacheSize(int)
     */
    public AnimationBuilder cachePercent(@FloatRange(from = 0.4, to = 1.0) float percent) {
        this.percent = percent;
        return this;
    }

    /**
     * set animation type, oneshot or loop
     *
     * @param oneShot false if the animation should loop
     * @return
     */
    public AnimationBuilder oneShot(boolean oneShot) {
        this.oneShot = oneShot;
        return this;
    }

    /**
     * set target view
     *
     * @param view target view
     * @return
     */
    public LazyAnimationDrawable into(@NonNull View view) {
        if (cacheSize <= 0) {
            cacheSize = (int) (frames.length * percent);
        }

        LazyAnimationDrawable animation = new LazyAnimationDrawable();
        animation.setCacheSize(cacheSize);
        animation.setFrames(frames, duration);
        animation.oneShot(oneShot);
        animation.attachTo(view);
        return animation;
    }
}
