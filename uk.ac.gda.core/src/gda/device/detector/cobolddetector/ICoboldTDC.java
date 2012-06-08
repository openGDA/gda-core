/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.cobolddetector;

/**
 * Interface necessary for Jython scripts to CoboldTDC Update from script definitions in GdaTdcJythonScripts
 */
public interface ICoboldTDC {
	/** 
	 *
	 */
	public void endCollection();

	/**
	 * @param b
	 */
	public void setStandAlone(boolean b);

	/** 
	 *
	 */
	public void saveCurrentDcf();

	/** 
	 *
	 */
	public void start();

	/**
	 * @param s
	 */
	public void setComment(String s);

	/** 
	 *
	 */
	public void showSpectra();

	/** 
	 *
	 */
	public void updateSpectrum();

	/**
	 * @param b
	 */
	public void setClearSpectrumBetweenRuns(boolean b);

	/** 
	 *
	 */
	public void restart();

	/**
	 * @param file
	 */
	public void executeCoboldCommandFile(String file);

	/** 
	 *
	 */
	public void showParams();

	/**
	 * @param b
	 */
	public void setSaveDCFsBetweenRuns(boolean b);

	/**
	 * @param i
	 */
	public void setZ1(int i);

	/**
	 * @param name
	 */
	public void setLmfName(String name);
}
