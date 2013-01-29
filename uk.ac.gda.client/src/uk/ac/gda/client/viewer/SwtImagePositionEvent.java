/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.viewer;

import java.util.List;

import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;


public class SwtImagePositionEvent implements IImagePositionEvent {

	private int[] imagePosition;
	private double[] position; 
	private short flags;
	private Mode mode;
	private SwtHitTestCalculator hitTestCalculator;
	
	
	public SwtImagePositionEvent(double[] pos, int[] imagePos, SwtHitTestCalculator hitTestCalculator) {
		this.position = pos;
		this.imagePosition = imagePos;
		this.hitTestCalculator = hitTestCalculator;
	}
	

	public SwtImagePositionEvent(double[] pos, int[] imagePos, short flags, Mode mode, SwtHitTestCalculator hitTestCalculator) {
		this(pos, imagePos, hitTestCalculator);
		this.flags = flags;
		this.mode = mode;
	}
	
	@Override
	public short getFlags() {
		return flags;
	}

	@Override
	public int[] getImagePosition() {
		return imagePosition;
	}

	@Override
	public int getPrimitiveID() {
		return hitTestCalculator.getPrimitiveID();
	}

	@Override
	public List<Integer> getPrimitiveIDs() {
		return hitTestCalculator.getPrimitiveIDs();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public double[] getPosition() {
		return position;
	}
	
	@Override
	public String toString() {
		return String.format("%s[position=(%.1f, %.1f), imagePosition=(%d, %d)]",
			getClass().getSimpleName(),
			position[0], position[1],
			imagePosition[0], imagePosition[1]);
	}
}
