/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdata;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;

import java.util.Arrays;

import org.springframework.util.StringUtils;

public class NXDetectorDataFileAppenderForSrs implements NXDetectorDataAppender {


	private final String filename;

	private final String filepathExtraName;

	public NXDetectorDataFileAppenderForSrs(String filename, String filepathExtraName) {
		this.filename = filename;
		this.filepathExtraName = filepathExtraName;
	}

	/**
	 * 
	 */
	@Override
	public void appendTo(NXDetectorData data, String detectorName) {

		assert (data instanceof NXDetectorDataWithFilepathForSrs);

		if (!StringUtils.hasLength(filename)) {
			throw new IllegalArgumentException("filename is null or zero length");
		}
		NXDetectorDataWithFilepathForSrs dataForSrs = (NXDetectorDataWithFilepathForSrs) data;

		NexusTreeNode fileNameNode = dataForSrs.addFileNames(detectorName, "image_data", new String[] { filename },
				true, true);
		fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
				new NexusGroupData(1)));
		// add filename as an NXNote
		dataForSrs.addFileName(detectorName, filename);
		int indexOf = Arrays.asList(data.getExtraNames()).indexOf(filepathExtraName);
		dataForSrs.setFilepathOutputFieldIndex(indexOf);
		data.setPlottableValue(filepathExtraName, 0.);// this is needed as we have added an entry in extraNames

	}

}
