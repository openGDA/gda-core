package org.opengda.detector.electronanalyser.api;
public interface ICopy {
	/**
	 * @return ICopy a copy of this object
	 */
	public ICopy clone();
	/**
	 * @param objectToCopy copy the values from the input object to this object.
	 */
	public void copy(ICopy objectToCopy);
}