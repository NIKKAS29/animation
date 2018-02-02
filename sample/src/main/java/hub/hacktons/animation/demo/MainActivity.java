package hub.hacktons.animation.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.hacktons.animation.demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickAnimationDrawable(View view) {
        startActivity(new Intent(this, AnimationXMLActivity.class));
    }

    public void onClickLazyAnimationDrawable(View view) {
        startActivity(new Intent(this, LottieAnimationActivity.class));
    }

    public void onClickLazyAnimationDrawableXML(View view) {
        startActivity(new Intent(this, LazyAnimationXMLActivity.class));
    }

    public void onClickGifAnimation(View view) {
        startActivity(new Intent(this, GifAnimationActivity.class));
    }
}
