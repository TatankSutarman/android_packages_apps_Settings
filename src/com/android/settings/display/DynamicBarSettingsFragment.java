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

import android.app.settings.SettingsEnums;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Configures the SystemUI Dynamic Bar and the event sources it is allowed to display.
 *
 * <p>The Dynamic Bar stores a JSON array of <em>disabled</em> event type IDs in
 * {@code ax_dynamic_bar_events}. This screen presents the inverse, user-facing switches, so an
 * enabled switch means that the event type is absent from the stored array.
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DynamicBarSettingsFragment extends DashboardFragment {

    private static final String TAG = "DynamicBarSettings";

    private static final String KEY_ENABLED = "ax_dynamic_bar_enabled";
    private static final String KEY_LOCKSCREEN_MEDIA_ENABLED =
            "infinity_bar_lockscreen_media_enabled";
    private static final String KEY_LOCKSCREEN_WEATHER_ENABLED =
            "infinity_bar_lockscreen_weather_enabled";
    private static final String KEY_LOCKSCREEN_WEATHER_SETTINGS =
            "infinity_bar_lockscreen_weather_settings";
    private static final String OMNIJAWS_PACKAGE = "org.omnirom.omnijaws";
    private static final String OMNIJAWS_SETTINGS_CLASS =
            "org.omnirom.omnijaws.SettingsActivity";
    private static final String KEY_EVENTS = "ax_dynamic_bar_events";
    private static final String KEY_ENABLE_ALL = "dynamic_bar_enable_all";
    private static final String KEY_RESET_DEFAULTS = "dynamic_bar_reset_defaults";
    private static final String EVENT_PREFERENCE_PREFIX = "dynamic_bar_event_";

    /*
     * Keep this list in sync with EVENT_TYPE_IDS in SystemUI's IslandActions.kt. Sports is
     * intentionally absent: QuickLook/sports integration is not enabled in this build.
     */
    private static final String[] EVENT_TYPE_IDS = {
            "media",
            "call",
            "notification",
            "audio_recording",
            "aosp_chip",
            "promoted_ongoing",
            "charging",
            "bluetooth",
            "hotspot",
            "vpn",
            "alarm",
            "timer",
            "stopwatch",
            "ringer",
            "torch",
            "biometric_unlock",
            "clipboard",
            "app_switch",
    };

    /*
     * Keep this in sync with DEFAULT_DISABLED_EVENT_TYPES in SystemUI's
     * AxDynamicBarSettings.kt. The array is only used when the secure setting has never been
     * written.
     */
    private static final Set<String> DEFAULT_DISABLED_EVENT_TYPES = new HashSet<>(Arrays.asList(
            "bluetooth",
            "hotspot",
            "vpn",
            "alarm",
            "timer",
            "stopwatch",
            "ringer",
            "clipboard",
            "app_switch",
            "torch",
            "biometric_unlock",
            "sports",
            "promoted_ongoing"));

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.ax_dynamic_bar_title);
        }

        final SwitchPreferenceCompat enabled = findPreference(KEY_ENABLED);
        if (enabled != null) {
            enabled.setPersistent(false);
            enabled.setOnPreferenceChangeListener((preference, newValue) -> {
                final boolean value = (Boolean) newValue;
                final boolean written = Settings.Secure.putIntForUser(
                        requireContext().getContentResolver(),
                        KEY_ENABLED,
                        value ? 1 : 0,
                        UserHandle.myUserId());
                if (!written) {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return written;
            });
        }

        final SwitchPreferenceCompat lockscreenMedia =
                findPreference(KEY_LOCKSCREEN_MEDIA_ENABLED);
        if (lockscreenMedia != null) {
            lockscreenMedia.setPersistent(false);
            lockscreenMedia.setOnPreferenceChangeListener((preference, newValue) -> {
                final boolean value = (Boolean) newValue;
                final boolean written = Settings.Secure.putIntForUser(
                        requireContext().getContentResolver(),
                        KEY_LOCKSCREEN_MEDIA_ENABLED,
                        value ? 1 : 0,
                        UserHandle.myUserId());
                if (!written) {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return written;
            });
        }

        final SwitchPreferenceCompat lockscreenWeather =
                findPreference(KEY_LOCKSCREEN_WEATHER_ENABLED);
        if (lockscreenWeather != null) {
            lockscreenWeather.setPersistent(false);
            lockscreenWeather.setOnPreferenceChangeListener((preference, newValue) -> {
                final boolean value = (Boolean) newValue;
                final boolean written = Settings.Secure.putIntForUser(
                        requireContext().getContentResolver(),
                        KEY_LOCKSCREEN_WEATHER_ENABLED,
                        value ? 1 : 0,
                        UserHandle.myUserId());
                if (!written) {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return written;
            });
        }

        final Preference weatherSettings = findPreference(KEY_LOCKSCREEN_WEATHER_SETTINGS);
        if (weatherSettings != null) {
            weatherSettings.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(Intent.ACTION_MAIN)
                        .setClassName(OMNIJAWS_PACKAGE, OMNIJAWS_SETTINGS_CLASS);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException | SecurityException e) {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_lockscreen_weather_unavailable,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        for (String eventType : EVENT_TYPE_IDS) {
            final SwitchPreferenceCompat preference =
                    findPreference(EVENT_PREFERENCE_PREFIX + eventType);
            if (preference == null) {
                continue;
            }
            preference.setPersistent(false);
            preference.setOnPreferenceChangeListener((changed, newValue) -> {
                final Set<String> disabled = readDisabledEventTypes();
                if ((Boolean) newValue) {
                    disabled.remove(eventType);
                } else {
                    disabled.add(eventType);
                }
                final boolean written = writeDisabledEventTypes(disabled);
                if (!written) {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return written;
            });
        }

        final Preference enableAll = findPreference(KEY_ENABLE_ALL);
        if (enableAll != null) {
            enableAll.setOnPreferenceClickListener(preference -> {
                final boolean written = writeDisabledEventTypes(
                        Collections.<String>emptySet());
                if (written) {
                    syncPreferences();
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_all_enabled,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        final Preference resetDefaults = findPreference(KEY_RESET_DEFAULTS);
        if (resetDefaults != null) {
            resetDefaults.setOnPreferenceClickListener(preference -> {
                final boolean written = writeDisabledEventTypes(
                        new HashSet<>(DEFAULT_DISABLED_EVENT_TYPES));
                if (written) {
                    syncPreferences();
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_reset_done,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(),
                            R.string.ax_dynamic_bar_events_write_failed,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        syncPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncPreferences();
    }

    private void syncPreferences() {
        final SwitchPreferenceCompat enabled = findPreference(KEY_ENABLED);
        if (enabled != null) {
            enabled.setChecked(isDynamicBarEnabled());
        }

        final SwitchPreferenceCompat lockscreenMedia =
                findPreference(KEY_LOCKSCREEN_MEDIA_ENABLED);
        if (lockscreenMedia != null) {
            lockscreenMedia.setChecked(isLockscreenMediaEnabled());
        }

        final SwitchPreferenceCompat lockscreenWeather =
                findPreference(KEY_LOCKSCREEN_WEATHER_ENABLED);
        if (lockscreenWeather != null) {
            lockscreenWeather.setChecked(isLockscreenWeatherEnabled());
        }

        final Preference weatherSettings = findPreference(KEY_LOCKSCREEN_WEATHER_SETTINGS);
        if (weatherSettings != null) {
            weatherSettings.setEnabled(isOmniJawsInstalled());
        }

        final Set<String> disabled = readDisabledEventTypes();
        for (String eventType : EVENT_TYPE_IDS) {
            final SwitchPreferenceCompat preference =
                    findPreference(EVENT_PREFERENCE_PREFIX + eventType);
            if (preference != null) {
                preference.setChecked(!disabled.contains(eventType));
            }
        }
    }

    private boolean isDynamicBarEnabled() {
        return Settings.Secure.getIntForUser(
                requireContext().getContentResolver(),
                KEY_ENABLED,
                0,
                UserHandle.myUserId()) == 1;
    }

    private boolean isLockscreenMediaEnabled() {
        return Settings.Secure.getIntForUser(
                requireContext().getContentResolver(),
                KEY_LOCKSCREEN_MEDIA_ENABLED,
                0,
                UserHandle.myUserId()) == 1;
    }

    private boolean isLockscreenWeatherEnabled() {
        return Settings.Secure.getIntForUser(
                requireContext().getContentResolver(),
                KEY_LOCKSCREEN_WEATHER_ENABLED,
                0,
                UserHandle.myUserId()) == 1;
    }

    private boolean isOmniJawsInstalled() {
        try {
            requireContext().getPackageManager().getPackageInfo(
                    OMNIJAWS_PACKAGE, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException | SecurityException e) {
            return false;
        }
    }

    private Set<String> readDisabledEventTypes() {
        final String raw = Settings.Secure.getStringForUser(
                requireContext().getContentResolver(),
                KEY_EVENTS,
                UserHandle.myUserId());
        if (TextUtils.isEmpty(raw) || raw.trim().isEmpty()) {
            return new HashSet<>(DEFAULT_DISABLED_EVENT_TYPES);
        }

        try {
            final JSONArray array = new JSONArray(raw);
            final Set<String> disabled = new HashSet<>();
            for (int i = 0; i < array.length(); i++) {
                final String eventType = array.optString(i);
                if (!TextUtils.isEmpty(eventType)) {
                    disabled.add(eventType);
                }
            }
            return disabled;
        } catch (JSONException e) {
            return new HashSet<>(DEFAULT_DISABLED_EVENT_TYPES);
        }
    }

    private boolean writeDisabledEventTypes(Set<String> disabled) {
        final JSONArray array = new JSONArray();
        for (String eventType : new TreeSet<>(disabled)) {
            array.put(eventType);
        }
        return Settings.Secure.putStringForUser(
                requireContext().getContentResolver(),
                KEY_EVENTS,
                array.toString(),
                UserHandle.myUserId());
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.dynamic_bar_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DISPLAY;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.dynamic_bar_settings);
}
