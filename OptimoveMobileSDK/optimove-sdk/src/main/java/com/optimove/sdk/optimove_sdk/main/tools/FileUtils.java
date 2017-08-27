package com.optimove.sdk.optimove_sdk.main.tools;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class FileUtils {

    public static Reader readFile(Context context) {

        return new Reader(context);
    }

    public static <T> Writer write(Context context, T content) {

        return new Writer<>(context, content);
    }

    public static Deleter deleteFile(Context context) {

        return new Deleter(context);
    }

    private static String getFullFileName(String fileName) {

        return String.format("com_optimove_sdk_%s", fileName);
    }

    private static boolean createFile(File file) {

        if (file.exists())
            return true;
        if (!file.getParentFile().exists()) {
            boolean didMkdirs = file.mkdirs();
            if (!didMkdirs)
                return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            OptiLogger.e(FileUtils.class, e);
            return false;
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static class Reader {

        private Context context;
        private String fileName;
        private FileInputStream fileInputStream;

        private Reader(Context context) {

            this.context = context;
        }

        public Reader named(String fileName) {

            this.fileName = getFullFileName(fileName);
            return this;
        }

        public Reader from(SourceDir sourceDir) {

            if (fileName == null) {
                OptiLogger.e(this, "Missing file name to read from");
                return this;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    if (!file.exists()) {
                        OptiLogger.e(this, "The cache directory has no %s file", fileName);
                        break;
                    }
                    try {
                        fileInputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        OptiLogger.e(this, e);
                    }
                    break;
                case INTERNAL:
                    try {
                        fileInputStream = context.openFileInput(fileName);
                    } catch (FileNotFoundException e) {
                        OptiLogger.e(this, e);
                    }
                    break;
            }
            return this;
        }

        @Nullable
        public String asString() {

            if (fileInputStream == null)
                return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            try {
                StringBuilder dataBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    dataBuilder.append(line);
                return dataBuilder.toString();
            } catch (IOException e) {
                OptiLogger.e(this, e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    OptiLogger.e(this, e);
                }
            }
            return null;
        }

        @Nullable
        public JSONObject asJson() {

            if (fileInputStream == null)
                return null;
            try {
                String jsonString = asString();
                if (jsonString != null)
                    return new JSONObject(jsonString);
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
            return null;
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static class Writer<T> {

        private Context context;
        private String fileName;
        private T content;
        private FileOutputStream fileOutputStream;

        private Writer(Context context, T content) {

            this.context = context;
            this.content = content;
        }

        public Writer<T> to(String fileName) {

            this.fileName = getFullFileName(fileName);
            return this;
        }

        public Writer in(SourceDir sourceDir) {

            if (fileName == null) {
                OptiLogger.e(this, "Missing a file name to write to");
                return this;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    boolean fileExists = createFile(file);
                    if (!fileExists) {
                        OptiLogger.e(this, "File name %s couldn't be created for write operation");
                        break;
                    }
                    try {
                        fileOutputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        OptiLogger.e(this, e);
                    }
                    break;
                case INTERNAL:
                    try {
                        fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        OptiLogger.e(this, e);
                    }
                    break;
            }
            return this;
        }

        public boolean now() {

            if (fileOutputStream == null)
                return false;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            try {
                writer.write(content.toString());
                writer.flush();
                return true;
            } catch (IOException e) {
                OptiLogger.e(this, e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    OptiLogger.e(this, e);
                }
            }
            return false;
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static class Deleter {

        private Context context;
        private String fileName;
        private SourceDir sourceDir;

        private Deleter(Context context) {

            this.context = context;
        }

        public Deleter named(String fileName) {

            this.fileName = getFullFileName(fileName);
            return this;
        }

        public Deleter from(SourceDir sourceDir) {

            this.sourceDir = sourceDir;
            return this;
        }

        public boolean now() {

            if (sourceDir == null) {
                OptiLogger.e(this, "Parent dir wasn't set when attempting to delete");
                return false;
            }
            if (fileName == null) {
                OptiLogger.e(this, "Missing a file name to delete");
                return false;
            }
            switch (sourceDir) {
                case CACHE:
                    File file = new File(context.getCacheDir(), fileName);
                    return !file.exists() || file.delete();
                case INTERNAL:
                    return context.deleteFile(fileName);
            }
            return false;
        }
    }

    public enum SourceDir {
        CACHE, INTERNAL
    }
}
