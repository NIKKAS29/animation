package hub.hacktons.animation.demo;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import cn.hacktons.animation.demo.R;

public class AnimationXMLActivity extends AppCompatActivity {

    private Animatable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_layout);
        ((TextView) findViewById(R.id.tvActivityName)).setText("原生帧动画");
        final ImageView imageView = findViewById(R.id.imageview);
        animationDrawable = (Animatable) imageView.getDrawable();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animationDrawable.stop();
    }
}
