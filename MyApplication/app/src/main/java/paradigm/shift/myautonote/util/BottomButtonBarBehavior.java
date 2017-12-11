package paradigm.shift.myautonote.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Class used to translate the bottom button bar up, when snackbar is displayed. This class is used
 * only in xml (fragment_my_notes.xml), hence the unused warning.
 *
 * Created by aravind on 11/28/17.
 */

@SuppressWarnings("unused")
public class BottomButtonBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    public BottomButtonBarBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}
