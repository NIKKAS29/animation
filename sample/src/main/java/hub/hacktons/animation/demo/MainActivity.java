package hub.hacktons.animation.demo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import cn.hacktons.animation.MFAnimationDrawable;
import cn.hacktons.animation.MFImageView;
import cn.hacktons.animation.demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MFImageView mfImageView = (MFImageView) findViewById(R.id.custom_imageView);
        mfImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = mfImageView.getDrawable();
                if (drawable instanceof MFAnimationDrawable) {
                    ((MFAnimationDrawable) drawable).start();
                }
            }
        });

    }
}
