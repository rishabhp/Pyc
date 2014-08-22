package com.pycitup.pyc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class PhotoGalleryFragment extends Fragment {

    public String TAG = PhotoGalleryFragment.class.getSimpleName();

    protected Cursor cursor;
    protected int columnIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // == Setting up the Grid View ==

        // Create a gridview and set an adapter for it
        //GridView gridView = (GridView) getActivity().findViewById(R.id.gridview);
        //gridView.setAdapter( new ImageAdapter(getActivity()) );

        // Emulator: getExternalStorageDirectory and getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        // gives these paths `/storage/emulated/0` ----- `/storage/emulated/0/Pictures`
        //
        // Mobile: same as emulator - although can't find the paths in android file explorer
        //
        // Note: In the file explorer (for emulator) the actual path is `/mnt/shell/emulated/0/Pictures`
        String mediaStorageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath();

        // Log.d(TAG, Environment.getExternalStorageDirectory() + " ----- " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));

        String targetPath = mediaStorageDir + "/Pyc/";

        File targetDir = new File(targetPath);

        // Target directory exists?
        if ( !targetDir.exists() ) {
            // Make the target directory recursively
            if ( !targetDir.mkdirs() ) {
                Log.e(TAG, "Failed to create directory.");
            }
        }

        File[] files = targetDir.listFiles();
        for (File file : files) {
            // Log.d(TAG, file.getAbsolutePath());
            //imageAdapter.add(file.getAbsolutePath());
        }


        // == Fetching from content provider ==

        String[] projection = { MediaStore.Images.Thumbnails._ID };
        cursor = getActivity().getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Thumbnails.IMAGE_ID + " DESC"
        );
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

        //cursor.moveToFirst();
        //String imagePath = cursor.getString(columnIndex);
        //Log.d(TAG, imagePath);

        // Create a gridview and set an adapter for it
        GridView gridView = (GridView) getActivity().findViewById(R.id.gridview);
        gridView.setAdapter( new ImageAdapter(getActivity()) );
    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<String> itemList = new ArrayList<String>();

        public ImageAdapter(Context context) {
            mContext = context;
        }

        public void add(String path) {
            itemList.add(path);
        }

        @Override
        public int getCount() {
            return cursor.getCount();
            //return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
            //return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
            //return 0;
        }

        public int dpToPx(int dps) {
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dps * scale + 0.5f);

            return pixels;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            int imageID = 0;

            int wPixel = dpToPx(120);
            int hPixel = dpToPx(120);

            // Move cursor to current position
            cursor.moveToPosition(position);
            // Get the current value for the requested column
            imageID = cursor.getInt(columnIndex);

            if (convertView == null) {
                imageView = new ImageView(mContext);

                imageView.setLayoutParams(new GridView.LayoutParams(wPixel, hPixel));

                // Set the content of the image based on the provided URI
                imageView.setImageURI(Uri.withAppendedPath(
                        MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setCropToPadding(true);
            }
            else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageURI(Uri.withAppendedPath(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID));

            return imageView;



            /*ImageView imageView;

            int wPixel = dpToPx(120);
            int hPixel = dpToPx(120);

            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(wPixel, hPixel));

                // like android:scaleType
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // set padding in pixels
                imageView.setPadding(8, 8, 8, 8);
                // respect padding with CENTER_CROP
                // (this method has been available since Jelly bean)
                imageView.setCropToPadding(true);
            }
            else {
                imageView = (ImageView) convertView;
            }

            // Get bitmap from image path
            Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position), wPixel, hPixel);

            imageView.setImageBitmap(bm);
            return imageView;*/
        }

        private Bitmap decodeSampledBitmapFromUri(String path, int reqW, int reqH) {
            Bitmap bm = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

            // Query the bitmap for the out[Props] data
            // without allocating memory for its pixels
            options.inJustDecodeBounds = true;
            // it will affect the options directly
            // so no need to re-store
            BitmapFactory.decodeFile(path, options);

            options.inSampleSize = downsampleBitmap(options, reqW, reqH);

            // Need bitmap pixel data now
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);

            return bm;
        }

        private int downsampleBitmap(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Default sample size
            int inSampleSize = 1;
            int rawWidth = options.outWidth;
            int rawHeight = options.outHeight;

            if (rawWidth > reqWidth || rawHeight > reqHeight) {
                // Calculate the samplesize on the smaller side
                if (rawWidth > rawHeight) {
                    inSampleSize = Math.round( (float) rawHeight / (float) reqHeight );
                }
                else {
                    inSampleSize = Math.round( (float) rawWidth / (float) reqWidth );
                }
            }

            return inSampleSize;
        }

    }
}