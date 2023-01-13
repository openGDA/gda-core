/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.observable.Observable;
import gda.observable.Observer;

/**
 * Scannable for the fast attenuator filters on i07.
 */
public class FastAttenuatorScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(FastAttenuatorScannable.class);

	private static final String MANUAL_MODE_PV = ":MODE";
	private static final String ATTENUATION_PV = ":ATTENUATION";
	private static final String FILE_NAME_PV = ":FILE:NAME";
	private static final String FILE_PATH_PV = ":FILE:PATH";
	private static final String STATE_PV = ":STATE";
	private static final String READBACK_PV_SUFFIX = "_RBV";
	private static final String[] INPUT_NAME = { "att" };
	private static final String[] OUTPUT_FORMAT = { "%f", "%10.6g" };
	private static final String[] EXTRA_NAMES = { "transmission" };

	private boolean busy = false;
	private FastAttenuatorFilters filters;

	private PVWithSeparateReadback<Integer> modePv;
	private PVWithSeparateReadback<Integer> attenuationPv;
	private ReadOnlyPV<Integer> statePv;
	private PV<String> fileNamePv;
	private PV<String> filePathPv;

	private ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	public FastAttenuatorScannable() {
		setInputNames(INPUT_NAME);
		setExtraNames(EXTRA_NAMES);
		setOutputFormat(OUTPUT_FORMAT);
	}

	@Override
	public void atScanStart() throws DeviceException {
		try {
			filePathPv.putWait(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
			fileNamePv.putWait("temporaryFile.h5");
		} catch (IOException ioe) {
			throw new DeviceException(ioe);
		}
		super.atScanStart();
	}

	public void setBasePv(String basePV) {
		modePv = new PVWithSeparateReadback<>(LazyPVFactory.newIntegerFromEnumPV(basePV + MANUAL_MODE_PV),
				LazyPVFactory.newReadOnlyIntegerFromEnumPV(basePV + MANUAL_MODE_PV + READBACK_PV_SUFFIX));
		attenuationPv = new PVWithSeparateReadback<>(LazyPVFactory.newIntegerFromEnumPV(basePV + ATTENUATION_PV),
				LazyPVFactory.newReadOnlyIntegerFromEnumPV(basePV + ATTENUATION_PV + READBACK_PV_SUFFIX));
		fileNamePv = LazyPVFactory.newStringFromWaveformPV(basePV + FILE_NAME_PV);
		filePathPv = LazyPVFactory.newStringFromWaveformPV(basePV + FILE_PATH_PV);
		statePv = LazyPVFactory.newReadOnlyIntegerFromEnumPV(basePV + STATE_PV);
	}

	public void setFilterManager(FastAttenuatorFilters filters) {
		this.filters = filters;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	/**
	 * Sets which filters the attenuator is currently using.
	 *
	 * @param filtersPresent
	 *            a value 0-15 describing which filters to use.
	 * @throws DeviceException
	 */
	public void setAttenuation(int filtersPresent) throws DeviceException {
		if (filtersPresent < 0 || filtersPresent > 15) {
			throw new IllegalArgumentException("Value must be between 0 and 15 inclusive.");
		}
		try {
			if (modePv.get() != 0) {
				notifyAndLog("Setting fast attenuators to manual mode in order to set present filters.");
				manualMode();
			}
			busy = true;
			attenuationPv.putWait(filtersPresent);
			attenuationPv.addObserver(new Observer<Integer>() {
				@Override
				public void update(Observable<Integer> source, Integer arg) {
					busy = false;
					attenuationPv.removeObserver(this);
				}
			}, Predicate.isEqual(filtersPresent));
		} catch (Exception e) {
			throw new DeviceException("Failed to set attenuation.", e);
		}
	}

	private void notifyAndLog(String message) {
		terminalPrinter.print(message);
		logger.warn(message);
	}

	/**
	 * Set the transmissions of the current filter set.
	 *
	 * @param filterTransmissions
	 *            array of four filter transmissions.
	 */
	public void setFilterTransmissions(Double[] filterTransmissions) {
		filters.setFilterTransmissions(filterTransmissions);
	}

	public List<Double> getFilterTransmissions() {
		return filters.getFilterTransmissions();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		setAttenuation(ScannableUtils.objectToDouble(position).intValue());
	}

	/**
	 * Get the transmission value of the currently in use filters.
	 *
	 * @return the current transmission.
	 * @throws IOException
	 */
	public double getCurrentTransmission() throws IOException {
		return filters.getCurrentTransmission(attenuationPv.get());
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			return new Object[] { attenuationPv.get(), getCurrentTransmission() };
		} catch (IOException e) {
			throw new DeviceException("Could not get transmission of attenuator filters", e);
		}
	}

	public void setFilterSet(String filterSetName) throws IOException {
		filters.setFilterSet(filterSetName);
	}

	public String getFilterSet() throws IOException {
		return filters.getFilterSet();
	}

	public void manualMode() throws IOException, IllegalStateException, TimeoutException, InterruptedException {
		modePv.putWait(0);
		statePv.waitForValue(Predicate.isEqual(0), 1);
	}

	public void continuousMode() throws IOException, IllegalStateException, TimeoutException, InterruptedException {
		modePv.putWait(1);
		statePv.waitForValue(Predicate.isEqual(1), 1);
	}

	public void singleShotMode() throws IOException, IllegalStateException, TimeoutException, InterruptedException {
		modePv.putWait(2);
		statePv.waitForValue(Predicate.isEqual(3), 1);
	}

}
