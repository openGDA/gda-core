/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.attenuator;

import gda.device.Attenuator;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of {@link Attenuator} that controls multiple independent filters.
 * 
 * <p><b>This implementation does not take energy into account.</b> It was written for Diamond's I04.1 beamline, which
 * operates at a fixed energy. Any method that involves energy, or takes energy as a parameter, will throw an
 * {@link UnsupportedOperationException}.
 */
public abstract class MultiFilterAttenuator extends DeviceBase implements Attenuator, InitializingBean, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(MultiFilterAttenuator.class);
	
	protected List<AttenuatorFilter> filters;
	
	/**
	 * Sets the filters that make up this attenuator.
	 */
	public void setFilters(List<AttenuatorFilter> filters) {
		this.filters = filters;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (filters == null) {
			throw new IllegalArgumentException("Property 'filters' is required");
		}
	}
	
	/**
	 * Possible transmissions for this attenuator. Calculated by considering all combinations of the individual filters.
	 */
	protected List<Transmission> transmissions;
	
	@Override
	public void configure() throws FactoryException {
		prepareTransmissionLookupTable();
	}
	
	protected void prepareTransmissionLookupTable() {
		int numFilters = filters.size();
		int combinations = (1 << numFilters); // 2 ^ numFilters
		logger.debug(String.format("%d filter(s), %d combinations", numFilters, combinations));
		transmissions = new Vector<Transmission>();
		for (int combination=0; combination<combinations; combination++) {
			boolean[] filterStates = new boolean[numFilters];
			double transmission = 1;
			for (int filter=0; filter<numFilters; filter++) {
				if ((combination & (1<<filter)) > 0) {
					filterStates[filter] = true;
					transmission *= (filters.get(filter).getTransmissionPercentage() / 100);
				}
			}
			Transmission t = new Transmission(transmission, filterStates);
			transmissions.add(t);
		}
		
		Collections.sort(transmissions, TRANSMISSION_COMPARATOR);
	}
	
	public List<Transmission> getTransmissions() {
		return Collections.unmodifiableList(transmissions);
	}
	
	public Transmission findClosestTransmission(double desiredTransmission) {
		Transmission key = new Transmission(desiredTransmission, null);
		int pos = Collections.binarySearch(transmissions, key, TRANSMISSION_COMPARATOR);
		if (pos >= 0) {
			// exact match found
			return transmissions.get(pos);
		}
		
		// position tells us where the desired transmission would be inserted in the list
		int inspos = -(pos+1);
		
		if (inspos == 0) {
			// desired transmission is lower than minimum achievable transmission
			return transmissions.get(0);
		}
		
		else if (inspos == transmissions.size()) {
			// desired transmission is higher than maximum achievable transmission
			return transmissions.get(transmissions.size() - 1);
		}
		
		// desired transmission is between two achievable transmissions - find the closest
		Transmission at1 = transmissions.get(inspos-1);
		Transmission at2 = transmissions.get(inspos);
		double diff1 = desiredTransmission - at1.getTransmission();
		double diff2 = at2.getTransmission() - desiredTransmission;
		return (diff1 < diff2) ? at1 : at2;
	}
	
	/**
	 * {@link Comparator} that compares {@link Transmission} objects using their transmission values.
	 */
	protected static final Comparator<Transmission> TRANSMISSION_COMPARATOR =
		new Comparator<Transmission>() {
			@Override
			public int compare(Transmission o1, Transmission o2) {
				return Double.compare(o1.getTransmission(), o2.getTransmission());
			}
		};
	
	/**
	 * Changes the physical state of the attenuator filters to match the specified transmission.
	 */
	protected abstract void setFilterStates(Transmission transmission);
	
	protected double desiredTransmission;
	
	protected Transmission actualTransmission;
	
	@Override
	public double setTransmission(double transmission) throws DeviceException {
		this.desiredTransmission = transmission;
		Transmission closestTransmission = findClosestTransmission(transmission);
		this.actualTransmission = closestTransmission;
		setFilterStates(closestTransmission);
		return closestTransmission.getTransmission();
	}
	
	@Override
	public double getTransmission() throws DeviceException {
		return actualTransmission.getTransmission();
	}
	
	@Override
	public double getClosestMatchEnergy() throws DeviceException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getClosestMatchTransmission(double transmission) throws DeviceException {
		Transmission closestTransmission = findClosestTransmission(transmission);
		return closestTransmission.getTransmission();
	}
	
	@Override
	public ClosestMatchTransmission getClosestMatchTransmission(double transmission, double energyInKeV) throws DeviceException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getDesiredEnergy() throws DeviceException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean[] getDesiredFilterPositions() throws DeviceException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getDesiredTransmission() throws DeviceException {
		return desiredTransmission;
	}
	
	@Override
	public String[] getFilterNames() throws DeviceException {
		String[] filterNames = new String[filters.size()];
		for (int i=0; i<filters.size(); i++) {
			filterNames[i] = filters.get(i).getName();
		}
		return filterNames;
	}
	
	@Override
	public boolean[] getFilterPositions() throws DeviceException {
		return actualTransmission.getFilterStates();
	}
	
	@Override
	public int getNumberFilters() throws DeviceException {
		return filters.size();
	}
	
}
