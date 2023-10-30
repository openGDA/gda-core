/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.device.ethercat;

import java.io.File;
import java.util.Objects;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDFileHDF5;

/**
 * File writer for a detector that is constantly acquiring,
 * manually started and stopped, to be used outside of a scan.
 */
public class SoftwareStartStopHDF5Writer {

	private final NDFileHDF5 fileWriterPlugin;
	private final String fileNamePrefix;

	public SoftwareStartStopHDF5Writer(NDFileHDF5 hdfWriter, String fileNamePrefix) {
		this.fileWriterPlugin = hdfWriter;
		this.fileNamePrefix = fileNamePrefix;
	}

	/**
	 * Start writing to a new file in the given directory.
	 * Writing will continue until {@link #stop()} is invoked.
	 */
	public void start(String directory) throws DeviceException {
		stop(); // in case we were already writing to an older file

		rethrowAsDeviceException(() -> {
			fileWriterPlugin.getFile().getPluginBase().disableCallbacks();
			fileWriterPlugin.setUseSWMR(true);
			new File(directory).mkdirs();
			fileWriterPlugin.setFilePath(directory);
			fileWriterPlugin.setFileName(fileNamePrefix);
			fileWriterPlugin.setAutoIncrement(1); // 1 = Yes
			fileWriterPlugin.setFileTemplate("%s%s%d.hdf5"); // {dir}/{prefix}{file number}.hdf5
			fileWriterPlugin.setNumCapture(0);
			fileWriterPlugin.getFile().setFileWriteMode(FileWriteMode.STREAM);
			fileWriterPlugin.startCapture();
			fileWriterPlugin.getFile().getPluginBase().enableCallbacks();

			// Full file path is computed by the IOC and will be blank
			// when writer starts. We therefore sleep until it appears.
			while (getFilePath().isEmpty()) {
				Thread.sleep(100);
			}
		});

	}

	public void stop() throws DeviceException {
		rethrowAsDeviceException(fileWriterPlugin::stopCapture);
	}

	public String getFilePath() throws DeviceException {
		try {
			return fileWriterPlugin.getFullFileName_RBV();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	/* area detector calls throw generic Exception */
	private interface ExceptionThrowingRunnable {
		void run() throws Exception; // NOSONAR please and thank you
	}

	private void rethrowAsDeviceException(ExceptionThrowingRunnable block) throws DeviceException {
		try {
			block.run();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fileNamePrefix, fileWriterPlugin);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SoftwareStartStopHDF5Writer other = (SoftwareStartStopHDF5Writer) obj;
		return Objects.equals(fileNamePrefix, other.fileNamePrefix) && Objects.equals(fileWriterPlugin, other.fileWriterPlugin);
	}

}
