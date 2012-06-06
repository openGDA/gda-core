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

package gda.data.scan.datawriter;

import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPoint;

import java.util.ArrayList;

public class XasAsciiNexusDatapointCompletingDataWriter implements DataWriter{

	@Override
	public void configureScanNumber(Long scanNumber) throws Exception {
//		sink.configureScanNumber(scanNumber);
		
	}

	DatapointCompletingDataWriter sink;
	DataIndexer indexer;

	public DataIndexer getIndexer() {
		return indexer;
	}

	public void setIndexer(DataIndexer indexer) {
		this.indexer = indexer;
	}

	public XasAsciiNexusDatapointCompletingDataWriter() throws Exception {
		XasAsciiNexusDataWriter xasAsciiNexusDataWriter = new XasAsciiNexusDataWriter();
		sink = new DatapointCompletingDataWriter();
		sink.setDatawriter(xasAsciiNexusDataWriter);
	}

	public void addData(IDataWriterExtender parent, ScanDataPoint dataPoint) throws Exception {
		sink.addData(parent, dataPoint);
	}

	@Override
	public void addData(IScanDataPoint point) throws Exception {
		if(getIndexer() != null)
			indexer.indexData(point);
		sink.addData(point);
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		sink.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void completeCollection() throws Exception {
		sink.completeCollection();
	}

	public void completeCollection(IDataWriterExtender parent) {
		sink.completeCollection(parent);
	}

	@Override
	public boolean equals(Object obj) {
		return sink.equals(obj);
	}

	@Override
	public String getCurrentFileName() {
		return sink.getCurrentFileName();
	}

	@Override
	public String getCurrentScanIdentifier() {
		return sink.getCurrentScanIdentifier();
	}

	public DataWriter getDatawriter() {
		return sink.getDatawriter();
	}

	@Override
	public ArrayList<String> getHeader() {
		return sink.getHeader();
	}

	@Override
	public int hashCode() {
		return sink.hashCode();
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		sink.removeDataWriterExtender(dataWriterExtender);
	}

	public void setDatawriter(DataWriter datawriter) {
		sink.setDatawriter(datawriter);
	}

	@Override
	public void setHeader(ArrayList<String> header) {
		sink.setHeader(header);
	}

	@Override
	public void setHeader(String header) {
		sink.setHeader(header);
	}

	@Override
	public String toString() {
		return sink.toString();
	}
	
}
