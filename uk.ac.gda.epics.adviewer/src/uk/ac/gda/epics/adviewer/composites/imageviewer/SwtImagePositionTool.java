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

package uk.ac.gda.epics.adviewer.composites.imageviewer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.swt.SWT;

import uk.ac.gda.epics.adviewer.composites.imageviewer.IDataPositionEvent.Mode;

public class SwtImagePositionTool {
	
	private static class ListenerPair
	{
		ImagePositionListener listener;
		SWT2DOverlayProvider provider;
		public ListenerPair(ImagePositionListener newListener, SWT2DOverlayProvider swtProvider) {
			this.listener = newListener;
			this.provider = swtProvider;
		}
	}
	
	private LinkedList<ListenerPair> listeners;

	public SwtImagePositionTool(){
		listeners = new LinkedList<ListenerPair>();
	}

	/**
	 * Converts the position of the mouse in the view (in view/screen pixels) to the position within the image (in
	 * image/canvas pixels).
	 */
	private int[] getImagePosHelper(MouseEvent event, int dbx, int dby, float zoomLevel) {
		
		// Position in view pixels
		int x = event.x;
		int y = event.y;
		
		// Take into account the digital zoom to convert to canvas pixels
		x /= zoomLevel;
		y /= zoomLevel;
		
		// Convert to an offset from the top-left of the image, in canvas pixels
		int imagePos[] = { x - dbx, y - dby};
		
		return imagePos;
	}

	/**
	 * Converts the position of the mouse in the view, in view/screen pixels, to image/canvas pixels.
	 */
	private int[] getZoomedPosHelper(MouseEvent event, float zoomLevel) {
		
		// Position in view pixels
		int x = event.x;
		int y = event.y;
		
		// Take into account the digital zoom to convert to canvas pixels
		x /= zoomLevel;
		y /= zoomLevel;
		
		int zoomedPostition[] = { x, y};
		
		return zoomedPostition;
	}

	public void activate(MouseEvent event, int dbx, int dby, float zoomLevel) {
		double pos[] = {event.x, event.y };
		int imagePos[] = getImagePosHelper(event, dbx, dby, zoomLevel);
		int zoomedPos[] = getZoomedPosHelper(event, zoomLevel);
		
		short flags = getFlagsHelper(event);


		for (ListenerPair pair : listeners) {
			SwtHitTestCalculator calculator = null;
			if (pair.provider != null) {
				calculator = new SwtHitTestCalculator(pair.provider, zoomedPos[0], zoomedPos[1]);
			}
			IImagePositionEvent imageEvent = new SwtImagePositionEvent(pos, imagePos, flags, Mode.START, calculator);
			pair.listener.imageStart(imageEvent);
		}
	}

	public void deactivate(MouseEvent event, int dbx, int dby, float zoomLevel) {
		double pos[] = {event.x, event.y };
		int zoomedPos[] = getZoomedPosHelper(event, zoomLevel);
		
		int imagePos[] = getImagePosHelper(event, dbx, dby, zoomLevel);
		short flags = getFlagsHelper(event);
		
		for (ListenerPair pair : listeners) {
			SwtHitTestCalculator calculator = null;
			if (pair.provider != null) {
				calculator = new SwtHitTestCalculator(pair.provider, zoomedPos[0], zoomedPos[1]);
			}
			IImagePositionEvent imageEvent = new SwtImagePositionEvent(pos, imagePos, flags, Mode.END, calculator);
			pair.listener.imageFinished(imageEvent);
		}
	}

	private static final boolean DEBUG = false;
	
	public void perform(MouseEvent event, int dbx, int dby, float zoomLevel) {
		
		// Terminology:
		//
		//  * Canvas: the stuff actually being drawn in the view. This changes size when the digital zoom (scroll
		//    wheel) is used.
		//
		//  * Image: this is part of the canvas. Also affected by the digital zoom.
		//
		//  * View: displays the canvas at different (digital) zoom levels. The amount of canvas it shows varies
		//    depending on the digital zoom level.
		
		// Parameters:
		//
		//  * event contains the coordinates of the mouse in the view, in screen pixels. Top left is (0, 0).
		//
		//  * dbx/dby give the image's offset from the top left of the view, in canvas pixels (not screen pixels).
		//
		//  * zoomLevel is the *digital* zoom level. It's (view size ÷ visible canvas size): as you zoom out, the
		//    visible canvas gets larger, so the zoom level gets smaller.
		
		// pos is the position of the mouse within the *view*, in view/screen pixels. Top left of the view is (0, 0).
		double pos[] = {event.x, event.y };
		
		// imagePos is the position of the mouse within the *image*, in canvas/image pixels. Top left of the image is
		// (0, 0). Bottom right will be the 'real' size of the image.
		int imagePos[] = getImagePosHelper(event, dbx, dby, zoomLevel);
		
		// zoomedPos is the position of the mouse within the view, in canvas/image pixels. Top left of the view is
		// (0, 0). (Similar to pos, but in canvas/image pixels, not view/screen pixels.)
		int zoomedPos[] = getZoomedPosHelper(event, zoomLevel);
		
		if (DEBUG) {
			System.out.printf("perform:    %-25s%-20s%-20s%s%n",
				String.format("pos=(%.1f, %.1f)", pos[0], pos[1]),
				String.format("db=(%d, %d)", dbx, dby),
				String.format("zoomLevel=%.3f", zoomLevel),
				String.format("imagePos=%s", Arrays.toString(imagePos)),
				String.format("zoomedPos=%s", Arrays.toString(zoomedPos))
			);
		}
		
		for (ListenerPair pair : listeners) {
			SwtHitTestCalculator calculator = null;
			if (pair.provider != null) {
				calculator = new SwtHitTestCalculator(pair.provider, zoomedPos[0], zoomedPos[1]);
			}
			IImagePositionEvent iEvent = new SwtImagePositionEvent(pos, imagePos, calculator);
			pair.listener.imageDragged(iEvent);
		}			
	}
	

	private short getFlagsHelper(MouseEvent event) {
		short flags = 0;
		if (event.button == 1) flags += IImagePositionEvent.LEFTMOUSEBUTTON;
		if (event.button == 3) flags += IImagePositionEvent.RIGHTMOUSEBUTTON;
		if ((event.getState() & SWT.CONTROL) != 0) flags += IImagePositionEvent.CTRLKEY;
		if ((event.getState() & SWT.SHIFT) != 0) flags += IImagePositionEvent.SHIFTKEY;
		return flags;
	}	

	/**
	 * Add another ImagePositionListener to the listener list
	 * @param newListener
	 * @param swtProvider Can be null, won't perform hit test
	 */
	public void addImagePositionListener(ImagePositionListener newListener, SWT2DOverlayProvider swtProvider)
	{
		listeners.add(new ListenerPair(newListener, swtProvider));
	}
	

	/**
	 * Remove an ImagePositionListener from the listener list
	 * @param listener listener to be removed
	 */
	public void removeImagePositionListener(ImagePositionListener listener)
	{
		for (Iterator<ListenerPair> iterator = listeners.iterator(); iterator.hasNext();) {
			ListenerPair pair = iterator.next();
			if (pair.listener == listener) {
				iterator.remove();
			}
		}
	}
}
