package cn.hacktons.animation;


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Based on <a href="https://github.com/tigerjj/FasterAnimationsContainer">FasterAnimationsContainer</>
 * Changes:
 * <ul>
 * <li>fix warning when reuse bitmap with option.in;</li>
 * <li>fix animation frozen issue after Home press</li>
 * <li>{@link FasterAnimationsContainer} is no longer singleton, so each ImageView
 * may control it's animation;</li>
 * <li>global bitmap cache supported;</li>
 * </ul>
 */
public class FasterAnimationsContainer {
    private class AnimationFrame {
        private int mResourceId;
        private int mDuration;

        AnimationFrame(int resourceId, int duration) {
            mResourceId = resourceId;
            mDuration = duration;
        }

        public int getResourceId() {
            return mResourceId;
        }

        public int getDuration() {
            return mDuration;
        }
    }

    // list for all frames of animation
    private ArrayList<AnimationFrame> mFrames;
    // index of current frame
    private int mIndex;
    // true if the animation should continue running. Used to stop the animation
    private boolean mShouldRun;
    // true if the animation prevents starting the animation twice
    private boolean mIsRunning;
    // Used to prevent holding ImageView when it should be dead.
    private SoftReference<ImageView> mImageRef;
    // Handler to communication with UIThread
    private Handler mHandler;
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
            refs.put(key, new WeakReference<Bitmap>(oldValue));
        }

        @Override
        protected Bitmap create(Integer key) {
            WeakReference<Bitmap> reference = refs.get(key);
            return reference != null ? reference.get() : super.create(key);
        }
    };
    private static int maxCacheSize;

    public FasterAnimationsContainer(int maxCachedBitmapCount) {
        init();
        Log.i("LifoCache", "max cache count = " + maxCachedBitmapCount);
        /*
         * should be at least 2 for bitmap decode reuse
         */
        maxCacheSize = maxCachedBitmapCount < 2 ? 2 : maxCachedBitmapCount;
        sharedCache.resize(maxCacheSize);
    }

    public void init() {
        mFrames = new ArrayList<AnimationFrame>();
        mHandler = new Handler();
        mShouldRun = false;
        mIsRunning = false;
        mIndex = -1;
    }

    public FasterAnimationsContainer into(ImageView imageView) {
        mImageRef = new SoftReference<ImageView>(imageView);
        return this;
    }

    /**
     * add a frame of animation
     *
     * @param resId    resource id of drawable
     * @param interval milliseconds
     */
    public void addFrame(int resId, int interval) {
        mFrames.add(new AnimationFrame(resId, interval));
    }

    /**
     * add all frames of animation
     *
     * @param resIds   resource id of drawable
     * @param interval milliseconds
     */
    public FasterAnimationsContainer with(int[] resIds, int interval) {
        removeAllFrames();
        for (int resId : resIds) {
            mFrames.add(new AnimationFrame(resId, interval));
        }
        return this;
    }

    /**
     * clear all frames
     */
    public void removeAllFrames() {
        mFrames.clear();
    }

    private AnimationFrame getNext() {
        mIndex++;
        if (mIndex >= mFrames.size())
            mIndex = 0;
        return mFrames.get(mIndex);
    }

    /**
     * Starts the animation
     */
    public synchronized void start() {
        mShouldRun = true;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mAnimationLoop);
    }

    /**
     * Stops the animation
     */
    public synchronized void stop() {
        mShouldRun = false;
        sharedCache.evictAll();
    }

    private Runnable mAnimationLoop = new Runnable() {

        private AnimationFrame pausedFrame;

        @Override
        public void run() {
            ImageView imageView = mImageRef.get();
            if (!mShouldRun || imageView == null) {
                mIsRunning = false;
                return;
            }
            mIsRunning = true;
            if (imageView.isShown()) {
                AnimationFrame frame = pausedFrame == null ? getNext() : pausedFrame;
                GetImageDrawableTask task = new GetImageDrawableTask(imageView);
                task.execute(frame.getResourceId());
                mHandler.postDelayed(this, frame.getDuration());
                pausedFrame = null;
            } else {
                if (pausedFrame == null) {
                    pausedFrame = getNext();
                    sharedCache.evictAll();
                }
                mHandler.postDelayed(this, pausedFrame.getDuration());
            }
        }
    };

    private class GetImageDrawableTask extends AsyncTask<Integer, Void, Bitmap> {

        private ImageView mImageView;
        private Resources mResource;

        public GetImageDrawableTask(ImageView imageView) {
            mImageView = imageView;
            mResource = mImageView.getResources();
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
                bitmap = BitmapFactory.decodeResource(mResource, resId, options);
                sharedCache.put(resId, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                mImageView.setImageBitmap(result);
            }
        }
    }

}
