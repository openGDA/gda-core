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
 * This package contains the components required to configure a fully-automated DIAD experiment.
 * The basic blocks are:
 * <ul>
 * <li> {@link IPlan}: the centralised object which coordinates the experiment
 * <li> {@link ISampleEnvironmentVariable} (SEV): Samples an external signal and broadcasts the sample
 * 		(unless it hasn't changed) to registered listeners (segments and triggers)
 * <li> {@link ISegment}: The experiment consists of a series of segments which run sequentially.
 * 		When active, these blocks enable and/or disable triggers.
 * <li> {@link ITrigger}: Triggers a job at a specified point (a function of time or SEV signal)
 * </ul>
 */
package uk.ac.diamond.daq.experiment.api.plan;