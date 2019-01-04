package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

/**
 * Painted colour box with a coloured border
 */
public class RegionBox extends Composite {
	public RegionBox(Composite parent, Color innerColour, Color outterColour, int borderWidth) {
		super(parent, SWT.NONE);

		addPaintListener(e -> {
			e.gc.setBackground(outterColour);
			e.gc.fillRectangle(e.x, e.y, e.width, e.height);
			e.gc.setBackground(innerColour);
			e.gc.fillRectangle(e.x + borderWidth, e.y + borderWidth, e.width - borderWidth * 2,
					e.height - borderWidth * 2);
		});
	}
}
