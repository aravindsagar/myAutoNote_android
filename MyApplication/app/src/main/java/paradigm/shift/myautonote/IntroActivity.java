package paradigm.shift.myautonote;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.ISlidePolicy;

import paradigm.shift.myautonote.util.PreferenceHelper;

public class IntroActivity extends AppIntro2 {
    private static class IntroScreen {
        String title, description;
        int drawableRes;
        int bgColor;

        IntroScreen(String title, String description, int drawableRes, int bgColor) {
            this.title = title;
            this.description = description;
            this.drawableRes = drawableRes;
            this.bgColor = bgColor;
        }

        public static IntroScreen[] getIntroScreens(Context context) {
            int bgColor = context.getResources().getColor(R.color.colorPrimary);
            return new IntroScreen[] {
                    new IntroScreen(
                            "Organize your notes into folders",
                            "Everything is searchable",
                            R.drawable.intro_1_faded, bgColor
                    ),
                    new IntroScreen(
                            "Notes are auto-formatted",
                            "Also supports inline images",
                            R.drawable.intro_autoformat, bgColor
                    ),
                    new IntroScreen(
                            "Intelligent new note suggestions",
                            "Gets better as you use the app more",
                            R.drawable.intro_suggestions, bgColor
                    )
            };
        }
    }

    public static class LogInFragment extends Fragment implements ISlidePolicy {
        private static final int SHAKE_X = 25;

        private EditText myUsernameView;

        public LogInFragment() {
            // Required empty public constructor
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View layout = inflater.inflate(R.layout.fragment_log_in, container, false);
            myUsernameView = layout.findViewById(R.id.username);
            return layout;
        }

        @Override
        public boolean isPolicyRespected() {
            String username = myUsernameView.getText().toString();

            boolean success = !username.isEmpty();

            if (success) {
                PreferenceHelper.putString(getActivity(), R.string.pref_key_username, username);
            }

            return success;
        }

        @Override
        public void onUserIllegallyRequestedNextPage() {
            Snackbar.make(myUsernameView, R.string.invalid_username, Snackbar.LENGTH_SHORT).show();
            ObjectAnimator animator = ObjectAnimator.ofFloat(myUsernameView, "translationX",
                    -SHAKE_X, SHAKE_X, -SHAKE_X, SHAKE_X, -SHAKE_X, SHAKE_X, -SHAKE_X, 0)
                    .setDuration(600);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new LogInFragment());
        for (IntroScreen s : IntroScreen.getIntroScreens(this)) {
            addSlide(AppIntro2Fragment.newInstance(s.title, s.description, s.drawableRes, s.bgColor));
        }
        setFadeAnimation();
        setScrollDurationFactor(2);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        PreferenceHelper.putBoolean(this, R.string.pref_key_intro_done, true);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment fragment) {
        super.onSkipPressed(fragment);
        if (fragment instanceof LogInFragment) {
            getPager().goToNextSlide();
        } else {
            onDonePressed(fragment);
        }
    }
}
