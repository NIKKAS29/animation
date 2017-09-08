package cn.hacktons.animation;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.URLUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by chaobin on 9/5/17.
 */
public class MFAnimationDrawable extends Drawable implements Drawable.Callback, Runnable, Animatable {
    private static final boolean DEBUG = false;
    private static final String TAG = "MFAnimationDrawable";
    private AnimationState mAnimationState;
    private Rect mHotspotBounds;
    private Drawable mCurrDrawable;
    private Drawable mLastDrawable;
    private int mAlpha = 0xFF;

    /**
     * Whether setAlpha() has been called at least once.
     */
    private boolean mHasAlpha;

    private int mCurIndex = -1;
    private int mLastIndex = -1;
    private boolean mMutated;

    // Animations.
    private Runnable mAnimationRunnable;
    private long mEnterAnimationEnd;
    private long mExitAnimationEnd;

    /**
     * The current frame, ranging from 0 to {@link #mAnimationState#getChildCount() - 1}
     */
    private int mCurFrame = 0;

    /**
     * Whether the drawable has an animation callback posted.
     */
    private boolean mRunning;

    /**
     * Whether the drawable should animate when visible.
     */
    private boolean mAnimating;

    public MFAnimationDrawable() {
        this(null, null);
    }

    /**
     * Sets whether this AnimationDrawable is visible.
     * <p>
     * When the drawable becomes invisible, it will pause its animation. A
     * subsequent change to visible with <code>restart</code> set to true will
     * restart the animation from the first frame. If <code>restart</code> is
     * false, the animation will resume from the most recent frame.
     *
     * @param visible true if visible, false otherwise
     * @param restart when visible, true to force the animation to restart
     *                from the first frame
     * @return true if the new visibility is different than its previous state
     */
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (restart || changed) {
                boolean startFromZero = restart || !mRunning ||
                    mCurFrame >= mAnimationState.getChildCount();
                setFrame(startFromZero ? 0 : mCurFrame, true, mAnimating);
            }
        } else {
            unscheduleSelf(this);
        }
        return changed;
    }

    /**
     * Starts the animation, looping if necessary. This method has no effect
     * if the animation is running.
     * <p>
     * <strong>Note:</strong> Do not call this in the
     * {@link android.app.Activity#onCreate} method of your activity, because
     * the {@link AnimationDrawable} is not yet fully attached to the window.
     * If you want to play the animation immediately without requiring
     * interaction, then you might want to call it from the
     * {@link android.app.Activity#onWindowFocusChanged} method in your
     * activity, which will get called when Android brings your window into
     * focus.
     *
     * @see #isRunning()
     * @see #stop()
     */
    @Override
    public void start() {
        mAnimating = true;

        if (!isRunning()) {
            // Start from 0th frame.
            setFrame(0, false, mAnimationState.getChildCount() > 1
                || !mAnimationState.mOneShot);
        }
    }

    /**
     * Stops the animation. This method has no effect if the animation is not
     * running.
     *
     * @see #isRunning()
     * @see #start()
     */
    @Override
    public void stop() {
        mAnimating = false;

        if (isRunning()) {
            unscheduleSelf(this);
        }
    }

    /**
     * Indicates whether the animation is currently running or not.
     *
     * @return true if the animation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * This method exists for implementation purpose only and should not be
     * called directly. Invoke {@link #start()} instead.
     *
     * @see #start()
     */
    @Override
    public void run() {
        nextFrame(false);
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        mCurFrame = 0;
        mRunning = false;
        super.unscheduleSelf(what);
    }

    /**
     * @return The number of frames in the animation
     */
    public int getNumberOfFrames() {
        return mAnimationState.getChildCount();
    }

    /**
     * @return The Drawable at the specified frame index
     */
    public Drawable getFrame(int index) {
        return mAnimationState.getChild(index);
    }

    /**
     * @return The duration in milliseconds of the frame at the
     * specified index
     */
    public int getDuration(int i) {
        return mAnimationState.mDurations[i];
    }

    /**
     * @return True of the animation will play once, false otherwise
     */
    public boolean isOneShot() {
        return mAnimationState.mOneShot;
    }

    /**
     * Sets whether the animation should play once or repeat.
     *
     * @param oneShot Pass true if the animation should only play once
     */
    public void setOneShot(boolean oneShot) {
        mAnimationState.mOneShot = oneShot;
    }

    /**
     * Adds a frame to the animation
     *
     * @param frame    The frame to add
     * @param duration How long in milliseconds the frame should appear
     */
    public void addFrame(/*@NonNull*/ Drawable frame, int duration) {
        mAnimationState.addFrame(frame, duration);
        if (!mRunning) {
            setFrame(0, true, false);
        }
    }

    private void nextFrame(boolean unschedule) {
        int nextFrame = mCurFrame + 1;
        final int numFrames = mAnimationState.getChildCount();
        final boolean isLastFrame = mAnimationState.mOneShot && nextFrame >= (numFrames - 1);

        // Loop if necessary. One-shot animations should never hit this case.
        if (!mAnimationState.mOneShot && nextFrame >= numFrames) {
            nextFrame = 0;
        }

        setFrame(nextFrame, unschedule, !isLastFrame);
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame >= mAnimationState.getChildCount()) {
            return;
        }
        mAnimating = animate;
        mCurFrame = frame;
        selectDrawable(frame);
        if (unschedule || animate) {
            unscheduleSelf(this);
        }
        if (animate) {
            // Unscheduling may have clobbered these values; restore them
            mCurFrame = frame;
            mRunning = true;
            scheduleSelf(this, SystemClock.uptimeMillis() + mAnimationState.mDurations[frame]);
        }
    }

    /**
     * Obtains styled attributes from the theme, if available, or unstyled
     * resources if the theme is null.
     */
    static TypedArray obtainAttributes(
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
    void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr)
        throws XmlPullParserException, IOException {
        boolean visible = attrs.getBoolean(visibleAttr, true);
        setVisible(visible, false);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme)
        throws XmlPullParserException, IOException {
        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.MFAnimationDrawable);
        inflateWithAttributes(r, parser, a, R.styleable.MFAnimationDrawable_visible);
        updateStateFromTypedArray(a);
        a.recycle();

        inflateChildElements(r, parser, attrs, theme);

        setFrame(0, true, false);
    }


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

            final TypedArray a = obtainAttributes(r, theme, attrs,
                R.styleable.MFAnimationDrawableItem);

            final int duration = a.getInt(R.styleable
                .MFAnimationDrawableItem_duration, -1);
            if (duration < 0) {
                throw new XmlPullParserException(parser.getPositionDescription()
                    + ": <item> tag requires a 'duration' attribute");
            }

            Drawable dr = null;
            TypedValue drawableValue = new TypedValue();
            a.getValue(R.styleable.MFAnimationDrawableItem_drawable, drawableValue);

            if (!TextUtils.isEmpty(drawableValue.string)) {
                String path = drawableValue.string.toString().toLowerCase();
                if (path.endsWith("png") || path.endsWith("jpg") || path.endsWith("jpeg")) {
                    if (isAfterLollipop()) {
                        dr = r.getDrawable(drawableValue.resourceId, theme);
                    } else {
                        dr = r.getDrawable(drawableValue.resourceId);
                    }
                    Log.i("Animation", path + ", id=" + Integer.toHexString(drawableValue
                        .resourceId) + ", drawable=" + dr);
                }
            }
            if (dr == null) {
                dr = a.getDrawable(R.styleable.MFAnimationDrawableItem_drawable);
            }
            a.recycle();

            if (dr == null) {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                    // Empty
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(parser.getPositionDescription()
                        + ": <item> tag requires a 'drawable' attribute or child tag"
                        + " defining a drawable");
                }
                if (isAfterLollipop()) {
                    dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                } else {
                    dr = Drawable.createFromXmlInner(r, parser, attrs);
                }
            }
            mAnimationState.addFrame(dr, duration);
            if (dr != null) {
                dr.setCallback(this);
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        mAnimationState.mOneShot = a.getBoolean(
            R.styleable.MFAnimationDrawable_oneshot, mAnimationState.mOneShot);
    }


    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mAnimationState.mutate();
            mMutated = true;
        }
        return this;
    }

    AnimationState cloneConstantState() {
        return new AnimationState(mAnimationState, this, null);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurrDrawable != null) {
            mCurrDrawable.draw(canvas);
        }
        if (mLastDrawable != null) {
            mLastDrawable.draw(canvas);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
            | mAnimationState.getChangingConfigurations();
    }

    @Override
    public void setAlpha(int alpha) {
        if (!mHasAlpha || mAlpha != alpha) {
            mHasAlpha = true;
            mAlpha = alpha;
            if (mCurrDrawable != null) {
                if (mEnterAnimationEnd == 0) {
                    mCurrDrawable.mutate().setAlpha(alpha);
                } else {
                    animate(false);
                }
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // TODO not support
    }

    @TargetApi(21)
    @Override
    public void setHotspot(float x, float y) {
        if (mCurrDrawable != null) {
            mCurrDrawable.setHotspot(x, y);
        }
    }

    @TargetApi(21)
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        if (mHotspotBounds == null) {
            mHotspotBounds = new Rect(left, top, right, bottom);
        } else {
            mHotspotBounds.set(left, top, right, bottom);
        }

        if (mCurrDrawable != null) {
            mCurrDrawable.setHotspotBounds(left, top, right, bottom);
        }
    }

    @Override
    public void getHotspotBounds(Rect outRect) {
        if (mHotspotBounds != null) {
            outRect.set(mHotspotBounds);
        } else {
            super.getHotspotBounds(outRect);
        }
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mLastDrawable != null) {
            return mLastDrawable.setState(state);
        }
        if (mCurrDrawable != null) {
            return mCurrDrawable.setState(state);
        }
        return false;
    }

    @Override
    protected boolean onLevelChange(int level) {
        if (mLastDrawable != null) {
            return mLastDrawable.setLevel(level);
        }
        if (mCurrDrawable != null) {
            return mCurrDrawable.setLevel(level);
        }
        return false;
    }

    @Override
    public int getIntrinsicWidth() {
        if (mAnimationState.isConstantSize()) {
            return mAnimationState.getConstantWidth();
        }
        return mCurrDrawable != null ? mCurrDrawable.getIntrinsicWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        if (mAnimationState.isConstantSize()) {
            return mAnimationState.getConstantHeight();
        }
        return mCurrDrawable != null ? mCurrDrawable.getIntrinsicHeight() : -1;
    }

    @Override
    public int getMinimumWidth() {
        if (mAnimationState.isConstantSize()) {
            return mAnimationState.getConstantMinimumWidth();
        }
        return mCurrDrawable != null ? mCurrDrawable.getMinimumWidth() : 0;
    }

    @Override
    public int getMinimumHeight() {
        if (mAnimationState.isConstantSize()) {
            return mAnimationState.getConstantMinimumHeight();
        }
        return mCurrDrawable != null ? mCurrDrawable.getMinimumHeight() : 0;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (who == mCurrDrawable && getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (who == mCurrDrawable && getCallback() != null) {
            getCallback().scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (who == mCurrDrawable && getCallback() != null) {
            getCallback().unscheduleDrawable(this, what);
        }
    }

    @Override
    public int getOpacity() {
        return mCurrDrawable == null || !mCurrDrawable.isVisible() ? PixelFormat.TRANSPARENT :
            PixelFormat.OPAQUE;
    }

    public boolean selectDrawable(int idx) {
        if (idx == mCurIndex) {
            return false;
        }

        final long now = SystemClock.uptimeMillis();

        if (DEBUG) android.util.Log.i(TAG, toString() + " from " + mCurIndex + " to " + idx
            + ": exit=" + mAnimationState.mExitFadeDuration
            + " enter=" + mAnimationState.mEnterFadeDuration);

        if (mAnimationState.mExitFadeDuration > 0) {
            if (mLastDrawable != null) {
                mLastDrawable.setVisible(false, false);
            }
            if (mCurrDrawable != null) {
                mLastDrawable = mCurrDrawable;
                mLastIndex = mCurIndex;
                mExitAnimationEnd = now + mAnimationState.mExitFadeDuration;
            } else {
                mLastDrawable = null;
                mLastIndex = -1;
                mExitAnimationEnd = 0;
            }
        } else if (mCurrDrawable != null) {
            mCurrDrawable.setVisible(false, false);
        }

        if (idx >= 0 && idx < mAnimationState.mNumChildren) {
            final Drawable d = mAnimationState.getChild(idx);
            mCurrDrawable = d;
            mCurIndex = idx;
            if (d != null) {
                if (mAnimationState.mEnterFadeDuration > 0) {
                    mEnterAnimationEnd = now + mAnimationState.mEnterFadeDuration;
                }
                initializeDrawableForDisplay(d);
            }
        } else {
            mCurrDrawable = null;
            mCurIndex = -1;
        }

        if (mEnterAnimationEnd != 0 || mExitAnimationEnd != 0) {
            if (mAnimationRunnable == null) {
                mAnimationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        animate(true);
                        invalidateSelf();
                    }
                };
            } else {
                unscheduleSelf(mAnimationRunnable);
            }
            // Compute first frame and schedule next animation.
            animate(true);
        }

        invalidateSelf();

        return true;
    }

    /**
     * Initializes a drawable for display in this container.
     *
     * @param d The drawable to initialize.
     */
    private void initializeDrawableForDisplay(Drawable d) {
        d.mutate();

        if (mAnimationState.mEnterFadeDuration <= 0 && mHasAlpha) {
            d.setAlpha(mAlpha);
        }

        d.setVisible(isVisible(), true);
        d.setState(getState());
        d.setLevel(getLevel());
        d.setBounds(getBounds());

        final Rect hotspotBounds = mHotspotBounds;
        if (hotspotBounds != null && isAfterLollipop()) {
            d.setHotspotBounds(hotspotBounds.left, hotspotBounds.top,
                hotspotBounds.right, hotspotBounds.bottom);
        }
    }

    private boolean isAfterLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    void animate(boolean schedule) {
        mHasAlpha = true;

        final long now = SystemClock.uptimeMillis();
        boolean animating = false;
        if (mCurrDrawable != null) {
            if (mEnterAnimationEnd != 0) {
                if (mEnterAnimationEnd <= now) {
                    mCurrDrawable.mutate().setAlpha(mAlpha);
                    mEnterAnimationEnd = 0;
                } else {
                    int animAlpha = (int) ((mEnterAnimationEnd - now) * 255)
                        / mAnimationState.mEnterFadeDuration;
                    if (DEBUG) android.util.Log.i(TAG, toString() + " cur alpha " + animAlpha);
                    mCurrDrawable.mutate().setAlpha(((255 - animAlpha) * mAlpha) / 255);
                    animating = true;
                }
            }
        } else {
            mEnterAnimationEnd = 0;
        }
        if (mLastDrawable != null) {
            if (mExitAnimationEnd != 0) {
                if (mExitAnimationEnd <= now) {
                    mLastDrawable.setVisible(false, false);
                    mLastDrawable = null;
                    mLastIndex = -1;
                    mExitAnimationEnd = 0;
                } else {
                    int animAlpha = (int) ((mExitAnimationEnd - now) * 255)
                        / mAnimationState.mExitFadeDuration;
                    if (DEBUG) android.util.Log.i(TAG, toString() + " last alpha " + animAlpha);
                    mLastDrawable.mutate().setAlpha((animAlpha * mAlpha) / 255);
                    animating = true;
                }
            }
        } else {
            mExitAnimationEnd = 0;
        }

        if (schedule && animating) {
            scheduleSelf(mAnimationRunnable, now + 1000 / 60);
        }
    }

    @Override
    public Drawable getCurrent() {
        return mCurrDrawable;
    }

    @Override
    public ConstantState getConstantState() {
        return mAnimationState;
    }

    protected void setConstantState(AnimationState state) {
        mAnimationState = state;

        // The locally cached drawables may have changed.
        if (mCurIndex >= 0) {
            mCurrDrawable = state.getChild(mCurIndex);
            if (mCurrDrawable != null) {
                initializeDrawableForDisplay(mCurrDrawable);
            }
        }

        // Clear out the last drawable. We don't have enough information to
        // propagate local state from the past.
        mLastIndex = -1;
        mLastDrawable = null;
    }

    private final static class AnimationState extends ConstantState {
        final MFAnimationDrawable mOwner;
        final Resources mRes;
        Drawable[] mDrawables;
        int mNumChildren;
        boolean mConstantSize = false;
        boolean mComputedConstantSize;
        int mConstantWidth;
        int mConstantHeight;
        int mConstantMinimumWidth;
        int mConstantMinimumHeight;
        boolean mMutated;

        int mEnterFadeDuration = 0;
        int mExitFadeDuration = 0;

        private int[] mDurations;
        private boolean mOneShot = false;

        AnimationState(AnimationState orig, MFAnimationDrawable owner, Resources res) {
            this.mOwner = owner;
            this.mRes = res != null ? res : orig != null ? orig.mRes : null;
            if (orig != null) {
                mConstantSize = orig.mConstantSize;
                mMutated = orig.mMutated;
                mEnterFadeDuration = orig.mEnterFadeDuration;
                mExitFadeDuration = orig.mExitFadeDuration;

                mConstantWidth = orig.getConstantWidth();
                mConstantHeight = orig.getConstantHeight();
                mConstantMinimumWidth = orig.getConstantMinimumWidth();
                mConstantMinimumHeight = orig.getConstantMinimumHeight();
                mComputedConstantSize = true;

                // Postpone cloning children and futures until we're absolutely
                // sure that we're done computing values for the original state.
                final Drawable[] origDr = orig.mDrawables;
                mDrawables = new Drawable[origDr.length];
                mNumChildren = orig.mNumChildren;

                mDurations = orig.mDurations;
                mOneShot = orig.mOneShot;
            } else {
                mDrawables = new Drawable[10];
                mNumChildren = 0;
                mDurations = new int[mDrawables.length];
                mOneShot = false;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new MFAnimationDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new MFAnimationDrawable(this, res);
        }

        public void addFrame(Drawable dr, int dur) {
            // Do not combine the following. The array index must be evaluated before
            // the array is accessed because super.addChild(dr) has a side effect on mDurations.
            int pos = addChild(dr);
            mDurations[pos] = dur;
        }

        public final int addChild(Drawable dr) {
            final int pos = mNumChildren;

            if (pos >= mDrawables.length) {
                growArray(pos, pos + 10);
            }
            dr.setVisible(false, true);
            dr.setCallback(mOwner);

            mDrawables[pos] = dr;
            mNumChildren++;
            mComputedConstantSize = false;

            return pos;
        }

        public void growArray(int oldSize, int newSize) {
            Drawable[] newDrawables = new Drawable[newSize];
            System.arraycopy(mDrawables, 0, newDrawables, 0, oldSize);
            mDrawables = newDrawables;
            int[] newDurations = new int[newSize];
            System.arraycopy(mDurations, 0, newDurations, 0, oldSize);
            mDurations = newDurations;
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        public final int getConstantHeight() {
            if (!mComputedConstantSize) {
                computeConstantSize();
            }

            return mConstantHeight;
        }

        public final int getConstantWidth() {
            if (!mComputedConstantSize) {
                computeConstantSize();
            }

            return mConstantWidth;
        }

        public final int getConstantMinimumWidth() {
            if (!mComputedConstantSize) {
                computeConstantSize();
            }

            return mConstantMinimumWidth;
        }

        public final int getConstantMinimumHeight() {
            if (!mComputedConstantSize) {
                computeConstantSize();
            }

            return mConstantMinimumHeight;
        }

        public final boolean isConstantSize() {
            return mConstantSize;
        }

        protected void computeConstantSize() {
            mComputedConstantSize = true;

            //createAllFutures();

            final int N = mNumChildren;
            final Drawable[] drawables = mDrawables;
            mConstantWidth = mConstantHeight = -1;
            mConstantMinimumWidth = mConstantMinimumHeight = 0;
            for (int i = 0; i < N; i++) {
                final Drawable dr = drawables[i];
                int s = dr.getIntrinsicWidth();
                if (s > mConstantWidth) mConstantWidth = s;
                s = dr.getIntrinsicHeight();
                if (s > mConstantHeight) mConstantHeight = s;
                s = dr.getMinimumWidth();
                if (s > mConstantMinimumWidth) mConstantMinimumWidth = s;
                s = dr.getMinimumHeight();
                if (s > mConstantMinimumHeight) mConstantMinimumHeight = s;
            }
        }

        private void mutate() {
            // No need to call createAllFutures, since future drawables will
            // mutate when they are prepared.
            final int N = mNumChildren;
            final Drawable[] drawables = mDrawables;
            for (int i = 0; i < N; i++) {
                if (drawables[i] != null) {
                    drawables[i].mutate();
                }
            }

            mMutated = true;
        }

        public final Drawable getChild(int index) {
            final Drawable result = mDrawables[index];
            return result;
        }

        public final int getChildCount() {
            return mNumChildren;
        }
    }

    private MFAnimationDrawable(AnimationState state, Resources res) {
        final AnimationState as = new AnimationState(state, this, res);
        setConstantState(as);
        if (state != null) {
            setFrame(0, true, false);
        }
    }
}
