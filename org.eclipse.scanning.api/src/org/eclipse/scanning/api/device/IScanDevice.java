/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.device;

import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * An {@link IRunnableDevice} that performs a scan when its {@link #run()} method is called.
 * Extends {@link IPositionListenable} so that {@link IPositionListeners} can be added to be
 * informed when positions are performed.
 */
public interface IScanDevice extends IRunnableEventDevice<ScanModel>, IPositionListenable {

}
