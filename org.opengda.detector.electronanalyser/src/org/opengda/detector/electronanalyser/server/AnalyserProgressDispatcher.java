package org.opengda.detector.electronanalyser.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
/**
 * notify the observer with progress value in percentage.
 * @author fy65
 *
 */
public class AnalyserProgressDispatcher implements MonitorListener,
		IObservable, Configurable, Findable {
	private static final Logger logger = LoggerFactory
			.getLogger(AnalyserProgressDispatcher.class);
	private String name;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private String currentPointPv;
	private int currentPoint;
	private String totalPointsPv;
	private int totalPoints;
	private ObservableComponent observerComp = new ObservableComponent();;

	public AnalyserProgressDispatcher() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			controller.setMonitor(channelManager.createChannel(currentPointPv),
					this);
		} catch (Exception e) {
			logger.error("Cannot set up monitor of : " + currentPointPv, e);
			throw new FactoryException("Cannot set up monitor of "
					+ currentPointPv, e);
		}
		try {
			controller.setMonitor(channelManager.createChannel(totalPointsPv),
					new TotalPointsMonitor());
		} catch (Exception e) {
			logger.error("Cannot set up monitor of : " + totalPointsPv, e);
			throw new FactoryException("Cannot set up monitor of "
					+ totalPointsPv, e);
		}
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		DBR dbr=arg0.getDBR();
		if (dbr.isINT()) {
			currentPoint=((DBR_Int)dbr).getIntValue()[0];
			observerComp.notifyIObservers(this, (currentPoint/totalPoints*100));
		}
	}

	private class TotalPointsMonitor implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				totalPoints = ((DBR_Int) dbr).getIntValue()[0];
			}
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		observerComp.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observerComp.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observerComp.deleteIObservers();
	}

}
