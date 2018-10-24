/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device;

import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * interface for switching on and off X-ray beam monitor and allow query of beam status.
 */
public interface IBeamMonitor extends IObservable, Findable {

	/**
	 * switch on beam monitoring
	 */
	public abstract void on();

	/**
	 * switch off beam monitoring
	 */
	public abstract void off();
	/**
	 * check if X-ray beam is on or not
	 * @return true - on, false - off
	 */
	public abstract Boolean isBeamOn();
	/**
	 * test if beam monitor is on or not
	 * @return false - monitor off, true - monitor ON
	 */
	public boolean isMonitorOn();

}