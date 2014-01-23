/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.example.rcpexample;

import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import uk.ac.gda.beans.ObservableModel;
import uk.ac.gda.client.observablemodels.ScannableWrapper;

public class MVCExampleModel extends ObservableModel implements IMVCExampleModel {

	boolean selected;
	ScannableWrapper wrapper;

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		firePropertyChange(IMVCExampleModel.SELECTED_PROPERTY_NAME, this.selected, this.selected = selected);
	}

	double position;

	@Override
	public double getPosition() {
		return position;
	}

	@Override
	public void setPosition(double position) {
		firePropertyChange(IMVCExampleModel.POSITION_PROPERTY_NAME, this.position, this.position = position);
	}

	protected DummyMotor dummyMotor;
	protected ScannableMotor scannable;

	@Override
	public ScannableWrapper getScannableWrapper() throws Exception {
		if (wrapper == null) {
			dummyMotor = new DummyMotor();
			dummyMotor.setName("dummy_motor");
			dummyMotor.configure();
			scannable = new ScannableMotor();
			scannable.setMotor(dummyMotor);
			scannable.setName("motor1");
			scannable.setUserUnits("mm");
			scannable.configure();
			wrapper = new ScannableWrapper(scannable);
		}
		return wrapper;
	}
}
