package com.example.komodo;

import java.io.IOException;

import com.example.huhuhu.R;

import android.app.Activity;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class Player extends Activity implements OnBufferingUpdateListener,
		OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener,
		SurfaceHolder.Callback, MediaControllerView.MediaPlayerControl {

	private static final String TAG = "MediaPlayerDemo";
	public static int screenWidth;
	public static int screenHeight;
	private double videoAspectRatio;
	private double screenAspectRatio;

	private MediaPlayer mMediaPlayer;
	private SurfaceView mPreview;
	private SurfaceHolder holder;
	private MediaControllerView mMediaController;

	private String path = "/storage/sdcard0/a.mp4";

	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	private ProgressBar mProgressBar;
	private boolean isVideoZoomed;
	private int bufferedPercentage;

	private ScaleGestureDetector scaleGestureDetector;

	/**
	 * 
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_main);
		mPreview = (SurfaceView) findViewById(R.id.surface);
		holder = mPreview.getHolder();
		holder.addCallback(this);

		if (mPreview.isHardwareAccelerated())
			System.out.println("PUTAS");
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar.setVisibility(View.GONE);
		isVideoZoomed = false;

		mMediaController = new MediaControllerView(this);
		scaleGestureDetector = new ScaleGestureDetector(this,
				new simpleOnScaleGestureListener());
	}

	private void playVideo() {
		doCleanUp();
		try {
			Log.d(TAG, "Path: " + path);
			mMediaPlayer = new MediaPlayer();

			try {
				mMediaPlayer.setDataSource(path);

			} catch (IOException eio) {
				Log.e(TAG, "error: " + eio.getMessage() + " -- " + path, eio);

			}

			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.prepare();

			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;

			screenAspectRatio = (double) screenWidth / (double) screenHeight;
			videoAspectRatio = (double) mMediaPlayer.getVideoWidth()
					/ (double) mMediaPlayer.getVideoHeight();

			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);
		mMediaController.show();
		return false;
	}

	public class simpleOnScaleGestureListener extends
			SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (detector.getScaleFactor() > 1) {
				if (!isVideoZoomed) {
					isVideoZoomed = true;
					updateVideoSize();
				}
			} else {
				if (isVideoZoomed) {
					isVideoZoomed = false;
					updateVideoSize();
				}
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			
		}

	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		Log.d(TAG, "onBufferingUpdate percent:" + percent);
		bufferedPercentage = percent;

	}

	public void onCompletion(MediaPlayer arg0) {
		Log.d(TAG, "onCompletion called");
	}

	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(TAG, "onVideoSizeChanged called");
		if (width == 0 || height == 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height
					+ ")");
			return;
		}

		mIsVideoSizeKnown = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		Log.d(TAG, "onPrepared called");

		mMediaController.setMediaPlayer(this);
		mMediaController
				.setAnchorView((FrameLayout) findViewById(R.id.container));

		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
		Log.d(TAG, "surfaceChanged called");

	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		Log.d(TAG, "surfaceDestroyed called");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		playVideo();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaPlayer();
		doCleanUp();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}

	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		screenWidth = 0;
		screenHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void updateVideoSize() {
		android.view.ViewGroup.LayoutParams lp = mPreview.getLayoutParams();

		if (screenAspectRatio > videoAspectRatio) {
			if (!isVideoZoomed) {
				System.out.println("CENAS");
				lp.height = screenHeight;
				lp.width = (int) (screenHeight * videoAspectRatio);
			} else {
				lp.height = (int) (screenWidth / videoAspectRatio);
				lp.width = screenWidth;
			}
		} else {
			if (!isVideoZoomed) {
				lp.width = screenWidth;
				lp.height = (int) (screenWidth / videoAspectRatio);
			} else {
				lp.width = (int) (screenHeight * videoAspectRatio);
				lp.height = screenHeight;
			}
		}
		mPreview.setLayoutParams(lp);
	}

	private void startVideoPlayback() {
		Log.v(TAG, "startVideoPlayback");
		updateVideoSize();
		mMediaPlayer.start();
	}

	@Override
	public void start() {
		mMediaPlayer.start();
	}

	@Override
	public void pause() {
		mMediaPlayer.pause();
	}

	@Override
	public int getDuration() {
		return mMediaPlayer.getDuration();
	}

	@Override
	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	@Override
	public void seekTo(int pos) {
		mMediaPlayer.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return bufferedPercentage;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public boolean isFullScreen() {
		return true;
	}

	@Override
	public void toggleFullScreen() {
	}
}