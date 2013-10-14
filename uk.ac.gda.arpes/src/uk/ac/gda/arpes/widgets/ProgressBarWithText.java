/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressBarWithText {

	private String barText = "";
	private ProgressBar bar;
	private int min = 0, max = 100, sele = 0;
	
	/**
	 * Add text on Bar, also works around the fact that the original ProgressBar doesn't like negative minimum
	 * 
	 */
	public ProgressBarWithText(Composite parent, int style) {
		bar = new ProgressBar(parent, style);
		bar.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point point = bar.getSize();
				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * barText.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				e.gc.drawString(barText, (point.x - width) / 2, (point.y - height) / 2, true);
			}
		});
	}

	public void setText(String newText) {
		if (barText.equals(newText))
			return;
		if (newText != null) 
			barText = newText;
		else 
			barText = "";
		bar.redraw();
	}
	
	public String getText() {
		if (barText.isEmpty())
			return null;
		return barText;
	}
	public int getMaximum () {
		return max;
	}

	public int getMinimum () {
		return min;
	}

	public int getSelection () {
		return sele;
	}
	
	public void setMaximum (int value) {
		if (value <= min) return;
		max = value;
		bar.setMaximum(max-min);
	}

	public void setMinimum (int value) {
		min = value;
	}

	public void setSelection (int value) {
		sele = value;
		bar.setSelection(sele-min);
	}

	public void setBackground(Color color) {
		bar.setBackground(color);
	}

	public void setForeground(Color color) {
		bar.setForeground(color);
	}

	public void setLayoutData(Object layoutData) {
		bar.setLayoutData(layoutData);
	}
}