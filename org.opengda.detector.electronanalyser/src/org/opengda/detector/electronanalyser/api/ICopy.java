package org.opengda.detector.electronanalyser.api;

public interface ICopy {

	/**
	 * @return ICopy a copy of this object
	 */
	ICopy clone();

	/**
	 * @param objectToCopy copy the values from the input object to this object.
	 */
	void copy(ICopy objectToCopy);
}