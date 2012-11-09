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

import gda.data.ScanToElogExtender.SDP2ElogInfo;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

import java.util.List;

public class ScanInformationTerminalLogger extends DataWriterExtenderBase {

	private List<SDP2ElogInfo> extractorList;
	
	private IScanDataPoint lastScanDataPoint;
	
	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		lastScanDataPoint = dataPoint;
		super.addData(parent, dataPoint);
	}
	
	@Override
	public void completeCollection(IDataWriterExtender parent) {
		if (lastScanDataPoint == null)
			return;
		int points = lastScanDataPoint.getCurrentPointNumber() + 1;
		String title = "";
		try {
			title = GDAMetadataProvider.getInstance().getMetadataValue("title");
		} catch (DeviceException e) {
		}

		StringBuilder body = new StringBuilder();

		body.append("number of scan points: ");
		body.append(points);
		body.append("\n");
		if (title != null && !"".equals(title)) {
			body.append("scan title: ");
			body.append(title);
			body.append("\n");
		}

		if (extractorList != null) {
			for(SDP2ElogInfo extractor: extractorList)
				body.append(
						extractor.extractInfo(lastScanDataPoint)
						);
		}
		
		InterfaceProvider.getTerminalPrinter().print(body.toString());
		
		lastScanDataPoint = null;
		super.completeCollection(parent);
	}

	public void setExtractorList(List<SDP2ElogInfo> extractorList) {
		this.extractorList = extractorList;
	}

	public List<SDP2ElogInfo> getExtractorList() {
		return extractorList;
	}
}