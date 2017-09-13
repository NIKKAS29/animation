---
layout: default
title: Frame Animation Optimization
---

## Lazy FrameAnimation | [中文](index-zh)

Lazy frameAnimation can help to avoid OutOfMemoryError when playing frame animation.
It loads an image on background thread with global bitmap cache.

As we known, Android loads all the drawables at once for any frame animations, so animation with 
many frames causes OutOfMemoryError easily. 

For more detail please refer to this article: [Optimization of Frame Animation on Android](https://www.zybuluo.com/avenwu/note/876161)

## How to use

```Groovy
compile 'com.github.avenwu:animation:0.2.0'
```

### With custom MockFrameImageView

```xml
<cn.hacktons.animation.MockFrameImageView
    android:id="@+id/imageview"
    android:layout_width="200dp"
    android:layout_height="200dp"
    android:layout_gravity="center"
    app:cache_percent="0.4"
    app:src="@drawable/loading_animation"/>
```

```java
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
```

### Use default ImageView

```java
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
```

![Animation](http://7u2jir.com1.z0.glb.clouddn.com/wuba/device-2017-09-12-153056.gif)

[Download Video](http://7u2jir.com1.z0.glb.clouddn.com/wuba/device-2017-09-12-153056.mp4)


## Optimization
The standard android frame animation is more suit for small animations with less images, so it 
won't lead to OutOfMemoryError while keep the animation fluent; As to lazy frameAnimation, we decode 
image dynamically and exert as little pressure as possible on system memory. These can increase the
 pressure on CPU, so we allow developer to set the cache size for the new balance between memory and
 CPU. The more we cache, the less we need to decode.

This project is fork from [FasterAnimationsContainer](https://github.com/tigerjj/FasterAnimationsContainer);
Since the original project seems no longer maintained actively and there are some issues 
need to be fixed before it can be used. We've send [Pull Request](https://github.com/tigerjj/FasterAnimationsContainer/issues/11)
 and fixed these issues in [Animation](https://github.com/hacktons/animation).
 
The mainly changes we've done:

1. fix warning when reuse bitmap with option.in;
2. fix animation frozen issue after Home press
3. FasterAnimationsContainer is no longer singleton, so each ImageView may control it's animation;
4. global bitmap cache supported;

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
