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

package gda.beamline.beamline.corba.impl;

import gda.beamline.BeamlineInfo;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.beamline.corba.CorbaBeamlineInfo;
import gda.beamline.corba.CorbaBeamlineInfoHelper;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.LoggingConstants;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the BeamlineInfo interface
 */
public class BeamlineAdapter implements BeamlineInfo, EventSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(BeamlineAdapter.class);
	CorbaBeamlineInfo paramsObj;

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
	public BeamlineAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		paramsObj = CorbaBeamlineInfoHelper.narrow(obj);
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
			logger.debug(LoggingConstants.FINEST, "BeamlineAdapter: Received event for NULL");
		notifyIObservers(this, obj);
	}

	@Override
	public void setDataDir(String value) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setDataDir(value);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public String getDataDir() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getDataDir();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public void setExperimentName(String experiment) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setExperimentName(experiment);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public String getExperimentName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getExperimentName();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public void setFileExtension(String fileExtension) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setFileExtension(fileExtension);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public String getFileExtension() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getFileExtension();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public long getFileNumber() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				long result = paramsObj.getFileNumber();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return 0;
	}

	@Override
	public String getFilePrefix() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getFilePrefix();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getFileSuffix() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getFileSuffix();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getHeader() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getHeader();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public long getNextFileNumber() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				long result = paramsObj.getNextFileNumber();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return 0;
	}

	@Override
	public String getProjectName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getProjectName();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public String getSubHeader() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getSubHeader();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public void setFilePrefix(String filePrefix) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setFilePrefix(filePrefix);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setFileSuffix(String fileSuffix) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setFileSuffix(fileSuffix);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setHeader(String header) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setHeader(header);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setProjectName(String project) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setProjectName(project);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setSubHeader(String subHeader) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setSubHeader(subHeader);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamlineInfoHelper.narrow(netService.reconnect(name));
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
}
