/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This displays a magnified part of a simple plot by having a duplicate plot with axis limits set accordingly.
 */
class DataMagnifierWindow extends JDialog implements Magnifier {
	private SimplePlot simplePlot;

	private XYPlot magnifiedPlot;

	/**
	 * @param c
	 */
	DataMagnifierWindow(Component c) {
		super((Frame) SwingUtilities.getRoot(c));
		setAlwaysOnTop(true);
	}

	/**
	 * Inner class exists so that it can override the paintComponent method of JPanel and so repaint itself
	 * automatically (as well as in response to changes to the magnifying rectangle).
	 */
	private class DataMagnifierPanel extends JPanel {
		/**
		 * Overrides the super class method to get the magnified plot to draw itself
		 *
		 * @param g
		 *            the Graphics to paint with
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (magnifiedPlot != null)
				magnifiedPlot.draw((Graphics2D) g, (Rectangle2D) getBounds(), null, null, null);
		}
	}

	/**
	 * Sets the SimplePlot to be magnified.
	 *
	 * @param simplePlot
	 *            the SimplePlot which the magnifier will be Magnifying
	 * @param collection
	 */
	@Override
	public void setSimplePlot(SimplePlot simplePlot, XYSeriesCollection collection) {
		this.simplePlot = simplePlot;
		setTitle("Data Magnifier");

		// The magnifiedPlot is effectively just another view of the data of the
		// simplePlot. By creating a JFreeChart and extracting its plot we get
		// all
		// the tediouse work done elsewhere.

		JFreeChart magnifiedChart = ChartFactory.createXYLineChart(simplePlot.getTitle(), null, null, collection, PlotOrientation.VERTICAL, false, false, false);
		magnifiedChart.setAntiAlias(false);
		magnifiedPlot = magnifiedChart.getXYPlot();
		magnifiedPlot.setRenderer(simplePlot.getChart().getXYPlot().getRenderer());
		if(simplePlot.isShowToolTip())
			magnifiedPlot.getRenderer().setToolTipGenerator(new SimpleXYToolTipGenerator());
		add(new DataMagnifierPanel());
	}

	/**
	 * The SimplePlot will call this method when the Rectangle to be magnified has changed.
	 *
	 * @param magnifyRectangle
	 *            the Rectangle to be magnified
	 */
	@Override
	public void update(Rectangle2D magnifyRectangle) {

		// The magnifyRectangle will be in Java coordinates, need to calculate
		// the axis limits required. This mechanism was copied from the zooming
		// methods within JFreeChart.

		double hLower = 0.0;
		double hUpper = 0.0;
		double vLower = 0.0;
		double vUpper = 0.0;
		double a;
		double b;
		Rectangle2D scaledDataArea;
		if (magnifyRectangle != null) {
			scaledDataArea = simplePlot.getScreenDataArea();
			hLower = (magnifyRectangle.getMinX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			hUpper = (magnifyRectangle.getMaxX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			vLower = (scaledDataArea.getMaxY() - magnifyRectangle.getMaxY()) / scaledDataArea.getHeight();
			vUpper = (scaledDataArea.getMaxY() - magnifyRectangle.getMinY()) / scaledDataArea.getHeight();

			Range r = simplePlot.getChart().getXYPlot().getDomainAxis().getRange();
			a = r.getLowerBound() + hLower * r.getLength();
			b = r.getLowerBound() + hUpper * r.getLength();
			Range newR = new Range(Math.min(a, b), Math.max(a, b));
			magnifiedPlot.getDomainAxis().setRange(newR);

			r = simplePlot.getChart().getXYPlot().getRangeAxis().getRange();
			a = r.getLowerBound() + vLower * r.getLength();
			b = r.getLowerBound() + vUpper * r.getLength();
			newR = new Range(Math.min(a, b), Math.max(a, b));
			magnifiedPlot.getRangeAxis().setRange(newR);

			repaint();
		}
	}
}
