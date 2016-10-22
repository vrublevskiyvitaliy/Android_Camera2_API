package com.example.final_camera2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private CameraHelper cameraHelper;
    private Button takePictureButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (Button) findViewById(R.id.btn_get_photo);
        assert takePictureButton != null;

        cameraHelper = new CameraHelper();

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.getImage();
            }
        });
    }
}
