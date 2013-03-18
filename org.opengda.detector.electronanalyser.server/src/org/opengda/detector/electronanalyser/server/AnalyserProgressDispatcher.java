package org.opengda.detector.electronanalyser.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.CAException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class AnalyserProgressDispatcher implements MonitorListener,
		Configurable, Findable {
	private static final Logger logger=LoggerFactory.getLogger(AnalyserProgressDispatcher.class);
	private String name;
	private VGScientaAnalyser analyser;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private String currentPointPv;
	private int currentPoint;
	private String totalPointsPv;
	private int totalPoints;

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
			controller.setMonitor(channelManager.createChannel(currentPointPv), this);
		} catch (Exception e) {
			logger.error("Cannot set up monitor of : "+ currentPointPv, e);
			throw new FactoryException("Cannot set up monitor of "+ currentPointPv, e);
		}
		try {
			controller.setMonitor(channelManager.createChannel(totalPointsPv), new TotalPointsMonitor());
		} catch (Exception e) {
			logger.error("Cannot set up monitor of : "+ totalPointsPv, e);
			throw new FactoryException("Cannot set up monitor of "+ totalPointsPv, e);
		}
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		DBR dbr=arg0.getDBR();
		if (dbr.isINT()) {
			currentPoint=((DBR_Int)dbr).getIntValue()[0];
		}
	}
	
	private class TotalPointsMonitor implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr=arg0.getDBR();
			if (dbr.isINT()) {
				totalPoints=((DBR_Int)dbr).getIntValue()[0];
			}
		}
	}

}
