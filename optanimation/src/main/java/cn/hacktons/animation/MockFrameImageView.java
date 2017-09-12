package cn.hacktons.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Simple ImageView wrapper to use {@link LazyAnimationDrawable} from xml
 * <h2>Usage</h2>
 * <pre>
 * {@code <cn.hacktons.animation.MockFrameImageView
 *      android:id="@+id/imageview"
 *      android:layout_width="200dp"
 *      android:layout_height="200dp"
 *      android:layout_gravity="center"
 *      app:cache_percent="0.4"
 *      app:src="@drawable/loading_animation"/>
 *
 *      ImageView imageView = (ImageView) findViewById(R.id.imageview);
 *      drawable = (LazyAnimationDrawable) imageView.getDrawable();
 *      // start animation
 *      drawable.start();
 *      // stop animation
 *      drawable.stop();
 * }
 * </pre>
 */
public class MockFrameImageView extends ImageView {

    public MockFrameImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public MockFrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MockFrameImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MockFrameImageView,
            defStyle, 0);
        if (isInEditMode()) {
            Drawable drawable = a.getDrawable(R.styleable.MockFrameImageView_src);
            setImageDrawable(drawable);
            a.recycle();
            return;
        }
        int size = a.getInt(R.styleable.MockFrameImageView_cache_size, 0);
        float percent = a.getFloat(R.styleable.MockFrameImageView_cache_percent, 0.4f);
        Drawable drawable = AnimationDrawableCompat.getDrawable(getResources(), a, 0);
        a.recycle();
        if (drawable instanceof LazyAnimationDrawable) {
            if (size == 0) {
                size = (int) (((LazyAnimationDrawable) drawable).getFrameCount() * percent);
            }
            ((LazyAnimationDrawable) drawable).setCacheSize(size);
            ((LazyAnimationDrawable) drawable).attachTo(this);
        }
    }
}
