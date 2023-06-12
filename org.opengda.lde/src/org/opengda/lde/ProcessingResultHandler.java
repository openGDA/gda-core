/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.opengda.lde;

import java.util.function.Consumer;

import org.opengda.lde.events.DataReductionFailedEvent;
import org.opengda.lde.events.DataReductionWarnEvent;
import org.opengda.lde.events.NewDataFileEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.ObservableFindable;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/** Handler to dispatch LDE Events based on the results of external processing */
@ServiceInterface(ObservableFindable.class)
public class ProcessingResultHandler implements Consumer<ReductionResponse>, ObservableFindable {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingResultHandler.class);

	/** Name for Findable */
	private String name;
	/** Observable delegate for IObservable */
	private ObservableComponent obsComp = new ObservableComponent();

	@Override
	public void accept(ReductionResponse t) {
		if (t == null) {
			logger.warn("Null result received");
			return;
		}
		switch (t.status()) {
		case OK:
			InterfaceProvider.getTerminalPrinter().print("Plotting reduced data from file " + t.calibrationFilepath());
			sendUpdate(new NewDataFileEvent(null, t.calibrationFilepath()));
			break;
		case WARN:
			InterfaceProvider.getTerminalPrinter().print("Data reduction returns WARN on file: "
					+ t.calibrationFilepath()
					+ "; Cause: "
					+ t.message());
			sendUpdate(new DataReductionWarnEvent(null, t.calibrationFilepath(), t.message()));
			break;
		case ERROR:
			InterfaceProvider.getTerminalPrinter().print("Data reduction failed: " + t.message());
			sendUpdate(new DataReductionFailedEvent(null, t.message()));
			break;
		default:
			logger.warn("Unexpected status: {}", t.message());
		}
		logger.info("Got processing response: {}", t);
	}

	private void sendUpdate(Object event) {
		obsComp.notifyIObservers(this, event);
	}
	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
	}
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
	}
	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}
}
