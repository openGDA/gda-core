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

	void endCollection();

	void setStandAlone(boolean b);

	void saveCurrentDcf();

	void start();

	void setComment(String s);

	void showSpectra();

	void updateSpectrum();

	void setClearSpectrumBetweenRuns(boolean b);

	void restart();

	void executeCoboldCommandFile(String file);

	void showParams();

	void setSaveDCFsBetweenRuns(boolean b);

	void setZ1(int i);

	void setLmfName(String name);
}
