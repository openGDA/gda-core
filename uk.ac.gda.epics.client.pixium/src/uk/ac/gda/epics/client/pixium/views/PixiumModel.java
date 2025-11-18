/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;

public interface PixiumModel {

	void setPUMode(int mode) throws Exception;

	int getPUMode() throws Exception;

	short getCalibrationRequiredState() throws Exception;

	void calibrate() throws Exception;

	short getCalibrateState() throws Exception;

	void stop() throws Exception;

	boolean registerPixiumViewController(IPixiumViewController controller);

	boolean removePixiumViewController(IPixiumViewController controller);
}
