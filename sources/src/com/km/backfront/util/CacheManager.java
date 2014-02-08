package com.km.backfront.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class CacheManager {

    private static final long MAX_SIZE = 5242880L; // 5MB
	private static final String TAG = "CacheManager";

    private CacheManager() {

    }

    public static boolean cachePicture(Context context, Bitmap picture, String filename, Bitmap.CompressFormat format) {
    	
    	String cacheFolder = context.getCacheDir().getAbsolutePath();
    	FileOutputStream out;
    	try {
			out = new FileOutputStream(cacheFolder + "/" + filename);
			return picture.compress(format, 100, out);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Unable to cache picture: "+filename);
			e.printStackTrace();
		}
    	return false;
    }
    
    public static Bitmap retrievePicture(Context context, String filename) {
    	
    	String cacheFolder = context.getCacheDir().getAbsolutePath();
    	return BitmapFactory.decodeFile(cacheFolder + "/" + filename);
    }
    
    public static Bitmap getFilePath(Context context, String filename) {
    	
    	String cacheFolder = context.getCacheDir().getAbsolutePath();
    	return BitmapFactory.decodeFile(cacheFolder + "/" + filename);
    }
    
    public static void cleanAll(Context context) {
    	
    	File cacheDir = context.getCacheDir();

        File[] files = cacheDir.listFiles();

        for (File file : files) {
            file.delete();
        }
    }
    
    public static void cacheData(Context context, byte[] data, String name) throws IOException {

        File cacheDir = context.getCacheDir();
        long size = getDirSize(cacheDir);
        long newSize = data.length + size;

        if (newSize > MAX_SIZE) {
            cleanDir(cacheDir, newSize - MAX_SIZE);
        }

        File file = new File(cacheDir, name);
        FileOutputStream os = new FileOutputStream(file);
        try {
            os.write(data);
        }
        finally {
            os.flush();
            os.close();
        }
    }

    public static byte[] retrieveData(Context context, String name) throws IOException {

        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, name);

        if (!file.exists()) {
            // Data doesn't exist
            return null;
        }

        byte[] data = new byte[(int) file.length()];
        FileInputStream is = new FileInputStream(file);
        try {
            is.read(data);
        }
        finally {
            is.close();
        }

        return data;
    }

    private static void cleanDir(File dir, long bytes) {

        long bytesDeleted = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            bytesDeleted += file.length();
            file.delete();

            if (bytesDeleted >= bytes) {
                break;
            }
        }
    }

    private static long getDirSize(File dir) {

        long size = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }
}