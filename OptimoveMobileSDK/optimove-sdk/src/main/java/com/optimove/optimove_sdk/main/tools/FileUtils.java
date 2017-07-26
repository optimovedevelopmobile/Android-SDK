package com.optimove.optimove_sdk.main.tools;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {

    public static boolean writeJsonToFile(Context context, String fileName, JSONObject data) {

        try {
            FileOutputStream outputStream = context.openFileOutput(getFullFileName(fileName), Context.MODE_PRIVATE);
            writeJson(outputStream, data);
            return true;
        } catch (IOException e) {
            OptiLogger.e(FileUtils.class, e);
        }
        return false;
    }

    @Nullable
    public static JSONObject readJsonFromFile(Context context, String fileName) {

        try {
            return readJson(context.openFileInput(fileName));
        } catch (JSONException | IOException e) {
            OptiLogger.e(FileUtils.class, e);
        }
        return null;
    }

    private static String getFullFileName(String fileName) {

        return String.format("OptimoveAnalytics_%s", fileName);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static void writeJson(FileOutputStream out, JSONObject data) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        try {
            writer.write(data.toString());
            writer.flush();
        } finally {
            writer.close();
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static JSONObject readJson(FileInputStream in) throws IOException, JSONException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            StringBuilder rawJsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                rawJsonBuilder.append(line);
            return new JSONObject(rawJsonBuilder.toString());
        } finally {
            reader.close();
        }
    }
}
