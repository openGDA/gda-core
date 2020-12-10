/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

/**
 * Used to indicate the overall health state of the beamline and the components that are configured as relevant to the
 * the beamline's status.<br>
 * <ul>
 * <li>ERROR indicates that the beamline is not in a usable state</li>
 * <li>WARNING indicates that the beamline is usable, but some scannables may not be in their ideal state</li>
 * <li>NOT_CHECKED is only applicable to individual components and indicates that the condition for that component is
 * disabled and therefore the component's state has not been checked.</li>
 * <ul>
 */
public enum BeamlineHealthState {
	ERROR, WARNING, NOT_CHECKED, OK;
}