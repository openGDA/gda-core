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

import org.eclipse.dawnsci.analysis.api.dataset.*;
import org.eclipse.dawnsci.analysis.dataset.impl.ByteDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LongDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.ShortDataset;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.NXDetectorData;

public class DataHelpers {

	public static IDataset getData(NXDetectorData nd, String det, String key) {
		NexusGroupData parentngd = nd.getData(det, key, NexusExtractor.SDSClassName);
		IDataset result = null;
		
		if (parentngd.getBuffer() instanceof float[]) {
			result = new FloatDataset((float[]) parentngd.getBuffer(), parentngd.dimensions);
		} else if (parentngd.getBuffer() instanceof double[]) {
			result = new DoubleDataset((double[]) parentngd.getBuffer(), parentngd.dimensions);
		} else if (parentngd.getBuffer() instanceof int[]) {
			result = new IntegerDataset((int[]) parentngd.getBuffer(), parentngd.dimensions);
		} else if (parentngd.getBuffer() instanceof long[]) {
			result = new LongDataset((long[]) parentngd.getBuffer(), parentngd.dimensions);
		} else if (parentngd.getBuffer() instanceof byte[]) {
			result = new ByteDataset((byte[]) parentngd.getBuffer(), parentngd.dimensions);
		} else if (parentngd.getBuffer() instanceof short[]) {
			result = new ShortDataset((short[]) parentngd.getBuffer(), parentngd.dimensions);
		} else {
			// FIXME not looking good if we get in here
		}
		
		if (result != null) result.setName(det+":"+key);
		return result;
	}
}
