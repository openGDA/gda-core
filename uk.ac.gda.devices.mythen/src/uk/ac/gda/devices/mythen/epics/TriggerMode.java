/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.epics;
/**
 * defines the timing mode of the Mythen Detector
 */
public enum TriggerMode {
	
	/**
	 * Software triggering only
	 */
	AUTO,
	
	/**
	 * Hardware triggered, works only if at least one of the signals is configured as trigger_in
	 */
	TRIGGER,
	/**
	 * works only if at least one of the signals is configured as ro_trigger_in
	 */
	RO_TRIGGER,
	/**
	 * Hardware gating,	works only if at least one of the signals is configured as gate_in
	 */
	GATING,
	
	/**
	 * triggered and gated, works only if one ofthe signals is configured as gate_in and one as trigger_in
	 */
	TRIGGERRED_GATING

}
