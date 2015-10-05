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

package gda.device.detector.xspress;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.scannable.PositionCallableProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.ac.gda.beans.xspress.XspressDetector;

@Deprecated
public class CallableXspress extends Xspress2Detector implements XspressDetector, PositionCallableProvider<NexusTreeProvider>  {
	private static final long serialVersionUID = 8674467307988118579L;

	AtomicBoolean readingOut = new AtomicBoolean(false);

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		setReadingOut(true);
		Callable<NexusTreeProvider> callable = new Callable<NexusTreeProvider>() {
			@Override
			public NexusTreeProvider call() throws Exception {
				NexusTreeProvider tree = getSuperReadout();
				setReadingOut(false);
				return tree;
			}
		};
		return callable;
		}

		@Override
		public NexusTreeProvider readout() throws DeviceException {
			Callable<NexusTreeProvider> positionCallable = getPositionCallable();

			try {
				NexusTreeProvider treeProvider = positionCallable.call();
				return treeProvider;
			} catch (DeviceException e) {
				throw e;
			} catch (Exception e) {
				throw new DeviceException("something wrong in the callable", e);
			}

		}

		private NexusTreeProvider getSuperReadout() throws Exception {
			return super.readout();
		}

		private void setReadingOut(boolean readingOut) {
			this.readingOut.set(readingOut);
			this.readingOut.notifyAll();
		}
}
