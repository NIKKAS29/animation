---
layout: default
title: 帧动画调优
---

## 帧动画 | [English](index)

使用 MockFrameAnimation 可以避免在执行帧动画时的内存问题，特别是OutOfMemoryError。他的实现原理在于异步加载图片，
并且支持全局图片缓存。

我们都知道原生的Android帧动画在加载序列帧时，是一次性将所有序列帧的图片编码到内存当中的，所以执行帧数较多的动画时很容易
发生内存不足，抛出OutOfMemoryError。

## 使用说明

```java
int ANIMATION_INTERVAL = 120;// 200ms
int[] IMAGE_RESOURCES = {
    R.drawable.num_0,
    R.drawable.num_1,
    R.drawable.num_2,
    // ...
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

## 优化点

综合来看，原生帧动画更适合体量较小，内存压力不那么大的帧动画，如此一次性加载所有帧，可以保证后续帧切换的流程性；
而MockFrameAnimation则为了解决内存问题，采取动态编码序列帧。这相当于用CPU的编码/计算能力换取了内存消耗；同时为了
达到适合的平衡，我们允许开发者设置图片缓存的张数，缓存数越大那么内存消耗越多，需要重新编码的次数也就相对更少；

这个项目起源于[FasterAnimationsContainer](https://github.com/tigerjj/FasterAnimationsContainer)；
原项目更多的像一个示意demo，存在诸多bug，需要修复才能满足正常使用，因此我们修复了这些问题，并且发起了
[Pull Request](https://github.com/tigerjj/FasterAnimationsContainer/issues/11)。
不过从维护记录来看原项目已经不太活跃，我们将这些改动和新增的功能优化做了梳理。

我们主要做了如下改动：

1. 消除bitmap编码警告；
2. 修复前后台切换后动画僵死的问题；
3. 取消单例模式，支持每个ImageView控制独立的动画；
4. 新增图片内存缓存；

## 开源协议

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
