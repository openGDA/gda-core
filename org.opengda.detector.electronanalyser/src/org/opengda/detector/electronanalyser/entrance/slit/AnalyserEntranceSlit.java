/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.entrance.slit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.pes.api.EntranceSlitInformationProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EntranceSlitInformationProvider.class)
public class AnalyserEntranceSlit extends ConfigurableBase implements EntranceSlitInformationProvider, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserEntranceSlit.class);
	private final Set<EntranceSlit> entranceSlitsSet = new HashSet<>();
	private final EntranceSlit defaultSlit;
	private EntranceSlit currentSlit;
	private String name;

	//Configurable in spring xml
	private EnumPositioner slitScannable;
	private String defaultSlitString = "100 0.1 curved vertical";

	public AnalyserEntranceSlit() {
		defaultSlit = parseEntranceSlitString(defaultSlitString);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) return;

		if (slitScannable==null) {
			logger.error("Failed to configure analyser entrance slit - slitScannable is null");
			return;
		}
		// set monitor to synchronise current slit
		slitScannable.addIObserver(this);

		// empty list of slits and fill list of slits from scannable
		entranceSlitsSet.clear();
		try {
			for (String slit: slitScannable.getPositionsList()) {
				logger.debug("Found: {}",slit);
				entranceSlitsSet.add(parseEntranceSlitString(slit));
			}
			// Update the current slit to avoid possible NPE
			setCurrentSlitByValue(Integer.parseInt(((String) slitScannable.getPosition()).split(" ")[0].strip()));
		} catch (DeviceException e) {
			logger.error("Failed to initially set up current analyser entrance slit", e);
		}

		logger.info("Finished configuring analyser entrance slit");
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("Reconfigure called");
		if (isConfigured()) setConfigured(false);
		dispose();
		configure();
	}

	@Override
	public void update(Object source, Object arg) {
		try {
			setCurrentSlitByValue(Integer.parseInt(((String) slitScannable.getPosition()).split(" ")[0].strip()));
		} catch (NumberFormatException | DeviceException e) {
			logger.error("Failed to set current slit after position update", e);
		}
	}

	@Override
	public void setCurrentSlitByValue(int number) {
		currentSlit = entranceSlitsSet.stream()
				.filter(slit -> slit.getRawValue()==number)
				.findFirst().orElseGet(this::getDefaultSlit);
	}

	private EntranceSlit parseEntranceSlitString(String newPositionString) {
		String[] values = newPositionString.split(" ");
		try {
			return new EntranceSlit(Integer.parseInt(values[0].strip()),
									Double.parseDouble(values[1].strip()),
									values[2].strip(),
									values[3].strip());
		} catch (Exception e) {
			logger.error("Failed to parse entrance slit values from epics string", e);
			logger.error("Setting entrance slit to a default value", e);
			return defaultSlit;
		}
	}

	@Override
	public Number getRawValue() {
		return currentSlit.getRawValue();
	}

	@Override
	public Double getSizeInMM() {
		return currentSlit.getSize();
	}

	@Override
	public String getShape() {
		return currentSlit.getShape();
	}

	@Override
	public String getDirection() {
		return currentSlit.direction;
	}

	public EntranceSlit getCurrentSlit() {
		return currentSlit;
	}

	public String getDefaultSlitString() {
		return defaultSlitString;
	}

	public void setDefaultSlitString(String defaultSlitString) {
		this.defaultSlitString = defaultSlitString;
	}

	public EnumPositioner getSlitScannable() {
		return slitScannable;
	}

	public void setSlitScannable(EnumPositioner slitScannable) {
		this.slitScannable = slitScannable;
	}

	public EntranceSlit getDefaultSlit() {
		return defaultSlit;
	}

	public void dispose() {
		if (slitScannable!=null) slitScannable.deleteIObserver(this);
	}

	/**
	 * Small immutable class to encapsulate the information about a entrance slit
	 */
	public static class EntranceSlit {
		private final int rawValue;
		private final double size;
		private final String shape;
		private final String direction;

		public EntranceSlit(int rawValue, double size, String shape, String direction) {
			this.rawValue = rawValue; // eg. 100, 200, 300
			this.size = size; // in mm eg 0.1 mm 0.3 mm
			this.shape = shape; // e.g. curved, straight, aperture
			this.direction = direction; // vertical or horizontal
		}

		public int getRawValue() {
			return rawValue;
		}

		public double getSize() {
			return size;
		}

		public String getShape() {
			return shape;
		}

		public String getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return "EntranceSlit [rawValue=" + rawValue + ", size=" + size + ", direction="+direction+", shape=" + shape + "]";
		}
	}

	@Override
	public double getSizeByRawValue(int rawValue) {
		return entranceSlitsSet.stream().filter(i->(i.getRawValue() == rawValue))
				.findFirst().get().getSize();
	}

	@Override
	public List<Integer> getSlitsRawValueList() {
		return entranceSlitsSet.stream().map(i->i.getRawValue()).sorted().toList();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
