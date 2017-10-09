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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.eclipse.january.dataset.DoubleDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import gda.scan.AxisSpec;

/**
 * Provides a simple data line oriented way to use the JFreeChart plotting classes.
 */

public class SimplePlot extends ChartPanel implements Printable, XYDataHandler {

	private static final Logger logger = LoggerFactory.getLogger(SimplePlot.class);

	/**
	 * Inner class used to store axis limits on the zoomStack
	 */
	private class AxisLimits {
		private boolean xAuto;

		private double xMax;

		private double xMin;

		private boolean yAuto;

		private double yMax;

		private double yMin;

		private boolean yTwoAuto;

		private double yTwoMax;

		private double yTwoMin;

		private AxisLimits(boolean xAuto, boolean yAuto, double xMin, double xMax, double yMin, double yMax,
				boolean yTwoAuto, double yTwoMin, double yTwoMax) {
			this.xAuto = xAuto;
			this.yAuto = yAuto;
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;

			this.yTwoAuto = yTwoAuto;
			this.yTwoMin = yTwoMin;
			this.yTwoMax = yTwoMax;
		}

		/**
		 * @return value of yTwoAuto
		 */
		public boolean isYTwoAuto() {
			return yTwoAuto;
		}

		/**
		 * @return yTwoMax
		 */
		public double getYTwoMax() {
			return yTwoMax;
		}

		/**
		 * @return yTwoMin
		 */
		public double getYTwoMin() {
			return yTwoMin;
		}

		/**
		 * @return Returns the xAuto.
		 */
		public boolean isXAuto() {
			return xAuto;
		}

		/**
		 * @return Returns the yAuto.
		 */
		public boolean isYAuto() {
			return yAuto;
		}

		/**
		 * @return Returns the xMax.
		 */
		public double getXMax() {
			return xMax;
		}

		/**
		 * @return Returns the xMin.
		 */
		public double getXMin() {
			return xMin;
		}

		/**
		 * @return Returns the yMax.
		 */
		public double getYMax() {
			return yMax;
		}

		/**
		 * @return Returns the yMin.
		 */
		public double getYMin() {
			return yMin;
		}

		/**
		 * Returns a suitable string representation
		 *
		 * @return the formatted string representation of the axis limits
		 */
		@Override
		public String toString() {
			return "AxisLimits: xMin = " + xMin + " xMax " + xMax + " yMin " + yMin + " yMax " + yMax;
		}
	}

	/**
	 * Inner class to deal with mouse positions on the plot. Implements ChartMouseListener. Mouse motion events can be
	 * used to provide continuous display of position - MousePositionTracker extends TextTitle so that it can be placed
	 * on a JFreeChart for this purpose. Mouse click events can be used to return mouse positions to SimplePlot.
	 */
	private class MousePositionTracker extends TextTitle implements ChartMouseListener, Runnable {
		private boolean gettingCoordinates = false;

		private Thread thread;

		private boolean working = false;

		private double[] cursorCoordinates = new double[2];

		private double[] entityCoordinates = new double[2];

		private boolean clickWasInsideAxes = false;

		/**
		 * This is the variable which stores the coordinates which are used by the right click menu
		 */
		public SimpleDataCoordinate coordinates;

		/**
		 * @param string
		 */
		public MousePositionTracker(String string) {
			super(string);
			setPosition(RectangleEdge.BOTTOM);
		}

		/**
		 * Implements the ChartMouseListener interface. Called when the mouse is moved. NB this is one step removed from
		 * directly overriding the MouseMoved method. (Which is actually overriden in the main SimplePlot class - this
		 * inconsistency ought to be sorted out).
		 *
		 * @param cme
		 *            a ChartMouseEvent representing the move.
		 */
		@Override
		public void chartMouseMoved(ChartMouseEvent cme) {
			// Set the text to the mouse position (after conversion by
			// SimplePlot)
			// If the mouse is outside the scaledDataArea then the text is
			// set to
			// " " (NB not "" which causes it to be removed from the chart
			// which is
			// then rescaled)
			if (working) {
				MouseEvent me = cme.getTrigger();
				coordinates = convertMouseEvent(me);
				if (getScreenDataArea().contains(me.getX(), me.getY()))
					setText(coordinateFormatter.formatCoordinates(coordinates));
				else
					setText(" ");
			}
		}

		/**
		 * Switches off the displaying of mouse position
		 */
		public void switchOff() {
			if (working) {
				working = false;
				// Setting the text to "" makes the chart remove it completely
				setText("");
			}
		}

		/**
		 * Switches on the displaying of mouse position
		 */
		public void switchOn() {
			if (!working) {
				working = true;
				// Setting the text to " " puts it on the chart (though there is
				// nothing to seeForXAxis of course).
				setText(" ");
			}
		}

		/**
		 * Implements the ChartMouseListener interface, called when a mouse button is clicked.
		 *
		 * @see org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart.ChartMouseEvent)
		 */
		@Override
		public void chartMouseClicked(ChartMouseEvent cme) {
			// If gettingCoordinates has been set by start method then gets
			// both the actual mouse coordinates and the coordinates of the
			// nearest plot entity.
			if (gettingCoordinates) {
				MouseEvent me = cme.getTrigger();
				if (getScreenDataArea().contains(me.getX(), me.getY())) {
					clickWasInsideAxes = true;
					SimpleDataCoordinate sdc = convertMouseEvent(me);
					logger.debug("Mouse clicked at " + sdc.getX() + " " + sdc.getY());
					logger.debug("          entity " + cme.getEntity());

					cursorCoordinates = sdc.toArray();

					XYItemEntity entityOne = (XYItemEntity) cme.getEntity();
					if (entityOne != null) {
						entityCoordinates[0] = entityOne.getDataset().getXValue(entityOne.getSeriesIndex(),
								entityOne.getItem());
						entityCoordinates[1] = entityOne.getDataset().getYValue(entityOne.getSeriesIndex(),
								entityOne.getItem());
					}
				} else
					logger.debug("Mouse was clicked outside axes");

				// Synchronize in order to set the flag which terminates
				// the run thread
				synchronized (this) {
					gettingCoordinates = false;
					notifyAll();
				}
			}
			// If not getting coordinates then look for clicks on the legend
			// and popup the LinePropertiesEditor if appropriate.
			else {
				ChartEntity ce = cme.getEntity();
				if (ce != null && ce instanceof SimpleLegendEntity) {
					if( linePropertiesEditor == null){
						linePropertiesEditor = new LinePropertiesEditor();
					}
					linePropertiesEditor.setCurrentLine(((SimpleLegendEntity) ce).getSeries());
					// Setting visible true when it is already true causes
					// annoying flashing.
					if (!linePropertiesEditor.isVisible()) {
						linePropertiesEditor.setVisible(true);
					}
					linePropertiesEditor.toFront();
				}
			}
		}

		/**
		 * Returns the cursor coordinates of the last mouse click
		 *
		 * @return double[] x and y coordinates of mouse click
		 */
		private double[] getCursorCoordinates() {
			double[] values = null;

			if (clickWasInsideAxes)
				values = cursorCoordinates;

			return values;
		}

		/**
		 * Returns the coordinates of the chart entity nearest the last mouse click.
		 *
		 * @return double[] x and y coordinates of entity
		 */
		private double[] getEntityCoordinates() {
			double[] values = null;

			if (clickWasInsideAxes)
				values = entityCoordinates;

			return values;
		}

		/**
		 * Waits for the thread to terminate.
		 */
		private void join() {
			// This is the proper way to do it according
			// to example 94 in the Java Developers Almanac
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("Unexpectedly interrupted in SimplePlot.MousePositionTracker.join()");
			}
		}

		/**
		 * Implements the Runnable interface. Just waits for the mouse to be clicked.
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			synchronized (this) {
				do {
					try {
						wait();
					} catch (InterruptedException ie) {
						logger.error("MousePositionTracker wait() unexpectedly interrupted");
					}
				} while (gettingCoordinates);
			}
		}

		/**
		 * Starts the collecting of coordinates.
		 */
		private void start() {
			gettingCoordinates = true;
			clickWasInsideAxes = false;
			thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
			thread.start();
		}
	}

	/**
	 * Inner class to control dragging out of rectangles.
	 */
	private class RectangleDragger implements Runnable {
		private Rectangle2D dragArea = null;

		private boolean draggingRectangle;

		private Point2D dragPoint = null;

		private Rectangle2D dragRectangle = null;

		private IObserver iObserver = null;

		/**
		 * Constructor
		 */
		private RectangleDragger() {
		}

		private RectangleDragger(IObserver iObserver) {
			this.iObserver = iObserver;
		}

		/**
		 * Returns the corners of the dragged rectangle
		 *
		 * @return array of minX, maxX, minY, maxY
		 */
		private double[] getValues() {
			// Convert the corners of the dragArea to user coordinates
			double[] values = new double[4];

			ValueAxis va = getChart().getXYPlot().getDomainAxis();
			values[0] = va.java2DToValue(dragArea.getMinX(), getScreenDataArea(), getChart().getXYPlot()
					.getDomainAxisEdge());
			values[1] = va.java2DToValue(dragArea.getMaxX(), getScreenDataArea(), getChart().getXYPlot()
					.getDomainAxisEdge());
			// The Y values need to be interchanged because screen
			// coordinates increase downwards
			va = getChart().getXYPlot().getRangeAxis();
			values[3] = va.java2DToValue(dragArea.getMinY(), getScreenDataArea(), getChart().getXYPlot()
					.getRangeAxisEdge());
			values[2] = va.java2DToValue(dragArea.getMaxY(), getScreenDataArea(), getChart().getXYPlot()
					.getRangeAxisEdge());

			return values;
		}

		/**
		 * SimplePlot calls this in place of the super class method when a rectangle is being dragged. See
		 * SimplePlot.mouseDragged.
		 *
		 * @param e
		 *            the MouseEvent
		 */
		private void mouseDragged(MouseEvent e) {
			// This is a scaled down version of the ChartPanel method
			// workings pressed into service in a slightly different way

			Graphics2D g2 = (Graphics2D) getGraphics();
			g2.setXORMode(Color.gray);

			// Undraw the previous rectangle
			if (dragRectangle != null) {
				g2.draw(dragRectangle);
			}

			// Calculate the new rectangle, it is constrained to be
			// within the scaled data area
			Rectangle2D scaledDataArea = getScreenDataArea();
			double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
			double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
			dragRectangle = new Rectangle2D.Double(dragPoint.getX(), dragPoint.getY(), xmax - dragPoint.getX(), ymax
					- dragPoint.getY());

			// Draw the new rectangle
			if (dragRectangle != null) {
				g2.draw(dragRectangle);
			}
			g2.dispose();
		}

		/**
		 * SimplePlot calls this in place of the super class method when a rectangle is being dragged. See
		 * SimplePlot.mousePressed.
		 *
		 * @param e
		 *            the MouseEvent
		 */
		private void mousePressed(MouseEvent e) {
			// This is a scaled down version of the ChartPanel method
			// workings pressed into service in a slightly different way

			// This is the starting point of the rectangle
			if (dragRectangle == null)
				dragPoint = ShapeUtilities.getPointInRectangle(e.getX(), e.getY(), getScreenDataArea());
		}

		/**
		 * SimplePlot calls this in place of the super class method when a rectangle is being dragged. See
		 * SimplePlot.mouseReleased.
		 *
		 * @param e
		 *            the MouseEvent
		 */
		private void mouseReleased(@SuppressWarnings("unused") MouseEvent e) {
			// This is a scaled down version of the ChartPanel method
			// workings pressed into service in a slightly different way

			// Calculate a Rectangle2D which represents the dragged area
			Rectangle2D scaledDataArea = getScreenDataArea();
			double x = dragPoint.getX();
			double y = dragPoint.getY();
			double w = Math.min(dragRectangle.getWidth(), scaledDataArea.getMaxX() - dragPoint.getX());
			double h = Math.min(dragRectangle.getHeight(), scaledDataArea.getMaxY() - dragPoint.getY());
			dragArea = new Rectangle2D.Double(x, y, w, h);

			// Undraw the dragRectangle
			Graphics2D g2 = (Graphics2D) getGraphics();
			g2.setXORMode(java.awt.Color.gray);
			g2.draw(dragRectangle);
			g2.dispose();

			dragPoint = null;
			dragRectangle = null;

			if (iObserver == null) {
				// This ends the thread
				synchronized (this) {
					draggingRectangle = false;
					notifyAll();
				}
			} else {
				iObserver.update(this, getValues());
			}
		}

		/**
		 *
		 */
		@Override
		public void run() {
			synchronized (this) {
				do {
					try {
						wait();
					} catch (InterruptedException ie) {
						logger.error("RectangleDragger wait() unexpectedly interrupted");
					}
				} while (draggingRectangle);
			}
		}
	}

	/**
	 * Provides LegendItems to a Legend.
	 */
	private class LegendItemsGetter implements LegendItemSource {
		private int datasetIndex;

		private boolean visible = true;

		private LegendItemsGetter(int datasetIndex) {
			this.datasetIndex = datasetIndex;
		}

		@Override
		public LegendItemCollection getLegendItems() {
			// This is a simplified version of what happens in XYPlot.
			LegendItemCollection result = new LegendItemCollection();
			if (visible) {
				XYDataset dataset = ((XYPlot) getChart().getPlot()).getDataset(datasetIndex);

				if (dataset != null) {
					XYItemRenderer renderer = ((XYPlot) getChart().getPlot()).getRenderer(datasetIndex);

					for (int i = 0; i < dataset.getSeriesCount(); i++) {
						if (renderer.isSeriesVisible(i) && renderer.isSeriesVisibleInLegend(i)) {
							LegendItem item = renderer.getLegendItem(datasetIndex, i);
							if (item != null) {
								result.add(item);
							}
						}
					}
				}
			}
			return result;

		}

		/**
		 * @param visible
		 */
		public void setVisible(boolean visible) {
			this.visible = visible;
		}
	}

	/**
	 *
	 */
	public static int LINECHART = 0;

	/**
	 *
	 */
	public static int BARCHART = 1;

	/**
	 *
	 */
	public static int LEFTYAXIS = 0;

	/**
	 *
	 */
	private static int RIGHTYAXIS = 1;

	private HashMap<Integer, SimpleXYSeries> seriesStore = new HashMap<Integer, SimpleXYSeries>();

	protected SimpleXYSeriesCollection leftSeriesCollection = new SimpleXYSeriesCollection(this);

	protected SimpleXYSeriesCollection rightSeriesCollection = null;

	private LegendTitle leftSeriesLegend = null;

	private LegendItemsGetter leftSeriesLegendItemsGetter = null;

	private LegendTitle rightSeriesLegend = null;

	private LegendItemsGetter rightSeriesLegendItemsGetter = null;

	private boolean legendVisible = true;

	private MousePositionTracker mpt = null;

	private boolean pointerTracking = false;

	private RectangleDragger rd = null;

	private String title = "title";

	private boolean titleVisible = true;

	private boolean yAxisTwoOn = false;

	private XYPlot xYPlot;

	private int secondaryAxisNumber = 1;

	private int secondaryDataSetIndex = 1;

	private CoordinateFormatter coordinateFormatter = null;

	private boolean zooming = false;

	private boolean turboMode = false;

	private JCheckBoxMenuItem zoomButton, turboModeButton, stripModeButton, xLimitsButton;

	private JCheckBoxMenuItem magnifyDataButton;

	private JMenuItem unZoomButton;

	private JMenuItem xLogLinButton;

	private JMenuItem yLogLinButton;

	private JMenuItem y2LogLinButton;

	private JMenuItem saveButton;

	private JMenuItem xFormatButton ;

	private JMenuItem yFormatButton;

	private JMenuItem addLine = null;

	private JMenuItem removeFirst = null;

	private JMenuItem removeLast = null;

	private JMenuItem clearLines = null;

	private JCheckBoxMenuItem xAxisVerticalTicksButton;

	private Stack<AxisLimits> zoomStack = new Stack<AxisLimits>();

	private boolean magnifyingImage = false;

	private LinePropertiesEditor linePropertiesEditor=null;

	private int magnifyXPoint;

	private int magnifyYPoint;

	private int magnifyWidth = 100;

	private int magnifyHeight = 50;

	private boolean magnifyingData = false;

	private DataMagnifierWindow dataMagnifierWindow;

	private Rectangle2D magnifyRectangle;

	private boolean magnifyRectangleIsNew;

	private Color dragColour = null;

	private Magnifier magnifier = null;

	private SimpleNumberAxis linearXAxis;

	private LogarithmicAxis logarithmicXAxis;

	private boolean xAxisLogarithmic = false;

	private SimpleNumberAxis dependentXAxis;

	private double emForXAxis;

	private double seeForXAxis;

	private boolean dependentXAxisOn = false;

	private AxisChangeListener xAxisChangeListener;

	private SimpleNumberAxis dependentYAxis;

	private double emForYAxis;

	private double seeForYAxis;

	private boolean dependentYAxisOn = false;

	private SimpleNumberAxis linearYAxis;

	private LogarithmicAxis logarithmicYAxis;

	private boolean yAxisLogarithmic = false;

	private SimpleNumberAxis linearYAxisTwo;

	private LogarithmicAxis logarithmicYAxisTwo;

	private boolean yAxisTwoLogarithmic = false;

	private NumberFormat xAxisNumberFormat;

	private NumberFormat yAxisNumberFormat;

	private NumberFormat yAxisTwoNumberFormat;

	private boolean currentlyDoingAZoom;

	private boolean batching = false;

	private static int HMAX = 5; // History limit

	// this is a FIFO buffer for keeping a history of SimpleXYSeries
	private LinkedList<SimpleXYSeries> history= null;

	private JFileChooser fc;

	private final int type;

	private boolean showToolTip;

	/**
	 * @return boolean
	 */
	public boolean isShowToolTip() {
		return showToolTip;
	}

	/**
	 * Enable/Disable the tool tip for main plot
	 * @param showToolTip
	 */
	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
		XYItemRenderer xyrenderer = xYPlot.getRenderer();
		if(showToolTip)
			xyrenderer.setToolTipGenerator(new SimpleXYToolTipGenerator());

		else
			xyrenderer.setToolTipGenerator(null);
		xYPlot.setRenderer(xyrenderer);
	}

	/**
	 * No argument constructor creates a LINECHART
	 */
	public SimplePlot() {
		this(LINECHART);
	}

	/**
	 * Constructor which creates either a LINECHART or a BARCHART
	 *
	 * @param type
	 *            either SimplePlot.LINECHART or SimplePlot.BARCHART
	 */
	// Create simplePlot with autoRange true
	public SimplePlot(int type) {
		this(type, true, true);
	}

	private static UpdatePlotQueue updateQueue = new UpdatePlotQueue();
	/**
	 * @param type
	 * @param autoRange
	 * @param enableHistory
	 */
	public SimplePlot(int type, boolean autoRange, boolean enableHistory ) {

		super(new JFreeChart(new XYPlot()));
		setChart(ChartFactory.createXYLineChart(title, "xaxis", "yaxis", leftSeriesCollection,
				PlotOrientation.VERTICAL, true, false, false));
		this.type = type;
		if (enableHistory) {
			history = new LinkedList<SimpleXYSeries>();
			addHistoryCommandsToPopupMenu();
		}

		// For width and height greater than these values the graph is drawn by
		// using a transform on the Graphics2D. If this comes into operation
		// then the placing of components added with wrapAndDisplay goes wrong.
		// The easiest way to keep out of trouble is to make them large. This
		// also
		// stops distortion of fonts when expanding to full screen size.
		setMaximumDrawWidth(5000);
		setMaximumDrawHeight(2000);

		// Next line added to fix sun.dc.pr.PRException on Windows PC's
		getChart().setAntiAlias(false);

		xYPlot = getChart().getXYPlot();
		if (type == BARCHART) {
			xYPlot.setRenderer(new SimpleXYBarRenderer());
		} else {
			xYPlot.setRenderer(new SimpleXYItemRenderer());
			xYPlot.getRenderer().setToolTipGenerator(new SimpleXYToolTipGenerator());
		}

		// The x and y axes are created and set here so that SimpleNumberAxis
		// can
		// be used. This allows SimplePlot to keep the CoordinateFormatter up to
		// date about the accuracy required (seeForXAxis paintComponent()). NB
		// Any secondary y axes will be of type NumberAxis.

		createXAxes(autoRange);
		xYPlot.setDomainAxis(linearXAxis);

		createYAxes(autoRange);
		xYPlot.setRangeAxis(linearYAxis);

		// There are two legends. We use the exisiting default one for
		// lines displayed with respect to the left Y axis and create a
		// new one for those of the right Y axis (if there is one).
		// Normally getChart().getPlot() would provide the LegendItemSource
		// for the default legend but we extract it and set its Source to be a
		// LegendItemsGetter (defined here) and change its position to RIGHT
		// (default is TOP).
		// leftSeriesLegend = getChart().getLegend();
		// leftSeriesLegend.setPosition(RectangleEdge.RIGHT);

		// This LegendItemsGetter will return only left series legend items.
		leftSeriesLegendItemsGetter = new LegendItemsGetter(0);

		// TEST CODE DO NOT REMOVE
		leftSeriesLegend = new SimpleLegendTitle(leftSeriesLegendItemsGetter);
		leftSeriesLegend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		leftSeriesLegend.setBorder(new BlockBorder());
		leftSeriesLegend.setBackgroundPaint(Color.white);
		leftSeriesLegend.setPosition(RectangleEdge.BOTTOM);
		leftSeriesLegend.setPosition(RectangleEdge.RIGHT);
		getChart().removeLegend();
		getChart().addLegend(leftSeriesLegend);
		// END OF TEST CODE

		leftSeriesLegend.setSources(new LegendItemSource[] { leftSeriesLegendItemsGetter });

		rightSeriesLegend = new SimpleLegendTitle(null);
		rightSeriesLegend.setPosition(RectangleEdge.RIGHT);
		rightSeriesLegend.setBorder(leftSeriesLegend.getBorder());
		rightSeriesLegend.setBackgroundPaint(leftSeriesLegend.getBackgroundPaint());

		// This LegendItemsGetter will return only right series legend items.
		rightSeriesLegendItemsGetter = new LegendItemsGetter(1);
		rightSeriesLegend.setSources(new LegendItemSource[] { rightSeriesLegendItemsGetter });
		getChart().addLegend(rightSeriesLegend);

		// The mousePositionTracker is added as a subtitle
		mpt = new MousePositionTracker("");
		addChartMouseListener(mpt);
		getChart().addSubtitle(mpt);

		// The coordinateFormatter is used in the MousePositionTracker when
		// displaying the mouse position.
		coordinateFormatter = new SimpleCoordinateFormatter();

		dataMagnifierWindow = new DataMagnifierWindow(SwingUtilities.getRoot(this));
		dataMagnifierWindow.setSimplePlot(this, leftSeriesCollection);

		// Make sure zooming starts in the correct state.
		setZooming(false);

		setTurboMode(false);
		setStripWidth(null);
		setTrackPointer(true);

	}

	/**
	 * @return Plot is in TurboMode When in TurboMode then: 1. rendering is done by TurboXYItemRenderer 2. addPoints
	 *         does not directly fire a DataSetChange but rather an update queue is used 3. the ranges for the x and y
	 *         are controlled by SimplePlot - in calls getRangeBounds and getDomainBounds allow such features are
	 *         stripchart and fixed xmin/xmax //TODO
	 */
	public boolean isTurboMode() {
		return turboMode;
	}

	private XYItemRenderer nonTurboRenderer;
	private XYItemRenderer nonTurboSecondaryRenderer;
	private TurboXYItemRenderer turboRenderer;
	private TurboXYItemRenderer turboSecondaryRenderer;

	/**
	 * @param turboMode
	 *            When in TurboMode then: 1. rendering is done by TurboXYItemRenderer 2. addPoints does not directly
	 *            fire a DataSetChange but rather an update queue is used 3. the ranges for the x and y are controlled
	 *            by SimplePlot - in calls getRangeBounds and getDomainBounds allow such features are stripchart and
	 *            fixed xmin/xmax //TODO *
	 */
	@Override
	public void setTurboMode(boolean turboMode) {
		if( type != LINECHART){
			turboMode = false;
			return;
		}
		boolean switchToTurbo = !isTurboMode() & turboMode & type == LINECHART;
		this.turboMode = turboMode & type == LINECHART;
		if( type == LINECHART){
			if ( turboRenderer == null){
				turboRenderer = new TurboXYItemRenderer();
				turboRenderer.setToolTipGenerator(new SimpleXYToolTipGenerator());
			}
			if ( turboSecondaryRenderer == null){
				turboSecondaryRenderer = new TurboXYItemRenderer();
			}
			if( switchToTurbo ){
				nonTurboRenderer = xYPlot.getRenderer();
				if( this.yAxisTwoOn ){
					nonTurboSecondaryRenderer = xYPlot.getRenderer(secondaryDataSetIndex);
				}
			}
			{
				XYItemRenderer renderer = turboMode ? turboRenderer : nonTurboRenderer;
				if( renderer != null ){
					if( renderer instanceof SimpleXYItemRenderer ){
						((SimpleXYItemRenderer)renderer).setXValueTransformer(leftSeriesCollection.getXValueTransformer());
					}
					xYPlot.setRenderer( renderer);
				}
			}
			if( yAxisTwoOn ){
				XYItemRenderer renderer = turboMode ? turboSecondaryRenderer : nonTurboSecondaryRenderer;
				if( renderer != null){
					if( renderer instanceof SimpleXYItemRenderer){
						((SimpleXYItemRenderer)renderer).setXValueTransformer(rightSeriesCollection.getXValueTransformer());
					}
					xYPlot.setRenderer(secondaryDataSetIndex, renderer);
				}
			}

		}
		if (turboMode){
			setZooming(true);
		} else {
			setStripWidth(null);
			setDomainBounds(null);
		}
		turboModeButton.setSelected(turboMode);
		stripModeButton.setEnabled(turboMode);
		xLimitsButton.setEnabled(turboMode);
	}

	/**
	 * @return Plot is in StripChart mode - the x range is set to display only the last set number of points
	 */
	public boolean isStripMode() {
		return stripWidth != null;
	}

	/**
	 * Turns strip chart mode on and off.
	 * @param stripWidth width in domain axis from last point value to min val
	 */
	public void setStripWidth(Double stripWidth) {
		this.stripWidth = type == LINECHART ? stripWidth : null;
		if (stripModeButton != null)
			stripModeButton.setSelected(isStripMode());
		if(isStripMode()){
			setDomainBounds(null);
		}
	}

	/**
	 * Creates the linear and logarithmic x axes
	 *
	 * @param autoRange
	 */
	private void createXAxes(boolean autoRange) {
		xAxisNumberFormat = NumberFormat.getNumberInstance();

		linearXAxis = new SimpleNumberAxis("xaxis");
		initAxis(linearXAxis, autoRange, xAxisNumberFormat);

		logarithmicXAxis = new LogarithmicAxis("xaxis");
		initLogAxis(logarithmicXAxis, autoRange, xAxisNumberFormat);

	}

	/**
	 * Creates the linear and logarithmic y axes
	 *
	 * @param autoRange
	 */
	private void createYAxes(boolean autoRange) {
		yAxisNumberFormat = NumberFormat.getNumberInstance();
		yAxisNumberFormat.setGroupingUsed(false);

		linearYAxis = new SimpleNumberAxis("yaxis");
		initAxis(linearYAxis, autoRange, yAxisNumberFormat);

		logarithmicYAxis = new LogarithmicAxis("yaxis");
		initLogAxis(logarithmicYAxis, autoRange, yAxisNumberFormat);
	}

	/**
	 * Creates the linear and logarithmic second y axes
	 *
	 * @param autoRange
	 */
	private void createYAxesTwo(boolean autoRange) {
		yAxisTwoNumberFormat = NumberFormat.getNumberInstance();
		yAxisTwoNumberFormat.setGroupingUsed(false);

		linearYAxisTwo = new SimpleNumberAxis("Second Y Axis");
		initAxis(linearYAxis, autoRange, yAxisTwoNumberFormat);

		logarithmicYAxisTwo = new LogarithmicAxis("Second Y Axis");
		initLogAxis(logarithmicYAxisTwo, autoRange,yAxisTwoNumberFormat);
	}

	/**
	 * Creates the JPopupMenu, overrides (but uses) the super class method adding items for the Magnification and
	 * Logarithmic axes.
	 *
	 * @param properties
	 *            boolean if true appears on menu
	 * @param copy
	 *            boolean if true appears on menu
	 * @param save
	 *            boolean if true appears on menu
	 * @param print
	 *            boolean if true appears on menu
	 * @param zoom
	 *            boolean if true appears on menu
	 * @return the popup menu
	 */
	@Override
	protected JPopupMenu createPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) {
		// Create the popup without the zooming parts
		JPopupMenu jpm = super.createPopupMenu(properties, copy, false, print, false);

		// as the save function on the chartpanel doesn't remember its location,
		// we shall remove it and and create a new save option
		if (save) {
			jpm.add(new JSeparator());

			// The save button

			saveButton = new JMenuItem("Save As");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveAs();
				}
			});

			jpm.add(saveButton);
		}

		jpm.add(new JSeparator());

		// This button toggles the data-type magnification
		magnifyDataButton = new JCheckBoxMenuItem("Magnify(Data)");
		magnifyDataButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMagnifyingData(!isMagnifyingData());
			}
		});
		jpm.add(magnifyDataButton);

		jpm.add(new JSeparator());

		// The zoomButton toggles the value of zooming.
		zoomButton = new JCheckBoxMenuItem("Zoom");
		zoomButton.setHorizontalTextPosition(SwingConstants.LEFT);
		zoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setZooming(!isZooming());
			}
		});
		jpm.add(zoomButton);

		// The unZoomButton is not a toggle, it undoes the last zoom.
		unZoomButton = new JMenuItem("UnZoom");
		unZoomButton.setEnabled(false);
		unZoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				unZoom();
			}
		});
		jpm.add(unZoomButton);

		if( type == LINECHART){
			jpm.add(new JSeparator());

			turboModeButton = new JCheckBoxMenuItem("Turbo Mode");
			turboModeButton.setHorizontalTextPosition(SwingConstants.LEFT);
			turboModeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setTurboMode(!isTurboMode());
				}
			});
			turboModeButton.setSelected(isTurboMode());
			jpm.add(turboModeButton);

			stripModeButton = new JCheckBoxMenuItem("StripChart Mode");
			stripModeButton.setHorizontalTextPosition(SwingConstants.LEFT);
			stripModeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String widthStr = "";
					{
						double width = lastDomainBoundsLeft != null ? lastDomainBoundsLeft.getLength() : Double.MAX_VALUE;
						widthStr = getXAxisNumberFormat().format(width);
						widthStr = JOptionPane.showInputDialog(null,
								"Enter the x strip width - clear for autorange", widthStr);
						if(widthStr == null) //cancel
							return;
					}
					Double newStripWidth = null;
					if(!widthStr.isEmpty()){
						try{
							newStripWidth = Double.valueOf(widthStr);
						} catch (Exception ex){
							logger.error(ex.getMessage(),ex);
						}
					}
					setStripWidth(newStripWidth);
				}
			});
			stripModeButton.setSelected(isStripMode());
			jpm.add(stripModeButton);

			xLimitsButton = new JCheckBoxMenuItem("Fix X Axis Limits");
			xLimitsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String minStr = "";
					{
						double min = lastDomainBoundsLeft != null ? lastDomainBoundsLeft.getLowerBound() : Double.MAX_VALUE;
						minStr = getXAxisNumberFormat().format(min);
						minStr = JOptionPane.showInputDialog(null,
								"Enter the min x value - clear for autorange", minStr);
						if(minStr == null) //cancel
							return;

					}
					String maxStr = "";
					if(!minStr.isEmpty()){
						double max = lastDomainBoundsLeft != null ? lastDomainBoundsLeft.getUpperBound() : -Double.MAX_VALUE;
						maxStr = getXAxisNumberFormat().format(max);
						maxStr = JOptionPane.showInputDialog(null, "Enter the max x value - clear for autorange", maxStr);
						if(maxStr == null) //cancel
							return;
					}
					Range newBounds = null;
					if(!maxStr.isEmpty() && !minStr.isEmpty()){
						try{
							newBounds = new Range(Double.valueOf(minStr), Double.valueOf(maxStr));
						} catch (Exception ex){
							logger.error(ex.getMessage(),ex);
						}
					}
					setDomainBounds( newBounds );
				}
			});
			xLimitsButton.setSelected(false);
			jpm.add(xLimitsButton);

		}

		jpm.add(new JSeparator());

		xLogLinButton = new JMenuItem("Logarithmic X axis");
		xLogLinButton.setEnabled(true);
		xLogLinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setXAxisLogarithmic(!isXAxisLogarithmic());
			}
		});
		jpm.add(xLogLinButton);

		yLogLinButton = new JMenuItem("Logarithmic Y axis");
		yLogLinButton.setEnabled(true);
		yLogLinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setYAxisLogarithmic(!isYAxisLogarithmic());
			}
		});
		jpm.add(yLogLinButton);

		y2LogLinButton = new JMenuItem("Logarithmic Y2 axis");
		y2LogLinButton.setEnabled(false);
		y2LogLinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setYAxisTwoLogarithmic(!isYAxisTwoLogarithmic());
			}
		});

		jpm.add(y2LogLinButton);

		jpm.add(new JSeparator());

		// Adding a new button to allow the user to select the formatting they
		// want on the x and y axis
		xFormatButton = new JMenuItem("X Axis Format");
		xFormatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String Format = getXAxisNumberFormat().format(0.0);
				String input = JOptionPane.showInputDialog(null,
						"Enter the new formatting for the X axis, of the form 0.0000E00", Format);
				// try forcing this into some objects
				try {
					setScientificXAxis(new DecimalFormat(input));
				} catch (Exception err) {
					logger.error("Could not use this format due to {}", e);
				}
			}
		});
		jpm.add(xFormatButton);


		// Adding a new button to allow the user to select the formatting they
		// want on the x and y axis
		yFormatButton = new JMenuItem("Y Axis Format");
		yFormatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String Format = getYAxisNumberFormat().format(0.0);
				String input = JOptionPane.showInputDialog(null,
						"Enter the new formatting for the Y axis, of the form 0.0000E00", Format);
				// try forcing this into some objects
				try {
					setScientificYAxis(new DecimalFormat(input));
				} catch (Exception err) {
					logger.error("Could not use this format due to {}", e);
				}
			}
		});
		jpm.add(yFormatButton);

		// The zoomButton toggles the value of zooming.
		xAxisVerticalTicksButton = new JCheckBoxMenuItem("Vertical X Ticks");
		xAxisVerticalTicksButton.setHorizontalTextPosition(SwingConstants.LEFT);
		xAxisVerticalTicksButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVerticalXAxisTicks(xAxisVerticalTicksButton.isSelected());
			}
		});
		jpm.add(xAxisVerticalTicksButton);


		return jpm;
	}

	@Override
	// This override doesn't strictly need to exist, but it's a little insulation against changes in the base class
	protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print, boolean zoom) {
		return createPopupMenu(properties, false, save, print, zoom);
	}

	// this is called later in constructor
	private void addHistoryCommandsToPopupMenu()
	{
		JPopupMenu jpm = getPopupMenu();

		jpm.add(new JSeparator());

		// New button to allow user to add current plot to stored plots
		// This history mechanism assumes that there is only one line being plotted
		// and this line is stored in the last entry of seriesStore that was not in history
		addLine = new JMenuItem("Add current line to history");
		addLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleXYSeries s = findLine(lastLineAdded - history.size());
				if (s != null) {
					history.addFirst(s);
					if (history.size() > HMAX) {
						history.removeLast();
					}
				}
			}
		});
		jpm.add(addLine);

		// New button to allow user to remove first plot from stored plots
		removeFirst = new JMenuItem("Remove most recent line from history");
		removeFirst.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				historyRemoveFirst();
			}
		});
		jpm.add(removeFirst);

		// New button to allow user to remove last plot from stored plots
		removeLast = new JMenuItem("Remove oldest line from history");
		removeLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				historyRemoveLast();
			}
		});
		jpm.add(removeLast);

		// New button to allow user to add current plot to stored plots
		clearLines = new JMenuItem("Clear history");
		clearLines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				historyClear();
			}
		});
		jpm.add(clearLines);
	}

	/**
	 * Remove history lines from leftSeriesCollection and seriesStore
	 */
	private void historyCleanse() {
		for (SimpleXYSeries s : history){
			leftSeriesCollection.removeSeries(s);
			seriesStore.remove(s.getLineNumber());

		}
		lastLineAdded -= history.size();
	}

	/**
	 * Repopulate this object with lines from history
	 */
	private void historyRepopulate() {
		// now blast the List of lines to the parent SimplePlot
		int h = lastLineAdded;
        for (int l = 0; l < history.size(); l++) {
        	SimpleXYSeries s = history.get(l);
        	s.setLineNumber(h + l + 1);
        	s.setName("History " + (l + 1));
			initializeLine(s);
		}
	}

	/**
	 *
	 */
	private void historyRemoveFirst() {
		historyCleanse();
		if (history.size() > 0)
			history.removeFirst();
		historyRepopulate();
		validate();
	}

	/**
	 *
	 */
	private void historyRemoveLast() {
		historyCleanse();
		if (history.size() > 0)
			history.removeLast();
		historyRepopulate();
		validate();
	}

	/**
	 *
	 */
	private void historyClear() {
		if( history == null)
			return;
		historyCleanse();
		history.clear();
		validate();
	}

	/**
	 * IDEWISOTT
	 */
	private void unZoom() {
		// Pop the set of limits at the top of the stack
		// and use them to reset axes..
		setAxisLimits(zoomStack.pop());

		// If the stack is now empty disable the unZoom button
		unZoomButton.setEnabled(!zoomStack.isEmpty());
	}

	/**
	 * @param minVal
	 */
	public void setXRangeMin(double minVal) {
		double maxval = linearXAxis.getRange().getUpperBound();
		linearXAxis.setRange(new Range(minVal, maxval));
		logarithmicXAxis.setRange(new Range(minVal, maxval));
	}

	/**
	 * @param maxVal
	 */
	public void setXRangeMax(double maxVal) {
		double minval = linearXAxis.getRange().getLowerBound();
		linearXAxis.setRange(new Range(minval, maxVal));
		logarithmicXAxis.setRange(new Range(minval, maxVal));
	}

	/**
	 * @param minVal
	 */
	public void setYRangeMin(double minVal) {
		double maxval = linearYAxis.getRange().getUpperBound();
		linearYAxis.setRange(new Range(minVal, maxval));
		logarithmicYAxis.setRange(new Range(minVal, maxval));
		if (linearYAxisTwo != null) {
			linearYAxisTwo.setRange(new Range(minVal, maxval));
			logarithmicYAxisTwo.setRange(new Range(minVal, maxval));
		}
	}

	/**
	 * @param maxVal
	 */
	public void setYRangeMax(double maxVal) {
		double minval = linearYAxis.getRange().getLowerBound();
		linearYAxis.setRange(new Range(minval, maxVal));
		logarithmicYAxis.setRange(new Range(minval, maxVal));
		if (linearYAxisTwo != null) {
			linearYAxisTwo.setRange(new Range(minval, maxVal));
			logarithmicYAxisTwo.setRange(new Range(minval, maxVal));
		}
	}

	private Collection<Integer> linesChanged = new Vector<Integer>();

	/**
	 * @param which
	 * @param x
	 * @param y
	 */
	@Override
	public void addPointToLine(int which, double x, double y) {
		addPointToLine(which, x, y, !isTurboMode());
	}

	/**
	 * Adds a single data point to a line. The line must already have been initialized.
	 *
	 * @param which
	 *            the line
	 * @param x
	 *            the new x value
	 * @param y
	 *            the new y value
	 * @param notifyDirectly
	 *            is the DataSet to fireDataSetChange directly or via a 0.5 period event
	 */
	void addPointToLine(int which, double x, double y, boolean notifyDirectly) {

		/*
		 * if (newPlot) { setXRangeMax(x + 0.1); setXRangeMin(x - 0.1); setYRangeMax(y + 0.1); setYRangeMin(y - 0.1);
		 * newPlot = false; } else { // before anything see if this needs to be rescaled if
		 * (linearXAxis.getRange().getLowerBound() > x) { setXRangeMin(x - linearXAxis.getRange().getLength()); } //
		 * before anything see if this needs to be rescaled if (linearXAxis.getRange().getUpperBound() < x) {
		 * setXRangeMax(x + linearXAxis.getRange().getLength()); } // before anything see if this needs to be rescaled
		 * if (linearYAxis.getRange().getLowerBound() > y) { setYRangeMin(y - linearYAxis.getRange().getLength()); } //
		 * before anything see if this needs to be rescaled if (linearYAxis.getRange().getUpperBound() < y) {
		 * setYRangeMax(y + linearYAxis.getRange().getLength()); } }
		 */

		SimpleXYSeries s = findLine(which);
		if (s == null) {
			logger.warn("SimplePlot - unable to find line " + Integer.toString(which));
			return;
		}
		s.add(x, y, notifyDirectly );
		if (!notifyDirectly ){
			updateQueue.update(this);
		}
		if (isTurboMode()) {
			synchronized (linesChanged) {
				if (!linesChanged.contains(which))
					linesChanged.add(which);
			}
		}
	}

	void addYAxisTwo(boolean autoRange) {
		if (!dependentYAxisOn) {
			createYAxesTwo(autoRange);
			y2LogLinButton.setEnabled(true);

			xYPlot.setRangeAxis(secondaryAxisNumber, linearYAxisTwo);
			yAxisTwoLogarithmic = false;

			// Note that the newly created SimpleXYSeriesCollection and
			// SimpleXYItemRenderer must have their x value transformers set
			// to whatever is currently in the leftSeriesCollection.
			rightSeriesCollection = new SimpleXYSeriesCollection(this);
			rightSeriesCollection.setXValueTransformer(leftSeriesCollection.getXValueTransformer());
			xYPlot.setDataset(secondaryDataSetIndex, rightSeriesCollection);
			xYPlot.mapDatasetToRangeAxis(secondaryDataSetIndex, secondaryAxisNumber);
			SimpleXYItemRenderer newRenderer = isTurboMode() ? new TurboXYItemRenderer() :new SimpleXYItemRenderer();
			newRenderer.setXValueTransformer(leftSeriesCollection.getXValueTransformer());
			xYPlot.setRenderer(secondaryDataSetIndex, newRenderer);

			// If there is to be a second YAxis then we move the Legend for
			// the
			// first.
			leftSeriesLegend.setPosition(RectangleEdge.LEFT);
			yAxisTwoOn = true;
		}
	}

	/**
	 * Converts the (pixel) coordinates of a MouseEvent into their actual plot value equivalents
	 *
	 * @param me
	 *            the MouseEvent to be converted
	 * @return SimpleDataCoordinate object containing the converted coordinates
	 */
	public SimpleDataCoordinate convertMouseEvent(MouseEvent me) {
		SimpleDataCoordinate sdc =  new SimpleDataCoordinate(
		xYPlot.getDomainAxis().java2DToValue(me.getX(), getScreenDataArea(), xYPlot.getDomainAxisEdge()),

		xYPlot.getRangeAxis().java2DToValue(me.getY(), getScreenDataArea(), xYPlot.getRangeAxisEdge()));
		if(dependentXAxisOn)
		{
			sdc.setDependentXCalibrationValues(this.emForXAxis, this.seeForXAxis);
		}
		if(dependentYAxisOn)
		{
			sdc.setDependentYCalibrationValues(this.emForYAxis, this.seeForYAxis);
		}
		return sdc;

	}

	/**
	 * Deletes a line
	 *
	 * @param which
	 *            the line to delete
	 */
	@Override
	public void deleteLine(int which) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			if(s.getAxis() == RIGHTYAXIS && rightSeriesCollection != null)
				rightSeriesCollection.removeSeries(s);
			else {
				if(s.isVisible()){
					leftSeriesCollection.removeSeries(s);
				}
			}
			seriesStore.remove(which);
			repaint();
		}
	}

	/**
	 * Deletes all lines
	 */

	@Override
	public void deleteAllLines() {
		leftSeriesCollection.removeAllSeries();
		if (rightSeriesCollection != null) {
			rightSeriesCollection.removeAllSeries();
		}
		seriesStore.clear();

		// all lines deleted, so set highest used line number to 0
		// (meaning the next line number will be 1)
		highestUsedLineNumber = 0;
	}

	/**
	 * Adds a second x axis which has values dependent on those of the first xaxis. This allows different units to be
	 * shown for the same data. The x values of this axis are related to those of the first xaxis by: xtwo = em * xone +
	 * see
	 *
	 * @param em
	 *            the gradient of the expression relating the two axes
	 * @param see
	 *            the intercept of the expression relating the two axes
	 */
	public void addDependentXAxis(double em, double see) {
		if (!dependentXAxisOn && !isXAxisLogarithmic()) {
			xLogLinButton.setEnabled(false);
			dependentXAxis = new SimpleNumberAxis("dependent xaxis");
			dependentXAxis.setAutoRange(false);
			this.emForXAxis = em;
			this.seeForXAxis = see;
			setDependentXAxisRange();
			xYPlot.setDomainAxis(1, dependentXAxis);
			dependentXAxisOn = true;
			// FIXME: should prevent setting of logarithmic while this is on

			xAxisChangeListener = new AxisChangeListener() {
				@Override
				public void axisChanged(AxisChangeEvent event) {
					if (!currentlyDoingAZoom) {
						// The superclass zooming mechanism correctly deals
						// with the dependent axis when zooming and resetting
						// the range actually causes it to go wrong.
						setDependentXAxisRange();
					}
				}
			};
			linearXAxis.addChangeListener(xAxisChangeListener);
		}
	}

	private void setDependentXAxisRange() {
		Range r = linearXAxis.getRange();
		Range newR = new Range(r.getLowerBound() * emForXAxis + seeForXAxis, r.getUpperBound() * emForXAxis
				+ seeForXAxis);
		dependentXAxis.setRange(newR);
	}

	/**
	 * Waits for the next mouse click and returns its coordinates.
	 *
	 * @return coordinates of cursor at mouse click
	 */
	public double[] getCursorCoordinates() {
		// MousePositionTracker is responsible for this
		mpt.start();
		mpt.join();
		return mpt.getCursorCoordinates();
	}

	/**
	 * Returns the last known position of the cursor
	 *
	 * @return coordinates.
	 */
	public double[] getCoordinates() {
		double result[] = { 0.0, 0.0 };
		if (mpt.coordinates != null) {
			result[0] = mpt.coordinates.getX();
			result[1] = mpt.coordinates.getY();
		}
		return result;
	}

	/**
	 * Waits for the next mouse click and returns the coordinates of the entity nearest to it.
	 *
	 * @return coordinates of entity.
	 */
	public double[] getEntityCoordinates() {
		// MousePositionTracker is responsible for this
		mpt.start();
		mpt.join();
		return mpt.getEntityCoordinates();
	}

	/**
	 * Gets the number of lines
	 *
	 * @return the number of lines
	 */
	@Override
	public int getNextAvailableLine() {
		return highestUsedLineNumber+1;
	}
	/**
	 * Returns the maximum value of the Y axis
	 *
	 * @return Y axis maximum value
	 */
	public double getYAxisMax() {
		// FIXME: this does not seem to return quite the right value
		// where does the actual axis maximum (always greater than
		// this) come from?
		return xYPlot.getRangeAxis().getUpperBound();
	}

	/**
	 * Returns the minimum value of the Y axis
	 *
	 * @return Y axis minimum value
	 */
	public double getYAxisMin() {
		return xYPlot.getRangeAxis().getLowerBound();
	}

	/**
	 * Returns the maximum value of the X axis
	 *
	 * @return X axis maximum value
	 */
	public double getXAxisMax() {

		// FIXME: this does not seem to return quite the right value
		// where does the actual axis maximum (always greater than
		// this) come from?
		return xYPlot.getDomainAxis().getUpperBound();
	}

	/**
	 * Returns the minimum value of the X axis
	 *
	 * @return X axis minimum value
	 */
	public double getXAxisMin() {
		return xYPlot.getDomainAxis().getLowerBound();
	}

	/**
	 * Initialises a line using LEFTYAXIS for its y values.
	 *
	 * @param which
	 *            the line to initialise (numbered from 0)
	 */
	public void initializeLine(int which) {
		initializeLine(which, LEFTYAXIS);
	}

	/**
	 * Initialises a line using specified axis for its y values
	 *
	 * @param which
	 *            the line to initialise (numbered from 0)
	 * @param axis
	 *            the axis - LEFTAXIS
	 */
	void initializeLine(int which, int axis) {
		initializeLine(which, axis, null, "","","", null);
	}

	/**
	 * Initialises a line using specified axis for its y values and name
	 *
	 * @param which
	 *            the line to initialise (numbered from 0)
	 * @param axis
	 *            the axis - LEFTAXIS
	 * @param name
	 */
	@Override
	public void initializeLine(int which, int axis, String name, String xAxisHeader, String yAxisHeader, String dataFileName, AxisSpec yAxisSpec) {
		initializeLineImpl(which, axis, name, null);
	}

	/**
	 * Initialises a line using specified SimpleXYSeries
	 *
	 * @param s
	 */
	public void initializeLine(SimpleXYSeries s) {
		initializeLineImpl(s.getLineNumber(), s.getAxis(), null, s);
	}

	private int lastLineAdded = 0; // last line added

	/** Highest line number used so far */
	private int highestUsedLineNumber=0;

	private void initializeLineImpl(int which, int axis, String name, SimpleXYSeries s) {

		if (lineExists(which)) {
			deleteLine(which);
		}
		if (yAxisTwoOn == false)
			axis = LEFTYAXIS;

		SimpleXYSeries newLine = s != null ? s : new SimpleXYSeries(name != null ? name : "line " + which, which, axis);
		newLine.setBatching(batching);
		if( newLine.isVisible()){
			if (axis == LEFTYAXIS) {
				leftSeriesCollection.addSeries(newLine);
			} else {
				rightSeriesCollection.addSeries(newLine);
			}
			synchronized (linesChanged) {
				linesChanged.clear();
			}
			lastLineAdded = which;
		}
		seriesStore.put(which, newLine);

		highestUsedLineNumber = Math.max(which, highestUsedLineNumber);
	}

	/**
	 * Sets the data points for a line - the line must already have been initialized.
	 *
	 * @param which
	 *            the line
	 * @param x
	 *            array of x values
	 * @param y
	 *            array of y values
	 */
	public void setLinePoints(int which, double x[], double y[]) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			s.setPoints(x, y);
			repaint();
		}
	}

	/**
	 * Checks whether a line exists
	 *
	 * @param lineNumber
	 *            the line to check
	 * @return true if there is a line with the given number
	 */
	private boolean lineExists(int lineNumber) {
		return (findLine(lineNumber) != null);
	}

	/**
	 * Sets a line to be drawn only as the lines joining individual points.
	 *
	 * @param which
	 *            the line
	 */
	public void linesOnly(int which) {
		setLineType(which, Type.LINEONLY);
	}

	/**
	 * Part of the implementation of MouseMotionListener - overrides the super class (ChartPanel) implementation so that
	 * the mouse can be used to select a rectangle as well as for zooming.
	 *
	 * @param e
	 *            the mouse event which caused the call
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// If the rectangle dragger is not in operation then call the
		// super class method (to deal with any possible zooming) then
		// deal with magnifyingImage or magnifyingData.

		if (rd == null) {
			super.mouseDragged(e);
			if ((magnifyingImage || magnifyingData) && (e.getModifiers() & InputEvent.BUTTON3_MASK) == 0) {
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.setXORMode(dragColour);
				if (magnifyRectangle != null) {
					if (magnifyRectangleIsNew) {
						magnifyRectangleIsNew = false;
					} else {
						g2.fill(magnifyRectangle);
					}
				}
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
					magnifyWidth = e.getX() - magnifyXPoint;
					magnifyHeight = e.getY() - magnifyYPoint;
				}
				recalculateMagnifyRectangle(e);

				if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0) {
					magnifier.update(magnifyRectangle);
				}

				if (magnifyRectangle != null) {
					g2.fill(magnifyRectangle);
				}
				g2.dispose();
			}
		} else {
			rd.mouseDragged(e);
		}
	}

	/**
	 * Part of the implementation of MouseListener - overrides the super class (ChartPanel) implementation so that the
	 * mouse can be used to select a rectangle as well as for zooming.
	 *
	 * @param e
	 *            the mouse event which caused the call
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// Unless a rectangle is being dragged we just
		// want to call the super class method (which
		// deals with zooming)
		if (rd == null) {
			super.mousePressed(e);
			if (magnifyingImage || magnifyingData) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					magnifyRectangle = null;
					magnifyXPoint = e.getX();
					magnifyYPoint = e.getY();
				}
				if (!e.isPopupTrigger()) {
					Graphics2D g2 = (Graphics2D) getGraphics();
					g2.setXORMode(dragColour);
					if (magnifyRectangle != null) {
						g2.fill(magnifyRectangle);
					}
					g2.dispose();
				}
			}
		} else {
			rd.mousePressed(e);
		}
	}

	/**
	 * Part of the implementation of MouseListener. Overrides the super class (ChartPanel) implementation so that the
	 * mouse can be used to select a rectangle as well as for zooming.
	 *
	 * @see #mousePressed(java.awt.event.MouseEvent)
	 * @see #mouseDragged(java.awt.event.MouseEvent)
	 * @param e
	 *            the mouse event which caused the call
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// Unless a rectangle is being dragged we just
		// want to call the super class method (which
		// deals with zooming)
		if (rd == null) {
			super.mouseReleased(e);

			if (magnifyingImage || magnifyingData) {
				// If the button released is BUTTON1 then the rectangle for
				// magnification will have been resized.
				if (e.getButton() == MouseEvent.BUTTON1) {
					magnifyWidth = e.getX() - magnifyXPoint;
					magnifyHeight = e.getY() - magnifyYPoint;
					magnifyRectangleIsNew = true;
				}

				// If the button released is BUTTON2 then the rectangle will
				// have been being dragged around. Need to redraw in XOR mode
				// one
				// last time to remove rectangle from plot.
				else if (e.getButton() == MouseEvent.BUTTON2) {
					Graphics2D g2 = (Graphics2D) getGraphics();
					g2.setXORMode(dragColour);
					if (magnifyRectangle != null) {
						g2.fill(magnifyRectangle);
					}
					g2.dispose();
				}
				recalculateMagnifyRectangle(e);
				magnifier.update(magnifyRectangle);
			}
		} else {
			rd.mouseReleased(e);
		}
	}

	/**
	 * Part of the implementation of MouseListener - overrides the super class (ChartPanel) implementation but currently
	 * does nothing.
	 *
	 * @param e
	 *            the mouse event which caused the call
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
	}

	/**
	 * Recalculates the rectangle which should be magnified.
	 *
	 * @param e
	 *            the MouseEvent which triggered the recalculation
	 */
	private void recalculateMagnifyRectangle(MouseEvent e) {
		if (magnifyWidth == 0 || magnifyHeight == 0)
			return;

		Rectangle2D scaledDataArea = getScreenDataArea();

		double widthToUse;
		double heightToUse;
		double xToUse;
		double yToUse;

		// If magnifyWidth is positive then e.getX() is
		// the end of the rectangle so the start is (e.getX() - magnifyWidth ).
		// If magnifyWidth is negative then e.getX() is the start of a
		// rectangle with the opposite sign width. Similarly for y.

		if (magnifyWidth > 0) {
			xToUse = e.getX() - magnifyWidth;
			widthToUse = magnifyWidth;
		} else {
			xToUse = e.getX();
			widthToUse = -1.0 * magnifyWidth;
		}

		if (magnifyHeight > 0) {
			yToUse = e.getY() - magnifyHeight;
			heightToUse = magnifyHeight;
		} else {
			yToUse = e.getY();
			heightToUse = -1.0 * magnifyHeight;
		}

		// xToUse and yToUse now specify the top left of the rectangle. In order
		// to keep the magnified rectangle inside the data area the start point
		// must be inside a rectangle which is the scaledDataArea reduced in
		// width
		// and height by the width and height of the magnifyRectangle.

		Point2D revisedStartPoint = ShapeUtilities.getPointInRectangle(xToUse, yToUse, new Rectangle2D.Double(
				scaledDataArea.getMinX(), scaledDataArea.getMinY(), scaledDataArea.getWidth() - magnifyWidth,
				scaledDataArea.getHeight() - magnifyHeight));
		magnifyRectangle = new Rectangle2D.Double(revisedStartPoint.getX(), revisedStartPoint.getY(), widthToUse,
				heightToUse);
	}

	/**
	 * Sets the visibility of the Legend
	 *
	 * @param newValue
	 *            true or false
	 */
	@Override
	public void setLegendVisible(boolean newValue) {
		legendVisible = newValue;
		leftSeriesLegendItemsGetter.setVisible(legendVisible);
		rightSeriesLegendItemsGetter.setVisible(legendVisible);
		repaint();
	}

	/**
	 * Sets the colour used to draw the line - NB this also sets the colour used to draw the point markers since this is
	 * the behaviour most people will want - to use different colours each call of setLineColor must be followed by a
	 * call of setLineMarkerColor.
	 *
	 * @param which
	 *            the line
	 * @param color
	 *            a Color
	 */
	@Override
	public void setLineColor(int which, Color color) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			s.setPaint(color);
			s.setSymbolPaint(color);
			repaint();
		}
	}

	/**
	 * Gets the reference to a particular line
	 *
	 * @param which
	 *            The number of the line which is required
	 * @return A SimpleXYSeries which is the line specified
	 */
	private SimpleXYSeries getLine(int which) {
		return findLine(which);
	}

	/**
	 * Finds a line from left or right series collections
	 *
	 * @param which
	 *            the line number
	 * @return the SimpleXYSeries which represents the line (or null)
	 */
	private SimpleXYSeries findLine(int which) {
		return seriesStore.get(which);
	}

	/**
	 * Sets the Marker for a line.
	 *
	 * @param which
	 * @param marker
	 */
	@Override
	public void setLineMarker(int which, Marker marker) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			s.setMarker(marker);
			repaint();
		}
	}

	/**
	 * Sets the name of a line - this is what will appear next to it in the legend.
	 *
	 * @param which
	 *            the line
	 * @param name
	 *            the name
	 */
	public void setLineName(int which, String name) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			s.setName(name);
			repaint();
		}
	}

	/**
	 * Set the line type (points, points and line, line).
	 *
	 * @param which
	 *            the line
	 * @param type
	 *            the type
	 */

	void setLineType(int which, Type type) {
		// This should be handled inside SimpleXYSeries
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			s.setType(type);
			repaint();
		}
	}

	/**
	 * Set the line type for all lines
	 * @param type
	 */
	@Override
	public void setLineType(Type type){
		for(SimpleXYSeries s : seriesStore.values() ){
			s.setType(type);
		}
		repaint();
	}
	/**
	 * Sets the line type.
	 *
	 * @param which
	 *            the line
	 * @param name
	 *            the name of the type to use, allowed values are: "lineonly", "pointsonly", "lineandpoints"
	 */
	public void setLineType(int which, String name) {
		setLineType(which, Type.fromString(name));
	}

	/**
	 * Sets whether or not line is visible.
	 *
	 * @param which
	 *            the line
	 * @param visibility
	 *            true to be visible
	 */
	@Override
	public void setLineVisibility(int which, boolean visibility) {
		SimpleXYSeries s;
		if ((s = findLine(which)) != null) {
			boolean isVisible = s.isVisible();
			if( isVisible == visibility)
				return;
			if(s.getAxis() == RIGHTYAXIS && rightSeriesCollection != null){
				if(isVisible){
					rightSeriesCollection.removeSeries(s);
				}
				s.setVisible(visibility);
				if(visibility){
					rightSeriesCollection.addSeries(s);
				}

			} else {
				if(isVisible){
					leftSeriesCollection.removeSeries(s);
				}
				s.setVisible(visibility);
				if(visibility){
					leftSeriesCollection.addSeries(s);
				}
			}
			repaint();
		}
	}

	/**
	 * Sets the title of the plot.
	 *
	 * @param title
	 *            the title to use
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;

		// See comments in setTitleVisible
		if (titleVisible) {
			getChart().setTitle(title);
		}
	}

	/**
	 * Sets the visibility of the title of the plot.
	 *
	 * @param newValue
	 */
	public void setTitleVisible(boolean newValue) {
		titleVisible = newValue;

		// The only way to get the chart not to draw the title
		// is to set it to null. The title text and title
		// visibility are stored here in SimplePlot so we
		// can do this. It could be made more elegant by adding an
		// AbstractTitle.INVISIBLE title position.
		if (titleVisible) {
			getChart().setTitle(title);
		} else {
			getChart().setTitle((String) null);
		}
	}

	/**
	 * Toggles display of pointer position.
	 *
	 * @param toggle
	 *            true to display, false to hide
	 */
	public void setTrackPointer(boolean toggle) {
		if (pointerTracking != toggle) {
			if (toggle) {
				mpt.switchOn();
			} else {
				mpt.switchOff();
			}
			pointerTracking = toggle;
		}
	}

	/**
	 * Sets the x axis autoscaling.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setXAxisAutoScaling(boolean newValue) {
		linearXAxis.setAutoRange(newValue);
		logarithmicXAxis.setAutoRange(newValue);
	}

	/**
	 * Sets whether the x axis autoscaling always includes zero.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setXAxisAutoScalingIncludesZero(boolean newValue) {
		linearXAxis.setAutoRangeIncludesZero(newValue);
		logarithmicXAxis.setAutoRangeIncludesZero(newValue);
	}

	/**
	 * Sets the x axis label.
	 *
	 * @param label
	 *            the label
	 */
	@Override
	public void setXAxisLabel(String label) {
		linearXAxis.setLabel(label);
		logarithmicXAxis.setLabel(label);
	}

	/**
	 * Sets the x axis limits.
	 *
	 * @param xmin
	 *            the minimum x value
	 * @param xmax
	 *            the maximum x value
	 */
	public void setXAxisLimits(double xmin, double xmax) {
		linearXAxis.setRange(new Range(xmin, xmax));
		logarithmicXAxis.setRange(new Range(xmin, xmax));
	}

	/**
	 * Sets the y axis autoscaling.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setYAxisAutoScaling(boolean newValue) {
		linearYAxis.setAutoRange(newValue);
		logarithmicYAxis.setAutoRange(newValue);
	}

	/**
	 * Sets whehter the y axis autoscaling always includes zero.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setYAxisAutoScalingIncludesZero(boolean newValue) {
		linearYAxis.setAutoRangeIncludesZero(newValue);
		logarithmicYAxis.setAutoRangeIncludesZero(newValue);
	}

	/**
	 * Sets the y axis label.
	 *
	 * @param label
	 *            the label
	 */
	@Override
	public void setYAxisLabel(String label) {
		linearYAxis.setLabel(label);
		logarithmicYAxis.setLabel(label);
	}

	/**
	 * Sets the y axis limits.
	 *
	 * @param ymin
	 *            the minimum y value
	 * @param ymax
	 *            the maximum y value
	 */
	public void setYAxisLimits(double ymin, double ymax) {
		linearYAxis.setRange(new Range(ymin, ymax));
		logarithmicYAxis.setRange(new Range(ymin, ymax));
	}

	/**
	 * Sets the second y axis autoscaling.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setYAxisTwoAutoScaling(boolean newValue) {
		linearYAxisTwo.setAutoRange(newValue);
		logarithmicYAxisTwo.setAutoRange(newValue);
	}

	/**
	 * Sets whehter the second y axis autoscaling always includes zero.
	 *
	 * @param newValue
	 *            the new value (true or false)
	 */
	public void setYAxisTwoAutoScalingIncludesZero(boolean newValue) {
		linearYAxisTwo.setAutoRangeIncludesZero(newValue);
		logarithmicYAxisTwo.setAutoRangeIncludesZero(newValue);
	}

	/**
	 * Sets the second y axis label.
	 *
	 * @param label
	 *            the label
	 */
	public void setYAxisTwoLabel(String label) {
		linearYAxisTwo.setLabel(label);
		logarithmicYAxisTwo.setLabel(label);
	}

	/**
	 * Gets the current CoordinateFormatter.
	 *
	 * @return current value of coordinateFormatter
	 */
	public CoordinateFormatter getCoordinateFormatter() {
		return coordinateFormatter;
	}

	/**
	 * Sets the current CoordinateFormatter.
	 *
	 * @param coordinateFormatter
	 *            the CoordinateFormatter to set as current.
	 */
	public void setCoordinateFormatter(CoordinateFormatter coordinateFormatter) {
		this.coordinateFormatter = coordinateFormatter;
	}

	/**
	 * Returns the current axis limits.
	 *
	 * @return AxisLimits
	 */
	private AxisLimits getAxisLimits() {
		return new AxisLimits(isXAxisAutoRange(), isYAxisAutoRange(), getXAxisMin(), getXAxisMax(), getYAxisMin(),
				getYAxisMax(), (linearYAxisTwo != null ? linearYAxisTwo.isAutoRange() : false),
				(linearYAxisTwo != null ? linearYAxisTwo.getLowerBound() : 0.0),
				(linearYAxisTwo != null ? linearYAxisTwo.getUpperBound() : 0.0));
	}

	/**
	 * Overrides the super class method in order to deal with remembering the previous axis limits (which are pushed
	 * onto a stack).
	 *
	 * @param selection
	 *            is the Rectangle2D selected for zooming but is only actually used in the super class
	 */
	@Override
	public void zoom(Rectangle2D selection) {
		// Push current AxisLimits onto stack
		zoomStack.push(getAxisLimits());

		// This flag means that a zoom is currently being calculated, Compare
		// the
		// zooming flag which means that zooming is actually allowed.
		currentlyDoingAZoom = true;
		// Get the super class to actually zoom
		super.zoom(selection);
		currentlyDoingAZoom = false;

		// Enable unZoomButton
		unZoomButton.setEnabled(!zoomStack.isEmpty());
	}

	/**
	 * Sets the axis limits (both axes).
	 *
	 * @param newLimits
	 *            an AxisLimits object containing the information
	 */
	public void setAxisLimits(AxisLimits newLimits) {
		if (newLimits.isXAuto()) {
			linearXAxis.setAutoRange(true);
			logarithmicXAxis.setAutoRange(true);
		} else {
			linearXAxis.setLowerBound(newLimits.getXMin());
			linearXAxis.setUpperBound(newLimits.getXMax());
			logarithmicXAxis.setLowerBound(newLimits.getXMin());
			logarithmicXAxis.setUpperBound(newLimits.getXMax());
		}
		if (newLimits.isYAuto()) {
			linearYAxis.setAutoRange(true);
			logarithmicYAxis.setAutoRange(true);
		} else {
			linearYAxis.setLowerBound(newLimits.getYMin());
			linearYAxis.setUpperBound(newLimits.getYMax());
			logarithmicYAxis.setLowerBound(newLimits.getYMin());
			logarithmicYAxis.setUpperBound(newLimits.getYMax());
		}

		// If linearYAxisTwo exists then logarithmicYAxisTwo will too.
		if (linearYAxisTwo != null) {
			if (newLimits.isYTwoAuto()) {
				linearYAxisTwo.setAutoRange(true);
				logarithmicYAxisTwo.setAutoRange(true);
			} else {
				linearYAxisTwo.setLowerBound(newLimits.getYTwoMin());
				linearYAxisTwo.setUpperBound(newLimits.getYTwoMax());
				logarithmicYAxisTwo.setLowerBound(newLimits.getYTwoMin());
				logarithmicYAxisTwo.setUpperBound(newLimits.getYTwoMax());
			}
		}
	}

	/**
	 * Determines if X (Domain) axis has autoRange set.
	 *
	 * @return autoRange true or false
	 */
	public boolean isXAxisAutoRange() {

		return xYPlot.getDomainAxis().isAutoRange();
	}

	/**
	 * Determines if Y (Range) axis has autoRange set.
	 *
	 * @return autoRange true or false
	 */
	public boolean isYAxisAutoRange() {
		return xYPlot.getRangeAxis().isAutoRange();
	}


	/**
	 * Function that is called when the new save as option on the right click menu is clicked.
	 */
	private void saveAs() {
		try {
			if(fc == null)
				fc = new JFileChooser();
			fc.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}
					String path = f.getPath();
					if (path.endsWith(".png")) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "PNG files";
				}

			});
			int returnValue = fc.showSaveDialog(this);
			logger.debug("check the save dialogue");
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				BufferedImage im = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
				super.paintComponent(im.createGraphics());
				ImageIO.write(im, "png", fc.getSelectedFile());
			}
		} catch (Exception e) {
			logger.error("Error in Save As",e);
		}
	}

	/**
	 * Overrides super class method in order to paint image for magnfication.
	 *
	 * @param g
	 *            the Graphics into which this should paint itself
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		coordinateFormatter.setXDigits(linearXAxis.getDigits());
		coordinateFormatter.setYDigits(linearYAxis.getDigits());
	}

	private void initLogAxis(LogarithmicAxis axis, boolean autoRange, NumberFormat formatter ){
		initAxis( axis, autoRange, formatter);
		axis.setAllowNegativesFlag(true);
	}
	private void initAxis(NumberAxis axis, boolean autoRange, NumberFormat formatter ){
		axis.setAutoRange(autoRange);
		axis.setLowerMargin(0.);
		axis.setUpperMargin(0.);
		axis.setAutoRangeIncludesZero(false);
		axis.setNumberFormatOverride(formatter);
	}
	/**
	 * @return Returns the zooming.
	 */
	private boolean isZooming() {
		return zooming;
	}

	/**
	 * Sets the value of zooming.
	 *
	 * @param zooming
	 *            The new value.
	 */
	@Override
	public void setZooming(boolean zooming) {
		this.zooming = zooming;
		zoomButton.setSelected(zooming);

		// This actually enables or disables zooming in the super class
		setMouseZoomable(zooming, false);

		// Stop the other buttons being used while zooming is true
		magnifyDataButton.setEnabled(!zooming);
		magnifier = null;

	}

	/**
	 * @return Returns the magnifyingData.
	 */
	private boolean isMagnifyingData() {
		return magnifyingData;
	}

	/**
	 * Sets the magnfiyingData flag - this magnfying mode uses a window containing another plot which has its axis
	 * limits set to display the magnfied data.
	 *
	 * @param magnifyingData
	 *            The new value.
	 */
	private void setMagnifyingData(boolean magnifyingData) {
		this.magnifyingData = magnifyingData;
		zoomButton.setEnabled(!magnifyingData);
		dataMagnifierWindow.setVisible(magnifyingData);

		if (magnifyingData) {
			dragColour = Color.red;
			magnifier = dataMagnifierWindow;
			magnifier.setSize(getSize());
			magnifier.update(magnifyRectangle);
		} else
			magnifier = null;
	}

	/**
	 * @return Returns the magnifyRectangle.
	 */
	public Rectangle2D getMagnifyRectangle() {
		return magnifyRectangle;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the x value at which a line has its maximum y value.
	 *
	 * @param text
	 *            the name of the line
	 * @return the x value corresponding to the maximum y value of this line
	 */
	public double getLineXValueOfPeak(String text) {

		return leftSeriesCollection.getSeriesXValueOfPeak(text);
	}

	/**
	 * Temporary main for speed testing purposes only
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame jf = new JFrame();
		final SimplePlot sp = new SimplePlot();
		jf.getContentPane().add(sp);
		jf.pack();
		jf.setVisible(true);

		sp.setXAxisAutoScaling(false);
		sp.setXAxisLimits(0.0, 300.0);
		sp.setYAxisLimits(0.0, 1.0);
		sp.initializeLine(0);
		final double x[] = new double[1000];
		final double y[] = new double[1000];
		final double[] numbers = new double[10];
		final double[] timesOne = new double[10];
		final double[] timesTwo = new double[10];
		for (int i = 0; i < 1000; i++)
			x[i] = i;

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					long timeOne;
					long timeTwo;
					for (int j = 0; j < 10; j++) {
						sp.initializeLine(j);
						timeOne = System.currentTimeMillis();
						for (int k = 0; k < 300; k++) {
							y[k] = Math.random();
							sp.addPointToLine(j, x[k], y[k]);
						}
						timeTwo = System.currentTimeMillis();
						numbers[j] = j;
						timesOne[j] = timeTwo - timeOne;
						System.out.println("LINE " + j + " " + timesOne[j]);
					}

				}
			});

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					sp.deleteAllLines();

				}
			});

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					long timeOne;
					long timeTwo;
					// SimpleXYSeries[] lines = new SimpleXYSeries[10];
					// sp.setBatchingRepaints(true);

					for (int j = 0; j < 10; j++) {
						sp.initializeLine(j);
						// lines[j] = sp.getActualLine(j);
						timeOne = System.currentTimeMillis();
						for (int k = 0; k < 300; k++) {
							y[k] = Math.random();
						}
						// lines[j].setPoints(x, y);
						sp.setLinePoints(j, x, y);
						timeTwo = System.currentTimeMillis();
						timesTwo[j] = timeTwo - timeOne;
						System.out.println("LINE " + j + " " + (timeTwo - timeOne));
					}

				}
			});

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					sp.deleteAllLines();
				}
			});

			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					sp.initializeLine(0);
					sp.setLinePoints(0, numbers, timesOne);

					sp.initializeLine(1);
					sp.setLinePoints(1, numbers, timesTwo);
				}
			});

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This overrides the method in ChartPanel which seems to behave slightly differently. The replacement may be
	 * unecessary -investigate.
	 */
	@Override
	public void createChartPrintJob() {
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		PageFormat pageFormat = printerJob.defaultPage();
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		printerJob.setPrintable(this, pageFormat);
		try {
			if (printerJob.printDialog()) {
				printerJob.print();
			}
		} catch (PrinterException pe) {
			logger.error("Caught PrinterException: " + pe.getMessage());
		}
	}

	/**
	 * Adds an item to the popup menu.
	 *
	 * @param jmi
	 *            the new menu item
	 */
	public void addPopupMenuItem(JComponent jmi) {
		getPopupMenu().add(jmi);
	}

	/**
	 * Set a SimpleValueTransformer for x coordinates - the SimpleValueTransformer will allow x values to be transformed
	 * on output only (not affecting the actual data)- this is useful, for example, in allowing a choice of units for x
	 * axis displays.
	 *
	 * @param xValueTransformer
	 *            the SimpleValueTransformer to use
	 */
	public void setXValueTransformer(SimpleValueTransformer xValueTransformer) {
		// The easiest way to implement this was to push the responsibility
		// down.
		// Both the Renderer (which actually draws the data) and the various
		// SeriesCollections (which keep the axes correct) need to know about
		// the Transformer.
		((SimpleXYItemRenderer) xYPlot.getRenderer()).setXValueTransformer(xValueTransformer);
		leftSeriesCollection.setXValueTransformer(xValueTransformer);
		if (rightSeriesCollection != null) {
			rightSeriesCollection.setXValueTransformer(xValueTransformer);
			((SimpleXYItemRenderer) xYPlot.getRenderer(secondaryDataSetIndex)).setXValueTransformer(xValueTransformer);
		}
	}


	/**
	 * Returns the x axis number format.
	 *
	 * @return xAxisNumberFormat
	 */
	@Override
	public NumberFormat getXAxisNumberFormat() {
		return xAxisNumberFormat;
	}

	/**
	 * Function to set up the X axis with scientific notation.
	 */
	@Override
	public void setScientificXAxis() {
		setScientificXAxis(new DecimalFormat("0.000E00"));
	}

	/**
	 * Function to set up the X axis with scientific notation. With the specified number Format.
	 *
	 * @param inFormat
	 *            the new formating for the X axis
	 */
	public void setScientificXAxis(NumberFormat inFormat) {
		xAxisNumberFormat = inFormat;
		linearXAxis.setNumberFormatOverride(xAxisNumberFormat);
		logarithmicXAxis.setNumberFormatOverride(xAxisNumberFormat);
	}

	/**
	 * Function to set up the Y axis with scientific notation.
	 */
	@Override
	public void setScientificYAxis() {
		setScientificYAxis(new DecimalFormat("0.0000E00"));
	}

	/**
	 * Function to set up the Y axis with scientific notation. With the inputed numberformat
	 *
	 * @param inFormat
	 *            the new formating for the Y axis
	 */
	public void setScientificYAxis(NumberFormat inFormat) {
		yAxisNumberFormat = inFormat;
		linearYAxis.setNumberFormatOverride(yAxisNumberFormat);
		logarithmicYAxis.setNumberFormatOverride(yAxisNumberFormat);

	}

	/**
	 * Returns the y axis number format.
	 *
	 * @return yAxisNumberFormat
	 */
	@Override
	public NumberFormat getYAxisNumberFormat() {
		return yAxisNumberFormat;
	}

	/**
	 * Returns the second y axis number format.
	 *
	 * @return yAxisTwoNumberFormat
	 */
	public NumberFormat getYAxisTwoNumberFormat() {
		return yAxisTwoNumberFormat;
	}

	/**
	 * specifies whether the X axis ticks are vertical or horizontal.
	 *
	 * @param value
	 */
	@Override
	public void setVerticalXAxisTicks(boolean value) {
		linearXAxis.setVerticalTickLabels(value);
		logarithmicXAxis.setVerticalTickLabels(value);
	}

	/**
	 * @return true if X axis is logarithmic
	 */
	public boolean isXAxisLogarithmic() {
		return xAxisLogarithmic;
	}

	/**
	 * @return true if first (left) Y axis is logarithmic
	 */
	public boolean isYAxisLogarithmic() {
		return yAxisLogarithmic;
	}

	/**
	 * Sets whether or not x axis is logarithmic.
	 *
	 * @param xAxisLogarithmic
	 *            true makes the axis logarithmic
	 */
	public void setXAxisLogarithmic(boolean xAxisLogarithmic) {
		this.xAxisLogarithmic = xAxisLogarithmic;
		if (xAxisLogarithmic) {
			xYPlot.setDomainAxis(logarithmicXAxis);
			xLogLinButton.setText("Linear X axis");
		} else {
			xYPlot.setDomainAxis(linearXAxis);
			xLogLinButton.setText("Logarithmic X axis");
		}
	}

	/**
	 * Sets whether or not first (left) y axis is logarithmic.
	 *
	 * @param yAxisLogarithmic
	 *            true makes the axis logarithmic
	 */
	public void setYAxisLogarithmic(boolean yAxisLogarithmic) {
		this.yAxisLogarithmic = yAxisLogarithmic;
		if (yAxisLogarithmic) {
			xYPlot.setRangeAxis(logarithmicYAxis);
			yLogLinButton.setText("Linear Y axis");
		} else {
			xYPlot.setRangeAxis(linearYAxis);
			yLogLinButton.setText("Logarithmic Y axis");
		}
	}

	/**
	 * @return true if second (right) Y axis is logarithmic
	 */
	public boolean isYAxisTwoLogarithmic() {
		return yAxisTwoLogarithmic;
	}

	/**
	 * Sets whether or not second (right) y axis is logarithmic.
	 *
	 * @param yAxisTwoLogarithmic
	 *            true makes the axis logarithmic
	 */
	public void setYAxisTwoLogarithmic(boolean yAxisTwoLogarithmic) {
		this.yAxisTwoLogarithmic = yAxisTwoLogarithmic;
		if (yAxisTwoLogarithmic) {
			xYPlot.setRangeAxis(secondaryAxisNumber, logarithmicYAxisTwo);
			y2LogLinButton.setText("Linear Y2 axis");
		} else {
			xYPlot.setRangeAxis(secondaryAxisNumber, linearYAxisTwo);
			y2LogLinButton.setText("Logarithmic Y2 axis");
		}
	}

	/**
	 * Sets the label for the dependent x axis.
	 *
	 * @param string
	 *            the label
	 */
	public void setDependentXAxisLabel(String string) {
		dependentXAxis.setLabel(string);
	}

	/**
	 * Sets the label for the dependent y axis.
	 *
	 * @param string
	 *            the label
	 */
	public void setDependentYAxisLabel(String string) {
		dependentYAxis.setLabel(string);
	}

	/**
	 * Specifies weather the graph is in batching mode or not
	 *
	 * @return true if the graph is batching.
	 */
	public boolean isBatching() {
		return batching;
	}

	/**
	 * Sets whether the graph is batching or not. When the graph is batching, any new data sent to the graph is not
	 * displayed, therefore speeding up the process. Once batching is set to false, all the data put into the graph is
	 * displayed in one redraw.
	 *
	 * @param batching
	 *            sets the batching flag to true or false
	 */
	public void setBatching(boolean batching) {
		this.batching = batching;
		if (leftSeriesCollection != null) {
			leftSeriesCollection.setBatching(batching);
		}

		if (rightSeriesCollection != null) {
			rightSeriesCollection.setBatching(batching);
		}
		if (!batching && history != null) {
			if (seriesStore.size() == 1)
				historyRepopulate();
		}
	}

	private Range leftRangeBounds = null;
	private Range rightRangeBounds = null;

	/**
	 * @param collection
	 * @param includeInterval
	 * @return range for the range axis Y
	 */
	Range getRangeBounds(SimpleXYSeriesCollection collection, @SuppressWarnings("unused") boolean includeInterval) {
		if (!isTurboMode())
			return null;

		Range domainBounds = null;
		Range rangeBounds = null;
		if ( collection == leftSeriesCollection ){
			domainBounds = leftDomainBounds;
			rangeBounds = leftRangeBounds;
		} else if ( collection == rightSeriesCollection ){
			domainBounds = rightDomainBounds;
			rangeBounds = rightRangeBounds;
		}
		if( rangeBounds != null)
			return rangeBounds;

		Double min = Double.POSITIVE_INFINITY;
		Double max = Double.NEGATIVE_INFINITY;

		if (isStripMode()) {
			synchronized (linesChanged) {
				if (linesChanged.size() == 0)
					return null;
				Iterator<Integer> iter = linesChanged.iterator();
				while (iter.hasNext()) {
					SimpleXYSeries sxys = collection.find(iter.next());
					// is it in this collection?
					if (sxys != null && sxys.isVisible()) {

						Double sxys_min, sxys_max;
						Double sxys_Xmax = sxys.getMaxX();
						if (Double.isInfinite(sxys_Xmax))
							return null;
						/*
						 * create range in data units over which we want to get the y min and max
						 */
						SimpleValueTransformer valTrans = collection.getXValueTransformer();
						Double maxXTransformed = valTrans.transformValue(sxys_Xmax);
						double minX = valTrans.transformValueBack(maxXTransformed - stripWidth);
						Double[] extents = sxys.getBounds(new Range(minX, sxys_Xmax));
						sxys_min = extents[2];
						sxys_max = extents[3];
						if (Double.isInfinite(sxys_min) || Double.isInfinite(sxys_max))
							return null;
						min = Math.min(min, sxys_min);
						max = Math.max(max, sxys_max);
					}

				}
			}
		} else {
			for( Object obj : collection.getSeries()){
				if(obj != null && obj instanceof SimpleXYSeries){
					SimpleXYSeries sxys = (SimpleXYSeries)obj;
					if (sxys.isVisible()) {
						if( domainBounds == null ){
							double sxys_min, sxys_max;
							sxys_min = sxys.getMinY();
							sxys_max = sxys.getMaxY();
							min = Math.min(min, sxys_min);
							max = Math.max(max, sxys_max);
						} else {
							double sxys_min, sxys_max;
							Double[] extents = sxys.getBounds(domainBounds);
							sxys_min = extents[2];
							sxys_max = extents[3];
							if (Double.isInfinite(sxys_min) || Double.isInfinite(sxys_max))
								return null;
							min = Math.min(min, sxys_min);
							max = Math.max(max, sxys_max);
						}
					}
				}
			}
		}
		if (Double.isInfinite(min) || Double.isInfinite(max))
			return null;

		Range newRange = new Range(min, max);
		return newRange;
	}

	/*
	 * The following are in transformed X values
	 */
	private Range lastDomainBoundsLeft = null;
	private Range leftDomainBounds = null;
	private Range rightDomainBounds = null;
	private Double stripWidth=null; //width in domain axis from last point value to min val


	/**
	 * @param collection
	 * @param includeInterval
	 * @return range for the domain axis X
	 */
	Range getDomainBounds(SimpleXYSeriesCollection collection,
			@SuppressWarnings("unused") boolean includeInterval) {
		Double min = Double.POSITIVE_INFINITY;
		Double max = Double.NEGATIVE_INFINITY;
		if (!isTurboMode())
			return null;

		if ( collection == leftSeriesCollection && leftDomainBounds != null )
			return leftDomainBounds;
		if ( collection == rightSeriesCollection && rightDomainBounds != null )
			return rightDomainBounds;

		if (isStripMode()) {
			/*
			 * In stripMode get max of changed lines - min will be the max minus the stripWidth
			 */
			synchronized (linesChanged) {
				if (linesChanged.size() == 0)
					return null;
				Iterator<Integer> iter = linesChanged.iterator();
				while (iter.hasNext()) {
					SimpleXYSeries sxys = collection.find(iter.next());
					// is it in this collection?
					if (sxys != null && sxys.isVisible()) {
						double sxys_max;
						sxys_max = sxys.getMaxX();
						max = Math.max(max, sxys_max);
					}

				}
				min = max; //we remove the stripWidth later as stripWidth is in final units after transformation
			}
		} else {
			/*
			 * go through all visible lines are get min and max
			 */
			for( Object obj : collection.getSeries()){
				if(obj != null && obj instanceof SimpleXYSeries){
					SimpleXYSeries sxys = (SimpleXYSeries)obj;
					if (sxys.isVisible()) {
						double sxys_min, sxys_max;
						sxys_min = sxys.getMinX();
						sxys_max = sxys.getMaxX();
						min = Math.min(min, sxys_min);
						max = Math.max(max, sxys_max);
					}
				}
			}
		}
		if (Double.isInfinite(min) || Double.isInfinite(max))
			return null;
		SimpleValueTransformer valTrans = collection.getXValueTransformer();
		Double maxTransformed = valTrans.transformValue(max);
		Double minTransformed = stripWidth != null ? maxTransformed - stripWidth : valTrans.transformValue(min);
		Range newRange = new Range(minTransformed, maxTransformed);

		if ( collection == leftSeriesCollection )
			lastDomainBoundsLeft = newRange;
		return newRange;
	}

	/*
	 * Depending on the mode of the plot we will either fire a DataSet change event or a chart.fireChange event which
	 * simply causes a refresh.
	 */
	@Override
	public void onUpdate(boolean force) {
		if(force){ //need to force a reassessment of limits so act as if data has changed
			repaint();
			SimpleXYSeries sxys = findLine(lastLineAdded);
			if ( sxys != null){
				sxys.fireSeriesChanged();
			}
			return;
		}
		synchronized (linesChanged) {
			if (linesChanged.size() == 0)
				return;
			Iterator<Integer> iter = linesChanged.iterator();
			while (iter.hasNext()) {
				SimpleXYSeries sxys = findLine(iter.next());
				if (sxys != null) {
					sxys.fireSeriesChanged();
				}
			}
		}
	}

	/**
	 * gives opportunity to plot to archive date not visible to reduce memory usage
	 * @param all
	 *
	 * @throws IOException
	 */
	@Override
	public void archive(boolean all, String archiveFolder) throws IOException {
		if(isTurboMode()){
			Iterator<SimpleXYSeries> iter = seriesStore.values().iterator();
			while(iter.hasNext()){
				SimpleXYSeries sxys = iter.next();
				if( all || !sxys.isVisible())
					sxys.archive();
			}
		}
	}

	/**
	 */
	@Override
	public void unArchive() {
		if(isTurboMode()){
			for( Object obj : leftSeriesCollection.getSeries()){
				((SimpleXYSeries)obj).unArchive();
			}
			if( rightSeriesCollection != null){
				for( Object obj : rightSeriesCollection.getSeries()){
					((SimpleXYSeries)obj).unArchive();
				}
			}
		}

	}

	/**
	 * @param leftRangeBounds
	 */
	@Override
	public void setLeftRangeBounds(Range leftRangeBounds) {
		this.leftRangeBounds = leftRangeBounds;
	}
	/**
	 * @param rightRangeBounds
	 */
	@Override
	public void setRightRangeBounds(Range rightRangeBounds) {
		this.rightRangeBounds = rightRangeBounds;
	}
	/**
	 * @param domainBounds  The same value is used for both left and rigth series
	 */
	@Override
	public void setDomainBounds(Range domainBounds) {
		this.leftDomainBounds = domainBounds;
		this.rightDomainBounds = domainBounds;
		xLimitsButton.setSelected(domainBounds != null);
		if(domainBounds != null){
			if(!zoomStack.isEmpty()){
				unZoom();
			}
			setStripWidth(null);
		}
		onUpdate(true);
	}

	/**
	 *
	 */
	@Override
	public void dispose(){
	}

	@Override
	protected void finalize() throws Throwable {
		logger.info("SimplePlot.finalize");
		super.finalize();
	}

	@Override
	public void copySettings(XYDataHandler other) {
		setScientificXAxis(other.getXAxisNumberFormat());
		setScientificYAxis(other.getYAxisNumberFormat());
		setDomainBounds(other.getLeftDomainBounds());
		setStripWidth(other.getStripWidth());
	}

	@Override
	public Range getLeftDomainBounds() {
		return leftDomainBounds;
	}


	@Override
	public Color getLineColor(int which) {
		return (Color) getLine(which).getPaint();
	}

	@Override
	public Marker getLineMarker(int which) {
		return getLine(which).getMarker();
	}

	@Override
	public Double getStripWidth() {
		return stripWidth;
	}

	@Override
	public void setsPointsForLine(int which, DoubleDataset xData, DoubleDataset yData) {
		//no used in Swing
	}

}


