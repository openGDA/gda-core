/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.hrpd.data;

import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.scan.IScanDataPoint;

import java.io.File;

public class FileBasedDataProcessor implements DataProcessing, IDataWriterExtender {
	private IDataWriterExtender extender = new DataWriterExtenderBase();
	protected File inputFile;
	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	protected File outputFile;

	@Override
	public void completeProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	public File processData(File rawdata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		extender.addData(parent, dataPoint);		
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		extender.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		outputFile = processData(inputFile);
		extender.completeCollection(parent);		
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		extender.removeDataWriterExtender(dataWriterExtender);		
	}
}
