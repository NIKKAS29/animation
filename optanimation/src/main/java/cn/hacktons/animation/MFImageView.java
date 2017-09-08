package cn.hacktons.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MFImageView extends ImageView {

    public MFImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public MFImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MFImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
            attrs, R.styleable.MFImageView, defStyle, 0);
        if (isInEditMode()) {
            return;
        }
        Drawable drawable = XmlCompat.getDrawable(getResources(), a, R.styleable
            .MFImageView_mf_src);
        a.recycle();
        setImageDrawable(drawable);
    }
}
