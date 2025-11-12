package org.opengda.detector.electronanalyser.client.sequenceeditor;

public interface AnimationUpdate {

	/*
	 * Override this method to give to the AnimationHandler
	 * what it needs to update the next frame of the animation
	 */
	void update();
}