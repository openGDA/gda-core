package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EObject;

/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

/**
 * @model
 */
public interface PixelModel extends EObject {

	/**
	 * @model
	 */
	short[] getMask();

	void setMask(short[] mask);

	/**
	 * @model
	 */
	short[] getTest();

	void setTest(short[] test);

	/**
	 * @model
	 */
	short[] getGainMode();

	void setGainMode(short[] gainMode);

	/**
	 * @model
	 */
	short[] getThresholdA();

	void setThresholdA(short[] thresholdA);

	/**
	 * @model
	 */
	short[] getThresholdB();

	void setThresholdB(short[] thresholdB);
}