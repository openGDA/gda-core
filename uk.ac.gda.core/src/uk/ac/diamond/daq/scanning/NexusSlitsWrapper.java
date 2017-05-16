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

package uk.ac.diamond.daq.scanning;

import java.text.MessageFormat;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXslit;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.NexusRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScanAttributeContainer;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.event.scan.DeviceValueMultiPosition;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Class provides an implementation which will write an NXslit to NeXus based on information from individual scannables
 */
public class NexusSlitsWrapper extends AbstractScannable<DeviceValueMultiPosition> implements INexusDevice<NXslit> {

	private static final Logger logger = LoggerFactory.getLogger(NexusSlitsWrapper.class);

	private Scannable x_gap;
	private Scannable y_gap;

	public NexusSlitsWrapper() {
		super(ScannableDeviceConnectorService.getInstance());
	}

	// implements IScannable<DeviceValueMultiPosition>

	@Override
	public DeviceValueMultiPosition getPosition() throws Exception {
		DeviceValueMultiPosition position = new DeviceValueMultiPosition();
		position.put(NXslit.NX_X_GAP, (double)x_gap.getPosition());
		position.put(NXslit.NX_Y_GAP, (double)y_gap.getPosition());
		return position;
	}

	@Override
	public void setPosition(DeviceValueMultiPosition value, IPosition position) throws Exception {
		logger.debug("setPosition({}, {}) called on {}", value, position, getName());

		if (value!=null) {
			logger.warn("non null setPosition() not expected on {}, ignoring...", getName());
		}

		if (position!=null) {
			write(value, getPosition(), position);
		}
	}

	private void write(DeviceValueMultiPosition demand, DeviceValueMultiPosition actual, IPosition loc) throws Exception {

		// There may be actual values when there aren't demand values, but there should never be demand values when
		// there are no actual values.
		if (xLzValue==null || yLzValue==null) {
			return;
		}
		if (actual!=null) {
			// write actual position
			final Dataset newXPositionData = DatasetFactory.createFromObject(actual.get(NXslit.NX_X_GAP));
			final Dataset newYPositionData = DatasetFactory.createFromObject(actual.get(NXslit.NX_Y_GAP));
			IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(loc);
			SliceND xSliceND = new SliceND(xLzValue.getShape(), xLzValue.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
			SliceND ySliceND = new SliceND(yLzValue.getShape(), yLzValue.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
			if (isWritingOn()) {
				xLzValue.setSlice(null, newXPositionData, xSliceND);
				yLzValue.setSlice(null, newYPositionData, ySliceND);
			}
		}

		if (xLzSet==null || yLzSet==null) {
			return;
		}
		if (demand!=null) {
			int index = loc.getIndex(getName());
			if (index<0) {
				throw new Exception("Incorrect data index for scan for value of '"+getName()+"'. The index is "+index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final Dataset newDemandPositionData = DatasetFactory.createFromObject(demand);
			if (isWritingOn()) {
				xLzSet.setSlice(null, newDemandPositionData, startPos, stopPos, null);
				yLzSet.setSlice(null, newDemandPositionData, startPos, stopPos, null);
			}
		}
	}

	// implements INexusDevice<NXslit>

	@Override
	public NexusObjectProvider<NXslit> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXslit positioner = NexusNodeFactory.createNXslit();

		NexusRole nexusRole = info.getScanRole(getName()).getNexusRole();
		if (nexusRole == NexusRole.PER_SCAN) {
			try {
				positioner.setX_gapScalar((Number)x_gap.getPosition());
				positioner.setY_gapScalar((Number)y_gap.getPosition());
			} catch (DeviceException e) {
				logger.error("Error getting position of {} or {}", x_gap.getName(), y_gap.getName(), e);
			}
		} else if (nexusRole == NexusRole.PER_POINT) {
			String floatFill = System.getProperty("GDA/gda.nexus.floatfillvalue", "NaN");
			double fill = "NaN".equalsIgnoreCase(floatFill) ? Double.NaN : Double.parseDouble(floatFill);

			xLzSet = positioner.initializeLazyDataset(NXslit.NX_X_GAP, 1, Double.class);
			yLzSet = positioner.initializeLazyDataset(NXslit.NX_Y_GAP, 1, Double.class);
			xLzSet.setFillValue(fill);
			yLzSet.setFillValue(fill);
			xLzSet.setChunking(new int[]{8}); // Faster than looking at the shape of the scan for this dimension because slow to iterate.
			yLzSet.setChunking(new int[]{8}); // Faster than looking at the shape of the scan for this dimension because slow to iterate.
			xLzSet.setWritingAsync(true);
			yLzSet.setWritingAsync(true);

			xLzValue = positioner.initializeLazyDataset(NXslit.NX_X_GAP, info.getRank(), Double.class);
			yLzValue = positioner.initializeLazyDataset(NXslit.NX_Y_GAP, info.getRank(), Double.class);
			xLzValue.setFillValue(fill);
			yLzValue.setFillValue(fill);
			xLzValue.setChunking(info.createChunk(false, 8)); // Might be slow, need to check this
			yLzValue.setChunking(info.createChunk(false, 8)); // Might be slow, need to check this
			xLzValue.setWritingAsync(true);
			yLzValue.setWritingAsync(true);
		}

		registerAttributes(positioner, this);

		NexusObjectWrapper<NXslit> nexusDelegate = new NexusObjectWrapper<>(
				getName(), positioner, NXslit.NX_X_GAP, NXslit.NX_Y_GAP);
		nexusDelegate.setDefaultAxisDataFieldName(NXslit.NX_X_GAP);
		nexusDelegate.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return nexusDelegate;
	}

	// Class

	private ILazyWriteableDataset xLzSet;
	private ILazyWriteableDataset yLzSet;
	private ILazyWriteableDataset xLzValue;
	private ILazyWriteableDataset yLzValue;

	private boolean writingOn = true;

	public boolean isWritingOn() {
		return writingOn;
	}

	public void setWritingOn(boolean writingOn) {
		this.writingOn = writingOn;
	}

	@ScanFinally
	public void nullify() {
		xLzSet   = null;
		xLzValue = null;
		yLzSet   = null;
		yLzValue = null;
	}

	/**
	 * Add the attributes for the given attribute container into the given nexus object.
	 * @param positioner
	 * @param container
	 * @throws NexusException if the attributes could not be added for any reason
	 */
	private static void registerAttributes(NXobject nexusObject, IScanAttributeContainer container) throws NexusException {
		// We create the attributes, if any
		nexusObject.setField("name", container.getName());
		if (container.getScanAttributeNames()!=null) {
			for(String attrName : container.getScanAttributeNames()) {
				try {
					nexusObject.setField(attrName, container.getScanAttribute(attrName));
				} catch (Exception e) {
					throw new NexusException(MessageFormat.format(
							"An exception occurred attempting to get the value of the attribute ''{0}'' for the device ''{1}''",
							container.getName(), attrName), e);
				}
			}
		}
	}

	public Scannable getX_gap() {
		return x_gap;
	}

	public void setX_gap(Scannable x_gap) {
		this.x_gap = x_gap;
		//this.x_gap.addIObserver(this);
	}

	public Scannable getY_gap() {
		return y_gap;
	}

	public void setY_gap(Scannable y_gap) {
		this.y_gap = y_gap;
		//this.y_gap.addIObserver(this);
	}
}
