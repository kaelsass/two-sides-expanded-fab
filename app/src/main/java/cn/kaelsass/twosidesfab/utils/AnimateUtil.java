package cn.kaelsass.twosidesfab.utils;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class AnimateUtil {

    public static void showFab(View view) {
        ViewHelper.setScaleX(view, 0);
        ViewHelper.setScaleY(view, 0);
        ViewPropertyAnimator.animate(view).cancel();
        ViewPropertyAnimator.animate(view).alpha(1).scaleX(1).scaleY(1).setDuration(200).start();
    }

    public static void hideFab(final View view) {
        ViewHelper.setScaleX(view, 1);
        ViewHelper.setScaleY(view, 1);
        ViewPropertyAnimator.animate(view).cancel();
        ViewPropertyAnimator.animate(view).alpha(0).scaleX(0).scaleY(0).setDuration(200).start();

    }
}
