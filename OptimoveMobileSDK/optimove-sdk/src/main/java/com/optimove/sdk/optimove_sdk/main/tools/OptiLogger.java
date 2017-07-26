package com.optimove.sdk.optimove_sdk.main.tools;

import android.support.annotation.Nullable;
import android.util.Log;

import com.optimove.sdk.optimove_sdk.BuildConfig;

public final class OptiLogger {

    private OptiLogger() {
    }

    /*****
     * Source functions
     *****/

    public static void v(String tag, @Nullable Throwable throwable, String message, Object... args) {
        if (!BuildConfig.DEBUG) {
            if (throwable != null)
                Log.v(tag, String.format(message, args), throwable);
            else
                Log.v(tag, String.format(message, args));
        }
    }

    public static void i(String tag, @Nullable Throwable throwable, String message, Object... args) {
        if (!BuildConfig.DEBUG) {
            if (throwable != null)
                Log.i(tag, String.format(message, args), throwable);
            else
                Log.i(tag, String.format(message, args));
        }
    }

    public static void d(String tag, @Nullable Throwable throwable, String message, Object... args) {
        if (!BuildConfig.DEBUG) {
            if (throwable != null)
                Log.d(tag, String.format(message, args), throwable);
            else
                Log.d(tag, String.format(message, args));
        }
    }

    public static void w(String tag, @Nullable Throwable throwable, String message, Object... args) {
        if (throwable != null)
            Log.w(tag, String.format(message, args), throwable);
        else
            Log.w(tag, String.format(message, args));
    }

    public static void e(String tag, @Nullable Throwable throwable, String message, Object... args) {
        if (throwable != null)
            Log.e(tag, String.format(message, args), throwable);
        else
            Log.e(tag, String.format(message, args));
    }

    /*****
     * Delegate Functions
     *****/

    public static void v(Class tagClass, Throwable throwable, String message, Object... args) {
        OptiLogger.v(tagClass.getSimpleName(), throwable, String.format(message, args));
    }

    public static void i(Class tagClass, Throwable throwable, String message, Object... args) {
        OptiLogger.i(tagClass.getSimpleName(), throwable, String.format(message, args));
    }

    public static void d(Class tagClass, Throwable throwable, String message, Object... args) {
        OptiLogger.d(tagClass.getSimpleName(), throwable, String.format(message, args));
    }

    public static void w(Class tagClass, Throwable throwable, String message, Object... args) {
        OptiLogger.w(tagClass.getSimpleName(), throwable, String.format(message, args));
    }

    public static void e(Class tagClass, Throwable throwable, String message, Object... args) {
        OptiLogger.e(tagClass.getSimpleName(), throwable, String.format(message, args));
    }

    public static void v(Object tagObject, Throwable throwable, String message, Object... args) {
        OptiLogger.v(tagObject.getClass().getSimpleName(), throwable, String.format(message, args));
    }

    public static void i(Object tagObject, Throwable throwable, String message, Object... args) {
        OptiLogger.i(tagObject.getClass().getSimpleName(), throwable, String.format(message, args));
    }

    public static void d(Object tagObject, Throwable throwable, String message, Object... args) {
        OptiLogger.d(tagObject.getClass().getSimpleName(), throwable, String.format(message, args));
    }

    public static void w(Object tagObject, Throwable throwable, String message, Object... args) {
        OptiLogger.w(tagObject.getClass().getSimpleName(), throwable, String.format(message, args));
    }

    public static void e(Object tagObject, Throwable throwable, String message, Object... args) {
        OptiLogger.e(tagObject.getClass().getSimpleName(), throwable, String.format(message, args));
    }

    public static void v(String tag, String message, Object... args) {
        OptiLogger.v(tag, null, message, args);
    }

    public static void i(String tag, String message, Object... args) {
        OptiLogger.i(tag, null, message, args);
    }

    public static void d(String tag, String message, Object... args) {
        OptiLogger.d(tag, null, message, args);
    }

    public static void w(String tag, String message, Object... args) {
        OptiLogger.w(tag, null, message, args);
    }

    public static void e(String tag, String message, Object... args) {
        OptiLogger.e(tag, null, message, args);
    }

    public static void v(Class tagClass, String message, Object... args) {
        OptiLogger.v(tagClass.getSimpleName(), null, message, args);
    }

    public static void i(Class tagClass, String message, Object... args) {
        OptiLogger.i(tagClass.getSimpleName(), null, message, args);
    }

    public static void d(Class tagClass, String message, Object... args) {
        OptiLogger.d(tagClass.getSimpleName(), null, message, args);
    }

    public static void w(Class tagClass, String message, Object... args) {
        OptiLogger.w(tagClass.getSimpleName(), null, message, args);
    }

    public static void e(Class tagClass, String message, Object... args) {
        OptiLogger.e(tagClass.getSimpleName(), null, message, args);
    }

    public static void v(Object tagObject, String message, Object... args) {
        OptiLogger.v(tagObject.getClass().getSimpleName(), null, message, args);
    }

    public static void i(Object tagObject, String message, Object... args) {
        OptiLogger.i(tagObject.getClass().getSimpleName(), null, message, args);
    }

    public static void d(Object tagObject, String message, Object... args) {
        OptiLogger.d(tagObject.getClass().getSimpleName(), null, message, args);
    }

    public static void w(Object tagObject, String message, Object... args) {
        OptiLogger.w(tagObject.getClass().getSimpleName(), null, message, args);
    }

    public static void e(Object tagObject, String message, Object... args) {
        OptiLogger.e(tagObject.getClass().getSimpleName(), null, message, args);
    }

    public static void v(String tag, Throwable throwable) {
        OptiLogger.v(tag, getMessageFrom(throwable));
    }

    public static void i(String tag, Throwable throwable) {
        OptiLogger.i(tag, getMessageFrom(throwable));
    }

    public static void d(String tag, Throwable throwable) {
        OptiLogger.d(tag, getMessageFrom(throwable));
    }

    public static void w(String tag, Throwable throwable) {
        OptiLogger.w(tag, getMessageFrom(throwable));
    }

    public static void e(String tag, Throwable throwable) {
        OptiLogger.e(tag, getMessageFrom(throwable));
    }

    public static void v(Class tagClass, Throwable throwable) {
        OptiLogger.v(tagClass, getMessageFrom(throwable));
    }

    public static void i(Class tagClass, Throwable throwable) {
        OptiLogger.i(tagClass, getMessageFrom(throwable));
    }

    public static void d(Class tagClass, Throwable throwable) {
        OptiLogger.d(tagClass, getMessageFrom(throwable));
    }

    public static void w(Class tagClass, Throwable throwable) {
        OptiLogger.w(tagClass, getMessageFrom(throwable));
    }

    public static void e(Class tagClass, Throwable throwable) {
        OptiLogger.e(tagClass, getMessageFrom(throwable));
    }

    public static void v(Object tagObject, Throwable throwable) {
        OptiLogger.v(tagObject, getMessageFrom(throwable));
    }

    public static void i(Object tagObject, Throwable throwable) {
        OptiLogger.i(tagObject, getMessageFrom(throwable));
    }

    public static void d(Object tagObject, Throwable throwable) {
        OptiLogger.d(tagObject, getMessageFrom(throwable));
    }

    public static void w(Object tagObject, Throwable throwable) {
        OptiLogger.w(tagObject, getMessageFrom(throwable));
    }

    public static void e(Object tagObject, Throwable throwable) {
        OptiLogger.e(tagObject, getMessageFrom(throwable));
    }

    private static String getMessageFrom(Throwable throwable) {
        return throwable.getMessage() == null ? throwable.getLocalizedMessage() : throwable.getMessage();
    }
}
