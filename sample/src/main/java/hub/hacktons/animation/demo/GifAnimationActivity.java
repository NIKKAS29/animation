package hub.hacktons.animation.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cn.hacktons.animation.demo.R;

public class GifAnimationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_animation_layout);
        ((TextView) findViewById(R.id.tvActivityName)).setText("Gif动画");
        final ImageView imageView = findViewById(R.id.imageview);

        Glide.with(this).load(R.drawable.jump).into(imageView);

        //animationDrawable = (Animatable) imageView.getDrawable();
        ((Switch) findViewById(R.id.button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //animationDrawable.start();
                } else {
                    //animationDrawable.stop();
                }
            }
        });
    }
}
