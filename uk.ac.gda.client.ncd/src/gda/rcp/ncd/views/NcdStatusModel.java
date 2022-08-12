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

package gda.rcp.ncd.views;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.diamond.daq.scm.api.events.NcdMetaType;
import uk.ac.diamond.daq.scm.api.events.NcdMsgFactory;
import uk.ac.diamond.daq.scm.api.events.StatusUpdated;

public class NcdStatusModel implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(NcdStatusModel.class);

	private static final String THICKNESS_METADATA = "sample_thickness";
	private static final String BACKGROUND_METADATA = "sample_background";

	private final Metadata meta;

	@FieldDescriptor(label="SAXS Mask", file=FileType.EXISTING_FILE)
	private String saxsMask;

	@FieldDescriptor(label="WAXS Mask", file=FileType.EXISTING_FILE)
	private String waxsMask;

	@FieldDescriptor(label="SAXS Calibration", file=FileType.EXISTING_FILE)
	private String saxsCalibration;

	@FieldDescriptor(label="WAXS Calibration", file=FileType.EXISTING_FILE)
	private String waxsCalibration;

	@FieldDescriptor(label="Sample Thickness", unit="mm", hint="Thickness of sample", minimum=0.0, numberFormat="#0.0")
	private double sampleThickness;

	@FieldDescriptor(label="Sample Background", file=FileType.EXISTING_FILE)
	private String sampleBackground = "";

	@FieldDescriptor(visible=false)
	private Scannable thicknessScannable;

	public double getSampleThickness() {
		try {
			return (double) thicknessScannable.getPosition();
		} catch (DeviceException e) {
		}
		return Double.NaN;
	}
	public void setSampleThickness(double thickness) {
		logger.trace("Setting sample thickness to {}", thickness);
		try {
			this.thicknessScannable.moveTo(thickness);
		} catch (DeviceException e) {
		}
	}
	public String getSampleBackground() {
		return meta.getMetadataValue(BACKGROUND_METADATA);
	}

	public void setSampleBackground(String backgroundPath) {
		logger.trace("Setting background file to {}", backgroundPath);
		if (backgroundPath == null) {
			backgroundPath = "";
		}
		meta.setMetadataValue(BACKGROUND_METADATA, backgroundPath);
	}
	private NcdMsgFactory saxsCalMsg = new NcdMsgFactory("SAXS", NcdMetaType.CALIBRATION);
	private NcdMsgFactory waxsCalMsg = new NcdMsgFactory("WAXS", NcdMetaType.CALIBRATION);
	private NcdMsgFactory saxsMaskMsg = new NcdMsgFactory("SAXS", NcdMetaType.MASK);
	private NcdMsgFactory waxsMaskMsg = new NcdMsgFactory("WAXS", NcdMetaType.MASK);

	public NcdStatusModel() {
		Finder.findOptionalOfType(THICKNESS_METADATA, Scannable.class)
				.ifPresent(this::setThicknessScannable);
		meta = GDAMetadataProvider.getInstance();
		if (meta != null) {
			meta.addIObserver(this);
		}
		MsgBus.publish(saxsCalMsg.refresh());
		MsgBus.publish(waxsCalMsg.refresh());
		MsgBus.publish(saxsMaskMsg.refresh());
		MsgBus.publish(waxsMaskMsg.refresh());
	}
	public String getSaxsMask() {
		return saxsMask;
	}

	public void setSaxsMask(String saxsMask) {
		logger.trace("Setting SAXS mask to {}", saxsMask);
		if (saxsMask == null) {
			MsgBus.publish(saxsMaskMsg.changeRequest(null, null));
		} else if (saxsMask.contains("#")) {
			String[] parts = saxsMask.split("#", 2);
			MsgBus.publish(saxsMaskMsg.changeRequest(parts[0], parts[1]));
		} else {
			MsgBus.publish(saxsMaskMsg.changeRequest(saxsMask, null));
		}
	}

	public String getSaxsCalibration() {
		return saxsCalibration;
	}

	public void setSaxsCalibration(String saxsCalibration) {
		logger.trace("Setting SAXS calibration to {}", saxsCalibration);
		MsgBus.publish(saxsCalMsg.changeRequest(saxsCalibration, null));
	}

	public String getWaxsCalibration() {
		return waxsCalibration;
	}

	public void setWaxsCalibration(String waxsCalibration) {
		logger.trace("Setting WAXS calibration to {}", waxsCalibration);
		MsgBus.publish(waxsCalMsg.changeRequest(waxsCalibration, null));
	}

	public String getWaxsMask() {
		return waxsMask;
	}

	public void setWaxsMask(String waxsMask) {
		logger.trace("Setting WAXS mask to {}", waxsMask);
		if (waxsMask == null) {
			MsgBus.publish(waxsMaskMsg.changeRequest(null, null));
		} else if (waxsMask.contains("#")) {
			String[] parts = waxsMask.split("#", 1);
			MsgBus.publish(waxsMaskMsg.changeRequest(parts[0], parts[1]));
		} else {
			MsgBus.publish(waxsMaskMsg.changeRequest(waxsMask, null));
		}
	}

	protected void setSaxsMaskDirect(String mask) {
		saxsMask = mask;
	}
	protected void setWaxsMaskDirect(String mask) {
		waxsMask = mask;
	}
	protected void setSaxsCalibrationDirect(String cal) {
		saxsCalibration = cal;
	}
	protected void setWaxsCalibrationDirect(String cal) {
		waxsCalibration = cal;
	}
	public Scannable getThicknessScannable() {
		return thicknessScannable;
	}
	public void setThicknessScannable(Scannable thicknessScannable) {
		if (this.thicknessScannable != null) {
			this.thicknessScannable.deleteIObserver(this);
		}
		this.thicknessScannable = thicknessScannable;
		if (thicknessScannable != null) {
			thicknessScannable.addIObserver(this);
		}
	}

	public void close() {
		if (thicknessScannable != null) {
			thicknessScannable.deleteIObserver(this);
		}
		if (meta != null) {
			meta.deleteIObserver(this);
		}
	}
	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScannablePositionChangeEvent) {
			ScannablePositionChangeEvent e = (ScannablePositionChangeEvent)arg;
			if (((Scannable)source).getName().equals(thicknessScannable.getName())) {
				sampleThickness = (double)e.newPosition;
				MsgBus.publish(new StatusUpdated());
			}
		} else if (source instanceof Metadata) {
			// This will cause a refresh for all entries that are updated - see DAQ-3862
			MsgBus.publish(new StatusUpdated());
		}
	}
}
