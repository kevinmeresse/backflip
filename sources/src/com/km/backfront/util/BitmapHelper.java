package com.km.backfront.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.km.backfront.R;
import com.km.backfront.album.AlbumStorageDirFactory;
import com.km.backfront.album.BaseAlbumDirFactory;
import com.km.backfront.album.FroyoAlbumDirFactory;
import com.km.backfront.album.IImage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class BitmapHelper {
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final String TAG = "BitmapHelper";
	
    // Scale and keep aspect ratio
    static public Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), false);  
    }
 
    // Scale and keep aspect ratio    
    static public Bitmap scaleToFitHeight(Bitmap b, int height) {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, false);  
    }
 
    // Scale and keep aspect ratio
    static public Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getWidth();
        float factorW = width / (float) b.getWidth();
        float factorToUse = (factorH > factorW) ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);  
    }
 
    // Scale and dont keep aspect ratio
    static public Bitmap strechToFill(Bitmap b, int width, int height) {
        float factorH = height / (float) b.getHeight();
        float factorW = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW), (int) (b.getHeight() * factorH), false);  
    }
    
    static public Bitmap cropImageTop(Bitmap bm) {
    	if (bm != null) {
	    	// Create array of pixels (of half the size of whole picture)
	    	int[] pix = new int[bm.getWidth() * bm.getHeight() / 2];
	    	// Extract pixels from Bitmap to the array
	    	bm.getPixels(pix, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight() / 2);
	    	// Create a new Bitmap (of half the size of whole picture)
	    	Bitmap bm2 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight() / 2, Bitmap.Config.ARGB_8888);
			// Copy pixels in the new Bitmap
	    	bm2.setPixels(pix, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight() / 2);
			// Clean
	    	bm.recycle();
			pix = null;
			// Return the final cropped picture
			return bm2;
    	}
    	
    	return null;
    }
    
    static public Bitmap cropImageBottom(Bitmap bm) {
    	if (bm != null) {
	    	// Create array of pixels (of half the size of whole picture)
	    	int[] pix = new int[bm.getWidth() * bm.getHeight() / 2];
	    	// Extract pixels from Bitmap to the array (starting half of the height)
	    	bm.getPixels(pix, 0, bm.getWidth(), 0, bm.getHeight() / 2, bm.getWidth(), bm.getHeight() / 2);
	    	// Create a new Bitmap (of half the size of whole picture)
	    	Bitmap bm2 = Bitmap.createBitmap(bm.getWidth(), bm.getHeight() / 2, Bitmap.Config.ARGB_8888);
			// Copy pixels in the new Bitmap
	    	bm2.setPixels(pix, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight() / 2);
			// Clean
	    	bm.recycle();
			pix = null;
			// Return the final cropped picture
			return bm2;
    	}
    	
    	return null;
    }
    
    static public Bitmap mergeImages(Bitmap bm1, Bitmap bm2) {
    	if (bm1 != null && bm2 != null) {
	    	// Create array of pixels (of twice the size of each picture)
	    	int[] pix = new int[bm1.getWidth() * bm1.getHeight() * 2];
	    	// Extract pixels from 2 pictures to the array
	    	bm1.getPixels(pix, 0, bm1.getWidth(), 0, 0, bm1.getWidth(), bm1.getHeight());
	    	bm2.getPixels(pix, bm1.getWidth() * bm1.getHeight(), bm2.getWidth(), 0, 0, bm2.getWidth(), bm2.getHeight());
	    	// Create a new Bitmap (of twice the size of each picture)
	    	Bitmap bm3 = Bitmap.createBitmap(bm1.getWidth(), bm1.getHeight() * 2, Bitmap.Config.ARGB_8888);
	    	// Copy pixels in the new Bitmap
	    	bm3.setPixels(pix, 0, bm1.getWidth(), 0, 0, bm1.getWidth(), bm1.getHeight() * 2);
	    	// Clean
	    	bm1.recycle();
	    	bm2.recycle();
			pix = null;
			// Return the final merged picture
			return bm3;
    	}
    	
    	return null;
    }
    
    static public void saveImageInGallery(Activity activity, Bitmap pic, String diff) throws IOException, FileNotFoundException {
    	// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_"+diff+JPEG_FILE_SUFFIX;
		File albumF = getAlbumDir(activity);
		String mCurrentPhotoPath = albumF.getAbsolutePath()+"/"+imageFileName;
		
		// Save picture on disk
		FileOutputStream out;
		out = new FileOutputStream(mCurrentPhotoPath);
		pic.compress(Bitmap.CompressFormat.JPEG, 80, out);
		out.close();
		
		// Add picture in Gallery
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    activity.sendBroadcast(mediaScanIntent);
    }
    
    static public void saveImageInGallery(Activity activity, Bitmap pic) throws IOException, FileNotFoundException {
    	// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_"+JPEG_FILE_SUFFIX;
		File albumF = getAlbumDir(activity);
		String mCurrentPhotoPath = albumF.getAbsolutePath()+"/"+imageFileName;
		
		// Save picture on disk
		FileOutputStream out;
		out = new FileOutputStream(mCurrentPhotoPath);
		pic.compress(Bitmap.CompressFormat.JPEG, 100, out);
		out.close();
		
		// Add picture in Gallery
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    activity.sendBroadcast(mediaScanIntent);
    }
    
    static public void saveImageInGallery(Activity activity, byte[] jpegData) throws IOException, FileNotFoundException {
    	// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_"+JPEG_FILE_SUFFIX;
		File albumF = getAlbumDir(activity);
		String mCurrentPhotoPath = albumF.getAbsolutePath()+"/"+imageFileName;
		
		// Save picture on disk
		FileOutputStream out;
		out = new FileOutputStream(mCurrentPhotoPath);
		out.write(jpegData);
		out.close();
		
		// Add picture in Gallery
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    activity.sendBroadcast(mediaScanIntent);
    }
    
    static private File getAlbumDir(Activity activity) {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			AlbumStorageDirFactory mAlbumStorageDirFactory = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
			} else {
				mAlbumStorageDirFactory = new BaseAlbumDirFactory();
			}
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(activity.getString(R.string.photo_album_name));
			
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d(activity.getString(R.string.app_name), "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(activity.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
    
    
    public static Bitmap makeBitmap3(byte[] jpegData, int maxWidth, int degrees) {
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bmp = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		float scale = ((float) maxWidth) / height;
		
		// Create matrix, scale and rotate
		Matrix matrix = new Matrix();
		if (maxWidth < width) {
			matrix.postScale(scale, scale);
		}
		if (degrees != 0) {
			matrix.postRotate(degrees);
		}
		
		// create the new Bitmap object
		return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
    }
    
    
    public static Bitmap makeBitmap2(byte[] jpegData, int minWidth) {
    	try {
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = true;
    		BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
    		if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
    			return null;
    		}
    		options.inSampleSize = computeSampleSize(options, minWidth, IImage.UNCONSTRAINED);
    		options.inJustDecodeBounds = false;
    	
    		options.inDither = false;
    		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    		return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
    	} catch (OutOfMemoryError ex) {
    		Log.e(TAG, "Got Out Of Memory exception ", ex);
    		return null;
		}
	}
    
    public static Bitmap makeBitmap(byte[] jpegData, int maxNumOfPixels) {
    	try {
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = true;
    		BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
    		if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
    			return null;
    		}
    		options.inSampleSize = computeSampleSize(options, IImage.UNCONSTRAINED, maxNumOfPixels);
    		options.inJustDecodeBounds = false;
    	
    		options.inDither = false;
    		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    		return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
    	} catch (OutOfMemoryError ex) {
    		Log.e(TAG, "Got Out Of Memory exception ", ex);
    		return null;
		}
	}
    
    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
    	int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
    	
    	int roundedSize;
    	if (initialSize <= 8) {
    		roundedSize = 1;
    		while (roundedSize < initialSize) {
    			roundedSize <<= 1;
    		}
    	} else {
    		roundedSize = (initialSize + 7) / 8 * 8;
    	}
    	
    	return roundedSize;
    }
    
    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
    	double w = options.outWidth;
    	double h = options.outHeight;
    	
    	int lowerBound = (maxNumOfPixels == IImage.UNCONSTRAINED) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
    	int upperBound = (minSideLength == IImage.UNCONSTRAINED) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
    
    	if (upperBound < lowerBound) {
    		// return the larger one when there is no overlapping zone.
    		return lowerBound;
    	}
    	
    	if ((maxNumOfPixels == IImage.UNCONSTRAINED) && (minSideLength == IImage.UNCONSTRAINED)) {
    		return 1;
    	} else if (minSideLength == IImage.UNCONSTRAINED) {
    		return lowerBound;
    	} else {
    		return upperBound;
    	}
    }
    
    /*private Bitmap createCaptureBitmap(byte[] data) {
        // This is really stupid...we just want to read the orientation in
        // the jpeg header.
        String filepath = ImageManager.getTempJpegPath();
        int degree = 0;
        if (saveDataToFile(filepath, data)) {
            degree = ImageManager.getExifOrientation(filepath);
            new File(filepath).delete();
        }

        // Limit to 50k pixels so we can return it in the intent.
        Bitmap bitmap = makeBitmap(data, 50 * 1024);
        bitmap = rotate(bitmap, degree);
        return bitmap;
    }*/
    
    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        return rotateAndMirror(b, degrees, false);
    }

    // Rotates and/or mirrors the bitmap. If a new bitmap is created, the
    // original bitmap is recycled.
    public static Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror) {
        if ((degrees != 0 || mirror) && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            if (mirror) {
                m.postScale(-1, 1);
                degrees = (degrees + 360) % 360;
                if (degrees == 0 || degrees == 180) {
                    m.postTranslate((float) b.getWidth(), 0);
                } else if (degrees == 90 || degrees == 270) {
                    m.postTranslate((float) b.getHeight(), 0);
                } else {
                    throw new IllegalArgumentException("Invalid degrees=" + degrees);
                }
            }

            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }
    
    public static Bitmap applyGaussianBlur(Bitmap src) {
    	//set gaussian blur configuration
    	double[][] GaussianBlurConfig = new double[][] {
    			{ 1, 2, 1 },
    			{ 2, 4, 2 },
    			{ 1, 2, 1 }
    	};
    	// create instance of Convolution matrix
    	ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
    	// Apply Configuration
    	convMatrix.applyConfig(GaussianBlurConfig);
    	convMatrix.Factor = 16;
    	convMatrix.Offset = 0;
    	//return out put bitmap
    	return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }
}