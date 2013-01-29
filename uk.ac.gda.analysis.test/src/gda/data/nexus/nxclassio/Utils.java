/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus.nxclassio;

import gda.data.nexus.extractor.NexusExtractorException;

import org.nexusformat.NexusException;

/**
 * Utility functions used in test cases
 *
 */
public class Utils {
	/**
	 * @param fileName
	 * @param dims
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	static public void makeMetaDataTestFile(String fileName, int[] dims) throws NexusException, NexusExtractorException {
		NexusFileHandle nfh = new NexusFileHandle(fileName, true);
		try {
			NexusPath nexusPath = new NexusPath();
			nexusPath.addGroupPath(new NexusGroup("scan1", NexusGroup.NXEntryClassName));
			NXInstrument instrument = new NXInstrument("instrument1", 
					new NXSource("source1",false), 
					new NXAperture(
							"aperture1", new NXGeometry ("geometry1", 
									new NXShape("shape1", "square", 2.0),
									new NXOrientation("orientation1", new double [] {1.0, 2.0}))));
			instrument.addToNexus(nfh, nexusPath);
			
			nexusPath.addGroupPath(new NexusGroup("dataset1", NexusGroup.NXDataClassName));
			{
				int totalLength = NexusFileHandle.calcTotalLength(dims);
				double[] dataIn = new double[totalLength];
				for (int index = 0; index < totalLength; index++) {
					dataIn[index] = index*2;
				}
				nfh.setDoubleData(nexusPath, "heading2", dims, dataIn);
				nfh.setDoubleData(nexusPath, "heading3", dims, dataIn);
			}
		} finally {
			try {
				nfh.close();
			} catch (NexusException e) {
				e.printStackTrace();
			}
		}
	}	
}
