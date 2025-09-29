// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.yolov8ncnn;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements SurfaceHolder.Callback
{
    public static final int REQUEST_CAMERA = 100;

    private Yolov8Ncnn yolov8ncnn = new Yolov8Ncnn();
    private int facing = 0;

    private Spinner spinnerModel;
    private Spinner spinnerCPUGPU;
    private int current_model = 0;
    private int current_cpugpu = 0;
    private ArrayList<String> modelTokens = new ArrayList<>();

    private SurfaceView cameraView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = (SurfaceView) findViewById(R.id.cameraview);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        Button buttonSwitchCamera = (Button) findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int new_facing = 1 - facing;

                yolov8ncnn.closeCamera();

                yolov8ncnn.openCamera(new_facing);

                facing = new_facing;
            }
        });

        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        // build model list dynamically from assets: yolov8*.param
        modelTokens = scanModels();
        if (modelTokens.isEmpty()) {
            modelTokens.add("n");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modelTokens);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapter);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_model)
                {
                    current_model = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        spinnerCPUGPU = (Spinner) findViewById(R.id.spinnerCPUGPU);
        spinnerCPUGPU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_cpugpu)
                {
                    current_cpugpu = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        reload();
    }

    private void reload()
    {
        boolean ret_init = false;
        if (current_model >= 0 && current_model < modelTokens.size()) {
            ret_init = yolov8ncnn.loadModelByName(getAssets(), modelTokens.get(current_model), current_cpugpu);
        }
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov8ncnn loadModel failed");
        }
    }

    private ArrayList<String> scanModels()
    {
        ArrayList<String> tokens = new ArrayList<>();
        try {
            String[] names = getAssets().list("");
            if (names != null) {
                Pattern p = Pattern.compile("^yolov8(.+)\\.param$");
                for (String n : names) {
                    Matcher m = p.matcher(n);
                    if (m.find()) {
                        String t = m.group(1);
                        if (t != null && t.length() > 0) tokens.add(t);
                    }
                }
            }
        } catch (Exception ignore) {}
        Collections.sort(tokens);
        return tokens;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        yolov8ncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        yolov8ncnn.openCamera(facing);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        yolov8ncnn.closeCamera();
    }
}
