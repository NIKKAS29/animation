FasterAnimationsContainer
============================
FasterAnimationsContainer will help you to avoid from OutOfMemoryError. Android loads all the drawables at once, so animation with many frames causes this error. This class loads & sets and releases an image on background thread.

Easy to implement
-------------------


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
FasterAnimationsContainer mFasterAnimationsContainer = new FasterAnimationsContainer(IMAGE_RESOURCES.length)
    .with(IMAGE_RESOURCES, ANIMATION_INTERVAL)
    .into(imageView);
mFasterAnimationsContainer.start();
// stop animation when necessary such as onDestroy
// mFasterAnimationsContainer.stop();

```
