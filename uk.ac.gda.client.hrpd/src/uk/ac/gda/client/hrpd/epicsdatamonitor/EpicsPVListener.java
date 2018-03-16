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

package uk.ac.gda.client.hrpd.epicsdatamonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
/**
 * A named Spring-configurable {@link MonitorListener} for an EPICS PV of type {@link DBRType#DOUBLE}.
 * This listener stores a double data array which updated via {@link MonitorEvent} from the EPICS PV by default,
 * unless its {@link #poll} property is set to true, in which case, it will poll data from EPICS PV every time
 * when {@link #getValue()} method is called.
 * <li>{@link #name} and {@link #pvName} must be specified for an instance.</li>
 * <li>{@link #disablePoll()} and {@link #enablePoll()} can be used to switch monitoring on and off dynamically.</li>
 * <li>The default mode is monitoring on.</li>
 *
 */
public abstract class EpicsPVListener extends ConfigurableBase implements MonitorListener, InitializationListener, InitializingBean, Findable, IObservable {

	private Logger logger = LoggerFactory.getLogger(EpicsPVListener.class);
	protected boolean first=true;
	protected String name;
	protected ObservableComponent observers=new ObservableComponent();

	protected Channel pvchannel;
	protected String pvName;
	protected EpicsChannelManager channelManager;
	protected Monitor pvmonitor;
	protected boolean poll=false;
	protected EpicsController controller;

	public EpicsPVListener() {
		channelManager=new EpicsChannelManager(this);
		controller=EpicsController.getInstance();
	}

	@Override
	public void configure() throws FactoryException {
		if (getPvName()!=null) {
			try {
				pvchannel=channelManager.createChannel(getPvName(), false);
				channelManager.creationPhaseCompleted();
				channelManager.tryInitialize(100);
			} catch (CAException e) {
				logger.error(getName()+": failed to create EPICS channel to "+getPvName(),e);
				throw new FactoryException(getName()+": failed to create EPICS channel to "+getPvName(),e);
			}
		}
		setConfigured(true);
	}

	public abstract void disablePoll();

	public void enablePoll() {
		if (pvmonitor != null) {
			pvmonitor.removeMonitorListener(this);
			setPoll(true);
		}
	}

	public abstract Object getValue();

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		if (isPoll()) {
			enablePoll();
		} else {
			disablePoll();
		}
		logger.info("{} initialisation completed.", getName());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(getPvName()==null) {
			throw new IllegalArgumentException("PV name property must be set");
		}
		if (getName()==null) {
			throw new IllegalArgumentException("name property must be set");
		}
	}

	public void dispose() {
		pvchannel.dispose();
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public boolean isPoll() {
		return poll;
	}

	public void setPoll(boolean poll) {
		this.poll = poll;
	}
	@Override
	public void addIObserver(IObserver observer) {
		observers.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observers.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observers.deleteIObservers();
	}

	@Override
	public abstract void monitorChanged(MonitorEvent ev);

}
