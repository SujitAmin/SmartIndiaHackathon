package sujannalijo.com.smartindiahackathon;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GetInputPdforImage extends AppCompatActivity {
    private Button upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_input_pdfor_image);
        upload= findViewById(R.id.upload);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }
            enable_button();
        }
    }

    private void enable_button() {
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(GetInputPdforImage.this)
                        .withRequestCode(10)
                        .start();

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode ==100 &(grantResults[0] ==PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }else{
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
        }
    }
    ProgressDialog progress;
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if(requestCode ==10 && resultCode ==RESULT_OK){
            progress = new ProgressDialog(GetInputPdforImage.this);
            progress.setTitle("Uploading");
            progress.setMessage("Please wait.......");
            progress.show();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    String file_path = f.getAbsolutePath();
                    String content_type = getMimeType(f.getPath());
                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1),file_body )
                            .build();
                    Request request = new Request.Builder()
                            .url("http://192.168.0.6/Testing/save_file.php")
                            .post(request_body)
                            .build();
                    try {
                        Response response =client.newCall(request).execute();
                        if(!response.isSuccessful()){
                            throw new IOException("Error :"+response);
                        }
                        progress.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }
    private String getMimeType(String path){
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
