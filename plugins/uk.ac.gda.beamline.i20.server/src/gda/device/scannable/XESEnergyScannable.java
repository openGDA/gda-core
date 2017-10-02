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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.exafs.xes.XesUtils;
import gda.exafs.xes.XesUtils.XesMaterial;
import gda.observable.IObserver;

public class XESEnergyScannable extends ScannableMotionUnitsBase implements IObserver {

	private XesSpectrometerScannable xes;
	private Scannable cut1Scannable;
	private Scannable cut2Scannable;
	private Scannable cut3Scannable;
	private Scannable materialScannable;

	@Override
	public void configure() {
		this.inputNames = new String[] { getName() };
		xes.addIObserver(this);
		configured = true;
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

	public int[] getCrystalCut() throws DeviceException {
		int cut1 = (int) Double.parseDouble(cut1Scannable.getPosition().toString());
		int cut2 = (int) Double.parseDouble(cut2Scannable.getPosition().toString());
		int cut3 = (int) Double.parseDouble(cut3Scannable.getPosition().toString());
		int[] cut = { cut1, cut2, cut3 };
		return cut;
	}

	public int getMaterialType() throws DeviceException {
		String materialVal = materialScannable.getPosition().toString();
		if (materialVal.equals("Si"))
			return 0;
		else if (materialVal.equals("Ge"))
			return 1;
		throw new DeviceException("Material type could not be determined");
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		XesMaterial material = getCurrentMaterial();
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
		XesMaterial material = getCurrentMaterial();
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

	private XesMaterial getCurrentMaterial() throws DeviceException {
		XesMaterial material = null;
		XesUtils.XesMaterial silicon = XesUtils.XesMaterial.SILICON;
		XesUtils.XesMaterial germanium = XesUtils.XesMaterial.GERMANIUM;
		if (getMaterialType() == 0)
			material = silicon;
		else if (getMaterialType() == 1)
			material =  germanium;
		return material;
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

}