package io.plaidapp.util.compat;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.Size;
import android.util.Property;

/**
 * Most or all of these are from From https://github.com/DreaminginCodeZH/MaterialProgressBar
 */
public final class ObjectAnimatorCompat {

    private static final int NUM_POINTS = 500;

    private ObjectAnimatorCompat() {
        throw new AssertionError("No instances.");
    }

    public static ObjectAnimator ofArgb(Object target, String propertyName, int... values) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ObjectAnimator.ofArgb(target, propertyName, values);
        } else {
            ObjectAnimator animator = ObjectAnimator.ofInt(target, propertyName, values);
            animator.setEvaluator(new ArgbEvaluator());
            return animator;
        }
    }

    public static <T> ObjectAnimator ofArgb(T target, Property<T, Integer> property,
            int... values) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ObjectAnimator.ofArgb(target, property, values);
        } else {
            ObjectAnimator animator = ObjectAnimator.ofInt(target, property, values);
            animator.setEvaluator(new ArgbEvaluator());
            return animator;
        }
    }

    public static ObjectAnimator ofFloat(Object target, String xPropertyName, String yPropertyName,
            Path path) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ObjectAnimator.ofFloat(target, xPropertyName, yPropertyName, path);
        } else {
            float[] xValues = new float[NUM_POINTS];
            float[] yValues = new float[NUM_POINTS];
            calculateXYValues(path, xValues, yValues);

            PropertyValuesHolder xPvh = PropertyValuesHolder.ofFloat(xPropertyName, xValues);
            PropertyValuesHolder yPvh = PropertyValuesHolder.ofFloat(yPropertyName, yValues);

            return ObjectAnimator.ofPropertyValuesHolder(target, xPvh, yPvh);
        }
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> xProperty,
            Property<T, Float> yProperty, Path path) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ObjectAnimator.ofFloat(target, xProperty, yProperty, path);
        } else {
            float[] xValues = new float[NUM_POINTS];
            float[] yValues = new float[NUM_POINTS];
            calculateXYValues(path, xValues, yValues);

            PropertyValuesHolder xPvh = PropertyValuesHolder.ofFloat(xProperty, xValues);
            PropertyValuesHolder yPvh = PropertyValuesHolder.ofFloat(yProperty, yValues);

            return ObjectAnimator.ofPropertyValuesHolder(target, xPvh, yPvh);
        }
    }

    private static void calculateXYValues(Path path, @Size(NUM_POINTS) float[] xValues,
            @Size(NUM_POINTS) float[] yValues) {

        PathMeasure pathMeasure = new PathMeasure(path, false /* forceClosed */);
        float pathLength = pathMeasure.getLength();

        float[] position = new float[2];
        for (int i = 0; i < NUM_POINTS; ++i) {
            float distance = (i * pathLength) / (NUM_POINTS - 1);
            pathMeasure.getPosTan(distance, position, null /* tangent */);
            xValues[i] = position[0];
            yValues[i] = position[1];
        }
    }
}
