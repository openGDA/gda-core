/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.client.UIHelper.showError;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_CONFIGURE;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_CONFIGURE_SHELL_TITLE;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_CONFIGURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_ERROR_CREATING_FOCUS_POINT;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_ERROR_FFT;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_ERROR_SNAPSHOT;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_SHOW_AF_POINTS;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_SHOW_AF_POINTS_TP;
import static uk.ac.gda.ui.tool.ClientMessages.AUTOFOCUS_START_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.getImage;
import static uk.ac.gda.ui.tool.images.ClientImages.TARGET;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.operations.image.FourierTransformImageOperation;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * An extension to {@link LiveStreamView} to edit a series of autofocus points on the view and use these to focus the
 * image.
 */
public class LiveStreamViewCameraControlsAutofocus implements LiveStreamViewCameraControlsExtension {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsAutofocus.class);

	private static final Color COLOUR_GREY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	private static final Color COLOUR_RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	/**
	 * Secondary ID of the current {@link LiveStreamView}
	 */
	private final String secondaryId;

	/**
	 * Points available on the view for focusing, keyed by the name of the point.<br>
	 * The user will typically choose the point(s) closest to the important areas of the sample.<br>
	 * Points can be moved & resized in the stream view.
	 */
	private final Map<String, FocusPoint> focusPoints;

	/**
	 * Check box to show/hide autofocus points
	 */
	private Button showAfPointCheck;

	/**
	 * Reference to the associated {@link LiveStreamView}<br>
	 * This is required to get a snapshot image.
	 */
	private LiveStreamView liveStreamView;

	/**
	 * Indicates whether sample rotation is permitted
	 */
	private boolean allowSampleRotation;

	public LiveStreamViewCameraControlsAutofocus(String secondaryId, List<FocusPoint> focusPoints) {
		this.secondaryId = secondaryId;

		// Store focus points, keyed by name
		this.focusPoints = focusPoints.stream().collect(Collectors.toUnmodifiableMap(FocusPoint::getName, Function.identity()));
	}

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		final IViewReference viewReference = getViewReference();
		if (viewReference == null) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error opening view", "No view found with id " + secondaryId);
			return;
		}
		liveStreamView = (LiveStreamView) viewReference.getView(false);

		final Composite autofocusComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(autofocusComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(autofocusComposite);

		// Check box to show/hide autofocus points
		showAfPointCheck = createClientButton(autofocusComposite, SWT.CHECK, AUTOFOCUS_SHOW_AF_POINTS, AUTOFOCUS_SHOW_AF_POINTS_TP);
		showAfPointCheck.addSelectionListener(widgetSelectedAdapter(e -> drawAfPoints()));

		// Button to show configuration dialog, allowing user to activate/deactivate AF points
		final Button configButton = createClientButton(autofocusComposite, SWT.PUSH, AUTOFOCUS_CONFIGURE, AUTOFOCUS_CONFIGURE_TP);
		configButton.addSelectionListener(widgetSelectedAdapter(e -> configAutofocus()));

		// Button to start AF procedure
		final Image targetImage = getImage(TARGET);
		final Button autofocusButton = new Button(autofocusComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(autofocusButton);
		autofocusButton.setImage(targetImage);
		autofocusButton.setToolTipText(getMessage(AUTOFOCUS_START_TP));
		autofocusButton.addSelectionListener(widgetSelectedAdapter(e -> runAutofocus()));
		targetImage.dispose();
	}

	// Find a view (of whatever type) with the given secondary id + stream type suffix
	private IViewReference getViewReference() {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (StreamType streamType : StreamType.values()) {
			final String secondaryIdWithSuffix = String.format("%s#%s", secondaryId, streamType.name());
			final IViewReference viewReference = activePage.findViewReference(LiveStreamView.ID, secondaryIdWithSuffix);
			if (viewReference != null) {
				return viewReference;
			}
		}
		return null;
	}

	/**
	 * Show/hide the ROIs overlaying the live stream, depending on the state of {@link #showAfPointCheck}
	 */
	private void drawAfPoints() {
		final IPlottingSystem<Composite> plotter = liveStreamView.getPlottingSystem();

		// Clear any existing ROIs on the plot
		plotter.clearRegions();
		if (!showAfPointCheck.getSelection()) {
			return;
		}

		// Show any configured ROIs
		focusPoints.values().stream()
			.filter(FocusPoint::isActive)
			.forEach(point -> drawAfPoint(point, plotter));
	}

	private void drawAfPoint(FocusPoint point, IPlottingSystem<Composite> plotter) {
		final RectangularROI focusRoi = point.toRectangularROI();
		try {
			focusRoi.setPlot(true); // ensure ROI is propagated to NX detector ROI plugin

			// make new region
			final IRegion plotRegion = plotter.createRegion(point.getName(), RegionType.BOX);

			// add roi to region
			plotRegion.setROI(focusRoi);

			// add region to plotter
			plotter.addRegion(plotRegion);
		} catch (Exception e) {
			final String message = getMessage(AUTOFOCUS_ERROR_CREATING_FOCUS_POINT) + " " + point.getName();
			showError(message, e, logger);
		}
	}

	/**
	 * Show {@link ConfigureDialog} to allow the user to choose which AF points are active
	 */
	private void configAutofocus() {
		// Update focusPoints in case the user has moved/resized any points
		final IPlottingSystem<Composite> plotter = liveStreamView.getPlottingSystem();
		plotter.getRegions(RegionType.BOX).stream()
			.map(r -> (RectangularROI) r.getROI())
			.forEach(roi -> {
				final FocusPoint point = focusPoints.get(roi.getName());
				if (point == null) return;
				final double[] mids = roi.getMidPoint();
				final double[] lengths = roi.getLengths();
				point.setxCentre(mids[0]);
				point.setyCentre(mids[1]);
				point.setxSize(lengths[0]);
				point.setySize(lengths[1]);
			});

		// Now open the dialog
		final Shell shell = Display.getDefault().getActiveShell();
		final ConfigureDialog configureDialog = new ConfigureDialog(shell);
		if (configureDialog.open() == Window.OK) {
			// Update active points based on the user's selection in the dialogue
			final Set<String> activePoints = configureDialog.getActivePoints();
			focusPoints.entrySet().stream().forEach(pointEntry -> {
				final String pointName = pointEntry.getKey();
				final FocusPoint point = pointEntry.getValue();
				point.setActive(activePoints.contains(pointName));
			});
			allowSampleRotation = configureDialog.isAllowSampleRotation();
			drawAfPoints();
		}
	}

	/**
	 * This function will eventually run the whole autofocus procedure.
	 * <p>
	 * For the time being, it displays the FFT values of the active focus points.<br>
	 * This will allow to evaluate whether the FFT gives a reliable indication of whether a point is in focus.
	 */
	private void runAutofocus() {
		try {
			final SortedMap<String, Integer> roiFfts = new TreeMap<>(getActiveRoiFfts());
			final StringBuilder sb = new StringBuilder(roiFfts.size() + " ROI(s) processed\n");

			roiFfts.entrySet().stream().forEach(roiFft -> sb.append(String.format("%s: %d%n", roiFft.getKey(), roiFft.getValue())));
			sb.append(String.format("%n%nAllow sample rotation: %b", allowSampleRotation));

			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "FFT values", sb.toString());
		} catch (LiveStreamException e) {
			final String message = getMessage(AUTOFOCUS_ERROR_FFT);
			showError(message, e, logger);
		}
	}

	/**
	 * Calculate the mean FFT value for each active ROI
	 *
	 * @return Map of ROI description to its mean FFT value
	 * @throws LiveStreamException
	 */
	private Map<String, Integer> getActiveRoiFfts() throws LiveStreamException {
		// Get snapshot of image
		final IDataset snapshotData = liveStreamView.getSnapshot().getDataset();

		// We need to read the current focus point positions from the plot: the user may have moved them
		final IPlottingSystem<Composite> plotter = liveStreamView.getPlottingSystem();
		final Collection<IRegion> regions = plotter.getRegions(RegionType.BOX);

		// For each of the active ROIs, calculate the FFT for a slice of the dataset corresponding to that ROI
		final Map<String, Integer> roiFfts = new HashMap<>(regions.size());
		final FourierTransformImageOperation fourierTransform = new FourierTransformImageOperation();
		regions.stream()
			.map(r -> (RectangularROI) r.getROI())
			.forEach(roi -> roiFfts.put(formatRoi(roi), getMeanFft(snapshotData, roi, fourierTransform)));
		return roiFfts;
	}

	/**
	 * Format a ROI for the pop-up dialog
	 */
	private String formatRoi(RectangularROI roi) {
		final double[] midPoint = roi.getMidPoint();
		final double[] lengths = roi.getLengths();
		return String.format("%s [%.2f %.2f %.2f, %.2f]", roi.getName(), midPoint[0], midPoint[1], lengths[0], lengths[1]);
	}

	/**
	 * Calculate the mean FFT value for a focus point
	 *
	 * @param snapshotData
	 *            complete snapshot image to be analysed
	 * @param focusRoi
	 *            the ROI for which the FFT is to be calculated
	 * @param fourierTransform
	 *            FFT algorithm
	 * @return mean FFT value for the point in the image
	 */
	private int getMeanFft(IDataset snapshotData, RectangularROI focusRoi, FourierTransformImageOperation fourierTransform) {
		final double[] startPoints = focusRoi.getPoint();
		final double[] endPoints = focusRoi.getEndPoint();
		final Slice xSlice = new Slice((int) startPoints[0], (int) endPoints[0]);
		final Slice ySlice = new Slice((int) startPoints[1], (int) endPoints[1]);
		final IDataset roiData = snapshotData.getSlice(ySlice, xSlice);
		final IDataset datasetFft = fourierTransform.processImage(roiData, null);
		return ((Double) datasetFft.mean()).intValue();
	}

	/**
	 * Define which focus points should be active by default.<br>
	 * If this is not called, all points will default to active.
	 *
	 * @param activeFocusPoints
	 *            names of the focus points that should be active
	 */
	public void setActiveFocusPoints(Collection<String> activeFocusPoints) {
		focusPoints.values().stream().forEach(point -> point.setActive(activeFocusPoints.contains(point.getName())));
	}

	public boolean isAllowSampleRotation() {
		return allowSampleRotation;
	}

	/**
	 * Class to specify a focus point as x/y centre and x/y size, as an easier way to specify a ROI
	 */
	public static class FocusPoint {
		private double xCentre;
		private double yCentre;
		private double xSize;
		private double ySize;
		private final String name;

		/**
		 * Defines whether this is the centre point of the pattern.<br>
		 * Specifically, it defines whether the point should be shown when the user clicks the "Centre only" button.
		 * <p>
		 * It is determines - though probably not very meaningful - to define multiple points as being "centre points".
		 */
		private boolean centre = false;

		/**
		 * Defines whether this point is part of a cross ("X" shape) of points in the pattern.<br>
		 * Specifically, it determines whether the point should be shown when the user click on the "X shape" button.
		 */
		private boolean cross = false;

		private boolean active = true;

		public FocusPoint(double xCentre, double yCentre, double xSize, double ySize, String name) {
			this.xCentre = xCentre;
			this.yCentre = yCentre;
			this.xSize = xSize;
			this.ySize = ySize;
			this.name = name;
		}

		public double getxCentre() {
			return xCentre;
		}

		public void setxCentre(double xCentre) {
			this.xCentre = xCentre;
		}

		public double getyCentre() {
			return yCentre;
		}

		public void setyCentre(double yCentre) {
			this.yCentre = yCentre;
		}

		public double getxSize() {
			return xSize;
		}

		public void setxSize(double xSize) {
			this.xSize = xSize;
		}

		public double getySize() {
			return ySize;
		}

		public void setySize(double ySize) {
			this.ySize = ySize;
		}

		public String getName() {
			return name;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public boolean isCentre() {
			return centre;
		}

		public void setCentre(boolean centre) {
			this.centre = centre;
		}

		public boolean isCross() {
			return cross;
		}

		public void setCross(boolean isCross) {
			this.cross = isCross;
		}

		public RectangularROI toRectangularROI() {
			final double ptx = xCentre - (xSize / 2.0);
			final double pty = yCentre - (ySize / 2.0);
			return new RectangularROI(ptx, pty, xSize, ySize, 0);
		}

		@Override
		public String toString() {
			return "FocusPoint [xCentre=" + xCentre + ", yCentre=" + yCentre + ", xSize=" + xSize + ", ySize=" + ySize
					+ ", name=" + name + ", centre=" + centre + ", isCross=" + cross + ", active=" + active + "]";
		}
	}

	/**
	 * A pop-up dialog showing the available autofocus point (in a pattern mirroring their layout on the camera image)
	 * and allowing the user to select/deselect points.
	 * <p>
	 * Active points are shown in red, inactive ones in grey.
	 */
	private class ConfigureDialog extends Dialog {
		private static final double MIN_BUTTON_SIZE = 16.0;

		/**
		 * Width of the live stream image
		 */
		private int imagexSize;

		/**
		 * Height of the live image
		 */
		private int imageySize;

		/**
		 * Scaling required to mirror size and position of the focus points in this view
		 */
		private double scalingFactor;

		/**
		 * A button corresponding to each focus point, keyed by the name of the point.
		 * <p>
		 * The data associated with each button indicates the name of the corresponding focus point and whether the
		 * corresponding point is active.
		 */
		private Map<String, Button> focusPointButtons;

		/**
		 * The activation state of each focus point, keyed by the name of the point.
		 */
		private Map<String, Boolean> activationStates;

		/**
		 * Check box to indicate whether sample rotation is permitted
		 */
		private Button sampleRotationCheck;

		/**
		 * Indicates whether sample rotation is permitted
		 * <p>
		 * This mirrors the variable in {@link LiveStreamViewCameraControlsAutofocus}
		 */
		private boolean allowSampleRotation;

		public ConfigureDialog(Shell shell) {
			super(shell);
			try {
				// Get dimensions of the live stream view
				final IDataset snapshotData = liveStreamView.getSnapshot().getDataset();
				final int[] dataShape = snapshotData.getShape();
				imagexSize = dataShape[1];
				imageySize = dataShape[0];

				// Find the minimum size of a ROI. This should be represented by a rectangle of MIN_BUTTON_SIZE
				double minRoiSize = Integer.MAX_VALUE;
				for (FocusPoint point : focusPoints.values()) {
					final RectangularROI roi = point.toRectangularROI();
					minRoiSize = Math.min(minRoiSize, Math.min(roi.getLength(0), roi.getLength(1)));
				}
				scalingFactor = MIN_BUTTON_SIZE / minRoiSize;

				// Set initial state of allowSampleRotation
				allowSampleRotation = LiveStreamViewCameraControlsAutofocus.this.allowSampleRotation;
			} catch (LiveStreamException e) {
				final String message = getMessage(AUTOFOCUS_ERROR_SNAPSHOT);
				logger.error(message, e);
				showError(message, e);
			}
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(getMessage(AUTOFOCUS_CONFIGURE_SHELL_TITLE));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			final Composite container = (Composite) super.createDialogArea(parent);
			GridDataFactory.swtDefaults().applyTo(container);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);

			// An area containing buttons corresponding to each available focus point
			final Composite focusPointComposite = new Composite(container, SWT.BORDER);
			final Point canvasSize = new Point((int) (imagexSize * scalingFactor), (int) (imageySize * scalingFactor));
			GridDataFactory.swtDefaults().hint(canvasSize).applyTo(focusPointComposite);
			focusPointButtons = new HashMap<>(focusPoints.size());
			activationStates = new HashMap<>();

			// Create a button in the form of a rectangle (scaled in size & position) corresponding to each focus point.
			for (FocusPoint point : focusPoints.values()) {
				final String pointName = point.getName();
				final RectangularROI roi = point.toRectangularROI();

				final Button button = new Button(focusPointComposite, SWT.NONE);
				button.setBounds((int) (roi.getPointX() * scalingFactor), (int) (roi.getPointY() * scalingFactor),
						(int) (roi.getLength(0) * scalingFactor), (int) (roi.getLength(1) * scalingFactor));
				button.setData(pointName);
				button.addListener(SWT.Selection, this::toggleActivationState);

				focusPointButtons.put(pointName, button);
				activationStates.put(pointName, point.isActive());
			}
			setButtonColours();

			// Buttons to perform special functions (select all points etc.)
			final Composite commandButtonComposite = new Composite(container, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(commandButtonComposite);
			GridLayoutFactory.swtDefaults().applyTo(commandButtonComposite);

			final Button selectAllButton = new Button(commandButtonComposite, SWT.PUSH);
			GridDataFactory.fillDefaults().applyTo(selectAllButton);
			selectAllButton.setText("Select all");
			selectAllButton.addListener(SWT.Selection, e -> selectAll());

			final Button centreOnlyButton = new Button(commandButtonComposite, SWT.PUSH);
			GridDataFactory.fillDefaults().applyTo(centreOnlyButton);
			centreOnlyButton.setText("Centre only");
			centreOnlyButton.addListener(SWT.Selection, e -> selectCentre());

			final Button xShapeButton = new Button(commandButtonComposite, SWT.PUSH);
			GridDataFactory.fillDefaults().applyTo(xShapeButton);
			xShapeButton.setText("X shape");
			xShapeButton.addListener(SWT.Selection, e -> selectX());

			sampleRotationCheck = new Button(commandButtonComposite, SWT.CHECK);
			GridDataFactory.fillDefaults().applyTo(sampleRotationCheck);
			sampleRotationCheck.setText("Allow sample rotation");
			sampleRotationCheck.setSelection(allowSampleRotation);
			sampleRotationCheck.addListener(SWT.Selection, e -> handleAllowSampleRotation());

			return container;
		}

		/**
		 * Set the colour of the buttons, based on their activation state
		 */
		private void setButtonColours() {
			focusPointButtons.values().stream().forEach(button -> {
				final boolean buttonActive = activationStates.get(button.getData());
				button.setBackground(getButtonColour(buttonActive));
			});
		}

		/**
		 * When the user clicks a point in the dialogue, toggle its activation state in {@link #activationStates} and
		 * change the colour of the point in the GUI.
		 *
		 * @param e
		 *            the button click event
		 */
		private void toggleActivationState(Event e) {
			final Button button = (Button) e.widget;
			final String pointName = (String) button.getData();
			final boolean newActivationState = !activationStates.get(pointName);
			activationStates.put(pointName, newActivationState);
			button.setBackground(getButtonColour(newActivationState));
		}

		/**
		 * Select all points in pattern
		 */
		private void selectAll() {
			activationStates.entrySet().stream().forEach(e -> e.setValue(true));
			setButtonColours();
		}

		/**
		 * Select the points that form a cross pattern
		 */
		private void selectX() {
			activationStates.entrySet().stream().forEach(e -> {
				final String pointName = e.getKey();
				final FocusPoint focusPoint = focusPoints.get(pointName);
				e.setValue(focusPoint.isCross());
			});
			setButtonColours();
		}

		/**
		 * Select the point(s) defined as the centre of the pattern
		 */
		private void selectCentre() {
			activationStates.entrySet().stream().forEach(e -> {
				final String pointName = e.getKey();
				final FocusPoint focusPoint = focusPoints.get(pointName);
				e.setValue(focusPoint.isCentre());
			});
			setButtonColours();
		}

		private void handleAllowSampleRotation() {
			allowSampleRotation = sampleRotationCheck.getSelection();
		}

		private Color getButtonColour(boolean active) {
			return active ? COLOUR_RED : COLOUR_GREY;
		}

		public Set<String> getActivePoints() {
			 return focusPointButtons.entrySet().stream()
			 .filter(b -> activationStates.get(b.getKey()))
			 .map(Map.Entry::getKey)
			 .collect(Collectors.toSet());
		}

		public boolean isAllowSampleRotation() {
			return allowSampleRotation;
		}
	}
}