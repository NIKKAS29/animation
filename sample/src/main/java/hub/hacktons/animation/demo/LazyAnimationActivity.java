package hub.hacktons.animation.demo;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import cn.hacktons.animation.AnimationBuilder;
import cn.hacktons.animation.demo.R;

public class LazyAnimationActivity extends AppCompatActivity {

    private Animatable animateDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lazy_animate_code);
        ((TextView) findViewById(R.id.tvActivityName)).setText(getClass().getSimpleName());
        int[] FRAMES = {
            R.drawable.num_0,
            R.drawable.num_1,
            R.drawable.num_2,
            R.drawable.num_3,
            R.drawable.num_4,
            R.drawable.num_5,
            R.drawable.num_6,
            R.drawable.num_7,
            R.drawable.num_8,
            R.drawable.num_9,
            R.drawable.num_a,
            R.drawable.num_b,
            R.drawable.num_c,
            R.drawable.num_d,
            R.drawable.num_e,
            R.drawable.num_f,
        };
        animateDrawable = new AnimationBuilder()
            .frames(FRAMES, 120/*duration*/)
            .cachePercent(0.4f)
            .oneShot(false)
            .into(findViewById(R.id.imageview));

        ((Switch) findViewById(R.id.button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    animateDrawable.start();
                } else {
                    animateDrawable.stop();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animateDrawable.stop();
    }
}
