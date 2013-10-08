/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.ncd.NcdController;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.eclipse.swt.widgets.Composite;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.ncd.rcp.views.SaxsQAxisCalibration;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;

public class QAxisCalibration extends SaxsQAxisCalibration {
	private static final Logger logger = LoggerFactory.getLogger(QAxisCalibration.class);
	private NcdController ncdcontroller = NcdController.getInstance();
	private Scannable energyscannable;
	
	@Override
	public void createPartControl(Composite parent) {
		GUI_PLOT_NAME = "Saxs Plot";
		energyscannable = (Scannable) Finder.getInstance().find("energy");
		super.createPartControl(parent);
	}
	
	@Override
	protected String getDetectorName() {
		return ncdcontroller.getDetectorName(NcdDetectorSystem.SAXS_DETECTOR);
	}
	
	@Override
	protected Amount<Energy> getEnergy() {
		Amount<Energy> amount = null;
		try {
			Object[] position = (Object[]) energyscannable.getPosition();
			amount = Amount.valueOf(((Double) position[0]), SI.KILO(NonSI.ELECTRON_VOLT));
		} catch (DeviceException e) {
			logger.error("exception reading pixel size off detector", e);
		}
		return amount;
	}
	
	@Override
	protected Amount<Length> getPixel() {
		String detectorName = ncdcontroller.getDetectorName(NcdDetectorSystem.SAXS_DETECTOR);
		INcdSubDetector detector = ncdcontroller.getDetectorByName(detectorName);
		Amount<Length> amount = null;
		try {
			amount = Amount.valueOf(detector.getPixelSize(), SI.METRE);
		} catch (DeviceException e) {
			logger.error("exception reading pixel size off detector", e);
		}
		return amount;
	}
}