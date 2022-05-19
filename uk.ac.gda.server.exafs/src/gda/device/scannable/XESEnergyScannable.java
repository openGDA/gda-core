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

package uk.ac.gda.beamline.i20.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotionUnitsBase;
import gda.device.scannable.ScannableStatus;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.XesUtils;
import gda.observable.IObserver;
import gda.util.CrystalParameters.CrystalMaterial;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IXesEnergyScannable.class)
public class XESEnergyScannable extends ScannableMotionUnitsBase implements IObserver, IXesEnergyScannable {

	private XesSpectrometerScannable xes;
	private Scannable cut1Scannable;
	private Scannable cut2Scannable;
	private Scannable cut3Scannable;
	private Scannable materialScannable;

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		this.inputNames = new String[] { getName() };
		xes.addIObserver(this);
		setConfigured(true);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return xes.isBusy();
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScannableStatus)
			notifyIObservers(this, arg);
	}

	@Override
	public int[] getCrystalCut() throws DeviceException {
		int cut1 = (int) Double.parseDouble(cut1Scannable.getPosition().toString());
		int cut2 = (int) Double.parseDouble(cut2Scannable.getPosition().toString());
		int cut3 = (int) Double.parseDouble(cut3Scannable.getPosition().toString());
		int[] cut = { cut1, cut2, cut3 };
		return cut;
	}

	@Override
	public CrystalMaterial getMaterialType() throws DeviceException {
		String materialVal = materialScannable.getPosition().toString();
		if (materialVal.equals("Si"))
			return CrystalMaterial.SILICON;
		else if (materialVal.equals("Ge"))
			return CrystalMaterial.GERMANIUM;
		throw new DeviceException("Material type could not be determined");
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		CrystalMaterial material = getMaterialType();
		String stringPosition = position.toString();
		double doublePosition = Double.parseDouble(stringPosition);
		double bragg = XesUtils.getBragg(doublePosition, material, getCrystalCut());
		if (bragg >= XesUtils.MIN_THETA && bragg <= XesUtils.MAX_THETA)
			xes.asynchronousMoveTo(bragg);
		else
			throw new DeviceException("Move to " + bragg + "deg out of limits. Must be " + XesUtils.MIN_THETA + " to " + XesUtils.MAX_THETA + " deg.");
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		CrystalMaterial material = getMaterialType();
		double energy = XesUtils.getFluoEnergy(Double.parseDouble(xes.getPosition().toString()), material, getCrystalCut());
		if(energy<100000){
			String en = String.valueOf(energy);
			if(en.length()>8){
				double enVal = Double.parseDouble(en.substring(0,7));
				return enVal;
			}
			return energy;
		}
		return 0;
	}

	@Override
	public void stop() throws DeviceException {
		xes.stop();
	}

	public XesSpectrometerScannable getXes() {
		return xes;
	}

	public void setXes(XesSpectrometerScannable xes) {
		this.xes = xes;
	}

	public Scannable getCut1() {
		return cut1Scannable;
	}

	public void setCut1(Scannable cut1) {
		this.cut1Scannable = cut1;
	}

	public Scannable getCut2() {
		return cut2Scannable;
	}

	public void setCut2(Scannable cut2) {
		this.cut2Scannable = cut2;
	}

	public Scannable getCut3() {
		return cut3Scannable;
	}

	public void setCut3(Scannable cut3) {
		this.cut3Scannable = cut3;
	}

	public Scannable getMaterial() {
		return materialScannable;
	}

	public void setMaterial(Scannable material) {
		this.materialScannable = material;
	}

	@Override
	public double getRadius() throws DeviceException {
		return xes.getRadius();
	}
}