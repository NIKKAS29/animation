## What is MockFrameAnimation

MockFrameAnimation can help to avoid OutOfMemoryError when using standard android frame animation.

As we known, Android loads all the drawables at once for any frame animations, so animation with 
many frames causes OutOfMemoryError easily. 

This class loads & sets and releases an image on background thread.

MockFrameAnimation is fork from [FasterAnimationsContainer](https://github.com/tigerjj/FasterAnimationsContainer);

Since the original project seems no longer maintained actively and there are some issues need to be
fixed before it can be used. We've send pull request and fixed these issues in MockFrameAnimation.

## Difference between other solutions

There are many ways to do animation in different case. We suggest one 
## How to use

```java
int ANIMATION_INTERVAL = 120;// 200ms
int[] IMAGE_RESOURCES = {
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

ImageView imageView = (ImageView) findViewById(R.id.imageview);
MockFrameAnimation animation = new MockFrameAnimation(IMAGE_RESOURCES.length)
    .with(IMAGE_RESOURCES, ANIMATION_INTERVAL)
    .into(imageView);
animation.start();
// stop animation when necessary such as onDestroy
// animation.stop();

```

![Animation](device-2017-09-01-182900.gif)

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