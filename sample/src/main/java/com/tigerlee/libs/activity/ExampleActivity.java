package com.tigerlee.libs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tigerlee.libs.R;

import cn.hacktons.animation.FasterAnimationsContainer;

public class ExampleActivity extends Activity {

    FasterAnimationsContainer mFasterAnimationsContainer;
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
        mFasterAnimationsContainer = new FasterAnimationsContainer(IMAGE_RESOURCES.length)
            .with(IMAGE_RESOURCES, ANIMATION_INTERVAL)
            .into(imageView);
        mFasterAnimationsContainer.start();
        // start another activity with the same animation
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AnotherAnimationActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFasterAnimationsContainer.stop();
    }
}
