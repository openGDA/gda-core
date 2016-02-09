/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import gda.device.Scannable;
import gda.factory.Finder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ScannableModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.ScanningException;

public class ScannableDeviceConnectorService implements IDeviceConnectorService {

	private Map<String, IScannable<?>> scannables;

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {

		if (scannables == null)
			scannables = new HashMap<>(31);
		if (scannables.containsKey(name))
			return (IScannable<T>) scannables.get(name);

		Finder finder = Finder.getInstance();
		Scannable scannable = finder.find(name);

		IScannable<T> s = (IScannable<T>) new ScannableNexusWrapper(scannable);
		scannables.put(name, s);
		return s;
	}

	public class ScannableDelegate implements IScannable<Object> {

		private Scannable delegate;

		public ScannableDelegate(Scannable scannable) {
			delegate = scannable;
		}

		@Override
		public void setLevel(int level) {
			delegate.setLevel(level);
		}

		@Override
		public int getLevel() {
			return delegate.getLevel();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public void setName(String name) {
			delegate.setName(name);
		}

		@Override
		public Object getPosition() throws Exception {
			return delegate.getPosition();
		}

		@Override
		public void setPosition(Object value) throws Exception {
			delegate.moveTo(value);
		}

		@Override
		public void setPosition(Object value, IPosition position) throws Exception {
			delegate.moveTo(position);
		}

		@Override
		public void configure(ScannableModel model) throws ScanningException {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public List<String> getScannableNames() throws ScanningException {
		return Finder.getInstance().listAllNames(Scannable.class.getName());

	}
}
