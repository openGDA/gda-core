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

import java.util.List;

import uk.ac.gda.beans.exafs.ISampleParameters;

public class I20SingleSampleIterator implements SampleEnvironmentIterator {

	private ISampleParameters sampleBean;

	@Override
	public void setSampleBean(ISampleParameters sampleBean) {
		this.sampleBean = sampleBean;
	}

	@Override
	public int getNumberOfRepeats() {
		return 1;
	}

	@Override
	public void next() {
		//
	}

	@Override
	public void resetIterator() {
		//
	}

	@Override
	public String getNextSampleName() {
		return sampleBean.getName();
	}

	@Override
	public List<String> getNextSampleDescriptions() {
		return sampleBean.getDescriptions();
	}
}
