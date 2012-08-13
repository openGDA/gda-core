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

package uk.ac.gda.client.viewer;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.IDataPositionEvent.Mode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.IImagePositionEvent;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.ImagePositionListener;

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

	private int[] getImagePosHelper(MouseEvent event, int dbx, int dby, float zoomLevel) {
		int x = event.x;
		int y = event.y;
		x /= zoomLevel;
		y /= zoomLevel;
		int imagePos[] = { x - dbx, y - dby};
		return imagePos;
	}

	private int[] getZoomedPosHelper(MouseEvent event, float zoomLevel) {
		int x = event.x;
		int y = event.y;
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

	public void perform(MouseEvent event, int dbx, int dby, float zoomLevel) {
		double pos[] = {event.x, event.y };
		int imagePos[] = getImagePosHelper(event, dbx, dby, zoomLevel);
		int zoomedPos[] = getZoomedPosHelper(event, zoomLevel);
		
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
