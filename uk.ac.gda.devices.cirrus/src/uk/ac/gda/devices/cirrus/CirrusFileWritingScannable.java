/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.cirrus;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.scannable.ScannableBase;
import gda.jython.InterfaceProvider;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zero-input, zero-output scannable which will run the Cirrus Microreactor during scans asynchronously to the scan
 * itself and write masses out to file every second during the scan.
 */
public class CirrusFileWritingScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(CirrusFileWritingScannable.class);
	
	private final CirrusDetector cirrus;

	private volatile boolean scanComplete;

	private String filename;

	private Thread cirrusThread;

	public CirrusFileWritingScannable(CirrusDetector cirrus) {
		this.cirrus = cirrus;
		inputNames = new String[] {};
		extraNames = new String[] {};
		outputFormat = new String[] {};
	}

	@Override
	public void atScanStart() throws DeviceException {
		scanComplete = false;
		int scanNumber = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation()
				.getScanNumber();
		String dataDir = PathConstructor.createFromDefaultProperty();
		filename = dataDir + scanNumber + "_cirrus.dat";
		
		startCirrusThread();
	}

	private void startCirrusThread() {
		cirrusThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Path path = Paths.get(filename);
				try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(path,
						StandardOpenOption.CREATE, StandardOpenOption.APPEND)))
				{
						
					String columnHeaders = new String();
					columnHeaders = "Time\t";
					Integer[] masses = cirrus.getMasses();
					for (Integer mass : masses) {
						columnHeaders += mass + "\t";
					}
					columnHeaders.trim();
					columnHeaders += "\n";
					out.write(columnHeaders.getBytes(), 0, columnHeaders.getBytes().length);
					
					while (!scanComplete) {
						cirrus.setCollectionTime(1);
						cirrus.collectData();
						cirrus.waitWhileBusy();

						String buf = new String();
						Date now = new Date();
						String nowString = now.toString();
						buf += nowString + "\t";

						Double[] pressures = ((NXDetectorData) cirrus.readout()).getDoubleVals();
						for (Double pressure : pressures) {
							buf += String.format("%.2f", pressure) + "\t";
						}
						buf.trim();
						buf += "\n";

						out.write(buf.getBytes(), 0, buf.getBytes().length);
					}
				} catch (Exception e) {
					logger.error("Exception in Cirrus thread - stopping reading mass spec data",e);
				}
			}
		});

		cirrusThread.start();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		scanComplete = true;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}
