/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;
import com.komodo.R;

import java.util.Collection;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler
{

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;

    private enum State
    {
        PREVIEW,
        SUCCESS,
        DONE
    }

    CaptureActivityHandler(CaptureActivity activity,
                           Collection<BarcodeFormat> decodeFormats,
                           String characterSet,
                           CameraManager cameraManager)
    {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, characterSet,new ViewfinderResultPointCallback(null));
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message)
    {
        if (message.what == R.id.zx_restart_preview)
        {
            Log.d(TAG, "Got restart preview message");
            restartPreviewAndDecode();

        } else if (message.what == R.id.zx_decode_succeeded)
        {
            Log.d(TAG, "Got decode succeeded message");
            state = State.SUCCESS;
            Bundle bundle = message.getData();
            Bitmap barcode = bundle == null ? null :
                                     (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
            activity.handleDecode((Result) message.obj, barcode);

        } else if (message.what == R.id.zx_decode_failed)
        {// We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.zx_decode);

        } else if (message.what == R.id.zx_return_scan_result)
        {
            Log.d(TAG, "Got return scan result message");
            activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
            activity.finish();

        } else if (message.what == R.id.zx_launch_product_query)
        {
            Log.d(TAG, "Got product query message");
            String url = (String) message.obj;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            activity.startActivity(intent);

        }
    }

    public void quitSynchronously()
    {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.zx_quit);
        quit.sendToTarget();
        try
        {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(1L);
        } catch (InterruptedException e)
        {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.zx_decode_succeeded);
        removeMessages(R.id.zx_decode_failed);
    }

    private void restartPreviewAndDecode()
    {
        if (state == State.SUCCESS)
        {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.zx_decode);
            activity.drawViewfinder();
        }
    }

}
