/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import uk.ac.diamond.daq.pes.api.IElectronAnalyser;

/**
 * This is intended to be the interface for the clients to interact with the analyser over RMI.
 *
 * The intension is to expand this interface over time, for now it just allows access to the energy range for validating KE in the GUI.
 *
 * @author James Mudd
 */
public interface IVGScientaAnalyserRMI extends IElectronAnalyser {


}
