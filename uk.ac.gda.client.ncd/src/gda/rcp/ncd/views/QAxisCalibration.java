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

import javax.measure.quantity.Length;

import org.eclipse.swt.widgets.Composite;
import org.jscience.physics.amount.Amount;

import uk.ac.diamond.scisoft.ncd.rcp.views.NcdQAxisCalibration;

public class QAxisCalibration extends NcdQAxisCalibration {

	//FIXME
	@Override
	public void createPartControl(Composite parent) {
		GUI_PLOT_NAME = "Dataset Plot";
		
		super.createPartControl(parent);
		
	}
	
	@Override
	protected String getDetectorName() {
		//  Override in subclass to refer to the calibrated detector
		return null;
	}
	@Override
	protected Amount<Length> getLambda() {
		// TODO Auto-generated method stub
		return super.getLambda();
	}
	
	@Override
	protected Amount<Length> getPixel(boolean b) {
		// TODO Auto-generated method stub
		return super.getPixel(b);
	}
}