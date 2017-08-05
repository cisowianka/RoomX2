package com.nn.roomx.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;


//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.ChecksumException;
//import com.google.zxing.FormatException;
//import com.google.zxing.MultiFormatReader;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.PlanarYUVLuminanceSource;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.Reader;
//import com.google.zxing.Result;
import com.nn.roomx.MainActivity;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;

import java.io.IOException;

/**
 * Created by user on 2017-01-26.
 */

public class QRCodeScannerDialog extends Dialog {

    private MainActivity context;
    private Camera camera;
    private LinearLayout dialogView;

    public QRCodeScannerDialog(MainActivity ctx) {
        super(ctx);
        this.context = ctx;
    }

    public Dialog startQRCodeScanner(){
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = context.getLayoutInflater();
         dialogView = (LinearLayout) inflater.inflate(R.layout.qr_scanner_dialog, null);
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        this.setContentView(dialogView);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = (int) (width * 0.9);
        lp.height = (int) (height * 0.9);
        this.getWindow().setAttributes(lp);

        return this;
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public void start(){
        SurfaceView cameraSurface = (SurfaceView)dialogView.findViewById(R.id.cpPreview);
        SurfaceHolder holder = cameraSurface.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(RoomxUtils.TAG, "SURCACE CHANGE");
                QRCodeScannerDialog.this.camera = Camera.open(findFrontFacingCamera());

                try {
                    camera.setPreviewDisplay(holder);
                    camera.setPreviewCallback(_previewCallback);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture.
                camera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    private Camera.PreviewCallback _previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {



            // Read Range
           Camera.Size size = camera.getParameters().getPreviewSize();
//
//            // Create BinaryBitmap
//            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
//                    data, size.width, size.height, 0, 0, size.width, size.height, false);
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//            // Read QR Code
//            Reader reader = new MultiFormatReader();
//            Result result = null;
//            try {
//                result = reader.decode(bitmap);
//                String text = result.getText();
//
//                Log.i(RoomxUtils.TAG, "readed value --------------------- " + text);
//
//            } catch (Exception e) {
//                Log.e(RoomxUtils.TAG, e.getMessage(), e);
//            }
        }
    };

}
