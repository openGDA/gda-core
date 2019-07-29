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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.device.memory.Gdhist;
import gda.factory.FactoryException;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A class to represent a detector for NCD.
 */
@ServiceInterface(INcdSubDetector.class)
public class NcdScalerDetector extends NcdSubDetector {

	private static final Logger logger = LoggerFactory.getLogger(NcdScalerDetector.class);
	protected Gdhist memory = null;
	private double pixelsize;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (memory != null) {
			memory.reconfigure();
		} else {
			throw new FactoryException("no memory configured!");
		}
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("NcdDetector reconfiguring " + getName());
		setConfigured(false);
		configure();
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		int[] devicedims = memory.getDimension();
		int[] datadims = new int[] {frames };
		float[] data = readFloat(0, 0, 0, devicedims[0], devicedims[1], frames);
		NexusGroupData ngd;

		int channels = devicedims[0];
		int i = 0;
		String description = (String) getAttribute("description");
		StringTokenizer labels = new StringTokenizer(description, "\r\n");
		for (int chan=0; chan<channels; chan++) {
			float[] frameData = new float[frames];
			System.arraycopy(data, i, frameData, 0, frames);
			ngd = new NexusGroupData(datadims, frameData);
			ngd.isDetectorEntryData = true;
			nxdata.addData(getName(), labels.nextToken(), ngd, "counts", 1);
			i += frames;
		}
	}

	@Override
	public void close() throws DeviceException {
		logger.debug("NcdDetector closing " + getName());
		if (memory != null) {
			memory.close();
		}
		setConfigured(false);
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
		for (int i = 0; i < dims.length; i++)
			supportedDimensions.add(new DataDimension(dims[i], dims[i]));
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