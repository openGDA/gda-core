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

package gda.images.camera;

import gda.device.DeviceException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class DummyAwtVideoReceiver extends DummyVideoReceiverBase<Image> {

	private static final Color BACKGROUND_COLOUR = Color.LIGHT_GRAY;
	private static final Color CIRCLE_COLOUR = Color.RED;
	
	private BufferedImage image;
	
	@Override
	protected void createBlankImage() {
		image = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(BACKGROUND_COLOUR);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
	}
	
	@Override
	protected Image updateImage() {
		Graphics g = image.getGraphics();
		
		// erase old circle
		g.setColor(BACKGROUND_COLOUR);
		g.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
		
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
		g.setColor(CIRCLE_COLOUR);
		g.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
		
		return image;
	}
	
	@Override
	public Image getImage() throws DeviceException {
		return image;
	}

	@Override
	public String getDisplayName() {
		return "DummyAwtVideo";
	}

}
