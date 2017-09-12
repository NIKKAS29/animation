package hub.hacktons.animation.demo;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import cn.hacktons.animation.demo.R;

public class LazyAnimationXMLActivity extends AppCompatActivity {
    private Animatable animateDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lazy_animate_xml);
        ((TextView) findViewById(R.id.tvActivityName)).setText(getClass().getSimpleName());
        ImageView imageView = (ImageView) findViewById(R.id.imageview);
        animateDrawable = (Animatable) imageView.getDrawable();
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
