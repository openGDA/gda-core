/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.mapping.ui.MappingUtils;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnection.IAxisChangeListener;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

/**
 * A class that wraps a {@link LiveStreamConnection} and implements {@link LiveStreamMapObject}
 * so that a live stream can be plotted in the map view.
 */
public class LiveStreamPlottable implements LiveStreamMapObject {

	private boolean isPlotted = false;

	private final LiveStreamConnection connection;

	private IAxisChangeListener axisChangeListener = this::fireAxisMoveListeners;

	private final Set<IAxisMoveListener> axisMoveListeners = new HashSet<>(4);

	private double[] colorRange;

	private IDynamicShape cachedDataset = null;

	public LiveStreamPlottable(LiveStreamConnection connection) {
		this.connection = connection;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return new Object[0];
	}

	@Override
	public double[] getRange() {
		final List<IDataset> axes = connection.getAxes();
		if (axes == null || axes.isEmpty()) return null;

		IDataset tmp = axes.get(0);
		axes.set(0, axes.get(1));
		axes.set(1, tmp);

		return MappingUtils.calculateRangeFromAxes(axes.toArray(new IDataset[axes.size()]));
	}

	@Override
	public String getLongName() {
		final CameraConfiguration camConfig = connection.getCameraConfig();
		if (camConfig.getDisplayName() != null)
			return camConfig.getDisplayName();
		if (camConfig.getName() != null)
			return camConfig.getName();
		return camConfig.getUrl();
	}

	@Override
	public IDataset getMap() {
		//used to save stream in a similar fashion to registered jpgs
		if (cachedDataset == null) return null;
		try {
			List<IDataset> axes = getAxes();
			Dataset d = DatasetUtils.sliceAndConvertLazyDataset(cachedDataset.getDataset());
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, d.getRank());
			md.setAxis(1, axes.get(0));
			md.setAxis(0, axes.get(1));
			d.setMetadata(md);
			return d;
		} catch (DatasetException e) {
			return null;
		}
	}

	@Override
	public boolean isLive() {
		return false;
	}

	@Override
	public void update() {
		// do nothing
	}

	@Override
	public int getTransparency() {
		return 0;
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		return null;
	}

	@Override
	public String getPath() {
		return connection.getCameraConfig().getDisplayName();
	}

	@Override
	public boolean isPlotted() {
		return isPlotted;
	}

	@Override
	public void setPlotted(boolean plotted) {
		this.isPlotted = plotted;
	}

	@Override
	public List<IDataset> getAxes() {
		return connection.getAxes();
	}

	@Override
	public void connect() throws LiveStreamException {
		connection.addAxisMoveListener(axisChangeListener);
		if (connection.isConnected()) {
			cachedDataset = connection.getStream();
		} else {
			cachedDataset = connection.connect();
		}
	}

	@Override
	public void disconnect() throws LiveStreamException {
		connection.removeAxisMoveListener(axisChangeListener);
		cachedDataset = null;
		connection.disconnect();
	}

	@Override
	public void addAxisListener(IAxisMoveListener listener) {
		axisMoveListeners.add(listener);
	}

	@Override
	public void removeAxisListener(IAxisMoveListener listener) {
		axisMoveListeners.remove(listener);
	}

	private void fireAxisMoveListeners() {
		for (IAxisMoveListener axisMoveListener : axisMoveListeners) {
			axisMoveListener.axisMoved();
		}
	}

	@Override
	public void setColorRange(double[] range) {
		colorRange = range;
	}

	@Override
	public double[] getColorRange() {
		return colorRange;
	}

	@Override
	public IDynamicShape getDynamicDataset() {
		return cachedDataset;
	}

	@Override
	public void setTransparency(int transparency) {
		// not implemented yet
	}

}
