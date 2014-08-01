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

package uk.ac.gda.exafs.ui.detectorviews;

import org.dawnsci.plotting.jreality.overlay.events.AbstractOverlayConsumer;
import org.dawnsci.plotting.jreality.overlay.events.OverlayDrawingEvent;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay1DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.swt.widgets.Display;

public class ROIWindowOverlay extends AbstractOverlayConsumer {
	private double xStart, xEnd, yMin, yMax;
	private boolean busy = false;
	private boolean draw = true;

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public ROIWindowOverlay(final Display display, final double  yMin, final double  yMax, final double  defaultXStart, final double  defaultXEnd) {
		super(display);
		this.yMin          = yMin;
		this.yMax          = yMax;
		this.xStart        = defaultXStart;
		this.xEnd          = defaultXEnd;
	}
	
	@Override
	public int [] createDrawingParts(final OverlayProvider provider) {
		int start = provider.registerPrimitive(PrimitiveType.LINE);
		int end   = provider.registerPrimitive(PrimitiveType.LINE);
	    return new int [] {start,end};
	}
	
    @Override
	protected void drawOverlay(final OverlayDrawingEvent event) {
		if (event.isInitialDraw())
			draw(xStart, xEnd);	
		else
			draw(event.getStart().getX(),event.getEnd().getX());
	}
       
    /**
     * Can be called to redraw the overlay when the ui changes box used
     * for the start and end value.
     * 
     * @param x1
     * @param x2
     */
    public void draw(final Number x1, final Number x2) {
        draw(x1.doubleValue(), x2.doubleValue());
    }
    
    private void draw(double xStart, double xEnd) {
    	this.xStart = xStart;
    	this.xEnd   = xEnd;
    	if (draw & (provider != null)){
	    	provider.begin(OverlayType.VECTOR2D);
			provider.setColour(parts[0], java.awt.Color.RED);
			provider.setColour(parts[1], java.awt.Color.RED);
			((Overlay1DProvider)provider).drawLine(parts[0], xStart, yMin, xStart, yMax);		
			((Overlay1DProvider)provider).drawLine(parts[1], xEnd,   yMin, xEnd,   yMax);
			provider.end(OverlayType.VECTOR2D);
		}
   }
    
    /**
     * @param x
     */
    public void setXStart(final double x) {
    	if (busy) {
        	this.xStart = x;
   		    return;
    	}
    	draw(x,xEnd);
    }
    
    /**
     * @param x
     */
    public void setXEnd(final double x) {
    	if (busy) {
        	this.xEnd = x;
   		    return;
    	}
    	draw(xStart,x);
    }

	/**
	 * @param draw
	 */
	public void setDraw(boolean draw) {
		this.draw  = draw;
	}
	
}