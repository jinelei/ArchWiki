package cn.jinelei.smart.archwiki.common.anim;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class ScaleOutAnimation {
	private static final float DEFAULT_SCALE_TO = .7f;
	private final float mTo;

	public ScaleOutAnimation() {
		this(DEFAULT_SCALE_TO);
	}

	public ScaleOutAnimation(float to) {
		mTo = to;
	}

	public Animator[] getAnimators(View view) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, mTo);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, mTo);
		return new ObjectAnimator[]{scaleX, scaleY};
	}
}

