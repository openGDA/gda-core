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

package gda.plots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Shows the default colours used by {@link SimpleXYSeries}.
 */
public class SimpleXYSeriesColours {

	public static void main(String[] args) {
		
		final Paint[] colours = SimpleXYSeries.defaultPaints;
		
		final int width = 300;
		final int spacing = 40;
		final int height = spacing * (colours.length + 1);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		
		// white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		// draw line for each colour
		for (int i=0; i<colours.length; i++) {
			final Color c = (Color) colours[i];
			g.setColor(c);
			final int y = spacing * (i+1);
			g.drawLine(0, y, width, y);
			final String text = String.format("%d: (%d, %d, %d)", i, c.getRed(), c.getGreen(), c.getBlue());
			g.drawString(text, spacing, y-4);
		}
		
		JLabel label = new JLabel(new ImageIcon(image));
		JFrame frame = new JFrame("SimpleXYSeries colours");
		frame.add(label);
		frame.pack();
		frame.setLocation(300, 300);
		frame.setVisible(true);
	}

}
