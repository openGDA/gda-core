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

package gda.epics.interfaceSpec;

/**
 * Xml Class
 */
public class Xml {
	/**
	 * 
	 */
	public static final String pv_name = "pv";
	/**
	 * 
	 */
	public static final String ro_name = "ro";
	/**
	 * 
	 */
	public static final String desc_name = "desc";
	/**
	 * 
	 */
	public static final String isReadonly_value = "true";
	/**
	 * 
	 */
	public static final String type_name = "type";
	/**
	 * 
	 */
	public static final String isBinary_value = "binary";
	/**
	 * 
	 */
	public static final String devices_name = "devices";
	/**
	 * 
	 */
	public static final String deviceFindByTypeAndName = "//devices/%s[@name='%s']";
	/**
	 * 
	 */
	public static final String deviceFindByName = "//devices/*[@name='%s']";
	/**
	 * 
	 */
	public static final String allDevices = "//devices/*[@name]/@name";

	/**
	 * 
	 */
	public static final String positioner_type_name = "positioner";
	/**
	 * 
	 */
	public static final String simplePvType_type_name = "simplePv";
	/**
	 * 
	 */
	public static final String simpleMotor_type_name = "simpleMotor";
	/**
	 * 
	 */
	public static final String simpleScaler_type_name = "simpleScaler";
	/**
	 * 
	 */
	public static final String pneumatic_type_name = "pneumatic";
	
	/**
	 * 
	 */
	public static final String pneumaticCallback_type_name="pneumaticCallback";

	/**
	 * 
	 */
	public static final String simpleMbbinary_type_name = "simpleMbbinary";
}
