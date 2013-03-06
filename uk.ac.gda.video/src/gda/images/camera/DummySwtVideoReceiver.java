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

package gda.images.camera;

import gda.device.DeviceException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class DummySwtVideoReceiver extends DummyVideoReceiverBase<ImageData>{

	private boolean drawGridlines;
	
	public void setDrawGridlines(boolean drawGridlines) {
		this.drawGridlines = drawGridlines;
	}
	
	private Image image;
	
	private GC gc;
	
	private Color backgroundColour;
	
	private Color circleColour;
	
	@Override
	protected void createBlankImage() {
		Display display = Display.getDefault();
		image = new Image(display, imageSize.width, imageSize.height);
		gc = new GC(image);
		
		backgroundColour = display.getSystemColor(SWT.COLOR_GRAY);
		circleColour = display.getSystemColor(SWT.COLOR_RED);
		
		gc.setBackground(backgroundColour);
		gc.fillRectangle(0, 0, imageSize.width, imageSize.height);
	}
	
	@Override
	protected ImageData updateImage() {
		
		// erase old circle
		gc.setBackground(backgroundColour);
		gc.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
		
		// move circle to new position
		circleX += circleDeltaX;
		circleY += circleDeltaY;
		
		// check whether circle has move beyond edge of image
		if (circleY < 0 || circleY >= (imageSize.height - CIRCLE_SIZE)) {
			circleY -= circleDeltaY * 2;
			circleDeltaY = -circleDeltaY;
		}
		if (circleX < 0 || circleX >= (imageSize.width - CIRCLE_SIZE)) {
			circleX -= circleDeltaX * 2;
			circleDeltaX = -circleDeltaX;
		}
		
		// draw new circle
		gc.setBackground(circleColour);
		gc.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
		
		if (drawGridlines) {
			
			gc.setBackground(backgroundColour);
			
			for (int x=0; x<=imageSize.width; x+=100) {
				gc.drawLine(x, 0, x, imageSize.height);
				gc.drawText(String.format("x=%d", x), x+5, 50);
			}
			
			for (int y=0; y<=imageSize.height; y+=100) {
				gc.drawLine(0, y, imageSize.width, y);
				gc.drawText(String.format("y=%d", y), 30, y+2);
			}
		}
		
		return image.getImageData();
	}
	
	@Override
	public ImageData getImage() throws DeviceException {
		return image.getImageData();
	}

}
