/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import org.eclipse.january.dataset.DatasetFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.areadetector.v17.NDPva;

/**
 * A basic strategy for collection using the PVA plugin
 */
public class PVAPlugin extends ADDirectReadBase {

	private final NDPva ndPva;

	public PVAPlugin(NDPva ndArray) {
		super(ndArray);
		this.ndPva = ndArray;
	}

	@Override
	public String getName() {
		return "pva";
	}

	@Override
	protected NexusGroupData getData() throws Exception {
		return NexusGroupData.createFromDataset(
				DatasetFactory.createFromObject(
						ndPva.getImageObject(), ndPva.getHeight(), ndPva.getWidth()
						)
				);
	}

	public NDPva getNdPva() {
		return ndPva;
	}
}