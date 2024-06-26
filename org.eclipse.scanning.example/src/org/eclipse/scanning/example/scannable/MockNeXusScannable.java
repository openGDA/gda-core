/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.example.scannable;

import java.text.MessageFormat;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.NexusRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.IScanAttributeContainer;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;

/**
 *
 * A class to wrap any IScannable as a positioner and then write to a nexus file
 * as the positions are set during the scan.
 *
 * @author Matthew Gerring
 *
 */
public class MockNeXusScannable extends MockScannable implements INexusDevice<NXpositioner> {

	public boolean isWritingOn() {
		return writingOn;
	}

	public void setWritingOn(boolean writingOn) {
		this.writingOn = writingOn;
	}

	public static final String FIELD_NAME_SET_VALUE = NXpositioner.NX_VALUE + "_set";

	private ILazyWriteableDataset lzSet;
	private ILazyWriteableDataset lzValue;

	private boolean writingOn = true;

	public MockNeXusScannable() {
		super();
	}

	public MockNeXusScannable(String name, Number position, int level) {
		super(name, position, level);
	}

	public MockNeXusScannable(String name, Number position, int level, String unit) {
		super(name, position, level, unit);
	}

	@ScanFinally
	public void scanFinally() {
		lzSet   = null;
		lzValue = null;
	}

	@Override
	public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
		positioner.setNameScalar(getName());

		if (info.getScanRole(getName()).getNexusRole() == NexusRole.PER_SCAN) {
			positioner.setField(FIELD_NAME_SET_VALUE, getPosition());
			positioner.setValueScalar(getPosition());
		} else { // NexusRole.PER_POINT
			final Class<? extends Number> positionClass = getPosition().getClass();
			this.lzSet = positioner.initializeLazyDataset(FIELD_NAME_SET_VALUE, 1, positionClass);
			lzSet.setChunking(8); // Faster than looking at the shape of the scan for this dimension because slow to iterate.
			lzSet.setWritingAsync(true);

			this.lzValue  = positioner.initializeLazyDataset(NXpositioner.NX_VALUE, info.getOuterRank(), positionClass);
			lzValue.setChunking(info.getOuterShape()); // use the scan shape for chunking
			lzValue.setWritingAsync(true);
		}

		if (getUnit() != null) {
			positioner.setAttribute(NXpositioner.NX_VALUE, "units", getUnit());
			positioner.setAttribute(FIELD_NAME_SET_VALUE, "units", getUnit());
		}

		registerAttributes(positioner, this);

		NexusObjectWrapper<NXpositioner> nexusDelegate = new NexusObjectWrapper<>(
				getName(), positioner, NXpositioner.NX_VALUE);
		nexusDelegate.setDefaultAxisDataFieldName(FIELD_NAME_SET_VALUE);
		return nexusDelegate;
	}

	@Override
	public Number setPosition(Number value, IPosition position) throws ScanningException {
		try {
			if (value!=null) {
				int index = position!=null ? position.getIndex(getName()) : -1;
				if (isRealisticMove()) {
					value = doRealisticMove(value, index, -1);
				}
				this.position = value;
				delegate.firePositionPerformed(-1, new Scalar<>(getName(), index, value.doubleValue()));
			}

			if (position!=null) {
				return write(value, getPosition(), position);
			}
		} catch (Exception e) {
			throw new ScanningException("Could not set position of scannable " + getName(), e);
		}
		return this.position;
	}

	private Number write(Number demand, Number actual, IPosition loc) throws ScanningException, DatasetException {

		if (lzValue==null) return actual;
		if (actual!=null) {
			// write actual position
			final Dataset newActualPositionData = DatasetFactory.createFromObject(actual);
			IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(loc);
			SliceND sliceND = new SliceND(lzValue.getShape(), lzValue.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
			if (isWritingOn()) lzValue.setSlice(null, newActualPositionData, sliceND);
		}

		if (lzSet==null) return actual;
		if (demand!=null) {
			int index = loc.getIndex(getName());
			if (index<0) {
				throw new ScanningException("Incorrect data index for scan for value of '"+getName()+"'. The index is "+index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final Dataset newDemandPositionData = DatasetFactory.createFromObject(demand);
			if (isWritingOn()) lzSet.setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
		return actual;
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
		if (container.getScanAttributeNames()!=null) for(String attrName : container.getScanAttributeNames()) {
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
