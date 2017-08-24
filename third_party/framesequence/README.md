# Framesequence

Fork of the experimental [Framesequence](https://android.googlesource.com/platform/frameworks/ex/+/master/framesequence/)
 gif decoder.

## Modifications

- Included [giflib](http://giflib.sourceforge.net/) dependency.
- Added `CMake` & `gradle` build scripts.
- Removed `make` build script.
- Removed webp support.
- Modified `FrameSequenceDrawable` to utilize `Drawable.ConstantState` & expose some GIF properties.
