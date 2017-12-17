package paradigm.shift.myautonote;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

public class ViewPhotoActivity extends AppCompatActivity implements OnOutsidePhotoTapListener {

    public static final String EXTRA_URI = "photo_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);

        setContentView(R.layout.activity_view_photo);
        Uri imgUri = getIntent().getParcelableExtra(EXTRA_URI);
        PhotoView photoView = findViewById(R.id.photo_view);
        photoView.setImageURI(imgUri);
        photoView.setOnOutsidePhotoTapListener(this);
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }

    @Override
    public void onOutsidePhotoTap(ImageView imageView) {
        onBackPressed();
    }
}
