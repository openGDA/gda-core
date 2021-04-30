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

package uk.ac.diamond.daq.devices.specs.phoibos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.epics.connection.EpicsController;
import gda.factory.ConfigurableBase;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyserStatus;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(ISpecsPhoibosAnalyserStatus.class)
public class SpecsPhoibosAnalyserStatus extends ConfigurableBase implements ISpecsPhoibosAnalyserStatus {

	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosAnalyserStatus.class);
	private String name;
	private final EpicsController epicsController = EpicsController.getInstance();
	private SpecsPhoibosSeparateIterationCollectionStrategy collectionStrategy;
	private String pvName;

	private final ObservableComponent observableComponent = new ObservableComponent();

	private SpecsPhoibosStatus specsStatus;
	private int collectionStrategyStatus;

	private String[] positions;

	@Override
	public void configure() {
		try {
			if (isConfigured()) {
				return;
			}

			// Handle events from collection strategy
			if (collectionStrategy != null) {
				collectionStrategy.addIObserver(this::processCollectionStrategyUpdate);
				collectionStrategyStatus = collectionStrategy.getStatus();
			}
			// Get labels from epics
			positions = epicsController.cagetLabels(getChannel(pvName));

			// Handle events from epics controller
			epicsController.setMonitor(getChannel(pvName), evt -> {
				logger.trace("Received event: {}", evt);
				DBR_Enum dbr = (DBR_Enum) evt.getDBR();
				int status = dbr.getEnumValue()[0];
				specsStatus = SpecsPhoibosStatus.get(status);
				observableComponent.notifyIObservers(this, isBusy());
				observableComponent.notifyIObservers(this, positions[status]);
			});
			specsStatus = SpecsPhoibosStatus.get(epicsController.cagetEnum(getChannel(pvName)));
			setConfigured(true);
		} catch (Exception e){
			logger.error("Could not set status monitor");
		}

	}

	private void processCollectionStrategyUpdate(@SuppressWarnings("unused") Object source, Object arg) {
		collectionStrategyStatus = (int)arg;
		observableComponent.notifyIObservers(this, isBusy());
	}

	/**
	 * This method takes into account both collectionStrategy and SPECS status
	 * and returns a true if either of them is busy
	 */
	@Override
	public boolean isBusy() {
		return collectionStrategyStatus == Detector.BUSY
				|| !(specsStatus == SpecsPhoibosStatus.IDLE
				|| specsStatus == SpecsPhoibosStatus.ABORTED
				|| specsStatus == SpecsPhoibosStatus.ERROR);
	}

	@Override
	public String getCurrentPosition() {
		try {
			return epicsController.cagetLabel(getChannel(pvName));
		} catch (Exception e) {
			return "UNKNOWN";
		}
	}

	private Channel getChannel(String fullPvName) throws TimeoutException, CAException {
		return epicsController.createChannel(fullPvName);
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getName() {
		return name;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public void setCollectionStrategy(SpecsPhoibosSeparateIterationCollectionStrategy collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}



}
