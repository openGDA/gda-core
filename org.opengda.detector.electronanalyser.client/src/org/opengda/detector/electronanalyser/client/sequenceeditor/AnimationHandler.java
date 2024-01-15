package org.opengda.detector.electronanalyser.client.sequenceeditor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningFuture;

public class AnimationHandler {

	private static AnimationHandler instance;
	private static final Logger logger = LoggerFactory.getLogger(AnimationHandler.class);

	private int currentFrameIndex = 0;
	private int numberOfFrames = 0;
	private int framesPerSecond = 2;
	private AnimationUpdate animationUpdate;

	private Image[] imageFrames = null;
	private boolean disposeImages = false;
	ListeningFuture<?> task = null;

	private AnimationHandler() {

	}

	public void start() {

		Runnable runnable = () -> {
			logger.debug("Starting animation thread");
			try {
				while (true) {
					Display.getDefault().asyncExec( () -> {
						//As this instance is static, we must be given the overwritten animationUpdate
						//method on what needs to be updated for the animation to display the next frame.
						if (animationUpdate != null) {
							animationUpdate.update();
						}
						else {
							logger.warn("No AnimationUpdate was provided, the thread can't update the animation.");
						}
					});

					final AnimationHandler animationHandler = getInstance();
					animationHandler.currentFrameIndex++;

					if (animationHandler.currentFrameIndex >= numberOfFrames) {
						animationHandler.currentFrameIndex = 0;
					}
					Thread.sleep(1000/animationHandler.getFramesPerSecond());
				}
			}
			catch (InterruptedException e) {
				logger.debug("Finished animation thread");
				currentFrameIndex = 0;
				Thread.currentThread().interrupt();
			}
		};
		task = Async.submit(runnable);
	}

	public boolean isThreadAlive() {
		if (task == null) {
			return false;
		}
		return !task.isDone();
	}

	/**
	 * Signals the thread to stop. It will finish the current execution in its run method and will
	 * then stop.
	 */
	public void cancel() {
		task.cancel(true);
	}

	public static AnimationHandler getInstance() {
		if (instance == null) {
			instance = new AnimationHandler();
		}
		return instance;
	}

	public Image getCurrentImageFrame() {
		if (imageFrames != null) {
			return imageFrames[currentFrameIndex];
		}
		return null;
	}

	/**
	 * Set the image frames to use in the animation. We assume they are system resource
	 * images and therefore not AnimationHandlers responsibility to dispose of images.
	 */
	public void setImageFrames(Image[] imageFrames) {
		if (disposeImages) {
			diposeImageFrames();
		}
		this.imageFrames = imageFrames;
		numberOfFrames = imageFrames.length;
		disposeImages = false;
	}

	/**
	 * Will create and store an array of images using the imageData provided to
	 * use in the animation. The AnimationHandler will dispose of the images the
	 * next time images are set again.
	 */
	public void setImageFrames(ImageData[] imageData) {
		if (disposeImages) {
			diposeImageFrames();
		}
		this.imageFrames = new Image[imageData.length];
		for (int i = 0; i < imageData.length ; i++) {
			imageFrames[i] = new Image(Display.getCurrent(), imageData[i]);
		}
		numberOfFrames = imageFrames.length;
		disposeImages = true;
	}

	private void diposeImageFrames() {
		for (Image image : this.imageFrames) {
			image.dispose();
		}
	}

	public int getCurrentFrameIndex() {
		return currentFrameIndex;
	}

	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public int getFramesPerSecond() {
		return framesPerSecond;
	}

	public void setAnimationUpdate(AnimationUpdate animationUpdate) {
		this.animationUpdate = animationUpdate;
	}

	public boolean getDisposeImages() {
		return disposeImages;
	}
}