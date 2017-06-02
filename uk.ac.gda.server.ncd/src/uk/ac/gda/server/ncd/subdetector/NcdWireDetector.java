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

package uk.ac.gda.server.ncd.subdetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.device.memory.Gdhist;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to represent a detector for NCD.
 */
public class NcdWireDetector extends NcdSubDetector implements INcdSubDetector {

	private static final Logger logger = LoggerFactory.getLogger(NcdWireDetector.class);
	protected Gdhist memory = null;
	protected boolean transposedData = false;
	private double pixelsize;

	public boolean isTransposedData() {
		return transposedData;
	}

	public void setTransposedData(boolean transposedData) {
		this.transposedData = transposedData;
	}

	@Override
	public void configure() throws FactoryException {
		if (memory != null) {
			memory.reconfigure();
		} else {
			throw new FactoryException("no memory configured!");
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("NcdDetector reconfiguring " + getName());
		configure();
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		int[] devicedims = memory.getDimension();
		int[] datadims = devicedims;
		float[] data = readFloat(0, 0, 0, devicedims[0], devicedims[1], frames);

		if (datadims[0] == 1 || datadims[1] == 1) {
			int channels = datadims[0] * datadims[1];
			datadims = new int[] { channels };
			if (transposedData) {
				float[] cooked = new float[frames * channels];
				int i = 0;
				for (int chan = 0; chan < channels; chan++) {
					for (int frame = 0; frame < frames; frame++) {
						cooked[frame * channels + chan] = data[i];
						i++;
					}
				}
				data = cooked;
			}
		}

		datadims = ArrayUtils.add(datadims, 0, frames);

		NexusGroupData ngd = new NexusGroupData(datadims, data);
		ngd.isDetectorEntryData = true;
		nxdata.addData(getTreeName(), ngd, "counts", 1, getInterpretation());

		addMetadata(nxdata);
	}

	@Override
	public void close() throws DeviceException {
		logger.debug("NcdDetector closing " + getName());
		if (memory != null) {
			memory.close();
		}

		configured = false;
	}

	/**
	 * @return Returns the memory.
	 */
	public Gdhist getMemory() {
		return memory;
	}

	/**
	 * @param memory
	 *            The memory to set.
	 */
	public void setMemory(Gdhist memory) {
		this.memory = memory;
	}

	@Override
	public void clear() throws DeviceException {
		memory.clear();
	}

	@Override
	public int getMemorySize() throws DeviceException {
		return memory.getMemorySize();
	}

	@Override
	public void start() throws DeviceException {
		memory.start();
	}

	@Override
	public void stop() throws DeviceException {
		memory.stop();
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dimension = memory.getDimension();
		return new int[] { dimension[0], dimension[1] };
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
		memory.setDimension(detectorSize);
	}

	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		return memory.read(x, y, t, dx, dy, dt);
	}

	public double[] read(int frame) throws DeviceException {
		return memory.read(frame);
	}

	public float[] readFloat(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		return memory.readFloat(x, y, t, dx, dy, dt);
	}

	public float[] readFloat(int frame) throws DeviceException {
		return memory.readFloat(frame);
	}

	@Override
	public String getDetectorType() {
		return detectorType;
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		int[] dims = memory.getSupportedDimensions();
		ArrayList<DataDimension> supportedDimensions = new ArrayList<DataDimension>();
		for (int dim : dims) {
			supportedDimensions.add(new DataDimension(dim, dim));
		}
		return supportedDimensions;
	}

	@Override
	public double getPixelSize() throws DeviceException {
		return pixelsize / getDataDimensions()[0];
	}

	@Override
	public void setPixelSize(double pixelsize) throws DeviceException {
		this.pixelsize = pixelsize * getDataDimensions()[0];
	}

	@Override
	public void atScanEnd() throws DeviceException {
	}

	@Override
	public void atScanStart() throws DeviceException {
	}

	@Override
	public void setTimer(Timer timer) {

	}
}