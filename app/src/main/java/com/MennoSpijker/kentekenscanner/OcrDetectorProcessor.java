/*
 * Copyright (C) The Android Open Source Project
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
package com.MennoSpijker.kentekenscanner;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.MennoSpijker.kentekenscanner.Camera.GraphicOverlay;
import com.MennoSpijker.kentekenscanner.View.KentekenHandler;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private static final boolean DIRECTSEARCH = true;
    private final GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private final OcrCaptureActivity ocrCaptureActivity;
    private static final String TAG = "OcrDetectorProcessor";

    private boolean snackbarOpened;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, OcrCaptureActivity ocrCaptureActivity) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.ocrCaptureActivity = ocrCaptureActivity;
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            final TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                if (KentekenHandler.kentekenValid(item.getValue())) {

                    if (DIRECTSEARCH) {
                        Intent data = new Intent();
                        data.putExtra(OcrCaptureActivity.TextBlockObject, item.getValue());
                        ocrCaptureActivity.setResult(CommonStatusCodes.SUCCESS, data);
                        AudioManager audio = (AudioManager) ocrCaptureActivity.getSystemService(Context.AUDIO_SERVICE);
                        if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            try {
                                Random rand = new Random();
                                MediaPlayer mPlayer;
                                int r = rand.nextInt(3);
                                Log.println(Log.INFO, TAG, Integer.toString(r));
                                mPlayer = MediaPlayer.create(ocrCaptureActivity, R.raw.beep);
                                mPlayer.start();
                                Vibrator vi = (Vibrator) ocrCaptureActivity.getSystemService(Context.VIBRATOR_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    vi.vibrate(VibrationEffect.EFFECT_CLICK);
                                } else {
                                    vi.vibrate(700);
                                }
                            } catch (Exception IE) {
                                Log.println(Log.ERROR, TAG, "no sound could be played.");
                                IE.printStackTrace();
                            }
                        }
                        ocrCaptureActivity.finish();
                    } else {
                        if (!snackbarOpened) {
                            final Snackbar snackbar = Snackbar.make(mGraphicOverlay, item.getValue(),
                                    Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dit kenteken opzoeken", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();

                                    Intent data = new Intent();
                                    data.putExtra(OcrCaptureActivity.TextBlockObject, item.getValue());
                                    ocrCaptureActivity.setResult(CommonStatusCodes.SUCCESS, data);
                                    AudioManager audio = (AudioManager) ocrCaptureActivity.getSystemService(Context.AUDIO_SERVICE);
                                    if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        try {
                                            Random rand = new Random();
                                            MediaPlayer mPlayer;
                                            int r = rand.nextInt(3);
                                            Log.println(Log.INFO, TAG, Integer.toString(r));
                                            mPlayer = MediaPlayer.create(ocrCaptureActivity, R.raw.beep);
                                            mPlayer.start();
                                            Vibrator vi = (Vibrator) ocrCaptureActivity.getSystemService(Context.VIBRATOR_SERVICE);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                vi.vibrate(VibrationEffect.EFFECT_CLICK);
                                            } else {
                                                vi.vibrate(700);
                                            }
                                        } catch (Exception IE) {
                                            Log.println(Log.ERROR, TAG, "no sound could be played.");
                                            IE.printStackTrace();
                                        }
                                    }
                                    ocrCaptureActivity.finish();

                                }
                            });
                            snackbar.addCallback(new Snackbar.Callback() {

                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    snackbarOpened = false;
                                }

                                @Override
                                public void onShown(Snackbar snackbar) {
                                    snackbarOpened = true;
                                }
                            });
                            snackbar.show();
                        }


                        try {
                            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                            mGraphicOverlay.add(graphic);
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getMessage();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}