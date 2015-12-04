package io.plaidapp.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import io.plaidapp.util.ViewUtils;

public class TextResizeTransition extends Transition {

    private static final String PROPNAME_TEXT =
            "io.plaidapp:TextResizeTransition:textSize";
    private static final String PROPNAME_PADDING_START =
            "io.plaidapp:TextResizeTransition:paddingStart";

    private static final String[] TRANSITION_PROPERTIES = { PROPNAME_TEXT, PROPNAME_PADDING_START };

    public TextResizeTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        if (!(transitionValues.view instanceof TextView)) {
            throw new UnsupportedOperationException("Doesn't work on "
                    + transitionValues.view.getClass().getName());
        }
        TextView view = (TextView) transitionValues.view;
        transitionValues.values.put(PROPNAME_TEXT, view.getTextSize());
        transitionValues.values.put(PROPNAME_PADDING_START, view.getPaddingStart());
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                   TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        float initialTextSize = (float) startValues.values.get(PROPNAME_TEXT);
        float targetTextSize = (float) endValues.values.get(PROPNAME_TEXT);
        TextView targetView = (TextView) endValues.view;
        targetView.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialTextSize);

        int initialPaddingStart = (int) startValues.values.get(PROPNAME_PADDING_START);
        int targetPaddingStart = (int) endValues.values.get(PROPNAME_PADDING_START);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(targetView,
                        ViewUtils.PROPERTY_TEXT_SIZE,
                        initialTextSize,
                        targetTextSize),
                ObjectAnimator.ofInt(targetView,
                        ViewUtils.PROPERTY_TEXT_PADDING_START,
                        initialPaddingStart,
                        targetPaddingStart));
        return animatorSet;
    }
}
