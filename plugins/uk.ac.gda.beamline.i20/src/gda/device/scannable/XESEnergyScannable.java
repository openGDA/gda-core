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
	private int materialType = -1;
	private int cut1Val = -1;
	private int cut2Val = -1;
	private int cut3Val = -1;
	
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
		if (arg instanceof ScannableStatus) {
			notifyIObservers(this, new ScannableStatus(getName(), ((ScannableStatus) arg).getStatus()));
		}
	}

	public int[] getCrystalCut() throws DeviceException {
		int cut1;
		int cut2;
		int cut3;
		if(cut1Val==-1||cut2Val==-1||cut3Val==-1){
			cut1 = (int) Double.parseDouble(cut1Scannable.getPosition().toString());
			cut2 = (int) Double.parseDouble(cut2Scannable.getPosition().toString());
			cut3 = (int) Double.parseDouble(cut3Scannable.getPosition().toString());
		}
		else{
			cut1 = this.cut1Val;
			cut2 = this.cut2Val;
			cut3 = this.cut3Val;
		}
		int[] cut = { cut1, cut2, cut3 };
		return cut;
	}

	public void setMaterialType(int type) {
		materialType = type;
	}

	public int getMaterialType() throws DeviceException {
		if (materialType == -1) {
			String materialVal = materialScannable.getPosition().toString();
			if (materialVal.equals("Si"))
				return 0;
			else if (materialVal.equals("Ge"))
				return 1;
			return -1;
		}
		return materialType;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		XesMaterial material = null;
		XesUtils.XesMaterial silicon = XesUtils.XesMaterial.SILICON;
		XesUtils.XesMaterial germanium = XesUtils.XesMaterial.GERMANIUM;
		if (getMaterialType() == 0)
			material = silicon;
		else if (getMaterialType() == 1)
			material =  germanium;
		double bragg = XesUtils.getBragg(Double.parseDouble(position.toString()), material, getCrystalCut());
		if (bragg >= 60.0 && bragg <= 85.0)
			xes.asynchronousMoveTo(bragg);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		XesMaterial material = null;
		XesUtils.XesMaterial silicon = XesUtils.XesMaterial.SILICON;
		XesUtils.XesMaterial germanium = XesUtils.XesMaterial.GERMANIUM;
		if (getMaterialType() == 0)
			material = silicon;
		else if (getMaterialType() == 1)
			material =  germanium;
		
		//xes.bragg=80;
		
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

	public void setCut1Val(int cut){
		cut1Val = cut;
	}
	
	public void setCut2Val(int cut){
		cut2Val = cut;
	}
	
	public void setCut3Val(int cut){
		cut3Val = cut;
	}
	
	public int getCut1Val(){
		return cut1Val;
	}
	
	public int getCut2Val(){
		return cut2Val;
	}
	
	public int getCut3Val(){
		return cut3Val;
	}
	
	public Scannable getMaterial() {
		return materialScannable;
	}

	public void setMaterial(Scannable material) {
		this.materialScannable = material;
	}
}
