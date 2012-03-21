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

package uk.ac.gda.client.microfocus.views;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

public class BeamCentreFigure extends Figure {
	
	private Polyline horz;
	private Polyline vert;
	private Dimension crossHairSize = new Dimension(100, 100);
	
	public BeamCentreFigure(){
		setLayoutManager(new XYLayout());

		horz = new Polyline();
		horz.setLineWidth(2);
		add(horz, new Rectangle(0, 0, -1, -1));
		
		vert = new Polyline();
		vert.setLineWidth(2);
		add(vert, new Rectangle(0, 0, -1, -1));
		
		update();
	}
	
	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
	
	/**
	 * Updates label contents and box size
	 * This needs to be called from UI thread
	 */
	private void update() {
		PointList horzPl = new PointList(2);
		horzPl.addPoint(new Point(0, crossHairSize.width/2));
		horzPl.addPoint(new Point(crossHairSize.width, crossHairSize.width/2));		
		horz.setPoints(horzPl);
		
		PointList vertPl = new PointList(2);
		vertPl.addPoint(new Point(crossHairSize.height/2, 0));
		vertPl.addPoint(new Point(crossHairSize.height/2, crossHairSize.height));		
		vert.setPoints(vertPl);
	}	
	
	public void setBeamSize(int size){
		this.crossHairSize.height = size;
		this.crossHairSize.width = size;
		update();
	}
	
	 public Dimension getCrossHairSize() {
		 return crossHairSize;
	 }	

}
