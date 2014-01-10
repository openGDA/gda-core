/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADDriverMerlinThresholdSweep;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerlinThresholdSweepConfigurer extends NullNXPlugin {
	
	private static final Logger logger = LoggerFactory.getLogger(MerlinThresholdSweepConfigurer.class);

	
	private final ADDriverMerlinThresholdSweep sweepDriver;
	
	private boolean returnSweepSettings = false;

	@Override
	public String getName() {
		return "threshold";
	}
	
	public MerlinThresholdSweepConfigurer(ADDriverMerlinThresholdSweep sweepDriver) {
		this.sweepDriver = sweepDriver;
	}
	
	@Override
	public String toString() {
		Double start;
		Double stop;
		Double step;
		Double number;
		Integer numberPointsPerSweep;
		try {
			start = getEstart();
			stop = getEstop();
			step = getEstep();
			number = getEnumber();
			numberPointsPerSweep = getNumberPointsPerSweep();
		} catch (IOException e) {
			logger.error("IOException reading start, stop, step or number", e);
			return " * Problem reading EPICS PVs";
		}
		return MessageFormat.format("estart:{0} estop:{1} estep:{2} enumber:{3} images_per_sweep: {4}", start, stop, step, number, numberPointsPerSweep);
		
	}
	public void setEstep(Double step) throws IOException {
		sweepDriver.setStep(step);
	}

	public Double getEstep() throws IOException {
		return sweepDriver.getStep();
	}

	public void setEstop(Double stop) throws IOException {
		sweepDriver.setStop(stop);
	}

	public Double getEstop() throws IOException {
		return sweepDriver.getStop();
	}

	public void setEstart(Double start) throws IOException {
		sweepDriver.setStart(start);
	}

	public Double getEstart() throws IOException {
		return sweepDriver.getStart();
	}

	public int getNumberPointsPerSweep() throws IOException {
		return sweepDriver.getNumberPointsPerSweep();
	}

	public void setEnumber(Double number) throws IOException {
		sweepDriver.setNumber(number);
	}

	public Double getEnumber() throws IOException {
		return sweepDriver.getNumber();
	}
	
	@Override
	public List<String> getInputStreamNames() {
		if (isReturnSweepSettings()) {
			return Arrays.asList(new String[]{"estart", "estop", "estep", "enumber"});
		}
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		if (isReturnSweepSettings()) {
			return Arrays.asList(new String[]{"%.5f", "%.5f", "%.5f", "%.5f"});
		}
		return Arrays.asList();
	}
	
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		if (isReturnSweepSettings()) {
			List<Double> elementValues = new ArrayList<Double>();
			try {
				elementValues.add(getEstart());
				elementValues.add(getEstop());
				elementValues.add(getEstep());
				elementValues.add(getEnumber());
			} catch (IOException e) {
				throw new DeviceException(e);
			}
			appenders.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), elementValues));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		return appenders;
	}

	public boolean isReturnSweepSettings() {
		return returnSweepSettings;
	}

	public void setReturnSweepSettings(boolean returnSweepSettings) {
		this.returnSweepSettings = returnSweepSettings;
	}

}
