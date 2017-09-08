package hub.hacktons.animation.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import cn.hacktons.animation.MockFrameAnimation;
import cn.hacktons.animation.demo.R;

/**
 * This activity has the same animations as {@link ExampleActivity} to test the global bitmap
 * cache
 */
public class AnotherAnimationActivity extends AppCompatActivity {

    MockFrameAnimation mFasterAnimationsContainer;
    private static final int[] IMAGE_RESOURCES = {
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

    private static final int ANIMATION_INTERVAL = 120;// 200ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        ((TextView) findViewById(R.id.tvActivityName)).setText(getClass().getCanonicalName());
        ImageView imageView = (ImageView) findViewById(R.id.imageview);
        mFasterAnimationsContainer = new MockFrameAnimation(IMAGE_RESOURCES.length)
            .with(IMAGE_RESOURCES, ANIMATION_INTERVAL)
            .into(imageView);
        ((Switch) findViewById(R.id.button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFasterAnimationsContainer.start();
                } else {
                    mFasterAnimationsContainer.stop();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFasterAnimationsContainer.stop();
    }
}
