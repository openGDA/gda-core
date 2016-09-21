/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator.example;


public class UpdatingBeanImpl implements UpdatingBean {

	public static final String UPDATE_BUTTON_TOOLTIP = "Select to turn on automatic updating of the X and Y values by a background thread";

	private boolean update;
	private double x;
	private double y;

	@Override
	public boolean isUpdate() {
		return update;
	}
	@Override
	public void setUpdate(boolean newValue) {
		this.update = newValue;
	}
	@Override
	public double getX() {
		return x;
	}
	@Override
	public void setX(double newValue) {
		this.x = newValue;
	}
	@Override
	public double getY() {
		return y;
	}
	@Override
	public void setY(double newValue) {
		this.y = newValue;
	}
}
