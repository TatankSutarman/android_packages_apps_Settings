/*
 * Copyright (C) 2026 The LineageOS Project
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

package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/**
 * Toggle for the SystemUI Dynamic Bar.
 *
 * <p>The Dynamic Bar is deliberately opt-in: enabling it makes SystemUI render ongoing activities
 * around the camera cutout and suppresses the corresponding built-in status-bar chips.
 */
public class DynamicBarPreferenceController extends TogglePreferenceController {

    public static final String KEY_DYNAMIC_BAR_ENABLED = "ax_dynamic_bar_enabled";

    public DynamicBarPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                KEY_DYNAMIC_BAR_ENABLED,
                /* def= */ 0,
                UserHandle.myUserId())
                == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                KEY_DYNAMIC_BAR_ENABLED,
                isChecked ? 1 : 0,
                UserHandle.myUserId());
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }
}
