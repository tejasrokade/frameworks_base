/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.internal.hardware;

import com.android.internal.R;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

public class AmbientDisplayConfiguration {

    private final Context mContext;
    private final boolean mAlwaysOnByDefault;

    public AmbientDisplayConfiguration(Context context) {
        mContext = context;
        mAlwaysOnByDefault = mContext.getResources().getBoolean(R.bool.config_dozeAlwaysOnEnabled);
    }

    public boolean enabled(int user) {
        return pulseOnNotificationEnabled(user)
                || pulseOnPickupEnabled(user)
                || pulseOnDoubleTapEnabled(user)
                || pulseOnLongPressEnabled(user)
                || pulseOnCustomDozeEventEnabled(user)
                || pulseOnMedia(user)
                || alwaysOnEnabled(user);
    }

    public boolean available() {
        return pulseOnNotificationAvailable() || pulseOnPickupAvailable()
                || pulseOnDoubleTapAvailable();
    }

    public boolean pulseOnNotificationEnabled(int user) {
        return boolSettingDefaultOff(Settings.Secure.DOZE_ENABLED, user) && pulseOnNotificationAvailable();
    }

    public boolean pulseOnNotificationAvailable() {
        return ambientDisplayAvailable();
    }

    private boolean pulseOnCustomDozeEventEnabled(int user) {
        return (Settings.System.getInt(mContext.getContentResolver(), Settings.System.CUSTOM_AMBIENT_POCKETMODE_GESTURE, 0) != 0
                || Settings.System.getInt(mContext.getContentResolver(), Settings.System.CUSTOM_AMBIENT_HANDWAVE_GESTURE, 0) != 0)
                && pulseOnNotificationAvailable();
    }

    public boolean pulseOnMedia(int user) {
        boolean enabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.FORCE_AMBIENT_FOR_MEDIA, 1, user) != 0;
        return enabled && ambientDisplayAvailable();
    }

    public boolean canForceDozeNotifications() {
        return mContext.getResources().getBoolean(R.bool.config_canForceDozeNotifications);
    }

    public boolean pulseOnPickupEnabled(int user) {
        boolean settingEnabled = boolSettingDefaultOff(Settings.Secure.DOZE_PULSE_ON_PICK_UP, user);
        return (settingEnabled || alwaysOnEnabled(user)) && pulseOnPickupAvailable();
    }

    public boolean pulseOnPickupAvailable() {
        return dozePulsePickupSensorAvailable() && ambientDisplayAvailable();
    }

    public boolean dozePulsePickupSensorAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_dozePulsePickup);
    }

    public boolean pulseOnPickupCanBeModified(int user) {
        return !alwaysOnEnabled(user);
    }

    public boolean pulseOnDoubleTapEnabled(int user) {
        return boolSettingDefaultOff(Settings.Secure.DOZE_PULSE_ON_DOUBLE_TAP, user)
                && pulseOnDoubleTapAvailable();
    }

    public boolean pulseOnDoubleTapAvailable() {
        return doubleTapSensorAvailable() && ambientDisplayAvailable();
    }

    public boolean doubleTapSensorAvailable() {
        return !TextUtils.isEmpty(doubleTapSensorType());
    }

    public String doubleTapSensorType() {
        return mContext.getResources().getString(R.string.config_dozeDoubleTapSensorType);
    }

    public String longPressSensorType() {
        return mContext.getResources().getString(R.string.config_dozeLongPressSensorType);
    }

    public boolean pulseOnLongPressEnabled(int user) {
        return pulseOnLongPressAvailable() && boolSettingDefaultOff(
                Settings.Secure.DOZE_PULSE_ON_LONG_PRESS, user);
    }

    private boolean pulseOnLongPressAvailable() {
        return !TextUtils.isEmpty(longPressSensorType());
    }

    public boolean alwaysOnEnabled(int user) {
        return alwaysOnEnabledSetting(user) || alwaysOnChargingEnabled(user);
    }

    public boolean alwaysOnAvailable() {
        return (alwaysOnDisplayDebuggingEnabled() || alwaysOnDisplayAvailable())
                && ambientDisplayAvailable();
    }

    public boolean alwaysOnAvailableForUser(int user) {
        return alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public String ambientDisplayComponent() {
        return mContext.getResources().getString(R.string.config_dozeComponent);
    }

    public boolean accessibilityInversionEnabled(int user) {
        return boolSettingDefaultOff(Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, user);
    }

    public boolean ambientDisplayAvailable() {
        return !TextUtils.isEmpty(ambientDisplayComponent());
    }

    private boolean alwaysOnDisplayAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_dozeAlwaysOnDisplayAvailable);
    }

    private boolean alwaysOnDisplayDebuggingEnabled() {
        return SystemProperties.getBoolean("debug.doze.aod", false) && Build.IS_DEBUGGABLE;
    }


    private boolean boolSettingDefaultOn(String name, int user) {
        return boolSetting(name, user, 1);
    }

    private boolean boolSettingDefaultOff(String name, int user) {
        return boolSetting(name, user, 0);
    }

    private boolean boolSetting(String name, int user, int def) {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(), name, def, user) != 0;
    }

    // omni additions start
    private boolean boolSettingSystem(String name, int user, int def) {
        return Settings.System.getIntForUser(mContext.getContentResolver(), name, def, user) != 0;
    }

    public boolean alwaysOnEnabledSetting(int user) {
        boolean alwaysOnEnabled = boolSetting(Settings.Secure.DOZE_ALWAYS_ON, user, mAlwaysOnByDefault ? 1 : 0);
        return alwaysOnEnabled && alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public boolean alwaysOnChargingEnabled(int user) {
        final boolean dozeOnChargeEnabled = boolSettingSystem(Settings.System.OMNI_DOZE_ON_CHARGE, user, 0);
        if (dozeOnChargeEnabled) {
            final boolean dozeOnChargeEnabledNow = boolSettingSystem(Settings.System.OMNI_DOZE_ON_CHARGE_NOW, user, 0);
            return dozeOnChargeEnabledNow && alwaysOnAvailable() && !accessibilityInversionEnabled(user);
        }
        return false;
    }
}
