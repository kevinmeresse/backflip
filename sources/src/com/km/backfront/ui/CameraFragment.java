package com.km.backfront.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.km.backfront.R;
import com.km.backfront.util.BitmapHelper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback {
	
	private static final String TAG = "CameraFragment";
	
	private static final int IDLE = 1;
    private static final int SNAPSHOT_IN_PROGRESS = 2;
    private static final boolean SWITCH_CAMERA = true;

	private static final String ARG_BACK_PIC_TAKEN = "back_pic_taken";
	private static final String ARG_FRONT_PIC_TAKEN = "front_pic_taken";
    private static int DEFAULT_QUALITY = 60;
    
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    private boolean mStartPreviewFail = false;
    private Parameters mParameters;
    private Parameters mInitialParams;
    private int mStatus = IDLE;
    private boolean mPreviewing;
    private boolean mPausing;
    private boolean mFirstTimeInitialized;
    // Add the media server tag
    public static boolean mMediaServerDied = false;
    private boolean flashIsOn = false;
    
    // Use the ErrorCallback to capture the crash count
    // on the mediaserver
    private final ErrorCallback mErrorCallback = new ErrorCallback();

    Camera mCamera;
    int mNumberOfCameras;
    int mCurrentCamera = 0;  // Camera ID currently chosen
    int mCameraCurrentlyLocked;  // Camera ID that's actually acquired

    // The first rear facing camera
    int backCameraId = -1;
    int frontCameraId = -1;
    
    // Layout objects
    PreviewFrameLayout frameLayout;
    private ImageButton photoBackButton;
	private ImageButton photoFrontButton;
	private ImageButton photoValidateButton;
	private ImageButton cancelButton;
	private ImageButton flashButton;
	private ImageView frontFlash;
	private ImageView photoBackPreview;
	private ImageView photoFrontPreview;
	
	private boolean backPictureTaken = false;
	private boolean frontPictureTaken = false;
	private Bitmap photoBack = null;
	private Bitmap photoFront = null;
	
	private String cacheFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // Retrieve the path to the cache folder
        cacheFolder = ((NewMomentActivity) getActivity()).getCacheDir().getAbsolutePath();
        // And delete the potential old cached pictures
        File oldBackPic = new File(cacheFolder+"/back.png");
        oldBackPic.delete();
        File oldFrontPic = new File(cacheFolder+"/front.png");
        oldFrontPic.delete();
        
        // Discover and assign back and front cameras ids
        discoverCameras();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.i(TAG, "onCreateView");

    	// Restore pictures state
    	if (savedInstanceState != null) {
    		backPictureTaken = savedInstanceState.getBoolean(ARG_BACK_PIC_TAKEN);
    		frontPictureTaken = savedInstanceState.getBoolean(ARG_FRONT_PIC_TAKEN);
        }
    	
    	// Inflate the layout for this fragment
    	View v = inflater.inflate(R.layout.fragment_camera, container, false);
    	
    	// Get the view objects
    	frameLayout = (PreviewFrameLayout) v.findViewById(R.id.frame_layout);
    	mSurfaceView = (SurfaceView) v.findViewById(R.id.camera_preview);
    	photoBackButton = (ImageButton) v.findViewById(R.id.camera_photo_back_button);
    	photoFrontButton = (ImageButton) v.findViewById(R.id.camera_photo_front_button);
    	photoValidateButton = (ImageButton) v.findViewById(R.id.camera_validate_photo_button);
    	cancelButton = (ImageButton) v.findViewById(R.id.camera_cancel_button);
    	flashButton = (ImageButton) v.findViewById(R.id.camera_flash_button);
    	frontFlash = (ImageView) v.findViewById(R.id.front_flash);
    	photoBackPreview = (ImageView) v.findViewById(R.id.preview_image_top);
    	photoFrontPreview = (ImageView) v.findViewById(R.id.preview_image_bottom);
    	
    	// Set up layout if images already exist
    	setupLayout();
    	
    	// Action: cancel
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});
		
		// Action: enable/disable flash
		flashButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mParameters = mCamera.getParameters();
				List<String> flashModes = mParameters.getSupportedFlashModes();
		        if (mCurrentCamera == backCameraId && flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
		        	if (!mParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
			        	mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
						flashButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_flash));
						flashIsOn = true;
		        	} else {
		        		mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
						flashButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_flash_disable));
						flashIsOn = false;
		        	}
		        	mCamera.setParameters(mParameters);
				} else if (mCurrentCamera == frontCameraId) {
					if (flashIsOn) {
						flashButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_flash_disable));
						flashIsOn = false;
					} else {
						flashButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_flash));
						flashIsOn = true;
					}
				} else {
					Toast.makeText(
							getActivity().getApplicationContext(),
							"Flash mode not supported...",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		// Action: take BACK photo
		photoBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera == null)
					return;
				mCamera.takePicture(new Camera.ShutterCallback() {

					@Override
					public void onShutter() {
						
					}

				}, null, new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						/*********************/
						// Raw picture
						/*try {
							BitmapHelper.saveImageInGallery(getActivity(), data);
						} catch (Exception e) {
							e.printStackTrace();
						}
						// Scaled picture with SampleSize
						Bitmap bitmap2 = BitmapHelper.makeBitmap2(data, 600);
						try {
							BitmapHelper.saveImageInGallery(getActivity(), bitmap2, "2");
						} catch (Exception e) {
							e.printStackTrace();
						}
						// Scaled picture with Matrix
						int degrees;
						if (mCurrentCamera == backCameraId) {
							degrees = 90;
						} else {
							degrees = 270;
						}
						Bitmap bitmap3 = BitmapHelper.makeBitmap3(data, 600, degrees);
						try {
							BitmapHelper.saveImageInGallery(getActivity(), bitmap3, "3");
						} catch (Exception e) {
							e.printStackTrace();
						}*/
						/*********************/
						
						// Crop and display
						//photoBack = BitmapHelper.cropImageTop(scalePhoto(data));
						int degrees;
						if (mCurrentCamera == backCameraId) {
							degrees = 90;
						} else {
							degrees = 270;
						}
						photoBack = BitmapHelper.cropImageTop(BitmapHelper.makeBitmap3(data, 600, degrees));
						photoBackPreview.setImageBitmap(photoBack);
						photoBackPreview.setVisibility(View.VISIBLE);
						
						// Save picture in cache
						FileOutputStream out;
						try {
							out = new FileOutputStream(cacheFolder + "/back.png");
							photoBack.compress(Bitmap.CompressFormat.PNG, 100, out);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
						// Display the right button for next step
						if (photoFront == null) {
							photoFrontButton.setVisibility(View.VISIBLE);
							switchCamera();
							photoFrontPreview.setVisibility(View.INVISIBLE);
						} else {
							photoValidateButton.setVisibility(View.VISIBLE);
						}
						photoBackButton.setVisibility(View.INVISIBLE);
						backPictureTaken = true;
					}
				});
			}
		});
		
		// Action: take FRONT photo
		photoFrontButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCamera == null)
					return;
				if (flashIsOn) {
					frontFlash.setVisibility(View.VISIBLE);
				}
				mCamera.takePicture(new Camera.ShutterCallback() {

					@Override
					public void onShutter() {
						
					}

				}, null, new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						frontFlash.setVisibility(View.INVISIBLE);
						// Crop and display
						//photoFront = BitmapHelper.cropImageBottom(scalePhoto(data));
						int degrees;
						if (mCurrentCamera == backCameraId) {
							degrees = 90;
						} else {
							degrees = 270;
						}
						photoFront = BitmapHelper.cropImageBottom(BitmapHelper.makeBitmap3(data, 600, degrees));
						photoFrontPreview.setImageBitmap(photoFront);
						photoFrontPreview.setVisibility(View.VISIBLE);
						
						// Save picture in cache
						FileOutputStream out;
						try {
							out = new FileOutputStream(cacheFolder + "/front.png");
							photoFront.compress(Bitmap.CompressFormat.PNG, 100, out);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						
						// Display the right button for next step
						photoValidateButton.setVisibility(View.VISIBLE);
						photoFrontButton.setVisibility(View.INVISIBLE);
						frontPictureTaken = true;
					}

				});
			}
		});
		
		// Action: validate photo
		photoValidateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (photoBack != null && !photoBack.isRecycled() && photoFront != null && !photoFront.isRecycled()) {
					((NewMomentActivity) getActivity()).setCurrentPhoto(BitmapHelper.mergeImages(photoBack, photoFront));
					// Redirect to the next fragment -> share moment
					Fragment shareMomentFragment = new ShareMomentFragment();
					FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
					transaction.replace(R.id.newMomentContainer, shareMomentFragment);
					transaction.addToBackStack("ShareMomentFragment");
					transaction.commit();
				}
			}
		});
		
		/*
         * To reduce startup time, we start the preview in another thread.
         * We make sure the preview is started at the end of onCreate.
         */
        Thread startPreviewThread = new Thread(new Runnable() {
            public void run() {
            	Log.i(TAG, "startPreviewThread");
                try {
                    mStartPreviewFail = false;
                    startPreview();
                } catch (Exception e) {
                    // In eng build, we throw the exception so that test tool
                    // can detect it and report it
                    if ("eng".equals(Build.TYPE)) {
                        //throw new RuntimeException(e);
                    	Log.e(TAG, "Fail to connect to camera service... "+e.getMessage());
                    }
                    mStartPreviewFail = true;
                }
                if (mCamera != null) {
                	Log.i(TAG, "Camera successfully started :-)");
                } else {
                	Log.i(TAG, "Failed to start camera :-(");
                }
            }
        });
        startPreviewThread.start();

        // don't set mSurfaceHolder here. We have it set ONLY within
        // surfaceChanged / surfaceDestroyed, other parts of the code
        // assume that when it is set, the surface is also set.
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	
        return v;
    }
    
    public void setupLayout() {
    	Log.i(TAG, "setupLayout");
    	if (frontPictureTaken) {
    		if (photoFront == null || photoFront.isRecycled()) {
    			// Get photo from cache disk
    			photoFront = BitmapFactory.decodeFile(cacheFolder + "/front.png");
    		}
    		if (photoFront != null) {
    			photoFrontPreview.setImageBitmap(photoFront);
    			photoFrontPreview.setVisibility(View.VISIBLE);
    			photoFrontButton.setVisibility(View.INVISIBLE);
    		} else {
    			frontPictureTaken = false;
    			photoFrontButton.setVisibility(View.VISIBLE);
    			photoFrontPreview.setVisibility(View.INVISIBLE);
    			mCurrentCamera = frontCameraId;
    		}
    	} else {
    		mCurrentCamera = frontCameraId;
    	}
    	if (backPictureTaken) {
    		if (photoBack == null || photoBack.isRecycled()) {
    			// Get photo from cache disk
    			photoBack = BitmapFactory.decodeFile(cacheFolder + "/back.png");
    		}
    		if (photoBack != null) {
    			photoBackPreview.setImageBitmap(photoBack);
    			photoBackPreview.setVisibility(View.VISIBLE);
    			photoBackButton.setVisibility(View.INVISIBLE);
    		} else {
    			backPictureTaken = false;
    			photoBackButton.setVisibility(View.VISIBLE);
    			photoBackPreview.setVisibility(View.INVISIBLE);
    			photoFrontButton.setVisibility(View.INVISIBLE);
    			mCurrentCamera = backCameraId;
    		}
    	} else {
    		mCurrentCamera = backCameraId;
    	}
    	if (backPictureTaken == true && frontPictureTaken == true) {
    		photoValidateButton.setVisibility(View.VISIBLE);
    	}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	Log.i(TAG, "onResume");

        mPausing = false;
        // Start the preview if it is not started.
        if (!mPreviewing && !mStartPreviewFail) {
            try {
                startPreview();
            } catch (Exception e) {
            	Log.e(TAG, "Exception caused by setPreviewDisplay()", e);
                return;
            }
        }
    }

    @Override
    public void onPause() {
    	Log.i(TAG, "onPause");
    	mPausing = true;
        stopPreview();
        // Close the camera now because other activities may need to use it.
        closeCamera();

        super.onPause();
    }
    
    private void switchCamera() {
    	Log.i(TAG, "switchCamera");
    	
    	stopPreview();
    	closeCamera();
    	if (frontCameraId != -1)
    		mCurrentCamera = frontCameraId;
		restartPreview();
    	
    	/*if (mCamera != null) {
			Log.i(TAG, "onClick - Stopping camera preview");
            mCamera.stopPreview();
            mPreview.setCamera(null);
            Log.i(TAG, "onClick - Releasing camera");
            mCamera.release();
            mCamera = null;
        }

        // Acquire the next camera and request Preview to reconfigure
        // parameters.
        mCurrentCamera = (mCameraCurrentlyLocked + 1) % mNumberOfCameras;
        Log.i(TAG, "onClick - Opening camera ("+mCurrentCamera+")");
        mCamera = Camera.open(mCurrentCamera);
        mCameraCurrentlyLocked = mCurrentCamera;
        mPreview.switchCamera(mCamera);

        // Start the preview
        Log.i(TAG, "onClick - Starting camera preview");
        mCamera.startPreview();*/
    }
    
    private Bitmap scalePhoto(byte[] data) {
    	Log.i(TAG, "scalePhoto");

		// Resize photo from camera byte array
		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
		Bitmap imageScaled = BitmapHelper.scaleToFitWidth(image, 800);
		
		// Override Android default landscape orientation and save portrait
		Matrix matrix = new Matrix();
		if (mCurrentCamera == backCameraId) {
			matrix.postRotate(90);
		} else {
			matrix.postRotate(270);
		}
		Bitmap rotatedScaledImage = Bitmap.createBitmap(imageScaled, 0,
				0, imageScaled.getWidth(), imageScaled.getHeight(),
				matrix, true);
		
		return rotatedScaledImage;
	}
    
    public void discoverCameras() {
    	Log.i(TAG, "discoverCameras");
    	// Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();
        Log.i(TAG, "Found "+mNumberOfCameras+" cameras.");
        
        // Find the ID of the rear-facing ("default") camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
            	mCurrentCamera = backCameraId = i;
            	Log.i(TAG, "- BACK ["+i+"]");
            }
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            	frontCameraId = i;
            	Log.i(TAG, "- FRONT ["+i+"]");
            }
        }
        
        // In case one of them is missing
        if (backCameraId == -1) {
        	backCameraId = mCurrentCamera;
        }
        if (frontCameraId == -1) {
        	frontCameraId = mCurrentCamera;
        }
    }
    
    private void startPreview() throws Exception {
    	Log.i(TAG, "startPreview");
        if (mPausing || isRemoving()) return;

        ensureCameraDevice();

        // If we're previewing already, stop the preview first (this will blank
        // the screen).
        if (mPreviewing) stopPreview();

        setPreviewDisplay(mSurfaceHolder);
        setCameraParameters();

        final long wallTimeStart = SystemClock.elapsedRealtime();
        final long threadTimeStart = Debug.threadCpuTimeNanos();

        mCamera.setErrorCallback(mErrorCallback);

        try {
            Log.v(TAG, "startPreview");
            mCamera.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        mPreviewing = true;
        mStatus = IDLE;

        long threadTimeEnd = Debug.threadCpuTimeNanos();
        long wallTimeEnd = SystemClock.elapsedRealtime();
        if ((wallTimeEnd - wallTimeStart) > 3000) {
            Log.w(TAG, "startPreview() to " + (wallTimeEnd - wallTimeStart)
                    + " ms. Thread time was"
                    + (threadTimeEnd - threadTimeStart) / 1000000 + " ms.");
        }
    }
    
    private void restartPreview() {
    	Log.i(TAG, "restartPreview");
        try {
            startPreview();
        } catch (Exception e) {
        	Log.e(TAG, "Exception caused by setPreviewDisplay()", e);
            return;
        }
    }
    
    private void ensureCameraDevice() throws Exception {
    	Log.i(TAG, "ensureCameraDevice ["+mCurrentCamera+"]");
        if (mCamera == null) {
        	Log.i(TAG, "Camera is null... Let's open it!");
        	mCamera = Camera.open(mCurrentCamera);
            mInitialParams = mCamera.getParameters();
        }
    }
    
    private final class ErrorCallback implements android.hardware.Camera.ErrorCallback {
	    public void onError(int error, android.hardware.Camera camera) {
	        if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
	             mMediaServerDied = true;
	             Log.v(TAG, "media server died");
	        }
	    }
	}
    
    private void closeCamera() {
    	Log.i(TAG, "closeCamera");
        if (mCamera != null) {
        	mCamera.release();
            mCamera = null;
            mPreviewing = false;
        }
    }
    
    private void stopPreview() {
    	Log.i(TAG, "stopPreview");
        if (mCamera != null && mPreviewing) {
            Log.v(TAG, "stopPreview");
            mCamera.stopPreview();
        }
        mPreviewing = false;
    }
    
    private void setPreviewDisplay(SurfaceHolder holder) {
    	Log.i(TAG, "setPreviewDisplay");
    	mCamera.setDisplayOrientation(90);
    	try {
            mCamera.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }
    
    private void setCameraParameters() {
    	Log.i(TAG, "setCameraParameters");
        mParameters = mCamera.getParameters();

        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera application.
        List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            mParameters.setPreviewFrameRate(max);
        }

        // Set picture size.
        List<Size> supported = mParameters.getSupportedPictureSizes();
        Size optimalPicSize = getOptimalPictureSize(supported, 800);
        if (optimalPicSize != null) {
        	Log.d(TAG, "Setting picture size: "+optimalPicSize.width+" x "+optimalPicSize.height);
        	mParameters.setPictureSize(optimalPicSize.width, optimalPicSize.height);
        }
        
        // Set the preview frame aspect ratio according to the picture size.
        Size size = mParameters.getPictureSize();
        Log.d(TAG, "Setting aspect ratio: "+size.width+" / "+size.height+" = "+(double) size.width / size.height);
        frameLayout.setAspectRatio((double) size.width / size.height);
        

        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mParameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, (double) size.width / size.height);
        if (optimalSize != null) {
        	Log.d(TAG, "Setting preview size: "+optimalSize.width+" x "+optimalSize.height);
            mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
        }
        
        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        /*List<Size> sizes = mParameters.getSupportedPreviewSizes();
        Size optimalPreviewSize = getOptimalPreviewSize2(getActivity(), sizes);
        if (optimalPreviewSize != null) {
            //mParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
            mParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        }*/
        
        // Set autofocus
        List<String> focusModes = mParameters.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        	// Autofocus mode is supported
        	mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        
        // Set default settings
        mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
    	mParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
    	mParameters.setExposureCompensation(0);

        // Set JPEG quality.
    	Log.d(TAG, "Setting JPG quality: "+DEFAULT_QUALITY);
        mParameters.setJpegQuality(DEFAULT_QUALITY);

        mCamera.setParameters(mParameters);
    }
    
    private static Point getDisplaySize(Activity activity, Point size) {
    	Log.i(TAG, "getDisplaySize");
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		size.set(metrics.widthPixels, metrics.heightPixels);
		return size;
	}
    
    public static Size getOptimalPictureSize(List<Size> sizes, int reqWidth) {
    	Log.i(TAG, "getOptimalPictureSize [required width: "+reqWidth+"]");
    	final double ASPECT_TOLERANCE = 0.1;
    	if (sizes == null) return null;
    	
    	Size optimalSize = null;
    	double minDiff = Double.MAX_VALUE;
    	
    	double targetRatio = (double) 4/3;
    	Log.d(TAG, "Supported picture sizes:");
    	for (Size size : sizes) {
    		double ratio = (double) size.width / size.height;
    		Log.d(TAG, " - "+size.width+" x "+size.height+" [ratio: "+ratio+"]");
    		if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
    		if (size.width >= reqWidth && Math.abs(size.width - reqWidth) < minDiff) {
    			optimalSize = size;
    			minDiff = Math.abs(size.width - reqWidth);
    		}
    	}
    	
    	if (optimalSize == null) {
    		Log.d(TAG, "Couldn't find one based on width... Trying based on ratio...");
    		minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
            	double ratio = (double) size.width / size.height;
            	if (size.width >= reqWidth && Math.abs(ratio - targetRatio) < minDiff) {
        			optimalSize = size;
        			minDiff = Math.abs(ratio - targetRatio);
        		}
            }
    	}
    	
    	Log.d(TAG, "Optimal size: "+optimalSize);
    	
    	return optimalSize;
    }
    
    private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
    	Log.i(TAG, "getOptimalPreviewSize");
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size
        
        int targetHeight = 0;
        		
        Point point = getDisplaySize(getActivity(), new Point());
        if (point != null && Math.min(point.x, point.y) > 0) {
        	targetHeight = Math.min(point.x, point.y);
        } else {
        	// We don't know the size of SurefaceView, use screen height
            WindowManager windowManager = (WindowManager)
            		getActivity().getSystemService(Context.WINDOW_SERVICE);
            targetHeight = windowManager.getDefaultDisplay().getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.v(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Log.v(TAG, String.format("Optimal preview size is %sx%s", optimalSize.width, optimalSize.height));
        return optimalSize;
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i(TAG, "surfaceCreated");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.i(TAG, "surfaceDestroyed");
        stopPreview();
        mSurfaceHolder = null;
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.i(TAG, "surfaceChanged");
        // Make sure we have a surface in the holder before proceeding.
        if (holder.getSurface() == null) {
            Log.d(TAG, "holder.getSurface() == null");
            return;
        }

        // We need to save the holder for later use, even when the mCameraDevice
        // is null. This could happen if onResume() is invoked after this
        // function.
        mSurfaceHolder = holder;

        // The mCameraDevice will be null if it fails to connect to the camera
        // hardware. In this case we will show a dialog and then finish the
        // activity, so it's OK to ignore it.
        if (mCamera == null) return;

        // Sometimes surfaceChanged is called after onPause or before onResume.
        // Ignore it.
        if (mPausing || isRemoving()) return;

        //if (mPreviewing && holder.isCreating()) {
            // Set preview display if the surface is being created and preview
            // was already started. That means preview display was set to null
            // and we need to set it now.
        	//setPreviewDisplay(holder);
        //} else {
            // 1. Restart the preview if the size of surface was changed. The
            // framework may not support changing preview display on the fly.
            // 2. Start the preview now if surface was destroyed and preview
            // stopped.
            restartPreview();
        //}
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
    	Log.i(TAG, "calculateInSampleSize");
	    // Raw width of image
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (width > reqWidth) {
	
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps
	        // width larger than the requested width.
	        while ((halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");

        // Save the current state of pictures
        outState.putBoolean(ARG_BACK_PIC_TAKEN, backPictureTaken);
        outState.putBoolean(ARG_FRONT_PIC_TAKEN, frontPictureTaken);
    }
}
