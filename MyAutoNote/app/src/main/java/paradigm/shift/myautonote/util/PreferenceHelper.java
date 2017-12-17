package paradigm.shift.myautonote.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Helper class to get preferences. Mainly for cleaner code while referencing strings from XML.
 * Created by aravind on 12/3/17.
 */

@SuppressWarnings("unused")
public class PreferenceHelper {
    public static boolean getBoolean(Context context, int resId, boolean defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(resId), defaultVal);
    }

    public static int getInt(Context context, int resId, int defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(resId), defaultVal);
    }

    public static float getFloat(Context context, int resId, float defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getFloat(context.getString(resId), defaultVal);
    }

    public static long getLong(Context context, int resId, long defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(resId), defaultVal);
    }

    public static String getString(Context context, int resId, String defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(resId), defaultVal);
    }

    public static void putBoolean(Context context, int resId, boolean val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(resId), val).apply();
    }

    public static void putInt(Context context, int resId, int val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(context.getString(resId), val).apply();
    }

    public static void putFloat(Context context, int resId, float val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putFloat(context.getString(resId), val).apply();
    }

    public static void putLong(Context context, int resId, long val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(context.getString(resId), val).apply();
    }

    public static void putString(Context context, int resId, String val) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(resId), val).apply();
    }

    public static void remove(Context context, int resId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove(context.getString(resId)).apply();
    }
}
