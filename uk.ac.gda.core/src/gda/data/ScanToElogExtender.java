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

package gda.data;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.DeviceException;
import gda.scan.IScanDataPoint;
import gda.util.ElogEntry;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanToElogExtender extends DataWriterExtenderBase {

	public interface SDP2ElogInfo {
		public String extractInfo(IScanDataPoint sdp);
	}

	private List<SDP2ElogInfo> extractorList;
	
	private Logger logger = LoggerFactory.getLogger(ScanToElogExtender.class);
	
	private IScanDataPoint lastScanDataPoint;
	
	String logID, groupID;

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		lastScanDataPoint = dataPoint;
		super.addData(parent, dataPoint);
	}
	
	@Override
	public void completeCollection(IDataWriterExtender parent) {
		if (lastScanDataPoint == null)
			return;
		String filename = lastScanDataPoint.getCurrentFilename();
		int points = lastScanDataPoint.getCurrentPointNumber() + 1;
		String command = lastScanDataPoint.getCommand();
		String scannumber = lastScanDataPoint.getScanIdentifier();
		String title = "";
		String visit = LocalProperties.get(LocalProperties.GDA_DEF_VISIT, "cm0-0");
		String userID = "gda";
		try {
			title = GDAMetadataProvider.getInstance().getMetadataValue("title");
		} catch (DeviceException e) {
		}
		try {
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException e) {
		}
		try {
			userID = GDAMetadataProvider.getInstance().getMetadataValue("federalid");
		} catch (DeviceException e) {
		}

		String subject = visit + "/" + scannumber + ": " + title + " (" + command + ")";
		
		StringBuilder body = new StringBuilder();

		body.append("Filename: <a href=\"file://");
		body.append(filename);
		body.append("\">");
		body.append(filename);
		body.append("</a><br />\n");
		body.append("<br />\n");
		body.append("Command: ");
		body.append(command);
		body.append("<br />\n");
		body.append("Points: ");
		body.append(points);
		body.append("<br />\n");
		body.append("Title: ");
		body.append(title);
		body.append("<br />\n");
		body.append("<br />\n");

		if (extractorList != null) {
			for(SDP2ElogInfo extractor: extractorList)
				body.append(extractor.extractInfo(lastScanDataPoint));
		}
		
		logger.info("posting to elog with title: {}",title);
		ElogEntry.postAsyn(subject, body.toString(), userID, null, logID, groupID, null);
		
		lastScanDataPoint = null;
		super.completeCollection(parent);
	}

	public String getLogID() {
		return logID;
	}

	public void setLogID(String logID) {
		this.logID = logID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public void setExtractorList(List<SDP2ElogInfo> extractorList) {
		this.extractorList = extractorList;
	}

	public List<SDP2ElogInfo> getExtractorList() {
		return extractorList;
	}
}