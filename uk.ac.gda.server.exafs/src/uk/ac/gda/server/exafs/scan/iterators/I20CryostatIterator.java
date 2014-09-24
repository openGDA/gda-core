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

package uk.ac.gda.server.exafs.scan.iterators;

import gda.device.DeviceException;
import gda.device.Scannable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.i20.CryostatParameters;
import uk.ac.gda.beans.exafs.i20.CryostatProperties;
import uk.ac.gda.beans.exafs.i20.CryostatSampleDetails;
import uk.ac.gda.beans.exafs.i20.I20SampleParameters;
import uk.ac.gda.doe.DOEUtils;

public class I20CryostatIterator implements SampleEnvironmentIterator {

	private Scannable cryostat;
	private Scannable cryostick_pos;
	private CryostatParameters parameters;
	private Set<Entry<CryostatSampleDetails, Double>> entries;
	private Iterator<Entry<CryostatSampleDetails, Double>> entriesIterator;
	private Entry<CryostatSampleDetails, Double> currentCollection;

	public I20CryostatIterator(Scannable cryostat, Scannable cryostick_pos) {
		this.cryostat = cryostat;
		this.cryostick_pos = cryostick_pos;
	}

	@Override
	public void setSampleBean(ISampleParameters sampleBean) {
		I20SampleParameters i20Bean = (I20SampleParameters) sampleBean;
		this.parameters = i20Bean.getCryostatParameters();

		List<Double> temperatures_array = new ArrayList<Double>();
		if (DOEUtils.isRange(parameters.getTemperature(), null)) {
			temperatures_array = DOEUtils.expand(parameters.getTemperature(), Double.class);
		} else if (DOEUtils.isList(parameters.getTemperature(), null)) {
			String[] temps = parameters.getTemperature().split(",");
			temperatures_array = new ArrayList<Double>();
			for (String temp : temps) {
				temperatures_array.add(Double.parseDouble(temp));
			}
		}

		boolean loopSampleFirst = parameters.getLoopChoice() == CryostatProperties.LOOP_OPTION[0];
		LinkedHashMap<CryostatSampleDetails, Double> dataCollections = new LinkedHashMap<CryostatSampleDetails, Double>();

		if (loopSampleFirst) {
			for (CryostatSampleDetails sample : parameters.getSamples()) {
				for (Double temp : temperatures_array) {
					dataCollections.put(sample, temp);
				}
			}
		} else {
			for (Double temp : temperatures_array) {
				for (CryostatSampleDetails sample : parameters.getSamples()) {
					dataCollections.put(sample, temp);
				}
			}
		}

		entries = dataCollections.entrySet();
		entriesIterator = entries.iterator();
	}

	@Override
	public int getNumberOfRepeats() {
		int repeats = 1;
		for (Entry<CryostatSampleDetails, Double> entry : entries) {
			repeats += entry.getKey().getNumberOfRepetitions();
		}
		return repeats;
	}

	@Override
	public void next() throws DeviceException, InterruptedException {
		currentCollection = entriesIterator.next();
		// log( "Moving cryostick_pos to ",self.cryostick_pos)
		Double motorPosition = currentCollection.getKey().getPosition();
		cryostick_pos.asynchronousMoveTo(motorPosition);
		Double temperature = currentCollection.getValue();
		// self.log( "Setting cryostat to",str(temp),"K...")
		cryostat.asynchronousMoveTo(temperature);
		// self.log("Waiting for cryostick_pos to move")
		cryostick_pos.waitWhileBusy();
		// log("cryostick_pos move complete.");
		// log("Waiting for Cryostat to set temperature");
		cryostat.waitWhileBusy();
		// log( "Cryostat temperature change complete.");
	}

	@Override
	public void resetIterator() {
		entriesIterator = entries.iterator();
	}

	@Override
	public String getNextSampleName() {
		return currentCollection.getKey().getSample_name();
	}

	@Override
	public List<String> getNextSampleDescriptions() {
		List<String> descriptions = new ArrayList<String>();
		descriptions.add(currentCollection.getKey().getSampleDescription());
		return descriptions;
	}
}
