/*
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/
package com.android.ts_calibrate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.widget.Toast;
import android.os.SystemProperties;

import com.android.ts_calibrate.R;

public class TsCalibrate extends Activity {
    final String TAG = "TsCalibration";

    final int UI_SCREEN_WIDTH = 480;
    final int UI_SCREEN_HEIGHT = 800;

    CrossView myview;
    int direction;
    int x1;
    int y1;
    private Calibrate cal;

    int xList[] = {
            50, UI_SCREEN_WIDTH - 50, UI_SCREEN_WIDTH - 50, 50, UI_SCREEN_WIDTH / 2
    };

    int yList[] = {
            50, 50, UI_SCREEN_HEIGHT - 50, UI_SCREEN_HEIGHT - 50, UI_SCREEN_HEIGHT / 2
    };

    static void setNotTitle(Activity act) {
        act.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static void setFullScreen(Activity act) {
        setNotTitle(act);
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private static final int DIALOG_START_CONFIRM = 1;
    private static final int DIALOG_FINISH_CALIBRATE = 2;
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_FINISH_CALIBRATE:
            return new AlertDialog.Builder(TsCalibrate.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_finish_dialog_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getBaseContext(), "Calibrate Done!", Toast.LENGTH_SHORT).show();
                        TsCalibrate.this.finish();
                    }
                })
                .create();
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(this);   
        myview = new CrossView(this);
        setContentView(myview);

        SystemProperties.set("ts.calibrate", "start");

        cal = new Calibrate();
        direction = 0;

        myview.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("TsCalibrate.OnTouch", event.getX() + "," + event.getY() + ",------> DownTime : " + event.getDownTime());

                v.invalidate();
                x1 = (int)(( event.getX() * 4095 ) / 479);
                y1 = (int)(( event.getY() * 4095 ) / 799);
                Log.i("TsCalibrate.OnTouch", " --->x1=" + x1 + "," + " --->y1=" + y1);

                if (direction <= 4) {
                    cal.get_sample(direction, x1, y1, xList[direction], yList[direction]);
                    direction++;

                }
		cal.calibrate_main();
                return false;
            }
        });
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            SystemProperties.set("ts.calibrate", "done");
            Toast.makeText(getBaseContext(), "Calibrate BACK key cancel!", Toast.LENGTH_SHORT).show();
            TsCalibrate.this.finish();
            break;
        case KeyEvent.KEYCODE_MENU:
            if(direction == 5) {
                direction++;
                cal.calibrate_main();
                SystemProperties.set("ts.calibrate", "done");
                showDialog(DIALOG_FINISH_CALIBRATE);
            }
            break;
        }
        return false;
    }
    
    public class CrossView extends View {
        public CrossView(Context context) {
            super(context);
        }

        public void onDraw(Canvas canvas) {

            Paint paint = new Paint();
            paint.setColor(Color.WHITE); 
            paint.setTextSize(24);
            canvas.drawText(getResources().getString(R.string.screen_note), 112, 420, paint);
            paint.setTextSize(48);
            if (direction == 0) {
                canvas.drawLine(30, 50, 70, 50, paint);
                canvas.drawLine(50, 30, 50, 70, paint);
                canvas.drawText(getResources().getString(R.string.screen_top_left),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 1) {
                canvas.drawLine(UI_SCREEN_WIDTH - 70, 50, UI_SCREEN_WIDTH - 30, 50, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - 50, 30, UI_SCREEN_WIDTH - 50, 70, paint);
                canvas.drawText(getResources().getString(R.string.screen_top_right),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 2) {
                canvas.drawLine(UI_SCREEN_WIDTH - 70, UI_SCREEN_HEIGHT - 50, UI_SCREEN_WIDTH - 30,
                        UI_SCREEN_HEIGHT - 50, paint);
                canvas.drawLine(UI_SCREEN_WIDTH - 50, UI_SCREEN_HEIGHT - 70, UI_SCREEN_WIDTH - 50,
                        UI_SCREEN_HEIGHT - 30, paint);
                canvas.drawText(getResources().getString(R.string.screen_bottom_right),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 3) {
                canvas.drawLine(30, UI_SCREEN_HEIGHT - 50, 70, UI_SCREEN_HEIGHT - 50, paint);
                canvas.drawLine(50, UI_SCREEN_HEIGHT - 70, 50, UI_SCREEN_HEIGHT - 30, paint);
                canvas.drawText(getResources().getString(R.string.screen_bottom_left),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else if (direction == 4) {
                canvas.drawLine(UI_SCREEN_WIDTH / 2 - 20, UI_SCREEN_HEIGHT / 2,
                        UI_SCREEN_WIDTH / 2 + 20, UI_SCREEN_HEIGHT / 2, paint);
                canvas.drawLine(UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 - 20,
                        UI_SCREEN_WIDTH / 2, UI_SCREEN_HEIGHT / 2 + 20, paint);
                canvas.drawText(getResources().getString(R.string.screen_center),
                                UI_SCREEN_WIDTH / 2 - 60, UI_SCREEN_HEIGHT / 2 - 60, paint);
            } else {
                paint.setTextSize(32);
                canvas.drawText(getResources().getString(R.string.screen_done), 156, 208, paint);
            }
            super.onDraw(canvas);
        }
    }
}

