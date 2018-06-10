# Swappable Image View

 [![Download](https://api.bintray.com/packages/iamogbz/android-swappable-imageview/com.ogbizi.android_swappable_imageview/images/download.svg)](https://bintray.com/iamogbz/android-swappable-imageview/com.ogbizi.android_swappable_imageview/_latestVersion)

Android View supporting animated image swapping, with extendable swap animation behaviours

## Usage

### Add dependency from JCenter

```gradle
repositories {
    jcenter()
}

dependencies {
    implementation 'com.ogbizi.android_swappable_imageview:android_swappable_imageview:0.0.0'
}
```

### Ready to go with the default swap `Behavior`

```xml
<com.ogbizi.android_swappable_imageview.SwappableImageView .../>
```

![Default Horizontal Swapping](docs/demo-main_cropped.gif)

#### with attributes

- `src (reference)` to the currently displayed drawable
- `nextSrc (reference)` to the drawable to be displayed next
- `prevSrc (reference)` to the drawable set to be previously displayed
- `loop (boolean)` if the view should loop drawables on showNext

#### or with methods

```java
int index = 2; // current drawable
// sets all drawable resource ids and currently displayed
swappableImageView.setDrawables(index, first, previous, current, next, ..., last);

// add a drawable to be displayed next by resource id
swappableImageView.setNext(drawableResourceId);

// sets a drawable to be the last displayed in the list
swappableImageView.setPrevious(drawableResourceId);

// sets the loop attribute
swappableImageView.setLooping(true);
```

![Horizontal Swap With Looping](docs/demo-loop_cropped.gif)

### Defining and using a custom `SwappableImageView.Behavior`

```java
public class CustomSwappableImageBehavior extends SwappableImageView.Behavior {...}

swappableImageView.setBehavior(new CustomSwappableImageBehavior());

swappableImageView.showPrevious()
swappableImageView.showNext()
```

Full documentation is available in package as `javadoc`
