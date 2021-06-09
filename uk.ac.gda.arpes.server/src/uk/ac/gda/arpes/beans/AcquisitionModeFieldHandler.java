/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.beans;

import org.exolab.castor.mapping.AbstractFieldHandler;

import uk.ac.diamond.daq.pes.api.AcquisitionMode;

public class AcquisitionModeFieldHandler extends AbstractFieldHandler {

	@Override
	public Object getValue(Object object) throws IllegalStateException {
		ARPESScanBean bean = (ARPESScanBean)object;
		return bean.getAcquisitionMode().getLabel();
	}

	@Override
	public Object newInstance(Object arg0) throws IllegalStateException {
		return AcquisitionMode.FIXED;
	}

	@Override
	public Object newInstance(Object arg0, Object[] arg1) throws IllegalStateException {
		return AcquisitionMode.FIXED;
	}

	@Override
	public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
		ARPESScanBean bean = (ARPESScanBean)object;
		bean.setAcquisitionMode(AcquisitionMode.FIXED);
	}

	@Override
	public void setValue(Object object, Object value) throws IllegalStateException, IllegalArgumentException {
		ARPESScanBean bean = (ARPESScanBean)object;
		String acquisitionMode = value.toString();
		bean.setAcquisitionMode(AcquisitionMode.valueOfLabel(acquisitionMode));
	}

}
