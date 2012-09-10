/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.observable.IObserver;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import uk.ac.gda.client.tomo.alignment.view.handlers.IShutterHandler;

public class TomoShutterHandler implements IShutterHandler {

	private Scannable eh1shtr;

	public void setEh1shtr(Scannable eh1shtr) {
		this.eh1shtr = eh1shtr;
	}

	public Scannable getEh1shtr() {
		return eh1shtr;
	}

	@Override
	public void openShutter(IProgressMonitor monitor) throws DeviceException {
		final SubMonitor progress = SubMonitor.convert(monitor);
		IObserver obs = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				progress.worked(1);
			}
		};
		progress.subTask("Opening shutter");
		eh1shtr.addIObserver(obs);
		eh1shtr.moveTo("Open");
		eh1shtr.deleteIObserver(obs);
		progress.worked(2);
	}

	@Override
	public void closeShutter(IProgressMonitor monitor) throws DeviceException {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.subTask("Closing Shutter");
		eh1shtr.moveTo("Close");
		progress.worked(2);
	}
}
