/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.jython.logger;

import gda.jython.IBatonStateProvider;
import gda.jython.JythonServerFacade;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;

/**
 * A BatonChangedLineLoggerAdapter listens for ScanDataPoints and logs them to the specified logger.
 */
public class BatonChangedAdapter implements IObserver {

	private final LineLogger logger;
	private final IBatonStateProvider batonStateProvider;

	/**
	 * 
	 * @param logger
	 * @param batonStateProvider The GDA's {@link JythonServerFacade} singleton is often a good choice.
	 */
	BatonChangedAdapter(LineLogger logger, IBatonStateProvider batonStateProvider) {
		this.logger = logger;
		this.batonStateProvider = batonStateProvider;
		batonStateProvider.addBatonChangedObserver(this);
	}

	@Override
	public void update(Object source, Object event) {
		if (event instanceof BatonChanged) {
			// JSF sends updates to all IObservers no matter which Provider interface they registered with
			ClientDetails batonHolder = batonStateProvider.getBatonHolder();
			logger.log("<<<Baton acquired by: '" + (batonHolder != null ? batonHolder.toString() : "no one") + "' >>>");
		}
	}
}