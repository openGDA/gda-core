/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.hrpd.sample.corba.impl;

import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.hrpd.SampleInfo;
import gda.hrpd.sample.corba.CorbaSampleInfo;
import gda.hrpd.sample.corba.CorbaSampleInfoHelper;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.LoggingConstants;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the SampleInfo interface
 */
public class SampleAdapter implements SampleInfo, EventSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(SampleAdapter.class);
	CorbaSampleInfo paramsObj;

	NetService netService;

	String name;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public SampleAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		paramsObj = CorbaSampleInfoHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		// subscribe to events coming over CORBA from the impl
		EventService eventService = EventService.getInstance();
		if (eventService != null)
			eventService.subscribe(this, new NameFilter(name, observableComponent));
	}

	// this is for accepting events over the CORBA event mechanism
	@Override
	public void inform(Object obj) {
		if (obj == null)
			logger.debug(LoggingConstants.FINEST, "SampleAdapter: Received event for NULL");
		notifyIObservers(this, obj);
	}

	@Override
	public String getCarouselNo() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getCarouselNo();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getComment() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getComment();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getDescription() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getDescription();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public int getRowOffset() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				int result = paramsObj.getRowOffset();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return 0;
	}

	@Override
	public String getSampleID() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getSampleID();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getSampleInfoFile() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getSampleInfoFile();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getSampleName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getSampleName();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getTitle() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getTitle();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public boolean isSaveExperimentSummary() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				boolean result = paramsObj.isSaveExperimentSummary();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return false;
	}

	@Override
	public void loadSampleInfo(int sampleNo) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.saveExperimentInfo(sampleNo);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void saveExperimentInfo(int sampleNo) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.saveExperimentInfo(sampleNo);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void saveSampleInfo(int sampleNo) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.saveExperimentInfo(sampleNo);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setBeamline(String beamline) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setBeamline(beamline);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setCarouselNo(String caroselNo) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setCarouselNo(caroselNo);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setComment(String comment) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setComment(comment);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setDate(String date) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setDate(date);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setDescription(String description) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setDescription(description);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setExperiment(String experiment) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setExperiment(experiment);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setProject(String project) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setProject(project);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setRowOffset(int rowOffset) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setRowOffset(rowOffset);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setRunNumber(String runNumber) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setRunNumber(runNumber);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setSampleID(String sampelID) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setSampleID(sampelID);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setSampleInfoFile(String sampleInfoFile) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setSampleInfoFile(sampleInfoFile);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setSampleName(String sampelName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setSampleName(sampelName);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setSaveExperimentSummary(boolean saveExperimentSummary) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setSaveExperimentSummary(saveExperimentSummary);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setTemperature(String temperature) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setTemperature(temperature);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setTime(String time) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setTime(time);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setTitle(String title) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setTitle(title);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setWavelength(String wavelength) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setWavelength(wavelength);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of this class.
	 * 
	 * @param theObserved
	 *            the observed class
	 * @param changeCode
	 *            the changed code
	 */
	public void notifyIObservers(java.lang.Object theObserved, java.lang.Object changeCode) {
		observableComponent.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public void close() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.close();
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public boolean isConfigured() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				boolean result = paramsObj.isConfigured();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return false;
	}

	@Override
	public void open() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.open();
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setCarouselNo(int caroselNo) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setCarouselNoInt(caroselNo);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void values() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.values();
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaSampleInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

}
