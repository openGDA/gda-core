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

package uk.ac.gda.client.hrpd.typedpvscannables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * Base class representing a single EPICS PV as a {@link Scannable}.
 * <p>
 * Subclass must <b>override {@link #asynchronousMoveTo(Object)}</b> method and the overriding method
 * must call <code>super.asynchronousMoveTo(Object)}</code> method first as it enforces read-only
 * property of the scannable.
 * <p>
 * When the {@link #readOnly} property is set to true, set to PV is disabled.
 *
 * Subclass is also required to set @ {@link #isBusy} property to ture before sending request to EPICS PV,
 * and the put callback is designed to set this property to false when the PV process is completed.
 *
 */
public abstract class EpicsPVScannable extends ScannableBase implements PutListener, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPVScannable.class);

	protected boolean isBusy = false;
	protected boolean readOnly=false;
	protected String pvName = "";

	/**
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			this.setInputNames(new String[] { getName() });
			setConfigured(true);
		}
	}

	/**
	 * @return the pvName
	 */
	public String getPvName() {
		return pvName;
	}

	/**
	 * @param pvName
	 *            the pvName to set
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isReadOnly()) {
			throw new IllegalAccessError("This object is read only.");
		}
	}

	/**
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	abstract public Object getPosition() throws DeviceException;

	/**
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		return isBusy;
	}

	/**
	 * @see gov.aps.jca.event.PutListener#putCompleted(gov.aps.jca.event.PutEvent)
	 */
	@Override
	public void putCompleted(PutEvent arg0) {
		// get here the callbacks from caputs made in asynchronousMoveTo
		isBusy = false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPvName()==null) {
			throw new IllegalStateException("PV name must be set.");
		}
	}

	@Override
	public String toString() {
		try {
			return ScannableUtils.getFormattedCurrentPosition(this);
		} catch (Exception e) {
			logger.warn("{}: exception while getting value", getName(), e);
			return valueUnavailableString();
		}
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

}
