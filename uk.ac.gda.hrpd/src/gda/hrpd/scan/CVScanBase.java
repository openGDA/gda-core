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

package gda.hrpd.scan;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.hrpd.data.MacDataWriter;
import gda.scan.ScanBase;

/**
 * I11 specific scan base - setting data writer
 *
 */
public abstract class CVScanBase extends ScanBase {
	/**
	 * constructor
	 */
	public CVScanBase() {
		super();
	}

	
//	protected void createScanDataPointPipeline() throws Exception {
//		DataWriter dataWriter = DefaultDataWriterFactory.createDataWriterFromFactory();
//		createScanDataPointPipe(dataWriter);
//		
//	}
//	
//	
//	protected void createScanDataPointPipe(DataWriter dataWriter) {
//		if (scannablesCanProvidePositionCallables()) {
//			scanDataPointPipeline = new MultithreadedScanDataPointPipeline(dataWriter, this);
//		} else {
//			scanDataPointPipeline = new BasicScanDataPointPipeline(dataWriter, this);
//		}
//	}
	
	/**
	 * Create a dataHandler object - MacDataWriter
	 * 
	 * @throws Exception
	 */
	@Override
	protected void createScanDataPointPipeline() throws Exception {
		try {
			// determine the format of data being written
			String dataHandlerTypeName = LocalProperties.get("gda.hrpd.data.cvscan.datawriter");
			dataHandlerTypeName = "gda.hrpd.data." + dataHandlerTypeName;
			Class<?> dataHandlerType = Class.forName(dataHandlerTypeName);
			// create a data handler object
			DataWriter dataHandler = (DataWriter) dataHandlerType.newInstance();
			((MacDataWriter)dataHandler).configure();
			Findable filereg = Finder.find("FileRegistrar");
			if (filereg != null) {
				((MacDataWriter) dataHandler).addDataWriterExtender((IDataWriterExtender) filereg);
			}
			super.createScanDataPointPipeline(dataHandler);
			//setDataWriter(dataHandler);
		} catch (IllegalAccessException ex) {
			throw ex;
		} catch (InstantiationException ex) {
			throw ex;
		} catch (ClassNotFoundException ex) {
			throw new Exception("Data format class: " + LocalProperties.get("gda.hrpd.data.cvscan.datawriter")
					+ " not found");
		}
	}

}