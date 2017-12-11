package paradigm.shift.myautonote.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * PhotoView which stores the URI of the displayed photo. Also does bitmap scaling.
 *
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

    @SuppressWarnings("unused")
    public UriPhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageURI(final Uri uri, final int imgHeight) {
        myImgUri = uri;
        new ScaleImageTask(this, imgHeight).execute();
    }

    public Uri getImgUri() {
        return myImgUri;
    }

    private static class ScaleImageTask extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<UriPhotoView> imageView;
        private int imgHeight;

        ScaleImageTask(UriPhotoView imageView, int imgHeight) {
            this.imageView = new WeakReference<>(imageView);
            this.imgHeight = imgHeight;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            UriPhotoView imgView = imageView.get();
            if (imgView == null) {
                return null;
            }
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(imgView.getContext().getContentResolver(),
                        imgView.getImgUri());
                float scaleFactor = ((float) imgHeight)/bitmap.getHeight();
                return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scaleFactor), imgHeight, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            UriPhotoView photoView = imageView.get();
            if (photoView == null) {
                return;
            }
            photoView.setImageBitmap(bitmap);
        }
    }
}
