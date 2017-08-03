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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.diamond.daq.scm.api.events.NcdMetaType;
import uk.ac.diamond.daq.scm.api.events.NcdMsgFactory;
import uk.ac.diamond.daq.scm.api.events.NcdStatus;
import uk.ac.diamond.daq.scm.api.events.StatusUpdated;

public class NcdStatusModel implements IObserver {

	private static final String THICKNESS_METADATA = "sample_thickness";

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
		try {
			this.thicknessScannable.moveTo(thickness);
		} catch (DeviceException e) {
		}
	}
	private NcdMsgFactory saxsCalMsg = new NcdMsgFactory("SAXS", NcdMetaType.CALIBRATION);
	private NcdMsgFactory waxsCalMsg = new NcdMsgFactory("WAXS", NcdMetaType.CALIBRATION);
	private NcdMsgFactory saxsMaskMsg = new NcdMsgFactory("SAXS", NcdMetaType.MASK);
	private NcdMsgFactory waxsMaskMsg = new NcdMsgFactory("WAXS", NcdMetaType.MASK);

	public NcdStatusModel() {
		Findable find = Finder.getInstance().find(THICKNESS_METADATA);
		if (find instanceof ScannableAdapter) {
			setThicknessScannable(((Scannable) find));
		} else {
		}
		MsgBus.subscribe(this);
		MsgBus.publish(saxsCalMsg.refresh());
		MsgBus.publish(waxsCalMsg.refresh());
		MsgBus.publish(saxsMaskMsg.refresh());
		MsgBus.publish(waxsMaskMsg.refresh());
	}
	public String getSaxsMask() {
		return saxsMask;
	}

	public void setSaxsMask(String saxsMask) {
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
		MsgBus.publish(saxsCalMsg.changeRequest(saxsCalibration, null));
	}

	public String getWaxsCalibration() {
		return waxsCalibration;
	}

	public void setWaxsCalibration(String waxsCalibration) {
		MsgBus.publish(waxsCalMsg.changeRequest(waxsCalibration, null));
	}

	public String getWaxsMask() {
		return waxsMask;
	}

	public void setWaxsMask(String waxsMask) {
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
	}
	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScannablePositionChangeEvent) {
			ScannablePositionChangeEvent e = (ScannablePositionChangeEvent)arg;
			if (((Scannable)source).getName().equals(thicknessScannable.getName())) {
				sampleThickness = (double)e.newPosition;
				MsgBus.publish(new StatusUpdated());
			}
		}
	}
}
