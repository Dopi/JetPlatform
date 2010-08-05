/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.layout;

import android.util.Config;
import android.util.Log;

public class WVGALayoutParameters implements LayoutParameters {
    private static final String TAG = "WVGALayoutParameters";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

    private int mType = -1;

    private static final int IMAGE_HEIGHT_LANDSCAPE = 480;
    private static final int TEXT_HEIGHT_LANDSCAPE  = 160;
    private static final int IMAGE_HEIGHT_PORTRAIT  = 800;
    private static final int TEXT_HEIGHT_PORTRAIT   = 400;

    public WVGALayoutParameters(int type) {
        if ((type != WVGA_LANDSCAPE) && (type != WVGA_PORTRAIT)) {
            throw new IllegalArgumentException(
                    "Bad layout type detected: " + type);
        }

        if (LOCAL_LOGV) {
            Log.v(TAG, "WVGALayoutParameters.<init>(" + type + ").");
        }
        mType = type;
    }

    public int getWidth() {
        return mType == WVGA_LANDSCAPE ? WVGA_LANDSCAPE_WIDTH
                                       : WVGA_PORTRAIT_WIDTH;
    }

    public int getHeight() {
        return mType == WVGA_LANDSCAPE ? WVGA_LANDSCAPE_HEIGHT
                                       : WVGA_PORTRAIT_HEIGHT;
    }

    public int getImageHeight() {
        return mType == WVGA_LANDSCAPE ? IMAGE_HEIGHT_LANDSCAPE
                                       : IMAGE_HEIGHT_PORTRAIT;
    }

    public int getTextHeight() {
        return mType == WVGA_LANDSCAPE ? TEXT_HEIGHT_LANDSCAPE
                                       : TEXT_HEIGHT_PORTRAIT;
    }

    public int getType() {
        return mType;
    }

    public String getTypeDescription() {
        return mType == WVGA_LANDSCAPE ? "WVGA-L" : "WVGA-P";
    }
}
