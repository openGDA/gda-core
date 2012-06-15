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
 * Interface that corresponds to $excalibur_ioc/excaliburApp/Db/mpxiiChipReg.template
 * 
 * @model
 */
public interface MpxiiiChipRegModel extends EObject {
	/**
	 * @model
	 */

	int getDacSense();

	/**
	 * 
	 */

	public void setDacSense(int dacSense);

	/**
	 * @model
	 */

	int getDacSenseDecode();

	/**
	 * 
	 */

	public void setDacSenseDecode(int dacSenseDecode);

	/**
	 * @model
	 */

	String getDacSenseName();

	/**
	 * 
	 */

	public void setDacSenseName(String dacSenseName);

	/**
	 * @model
	 */

	int getDacExternal();

	/**
	 * 
	 */

	public void setDacExternal(int dacExternal);

	/**
	 * @model
	 */

	int getDacExternalDecode();

	/**
	 * 
	 */

	public void setDacExternalDecode(int dacExternalDecode);

	/**
	 * @model
	 */

	String getDacExternalName();

	/**
	 * 
	 */

	public void setDacExternalName(String dacExternalName);

	/**
	 * @model type="AnperModel" containment="true"
	 */

	AnperModel getAnper();

	/**
	 * 
	 */
	public void setAnper(AnperModel anper);

	/**
	 * @model type="PixelModel" containment="true"
	 */

	PixelModel getPixel();

	/**
	 * 
	 */
	public void setPixel(PixelModel pixel);

}
