package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;

import com.kozyrev.jotdown_room.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteCamera {

    private Uri cameraImageUri;
    private Context appContext;
    private Context activityContext;

    private ImageView imageView;

    public NoteCamera(Context appContext, Context activityContext, ImageView imageView){
        this.appContext = appContext;
        this.activityContext = activityContext;

        this.imageView = imageView;
    }

    public String getPhotoFromCamera(int imageViewHeight){
        imageView.getLayoutParams().height = imageViewHeight;
        Picasso.get()
                .load(cameraImageUri)
                .resize(533, 300)
                .centerCrop()
                .into(imageView);
        return cameraImageUri.toString();
    }

    public Intent callCameraApp(){
        Intent cameraAppIntent = new Intent();
        cameraAppIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex){
            ex.printStackTrace();
        }

        String authorities = appContext.getPackageName() + ".fileprovider";
        cameraImageUri = FileProvider.getUriForFile(activityContext, authorities, photoFile);

        cameraAppIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        return cameraAppIntent;
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }
}
