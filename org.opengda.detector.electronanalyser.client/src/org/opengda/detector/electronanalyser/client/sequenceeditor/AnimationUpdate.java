package org.opengda.detector.electronanalyser.client.sequenceeditor;

public interface AnimationUpdate {

	/*
	 * Override this method to give to the AnimationHandler on what it needs to update
	 * when the next frame of the animation is ready to be displayed
	 */
	public void update();
}