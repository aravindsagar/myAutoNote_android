package paradigm.shift.myautonote;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import paradigm.shift.myautonote.util.TypeWriter;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        TypeWriter[] aboutViews = new TypeWriter[] {
                rootView.findViewById(R.id.about_line1),
                rootView.findViewById(R.id.about_line2),
                rootView.findViewById(R.id.about_line3),
                rootView.findViewById(R.id.about_line4),
                rootView.findViewById(R.id.about_line5),
        };
        new AboutAnimator(
                aboutViews,
                new String[] {"MyAutoNote", "by Paradigm Shift", "Aravind Sagar", "Drew Schwartz", "Emily Chen"},
                new boolean[] {false, false, true, true, true}
        ).start();
        return rootView;
    }

    private class AboutAnimator implements TypeWriter.OnAnimationEndListener, Animator.AnimatorListener {
        TypeWriter[] myViews;
        String[] myTexts;
        boolean[] myHasDrawable;
        Path myZoomInPath;

        int curIdx;

        AboutAnimator(TypeWriter[] views, String[] texts, boolean[] hasDrawable) {
            myViews = views;
            myTexts = texts;
            myHasDrawable = hasDrawable;
            myZoomInPath = new Path();
            myZoomInPath.moveTo(0, 0);
            myZoomInPath.lineTo(1, 1);
            curIdx = 0;
        }

        public void start() {
            if (curIdx >= myViews.length) {
                return;
            }
            TypeWriter curV = myViews[curIdx];
            curV.setVisibility(View.VISIBLE);
            if (myHasDrawable[curIdx]) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(curV, "scaleX", "scaleY", myZoomInPath);
                animator.addListener(this);
                animator.start();
            } else {
                onAnimationEnd(new ObjectAnimator());
            }
        }

        @Override
        public void onAnimationEnd(View v) {
            try {
                v.setBackgroundColor(getResources().getColor(R.color.textNoHighlight));
                curIdx++;
                start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            try {
                TypeWriter curV = myViews[curIdx];
                curV.setBackgroundColor(getResources().getColor(R.color.textHighlight));
                curV.setOnAnimationEndListener(this).animateText(myTexts[curIdx]);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
