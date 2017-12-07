package paradigm.shift.myautonote.util;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by aravind on 12/6/17.
 */

public class UriPhotoView extends PhotoView {
    private Uri myImgUri;

    public UriPhotoView(Context context) {
        super(context);
    }

    public UriPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public UriPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    public UriPhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageURI(Uri uri) {
        myImgUri = uri;
        super.setImageURI(uri);
    }

    public Uri getImgUri() {
        return myImgUri;
    }
}
