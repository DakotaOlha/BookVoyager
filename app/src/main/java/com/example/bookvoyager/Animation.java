package com.example.bookvoyager;

import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.os.Handler;

import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

public class Animation {
    private Handler handler;

    public Animation() {
        this.handler = new Handler();
    }

    public void showReward(CardView rewardCard, LottieAnimationView confettiAnimation, Runnable onComplete) {
        rewardCard.setVisibility(View.VISIBLE);
        rewardCard.setAlpha(0f);
        rewardCard.setScaleX(0f);
        rewardCard.setScaleY(0f);

        rewardCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .withEndAction(() -> {
                    if (confettiAnimation != null) {
                        confettiAnimation.playAnimation();
                    } else {
                        Log.e("Animation", "confettiAnimation is null");
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    public void hideReward(CardView rewardCard, LottieAnimationView confettiAnimation, Runnable onComplete) {
        rewardCard.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(400)
                .withEndAction(() -> {
                    rewardCard.setVisibility(View.GONE);
                    rewardCard.setAlpha(1f);
                    rewardCard.setScaleX(1f);
                    rewardCard.setScaleY(1f);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    public void showRewardWithAutoHide(CardView rewardCard, LottieAnimationView confettiAnimation, long delayMillis) {
        showReward(rewardCard, confettiAnimation, () -> {
            handler.postDelayed(() -> {
                hideReward(rewardCard, confettiAnimation, null);
            }, delayMillis);
        });
    }
}