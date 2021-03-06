package com.android.ts_calibrate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;

public class Calibrate {
    private calibration cal;

    public Calibrate() {
        cal = new calibration();
    }

    class calibration {
        int x[] = new int[5];
        int xfb[] = new int[5];
        int y[] = new int[5];
        int yfb[] = new int[5];
        int a[] = new int[7];
    };

    boolean perform_calibration() {
        int j;
        float n, x, y, x2, y2, xy, z, zx, zy;
        float det, a, b, c, e, f, i;
        float scaling = (float)65536.0;

        // Get sums for matrix
        n = x = y = x2 = y2 = xy = 0;
        for (j = 0; j < 5; j++) {
            n += 1.0;
            x += (float)cal.x[j];
            y += (float)cal.y[j];
            x2 += (float)(cal.x[j] * cal.x[j]);
            y2 += (float)(cal.y[j] * cal.y[j]);
            xy += (float)(cal.x[j] * cal.y[j]);
        }

        // Get determinant of matrix -- check if determinant is too small
        det = n * (x2 * y2 - xy * xy) + x * (xy * y - x * y2) + y * (x * xy - y * x2);
        if (det < 0.1 && det > -0.1) {
            Log.i("ts_calibrate: determinant is too small -- %f\n", "" + det);
            return false;
        }

        // Get elements of inverse matrix
        a = (x2 * y2 - xy * xy) / det;
        b = (xy * y - x * y2) / det;
        c = (x * xy - y * x2) / det;
        e = (n * y2 - y * y) / det;
        f = (x * y - n * xy) / det;
        i = (n * x2 - x * x) / det;

        // Get sums for x calibration
        z = zx = zy = 0;
        for (j = 0; j < 5; j++) {
            z += (float)cal.xfb[j];
            zx += (float)(cal.xfb[j] * cal.x[j]);
            zy += (float)(cal.xfb[j] * cal.y[j]);
        }

        // Now multiply out to get the calibration for framebuffer x coord
        cal.a[0] = (int)((a * z + b * zx + c * zy) * (scaling));
        cal.a[1] = (int)((b * z + e * zx + f * zy) * (scaling));
        cal.a[2] = (int)((c * z + f * zx + i * zy) * (scaling));

        System.out.printf("%f %f %f\n", (a * z + b * zx + c * zy), (b * z + e * zx + f * zy), (c
                * z + f * zx + i * zy));

        // Get sums for y calibration
        z = zx = zy = 0;
        for (j = 0; j < 5; j++) {
            z += (float)cal.yfb[j];
            zx += (float)(cal.yfb[j] * cal.x[j]);
            zy += (float)(cal.yfb[j] * cal.y[j]);
        }

        // Now multiply out to get the calibration for framebuffer y coord
        cal.a[3] = (int)((a * z + b * zx + c * zy) * (scaling));
        cal.a[4] = (int)((b * z + e * zx + f * zy) * (scaling));
        cal.a[5] = (int)((c * z + f * zx + i * zy) * (scaling));

        System.out.printf("%f %f %f\n", (a * z + b * zx + c * zy), (b * z + e * zx + f * zy), (c
                * z + f * zx + i * zy));

        // If we got here, we're OK, so assign scaling to a[6] and return
        cal.a[6] = (int)scaling;
        return true;
        /*
         * // This code was here originally to just insert default values
         * for(j=0;j<7;j++) { c->a[j]=0; } c->a[1] = c->a[5] = c->a[6] = 1;
         * return 1;
         */

    }

    void get_sample(int index, int x1, int y1, int x, int y) {
        cal.x[index] = x1;
        cal.y[index] = y1;

        cal.xfb[index] = x;
        cal.yfb[index] = y;
    }

    int calibrate_main() {
        int result = 0;
        if (perform_calibration()) {
            String strPara = String.format("%d %d %d %d %d %d %d", cal.a[1], cal.a[2], cal.a[0],
                    cal.a[4], cal.a[5], cal.a[3], cal.a[6]);

            boolean success = new File("/data/etc").mkdir();
            if (!success) {
                Log.i(this.toString(), "Calibrate.calibrate_main: mkdir /data/etc fail, dir exist");
            }

            File desFile = new File("/data/etc/pointercal");
            if (!desFile.exists())
                try {
                    desFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            FileOutputStream fos;

            try {
                fos = new FileOutputStream(desFile);
                byte[] buf = strPara.getBytes();
                int bytesRead = buf.length;
                try {
                    fos.write(buf, 0, bytesRead);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            result = 0;

        } else {
            result = -1;
        }
        return result;
    }
}
