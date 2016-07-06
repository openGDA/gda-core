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

package uk.ac.gda.server.ncd.data;

import org.eclipse.january.dataset.IDataset;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.NXDetectorData;

public class DataHelpers {

	public static IDataset getData(NXDetectorData nd, String det, String key) {
		NexusGroupData parentngd = nd.getData(det, key, NexusExtractor.SDSClassName);
		IDataset result = parentngd.toDataset();
		
		if (result != null) result.setName(det+":"+key);
		return result;
	}
}
