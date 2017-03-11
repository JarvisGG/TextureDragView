package com.jarvis.textureviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.jarvis.textureviewdemo.view.DragTextureView;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private DragTextureView dragTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.start_video);
        dragTextureView = (DragTextureView) findViewById(R.id.text_view);

        button.setOnClickListener(v -> {
            dragTextureView.startVideo();
        });
    }
}
