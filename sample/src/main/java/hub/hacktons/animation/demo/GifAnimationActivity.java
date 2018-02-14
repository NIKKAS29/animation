package hub.hacktons.animation.demo;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import cn.hacktons.animation.demo.R;

public class GifAnimationActivity extends AppCompatActivity {
    private Animatable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_animation_layout);
        ((TextView) findViewById(R.id.tvActivityName)).setText("Gif动画");
        final ImageView imageView = findViewById(R.id.imageview);
        //Glide.with(GifAnimationActivity.this).load(R.drawable.jump).into(imageView);
        Glide.with(GifAnimationActivity.this).asGif().load(R.drawable.jump).listener(new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                animationDrawable = resource;
                imageView.setImageDrawable(resource);
                setup();
                return true;
            }
        }).into(imageView);
    }

    private void setup() {
        ((Switch) findViewById(R.id.button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    animationDrawable.start();
                } else {
                    animationDrawable.stop();
                }
            }
        });
    }
}
