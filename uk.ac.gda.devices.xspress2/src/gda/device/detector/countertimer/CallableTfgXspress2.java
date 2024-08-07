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

package gda.device.detector.countertimer;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import gda.device.DeviceException;
import gda.device.scannable.PositionCallableProvider;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

@Deprecated(since="GDA 8.44")
public class CallableTfgXspress2 extends TfgXspress2 implements PositionCallableProvider<Object> {

	private static final long serialVersionUID = 657534701405273689L;
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(CallableTfgXspress2.class);

	public CallableTfgXspress2() {
		logger.deprecatedClass();
	}

	AtomicBoolean readingOut = new AtomicBoolean(false);
	@Override
	public Callable<Object> getPositionCallable() throws DeviceException {
		setReadingOut(true);
		Callable<Object> callable = new Callable<>(){
			@Override
			public Object call() throws Exception {
				Object reply = getSuperReadout();
			setReadingOut(false);
			return reply;
			}
		};
			return callable;
	}

	@Override
	public Object readout() throws DeviceException {
		Callable<Object> positionCallable = getPositionCallable();

		try {
			Object treeProvider = positionCallable.call();
			return treeProvider;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("something wrong in the callable", e);
		}

	}

	protected Object getSuperReadout() throws DeviceException {
		return super.readout();
	}

	private void setReadingOut(boolean readingOut) {
		this.readingOut.set(readingOut);
		this.readingOut.notifyAll();
	}
}