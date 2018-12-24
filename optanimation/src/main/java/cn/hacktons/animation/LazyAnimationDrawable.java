package cn.hacktons.animation;


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * LazyAnimationDrawable is similar to AnimationDrawable; It's design for frame animation which
 * has large bitmap. Use {@link AnimationBuilder}/{@link MockFrameImageView} to make new instance
 * of drawable.
 *
 * <h2>{@link LazyAnimationDrawable}</h2>
 * <ul>
 *     <li>Load frame bitmap dynamically to avoid memory overhead</li>
 *     <li>Bitmap cache support, all frame bitmap are cache in the global {@link LifoCache}</li>
 *     <li>Traditional XML definition is supported through {@link MockFrameImageView}</li>
 * </ul>
 *
 * <h2>{@link android.graphics.drawable.AnimationDrawable}</h2>
 * <ul>
 *     <li>Standard API for frame animation, easy to use</li>
 *     <li>Often leads to {@link OutOfMemoryError} since it loads all frames at once</li>
 * </ul>
 *
 */
public class LazyAnimationDrawable extends Drawable implements Runnable, Animatable {

    private ArrayList<AnimationFrame> mFrames = new ArrayList<AnimationFrame>();
    private int mCurFrame = 0;
    private boolean mRunning;
    private boolean mAnimating;
    private boolean mOneShot;
    private Resources mResource;
    /**
     * we use the first bitmap's width & height
     */
    private int mBitmapWidth;
    private int mBitmapHeight;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mCurBitmap;
    private SoftReference<View> mViewRef;

    private AnimateOnCompleteCallback mAnimateOnCompleteCallback;

    private static int maxCacheSize;
    /**
     * strong reference for cache
     */
    private static LifoCache<Integer, Bitmap> sharedCache = new LifoCache<Integer, Bitmap>(4) {
        /**
         * avoid permanent bitmap cache
         */
        SparseArray<WeakReference<Bitmap>> refs = new SparseArray<>(4);

        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            if (evicted) {
                refs.put(key, new WeakReference<Bitmap>(oldValue));
            }
        }

        @Override
        protected Bitmap create(Integer key) {
            WeakReference<Bitmap> reference = refs.get(key);
            return reference != null ? reference.get() : super.create(key);
        }
    };
    private static int[] AnimationDrawable = {
        android.R.attr.visible,
        android.R.attr.oneshot
    };

    private static final int AnimationDrawable_visible = 0;
    private static final int AnimationDrawable_oneshot = 1;

    private static int[] AnimationDrawableItem = {
        android.R.attr.duration,
        android.R.attr.drawable
    };
    private static final int AnimationDrawableItem_duration = 0;
    private static final int AnimationDrawableItem_drawable = 1;

    LazyAnimationDrawable() {
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    int getFrameCount() {
        return mFrames.size();
    }

    /**
     * should be at least 2 for bitmap decode reuse
     *
     * @param maxCachedBitmapCount
     */
    void setCacheSize(int maxCachedBitmapCount) {
        Log.i("LifoCache", "max cache count = " + maxCachedBitmapCount);
        maxCacheSize = maxCachedBitmapCount < 2 ? 2 : maxCachedBitmapCount;
        sharedCache.resize(maxCacheSize);
    }

    void attachTo(@NonNull View imageView) {
        mViewRef = new SoftReference<View>(imageView);
        mResource = imageView.getResources();
        setCallback(imageView);
        inflateFirst(imageView);
    }

    /**
     * Starts the animation
     */
    public void start() {
        mAnimating = true;
        if (!isRunning()) {
            setFrame(0, false, true);
        }
    }

    /**
     * Stops the animation
     */
    public void stop() {
        mAnimating = false;
        sharedCache.evictAll();
        if (isRunning()) {
            unscheduleSelf(this);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Bitmap bitmap = mCurBitmap;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, mPaint);
        }
    }

    private void computeBitmapSize(Bitmap bitmap) {
        if (bitmap != null) {
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
        } else {
            mBitmapWidth = mBitmapHeight = -1;
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    @Deprecated
    @Override
    public void setAlpha(int alpha) {
        Log.e("LifoCache", "setAlpha not supported @" + getClass().getSimpleName());
    }

    @Deprecated
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        Log.e("LifoCache", "setColorFilter not supported @" + getClass().getSimpleName());
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (restart || changed) {
                boolean startFromZero = restart || !mRunning ||
                    mCurFrame >= mFrames.size();
                setFrame(startFromZero ? 0 : mCurFrame, true, mAnimating);
            }
        } else {
            Log.i("LifoCache", "setVisible false: unscheduleSelf");
            unscheduleSelf(this);
        }
        return changed;
    }

    @Override
    public void run() {
        View view = mViewRef != null ? mViewRef.get() : null;
        boolean show = view != null && view.isShown();
        if (show) {
            nextFrame(false);
        } else {
            emptyFrame();
        }
    }

    private void inflateFirst(@NonNull View imageView) {
        if (mFrames.size() > 0) {
            AnimationFrame frame = mFrames.get(0);
            // decode first bitmap on UI thread
            /*BitmapDecodeTask task = new BitmapDecodeTask(imageView);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frame.getResourceId());*/
            Bitmap bitmap = sharedCache.get(frame.getResourceId());
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(mResource, frame.getResourceId(), null);
            }
            sharedCache.put(frame.getResourceId(), bitmap);
            if (bitmap != null) {
                computeBitmapSize(bitmap);
                if (imageView instanceof ImageView) {
                    ((ImageView) imageView).setImageDrawable(LazyAnimationDrawable.this);
                } else {
                    imageView.setBackground(LazyAnimationDrawable.this);
                }
            }
            invalidateSelf();
        }
    }

    void completeCallback(AnimateOnCompleteCallback callback){
        mAnimateOnCompleteCallback = callback;
    }

    void oneShot(boolean oneShot) {
        mOneShot = oneShot;
    }

    private void addFrame(int resId, int duration) {
        mFrames.add(new AnimationFrame(resId, duration));
    }

    /**
     * add all frames of animation
     *
     * @param resIds   resource id of drawable
     * @param duration milliseconds
     */
    void setFrames(int[] resIds, int duration) {
        removeAllFrames();
        for (int resId : resIds) {
            mFrames.add(new AnimationFrame(resId, duration));
        }
    }

    /**
     * clear all frames
     */
    private void removeAllFrames() {
        mFrames.clear();
    }

    private void nextFrame(boolean unschedule) {
        int nextFrame = mCurFrame + 1;
        final int numFrames = mFrames.size();
        boolean oneShot = mOneShot;
        final boolean isLastFrame = oneShot && nextFrame >= (numFrames - 1);
        // loop
        if (!oneShot && nextFrame >= numFrames) {
            nextFrame = 0;
        }
        setFrame(nextFrame, unschedule, !isLastFrame);
    }

    private void emptyFrame() {
        scheduleSelf(this, SystemClock.uptimeMillis() + mFrames.get(mCurFrame).getDuration());
        evictAllCache();
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame >= mFrames.size()) {
            mAnimateOnCompleteCallback.onComplete();
            return;
        }
        mCurFrame = frame;
        mAnimating = animate;
        selectFrame(frame);
        if (unschedule || animate) {
            unscheduleSelf(this);
        }
        if (animate) {
            mCurFrame = frame;
            mRunning = true;
            scheduleSelf(this, SystemClock.uptimeMillis() + mFrames.get(frame).getDuration());
        }
    }

    @Override
    public void invalidateSelf() {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, this, when);
        }
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        mCurFrame = 0;
        mRunning = false;
        Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, this);
        }
    }

    private void selectFrame(int idx) {
        AnimationFrame frame = mFrames.get(idx);
        BitmapDecodeTask task = new BitmapDecodeTask(mResource);
        task.execute(frame.getResourceId());
    }

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        final TypedArray a = obtainAttributes(r, theme, attrs, AnimationDrawable);
        inflateWithAttributes(r, parser, a, AnimationDrawable_visible);
        updateStateFromTypedArray(a);
        a.recycle();

        inflateChildElements(r, parser, attrs, theme);
    }


    @SuppressWarnings("ResourceType")
    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs,
                                      Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;

        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
            && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            final TypedArray a = obtainAttributes(r, theme, attrs, AnimationDrawableItem);

            final int duration = a.getInt(AnimationDrawableItem_duration, -1);
            if (duration < 0) {
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <item> tag requires a 'duration' attribute");
            }

            TypedValue drawableValue = new TypedValue();
            a.getValue(AnimationDrawableItem_drawable, drawableValue);

            if (!TextUtils.isEmpty(drawableValue.string)) {
                String path = drawableValue.string.toString().toLowerCase();
                if (path.endsWith("png") || path.endsWith("jpg") || path.endsWith("jpeg")) {
                    Log.i("MockFrameAnimation", "new drawable found: " + path);
                } else {
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a bitmap 'drawable': " + path);
                }
            }

            a.recycle();
            addFrame(drawableValue.resourceId, duration);
        }
    }

    private boolean isAfterLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @SuppressWarnings("ResourceType")
    private void updateStateFromTypedArray(TypedArray a) {
        mOneShot = a.getBoolean(AnimationDrawable_oneshot, mOneShot);
    }

    /**
     * Obtains styled attributes from the theme, if available, or unstyled
     * resources if the theme is null.
     */
    private TypedArray obtainAttributes(
        Resources res, Resources.Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    /**
     * Inflate a Drawable from an XML resource.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int
        visibleAttr)
        throws XmlPullParserException, IOException {
        boolean visible = attrs.getBoolean(visibleAttr, true);
        setVisible(visible, false);
    }

    /**
     * Drop the decoding job on to async to
     */
    private class BitmapDecodeTask extends AsyncTask<Integer, Void, Bitmap> {

        private Resources mResource;
        private View mView;

        BitmapDecodeTask(Resources resources) {
            mResource = resources;
        }

        BitmapDecodeTask(View imageView) {
            mView = imageView;
            mResource = imageView.getResources();
        }

        @SuppressLint("NewApi")
        @Override
        protected Bitmap doInBackground(Integer... params) {
            int resId = params[0];
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmap = sharedCache.get(resId);
            if (bitmap == null) {
                if (sharedCache.size() >= maxCacheSize) {
                    Integer lastKey = sharedCache.lastKey(2);
                    if (lastKey != null) {
                        Bitmap b = sharedCache.get(lastKey);
                        if (b != null) {
                            options.inBitmap = b;
                        }
                        sharedCache.remove(lastKey);
                    }
                }
                try {
                    bitmap = BitmapFactory.decodeResource(mResource, resId, options);
                    sharedCache.put(resId, bitmap);
                } catch (OutOfMemoryError e) {
                    Log.w("LifoCache", "decode bitmap failed, maybe too large", e);
                    // not instant gc
                    evictAllCache();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                mCurBitmap = result;
                // invalidate so we get a change to draw again
                invalidateSelf();
            }
        }
    }

    /**
     * Clear all cache and notify gc
     */
    private void evictAllCache() {
        sharedCache.evictAll();
        System.gc();
    }

    private class AnimationFrame {
        private int mResourceId;
        private int mDuration;

        AnimationFrame(int resourceId, int duration) {
            mResourceId = resourceId;
            mDuration = duration;
        }

        int getResourceId() {
            return mResourceId;
        }

        int getDuration() {
            return mDuration;
        }
    }
}
