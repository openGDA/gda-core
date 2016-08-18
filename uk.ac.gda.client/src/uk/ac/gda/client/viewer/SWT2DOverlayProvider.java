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

package uk.ac.gda.client.viewer;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingConstants;

import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay2DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.VectorOverlayStyles;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.enums.LabelOrientation;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class SWT2DOverlayProvider implements Overlay2DProvider {

	private IFigure topFigure;
	/* We attempt to sort figures by Z-order by storing in a sorted map.
	 * We use the index as the Z-order indication. This can fail though as
	 * the index wraps. There is also no defined API issue on the expected Z-order,
	 * so we are a bit free to do what we want?
	 */
	private Map<Integer, IFigure> figures;

	private boolean overlayInOperation = false;
	private int primKeyID = 0;
	private Cursor defaultCursor;
	Map<Color, org.eclipse.swt.graphics.Color> colorToSwtColorMap = new HashMap<Color, org.eclipse.swt.graphics.Color>();

	public SWT2DOverlayProvider(IFigure topFigure) {
		figures = new TreeMap<Integer, IFigure>(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				// sort in reverse order, see hitTest for iteration order use
				return o2.compareTo(o1);
			}

		});
		this.topFigure = topFigure;
		this.defaultCursor = topFigure.getCursor();
	}

	public void dispose() {
		Collection<org.eclipse.swt.graphics.Color> swtColours = colorToSwtColorMap.values();
		for (org.eclipse.swt.graphics.Color colour : swtColours) {
			colour.dispose();
		}
	}

	@Override
	public void drawArrow(int primID, double sx, double sy, double ex, double ey) {
		// This should be the following exception, or ideally implemented because the line profile for
		// mx uses it. Without an implementation, there simply is no arrow on the line
//		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void drawArrow(int primID, double sx, double sy, double ex, double ey,
			double arrowPos) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void drawBox(int primID, double lux, double luy, double rlx, double rly) {
		if (overlayInOperation){
			IFigure figure = figures.get(primID);
			if (figure != null) {
				Rectangle bounds = topFigure.getBounds();
				figure.setBounds(new Rectangle(bounds.x + (int)lux, bounds.y + (int)luy, (int)(rlx-lux), (int)(rly-luy) ));
			}
		}
	}

	@Override
	public void drawCircle(int primID, double cx, double cy, double radius) {
		if (overlayInOperation){
			IFigure figure = figures.get(primID);
 			if (figure != null) {
				Rectangle bounds = topFigure.getBounds();
				Rectangle rect = new Rectangle(bounds.x + (int)(cx- radius), bounds.y + (int)(cy- radius), (int)radius*2, (int)radius*2 );
				figure.setBounds(rect);
 			}
		}
	}

	@Override
	public void drawLabel(int primID, double lx, double ly) {
		if (overlayInOperation){
			IFigure figure = figures.get(primID);
			if (figure instanceof Label) {
				Rectangle bounds = topFigure.getBounds();
				Label label = (Label) figure;
				Dimension preferredSize = label.getPreferredSize();
				label.setBounds(new Rectangle(bounds.x + (int) lx, bounds.y + (int) ly, preferredSize.width,
						preferredSize.height));
			}
		}
	}


	@Override
	public void drawLine(int primID, double sx, double sy, double ex, double ey) {
		if (overlayInOperation) {
			IFigure figure = figures.get(primID);
			if (figure instanceof PolylineShape) {
				PolylineShape poly = (PolylineShape)figure;
				poly.setBounds(topFigure.getBounds());
				poly.setOutline(true);
				poly.removeAllPoints();
				poly.addPoint(new Point((int) sx, (int) sy));
				poly.addPoint(new Point((int) ex, (int) ey));
			}
		}
	}

	@Override
	public void drawPoint(int primID, double px, double py) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}


	@Override
	public void drawPoints(int primID, double[] px, double[] py) {
		if (overlayInOperation && px.length == py.length) {
			IFigure figure = figures.get(primID);
			if (figure instanceof PointFigure) {
				PointFigure poly = (PointFigure) figure;
				poly.setBounds(topFigure.getBounds());
				poly.setOutline(true);
				poly.removeAllPoints();
				PointList pl = new PointList(px.length);
				for (int i = 0; i < px.length; i++) {
					pl.addPoint((int) px[i], (int) py[i]);
				}
				poly.setPoints(pl);
			}
		}
	}

	@Override
	public void drawSector(int primID, double cx, double cy, double inRadius,
			double outRadius, double startAngle, double endAngle) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void drawTriangle(int primID, double x1, double y1, double x2, double y2,
			double x3, double y3) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}


	@Override
	public void rotatePrimitive(int primID, double angle, double rcx, double rcy) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void setAnchorPoints(int primID, double x, double y) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void setThickPoints(int primID, boolean on) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void translatePrimitive(int primID, double tx, double ty) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public boolean begin(OverlayType type) {
		if (!overlayInOperation){
			overlayInOperation = true;
			return true;
		}
		return false;
	}

	@Override
	public void end(OverlayType type) {
		overlayInOperation = false;
	}

	// It may make life easier one day to make this
	// method public?
	private int registerPrimitive(IFigure figure) {
		primKeyID++;

		while (figures.containsKey(primKeyID))
		{
			primKeyID = (primKeyID + 1)%Integer.MAX_VALUE;
		}

		figures.put(primKeyID, figure);
		topFigure.add(figure);


		return primKeyID;
	}

	@Override
	public int registerPrimitive(PrimitiveType primType, boolean fixedSize) {
		if (fixedSize)
			throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");

		IFigure figure = null;
		switch(primType)
		{
			case LINE:
				figure = new PolylineShape();
			break;
			case BOX:
				figure = new RectangleFigure();
			break;
			case ARROW:
				figure = new RectangleFigure();
			break;
			case CIRCLE:
				figure = new Ellipse();
			break;
			case TRIANGLE:
				figure = new Triangle();
			break;
			case SECTOR:
				figure = new RectangleFigure();
			break;
			case LABEL:
				Label lab = new Label();
				lab.setText("...");
				figure = lab;
			break;
			case POINT:
				figure = new RectangleFigure();
			break;
			case POINTLIST:
				figure = new PointFigure();
			break;
			case RING:
			break;
			case ELLIPSE:
			break;
			case IMAGE:
				figure = new ImageWithTransparencyFigure();
			break;
		}

		return registerPrimitive(figure);
	}

	@Override
	public int registerPrimitive(PrimitiveType primType) {
		return registerPrimitive(primType, false);
	}

	private org.eclipse.swt.graphics.Color getSwtColour(Color awtColour) {
		org.eclipse.swt.graphics.Color swtColour = colorToSwtColorMap.get(awtColour);
		if (swtColour == null) {
			swtColour = new org.eclipse.swt.graphics.Color(null, awtColour.getRed(), awtColour.getGreen(), awtColour.getBlue());
			colorToSwtColorMap.put(awtColour, swtColour);
		}
		return swtColour;
	}

	@Override
	public void setColour(int primID, Color colour) {
		IFigure fig = figures.get(primID);
		if (fig != null){
			org.eclipse.swt.graphics.Color swtColor = getSwtColour(colour);
			fig.setBackgroundColor(swtColor);
			fig.setForegroundColor(swtColor);
		}
	}

	@Override
	public boolean setLabelFont(int primID, Font font) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public boolean setLabelOrientation(int primID, LabelOrientation orient) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public boolean setLabelText(int primID, String text, int alignment) {
		if (alignment != SwingConstants.CENTER)
			throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
		if (overlayInOperation){
			IFigure figure = figures.get(primID);
			if (figure instanceof Label) {
				Label label = (Label) figure;
				label.setText(text);
				Dimension preferredSize = label.getPreferredSize();
				Rectangle bounds = label.getBounds();
				label.setBounds(new Rectangle(bounds.x - preferredSize.width / 2, bounds.y - preferredSize.height / 2, preferredSize.width, preferredSize.height));
				return true;
			} else if (figure != null) {
				if (!"".equals(text)) {
					// safe typecast, only this code should be adding tooltips
					Label toolTip = (Label) figure.getToolTip();
					if (toolTip == null) {
						toolTip = new Label();
						toolTip.setBorder(new MarginBorder(2));
					}
					toolTip.setText(text);
					figure.setToolTip(toolTip);
				} else {
					figure.setToolTip(null);
				}
			}
		}
		return false;
	}

	@Override
	public void setLineThickness(int primID, double thickness) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public boolean setCustomLineStyle(int primID, float[] dash) {
		final IFigure figure = figures.get(primID);
		if (figure instanceof Shape) {
			final Shape shape = (Shape) figure;
			shape.setLineStyle(SWT.LINE_CUSTOM);
			shape.setLineDash(dash);
			return true;
		}
		return false;
	}

	@Override
	public void setOutlineColour(int primID, Color colour) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public boolean setOutlineTransparency(int primID, double transparency) {
		IFigure fig = figures.get(primID);
		if (fig instanceof Shape) {
			Shape shape = (Shape) fig;
			shape.setOutline(transparency != 1.0);
			return true;
		}
		return false;
	}

	@Override
	public boolean setPrimitiveVisible(int primID, boolean visible) {
		IFigure fig = figures.get(primID);
		if (fig != null){
			fig.setVisible(visible);
			return true;
		}
		return false;
	}

	@Override
	public void setStyle(int primID, VectorOverlayStyles newStyle) {
		IFigure fig = figures.get(primID);
		if (fig instanceof Shape) {
			Shape shape = (Shape)fig;
			switch (newStyle) {
			case FILLED:
				shape.setFill(true);
				shape.setOutline(false);
				break;
			case FILLED_WITH_OUTLINE:
				shape.setFill(true);
				shape.setOutline(true);
				break;
			case OUTLINE:
				shape.setFill(false);
				shape.setOutline(true);
				break;
			}
		}
	}

	@Override
	public boolean setTransparency(int primID, double transparency) {
		IFigure fig = figures.get(primID);
		if (fig instanceof Shape){
			Shape shape = (Shape)fig;
			shape.setAlpha((int) ((1.0-transparency) * 255.0));
			return true;
		}
		else if (fig instanceof ImageWithTransparencyFigure) {
			ImageWithTransparencyFigure transparentFig = (ImageWithTransparencyFigure) fig;
			transparentFig.setTransparency(transparency);
			return true;
		}
		return false;
	}


	@Override
	public void unregisterPrimitive(int primID) {
		IFigure figure = figures.remove(primID);
		if (figure != null) {
			topFigure.remove(figure);
		}
	}

	@Override
	public void unregisterPrimitive(List<Integer> ids) {
		Iterator<Integer> iter = ids.iterator();
		while (iter.hasNext()) {
			Integer next = iter.next();
			IFigure figure = figures.remove(next);
			if (figure != null) {
				topFigure.remove(figure);
			}
		}
	}

	@Override
	public void drawRing(int primID, double cx, double cy, double inRadius, double outRadius) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void drawEllipse(int primID, double cx, double cy, double a, double b, double omega) {
		throw new UnsupportedOperationException("Unsupported/implemented for SWT 2D Provider, please implement if needed");
	}

	@Override
	public void restoreDefaultPlotAreaCursor() {
		topFigure.setCursor(defaultCursor);
	}

	@Override
	public void setPlotAreaCursor(int cursor) {
		Cursor tempCursor = Display.getCurrent().getSystemCursor(cursor);
		if (tempCursor != null)
			topFigure.setCursor(tempCursor);
	}


	/**
	 * Returns the primitive ID at the coordinates specified.
	 * @param x to lookup
	 * @param y to lookup
	 * @note There is no documented concept of Z-order in overlay providers, therefore if multiple
	 * figures are present it can be "random". We try and return the last one created, but
	 * that is prone to errors to.
	 * @return primitive ID, or -1 if no registered primitives are hit
	 */
	public List<Integer> hitTest(int x, int y) {
		List<Integer> hits = new LinkedList<Integer>();
		for (Entry<Integer, IFigure> entry : figures.entrySet()) {
			if (entry.getValue().containsPoint(x, y)) {
				hits.add(entry.getKey());
			}
		}
		return hits;
	}

	@Override
	public void drawImage(int imageID, Image image, double lux, double luy, double rlx, double rly) {
		if (overlayInOperation){
			IFigure figure = figures.get(imageID);
			if (figure instanceof ImageWithTransparencyFigure) {
				ImageWithTransparencyFigure image1 = (ImageWithTransparencyFigure) figure;
				image1.setImage(image);
				Rectangle bounds = topFigure.getBounds();
				image1.setBounds(new Rectangle(bounds.x + (int)lux, bounds.y + (int)luy, (int)(rlx-lux), (int)(rly-luy) ));
			}
			else if (figure instanceof ImageFigure) {
				ImageFigure image1 = (ImageFigure) figure;
				image1.setImage(image);
				Rectangle bounds = topFigure.getBounds();
				image1.setBounds(new Rectangle(bounds.x + (int)lux, bounds.y + (int)luy, (int)(rlx-lux), (int)(rly-luy) ));
			}
		}
	}

	@Override
	public boolean isDrawable(double xSize, double ySize) {
		if (Math.abs(xSize) < 1 || Math.abs(ySize) < 1) {
			return false;
		}
		return true;
	}

}
