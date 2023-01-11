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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PVWithSeparateReadback;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

/**
 * Class managing the filters of {@link FastAttenuatorScannable}.  This extends {@link ScannableBase} so it can be added to the metadata easily.
 */
public class FastAttenuatorFilters extends ScannableBase {

	/**
	 * Enum of types of filter installed in the fast attenuators.
	 */
	private enum FilterSet {
		CU(0, "Cu"), MO1(1, "Mo1"), MO2(2, "Mo2"), MO3(3, "Mo3"), AG1(4, "Ag1"), AG2(5, "Ag2");

		int position;
		String name;

		private FilterSet(int pos, String name) {
			this.position = pos;
			this.name = name;
		}
	}

	private static final String[] OUTPUT_FORMAT = new String[] { "%s", "%s" };
	private static final String FILTER_SET_PV = ":FILTER_SET";
	private static final String READBACK_PV_SUFFIX = "_RBV";
	private static final String[] INPUT_NAMES = { "filter_set", "filter_transmissions" };

	private List<Double> filterTransmissions = Arrays.asList( 1.0, 1.0, 1.0, 1.0 );
	private double[] precalculatedTransmissions = new double[16];
	private PVWithSeparateReadback<Integer> filterSetPv;
	private ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();

	public FastAttenuatorFilters() {
		populateTransmissionsMap();
		setInputNames(INPUT_NAMES);
		setOutputFormat(OUTPUT_FORMAT);
	}

	public void setBasePv(String basePV) {
		filterSetPv = new PVWithSeparateReadback<>(LazyPVFactory.newIntegerFromEnumPV(basePV + FILTER_SET_PV),
				LazyPVFactory.newReadOnlyIntegerFromEnumPV(basePV + FILTER_SET_PV + READBACK_PV_SUFFIX));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	/**
	 * Set the transmissions of the current filter set.
	 *
	 * @param filterTransmissions
	 *            array of four filter transmissions.
	 */
	public void setFilterTransmissions(Double[] filterTransmissions) {
		if (filterTransmissions.length != 4) {
			throw new IllegalArgumentException("Must specify 4 transmissions, 1 for each filter in the current set.");
		}
		this.filterTransmissions = Arrays.asList(filterTransmissions);
		populateTransmissionsMap();
	}

	public List<Double> getFilterTransmissions() {
		return filterTransmissions;
	}

	/**
	 * Transmissions of different filter configurations are recalculated whenever the filter set is changed so they don't have to be done on the fly.
	 */
	private void populateTransmissionsMap() {
		for (int filters = 0; filters < 16; filters++) {
			double t1 = (filters & 1) > 0 ? filterTransmissions.get(0) : 1;
			double t2 = (filters & 2) > 0 ? filterTransmissions.get(1) : 1;
			double t3 = (filters & 4) > 0 ? filterTransmissions.get(2) : 1;
			double t4 = (filters & 8) > 0 ? filterTransmissions.get(3) : 1;

			double totalTransmission = t1 * t2 * t3 * t4;
			precalculatedTransmissions[filters] = totalTransmission;
		}
	}

	public void setFilterSet(String filterSetName) throws IOException {
		try {
			FilterSet set = FilterSet.valueOf(filterSetName.replaceAll("\\s", "").toUpperCase());
			filterSetPv.putWait(set.position);
		} catch (IllegalArgumentException iae) {
			terminalPrinter.print("Filter set name not recognised, valid filters are Cu, Mo1, Mo2, Mo3, Ag1 and Ag2");
		}
	}

	public String getFilterSet() throws IOException {
		Integer setPosition = filterSetPv.get();
		return FilterSet.values()[setPosition].name;
	}

	public double getCurrentTransmission(Integer attenuation) {
		return precalculatedTransmissions[attenuation];
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			return new Object[] { getFilterSet(), filterTransmissions };
		} catch (IOException ioe) {
			throw new DeviceException(ioe);
		}
	}

	/**
	 * Get reference to the transmission function. This is to allow this
	 * to be injected to be used in a scanning processor without requiring
	 * a dependency on this plugin directly.
	 */
	public Function<Integer, Double> getCurrentTransmissionFunction() {
		return this::getCurrentTransmission;
	}
}
