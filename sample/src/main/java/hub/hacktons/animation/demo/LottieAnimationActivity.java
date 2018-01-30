package hub.hacktons.animation.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import cn.hacktons.animation.demo.R;

public class LottieAnimationActivity extends AppCompatActivity {
    private LottieAnimationView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottie_animate_xml);
        ((TextView) findViewById(R.id.tvActivityName)).setText("Lottie");
        imageView = findViewById(R.id.imageview);

        ((Switch) findViewById(R.id.button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageView.playAnimation();
                } else {
                    imageView.cancelAnimation();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageView.cancelAnimation();
    }
}
