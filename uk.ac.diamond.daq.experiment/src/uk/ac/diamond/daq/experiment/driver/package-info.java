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

/**
 * The experiment driver is the thing that defines the experiment from start to finish.
 * This could be e.g. a strain sequence applied to a sample, some acid being dropped
 * into a piece of material and left for a predetermined amount of time, some fibres
 * being pulled until the snap, etc. The driver may be software or hardware-triggered.
 * <p>
 * Any number of scans may be performed during the experiment, but they are referred to
 * as measurements, not the experiment themselves. The collection of measurements need
 * to be logically grouped to reflect this concept.
 * <p>
 * In a fully-automated mode, measurements are software-triggered based on some signal
 * recorded from the experiment driver. This need not necessarily be the same signal
 * which is being driven e.g. in a strain experiment, we may be controlling the displacement
 * of the mechanical rig, but measurements might be triggered by the load across the sample.
 *
 * @author Douglas Winter
 *
 */
package uk.ac.diamond.daq.experiment.driver;