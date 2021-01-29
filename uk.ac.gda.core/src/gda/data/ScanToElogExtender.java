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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.scan.IScanDataPoint;
import gda.util.ElogEntry;

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
		int scannumber = lastScanDataPoint.getScanIdentifier();
		String title = GDAMetadataProvider.getInstance().getMetadataValue("title");
		String visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		String userID = GDAMetadataProvider.getInstance().getMetadataValue("federalid");

		String subject = visit + "/" + scannumber + ": " + title + " (" + command + ")";

		ElogEntry entry = new ElogEntry(subject, userID, visit, logID, groupID)
				.addHtml("Filename: <a href=\"file://"+filename+"</a>")
				.addText("Command: " + command + "\n"
						+ "Points: " + points + "\n"
						+ "Title: " + title + "\n");

		if (extractorList != null) {
			extractorList.forEach(e -> entry.addText(e.extractInfo(lastScanDataPoint)));
		}

		logger.info("posting to elog with title: {}",title);
		entry.postAsync();

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