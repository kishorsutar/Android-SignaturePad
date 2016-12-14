package com.ap.github.kishorsutar.androidsinganturepad;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ap.github.kishorsutar.androidsinganturepad.view.SignatureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SignatureView signatureView;
    private Button clearButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyIfAppHasPermissionToWrite(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clearButton = (Button) this.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);
        saveButton = (Button) this.findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        signatureView = (SignatureView) this.findViewById(R.id.signView);
        signatureView.setOnSignedListener(new SignatureView.OnSignedListener() {

            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {

            }

            @Override
            public void onClear() {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_button:
                clearView();
                break;
            case R.id.save_button:
                saveSignatureAsImage();
                break;
        }
    }

    private void clearView() {
        signatureView.clear();
    }

    private void saveSignatureAsImage() {
        Bitmap signatureBitmap = signatureView.getSignatureBitmap();
        if (addJpegToGallery(signatureBitmap)) {
            Toast.makeText(MainActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
            clearView();
        } else {
            Toast.makeText(MainActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean addJpegToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("Signature"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJpeg(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private File getAlbumStorageDir(String dirName) {
        File file = null;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dirName);
            if(!file.isDirectory()) {
                if (!file.mkdirs())
                    Log.e("Signature", "Could not create Directory ");
            } else {
                Log.i("FILE", "directory already present");
            }
        } catch (Exception ap) {
            ap.printStackTrace();
        }

        return file;
    }

    private void saveBitmapToJpeg(Bitmap signBitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(signBitmap.getWidth(), signBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(signBitmap, 0, 0, null);
//        canvas.drawColor(Color.WHITE);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        MainActivity.this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                // if request codes are cancelled, result array must be empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
        }

    }

    private void verifyIfAppHasPermissionToWrite(Activity activity) {
// check if we have write permissions
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // show user to allow permission.
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }

}
