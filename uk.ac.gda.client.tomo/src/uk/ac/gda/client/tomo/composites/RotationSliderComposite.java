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

package uk.ac.gda.client.tomo.composites;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.InputEvent;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract class that defines the rotation slider composite. Implementations of these are
 * {@link TomoCoarseRotationComposite} and {@link TomoFineRotationComposite}.<br>
 * Contains a triangle which can be dragged along.
 */
public abstract class RotationSliderComposite extends Composite {
	public interface SliderSelectionListener {
		/**
		 * @param sliderComposite
		 * @param initalSliderDegree
		 * @param totalWidth
		 *            - of the slider as in the boundary width of the slider
		 */
		void sliderMoved(RotationSliderComposite sliderComposite, double initalSliderDegree, int totalWidth);

		/**
		 * event propagated to listeners when the slider needs to be moved to the specified degree.
		 * 
		 * @param deg
		 */
		void sliderMovedTo(double deg);
	}

	private final static DecimalFormat df = new DecimalFormat("#.###");
	protected static final String TEXT_SMALL_7 = "bold-text_small-7";
	private static final double SLIDER_START_TOLERANCE = 1;
	private FigureCanvas figCanvas;
	private List<SliderSelectionListener> sliderListeners = new ArrayList<SliderSelectionListener>();
	private final int direction;
	protected RectangleFigure sliderBoundary;
	protected Triangle sliderTriangle;
	private Label triangleLblFigure;
	/**
	 * labels that appear along the slider.
	 */
	private final String[] labels;
	/**
	 * flag to check whether the "Ctrl" button needs to be pressed when the control is used.
	 */
	private final boolean ctrlPressRequired;
	/**
	 * utility variable to check whether the slider has ever been moved.
	 */
	private boolean moved = false;
	protected FontRegistry fontRegistry;

	/**
	 * Constructor that labels as a parameter. There can be upto 5 labels.<br>
	 * If there is 1 - label will be placed in the center.<br>
	 * If there are 2 labels - the labels will be placed - left end and right end<br>
	 * if there are 3 labels - the labels will be placed - left, center, and right<br>
	 * If there are 4 labels, - the labels will be placed left, between center and left, between center and right, and
	 * right<br>
	 * if there are 5 labels -
	 * 
	 * @param parent
	 * @param style
	 * @param labels
	 * @param ctrlPressRequired
	 */
	public RotationSliderComposite(Composite parent, int style, String[] labels, boolean ctrlPressRequired) {
		super(parent, SWT.None);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(TEXT_SMALL_7, new FontData[] { new FontData(fontName, 7, SWT.BOLD) });
		}
		initialize();
		direction = style;
		this.labels = labels;
		this.ctrlPressRequired = ctrlPressRequired;
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		figCanvas = new FigureCanvas(this);
		figCanvas.setBackground(ColorConstants.white);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	protected void initialize() {
		// Do nothing in base class
	}

	/**
	 * Moves the slider by a certain units. for move slider by -20°
	 * 
	 * @param degree
	 */
	public void moveSlider(double degree) {
		/* Current slider degree - state the degree on the slider */
		double currentSliderDegree = getCurrentSliderDegree();
		/* Degrees to move the slider to */
		double degreeToMoveTo = currentSliderDegree + degree;
		/* Convert the slider degree base from 180° to 360° */
		moveSliderTo(degreeToMoveTo);
	}

	/**
	 * Move the slider to the specified unit. for eg: move slider to 90°
	 * 
	 * @param degreeToMoveTo
	 */
	public void moveSliderTo(double degreeToMoveTo) {
		// To suggest to the slider that it has been moved.
		moved = true;
		degreeToMoveTo = degreeToMoveTo % getTotalSliderDegree();
		double newPosition = degreeToMoveTo + getDegreeBase();
		//
		if (degreeToMoveTo > getDegreeBase()) {
			newPosition = Math.abs(getDegreeBase() - degreeToMoveTo);
		} else if (degreeToMoveTo < -(getDegreeBase())) {
			newPosition = getTotalSliderDegree() + newPosition;
		}
		/* The number of pixels per degree - calculated relative to the sliderboundary width */
		double numPixelPerDeg = sliderBoundary.getBounds().width / getTotalSliderDegree();
		/**/
		double newX = SLIDER_START_TOLERANCE + (newPosition * numPixelPerDeg);
		sliderTriangle.setLocation(new PrecisionPoint(newX, sliderTriangle.getLocation().y));

		// Labels inside the label
		updateSliderLabel();

	}

	/**
	 * @return baseDegree
	 */
	protected abstract double getDegreeBase();

	/**
	 * @return totalSliderDegree
	 */
	protected abstract double getTotalSliderDegree();

	/**
	 * @return sliderDegree that is based on the total slider degree - {@link #getTotalSliderDegree()} and the degree
	 *         base {@link #getDegreeBase()}
	 */
	public double getCurrentSliderDegree() {

		double sliderX = (sliderTriangle.getBounds().x + sliderTriangle.getBounds().width / 2)
				- (sliderTriangle.getBounds().width / 2 + 1);

		double parentX = sliderBoundary.getBounds().width;

		double xRatio = sliderX / parentX;

		double degAct = xRatio * getTotalSliderDegree();

		return degAct - getDegreeBase();
	}

	@SuppressWarnings("unused")
	protected IFigure getContents() {
		IFigure panel = new Panel();

		panel.setLayoutManager(new SliderLayout());
		LineBorder border = new LineBorder(2);
		border.setColor(ColorConstants.gray);
		panel.setBorder(border);
		sliderTriangle = new Triangle();
		sliderTriangle.setFill(true);
		sliderTriangle.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEWE));

		addLabelToTriangle();
		sliderBoundary = new RectangleFigure();
		sliderBoundary.setBackgroundColor(ColorConstants.black);

		if (direction == SWT.UP) {
			sliderTriangle.setDirection(PositionConstants.NORTH);
		} else {
			sliderTriangle.setDirection(PositionConstants.SOUTH);
		}

		sliderTriangle.setBackgroundColor(ColorConstants.darkGreen);
		sliderTriangle.setSize(getSliderTriangleDimension());

		new Dragger(sliderTriangle);

		panel.add(sliderTriangle);
		panel.add(sliderBoundary);

		addSliderMarkers(panel);
		return panel;
	}

	/**
	 * To be implemented by concrete class that requires to add label to the slider figure.
	 * 
	 * @see TomoCoarseRotationComposite#addLabelToTriangle()
	 */
	protected void addLabelToTriangle() {
		sliderTriangle.setLayoutManager(new XYLayout());
		triangleLblFigure = new Label("0°");
		triangleLblFigure.setBackgroundColor(ColorConstants.white);
		//

		int x = getSliderTriangleDimension().width / 4 - 1;
		int y = getSliderTriangleDimension().height / 4 - 1;
		sliderTriangle.add(triangleLblFigure, new Rectangle(x, y, -1, -1));
	}

	protected abstract Dimension getSliderTriangleDimension();

	/**
	 * @return labels
	 */
	protected String[] getLabels() {
		return labels;
	}

	/**
	 * Add the labels or buttons along the slider in the panel. This uses the labels that are provided to the
	 * constructor.
	 * 
	 * @param panel
	 */
	protected abstract void addSliderMarkers(IFigure panel);

	/**
	 * Layout for the sliders.
	 */
	private class SliderLayout extends XYLayout {

		@Override
		public void layout(IFigure parent) {
			super.layout(parent);
			Rectangle parentBounds = parent.getBounds();
			sliderBoundary.setFill(true);
			if (SWT.DOWN == direction) {
				int xVal = sliderTriangle.getLocation().x;
				if (!moved) {
					xVal = parentBounds.width / 2 - sliderTriangle.getBounds().width / 2;
				}
				sliderTriangle.setLocation(new Point(xVal, 0));
				//
				int sliderBoundaryX = sliderTriangle.getBounds().width / 2;
				int sliderBoundaryY = sliderTriangle.getLocation().y + sliderTriangle.getSize().height;
				int sliderBoundaryWidth = parentBounds.width - sliderTriangle.getBounds().width;
				int sliderBoundaryHeight = 3;
				sliderBoundary.setBounds(new Rectangle(sliderBoundaryX, sliderBoundaryY, sliderBoundaryWidth,
						sliderBoundaryHeight));

				layoutDownSliderMarkers(parentBounds);
			} else if (SWT.UP == direction) {
				int xVal = sliderTriangle.getLocation().x;
				if (!moved) {
					xVal = parentBounds.width / 2 - sliderTriangle.getBounds().width / 2;
				}
				sliderTriangle.setLocation(new Point(xVal, parentBounds.height - 20));
				sliderBoundary.setBounds(new Rectangle(sliderTriangle.getBounds().width / 2, parentBounds.height - 23,
						parentBounds.width - sliderTriangle.getBounds().width, 3));
				/* labels */
				layoutUpSliderMarkers(parentBounds);
			}
			if (triangleLblFigure != null) {
				triangleLblFigure.setFont(fontRegistry.get(TEXT_SMALL_7));
				triangleLblFigure.getParent().repaint();
			}
		}
	}

	/**
	 * @param parentBounds
	 */
	protected abstract void layoutDownSliderMarkers(Rectangle parentBounds);

	/**
	 * @param parentBounds
	 */
	protected abstract void layoutUpSliderMarkers(Rectangle parentBounds);

	class Dragger extends MouseMotionListener.Stub implements MouseListener {
		public Dragger(IFigure figure) {
			figure.addMouseMotionListener(this);
			figure.addMouseListener(this);
		}

		Point movedPoint;
		private double initialSliderDegree;

		@Override
		public void mouseReleased(MouseEvent e) {
			if (ctrlPressRequired) {
				if ((e.getState() & InputEvent.CONTROL) == 0) {
					return;
				}
			}
			updateListeners(initialSliderDegree);
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (ctrlPressRequired) {
				if (e.getState() != InputEvent.CONTROL) {
					return;
				}
			}
			initialSliderDegree = getCurrentSliderDegree();
			movedPoint = e.getLocation();
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (ctrlPressRequired) {
				if ((e.getState() & InputEvent.CONTROL) == 0) {
					return;
				}
			}
			moved = true;
			Point p = e.getLocation();
			if (p != null) {
				Dimension delta = p.getDifference(movedPoint);
				Figure f = ((Figure) e.getSource());
				// Restricted drag movement for the triangle
				if (delta.width + f.getBounds().x >= 1) {
					if (f.getBounds().width + f.getBounds().x + delta.width <= f.getParent().getBounds().width + 1) {
						f.setBounds(f.getBounds().getTranslated(delta.width, 0));
						updateSliderLabel();
					}
				}
				movedPoint = p;
			}
		}

	}

	/**
	 * To be implemented by concrete classes that require labels within their shapes.
	 * 
	 * @see TomoCoarseRotationComposite#updateSliderLabel()
	 */
	protected void updateSliderLabel() {
		double currentSliderDegree = getCurrentSliderDegree();
		triangleLblFigure.setText(df.format(currentSliderDegree) + "°");
	}

	public void addSliderEventListener(SliderSelectionListener listener) {
		sliderListeners.add(listener);
	}

	public void removeSliderEventListener(SliderSelectionListener listener) {
		sliderListeners.remove(listener);
	}

	protected void updateDegreeMovedTo(double deg) {
		for (SliderSelectionListener ssl : sliderListeners) {
			ssl.sliderMovedTo(deg);
		}
	}

	protected void updateListeners(double initialSliderDegree) {
		for (SliderSelectionListener ssl : sliderListeners) {
			ssl.sliderMoved(this, initialSliderDegree, sliderBoundary.getParent().getSize().width);
		}
	}

	@Override
	public void dispose() {
		figCanvas.dispose();
		sliderListeners.clear();
		super.dispose();

	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			sliderTriangle.setBackgroundColor(ColorConstants.gray);
		} else {
			sliderTriangle.setBackgroundColor(ColorConstants.darkGreen);
		}
		super.setEnabled(enabled);
	}
}