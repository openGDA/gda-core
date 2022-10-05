/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dawnsci.plotting.system.AnnotationWrapper;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.calibration.CalibratedAxesProvider;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.SnapshotView;

public class LiveStreamViewForPlate extends LiveStreamView {

	public static final String ID = "uk.ac.diamond.daq.sample.plate.management.ui.LiveStreamViewForPlate";

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewForPlate.class);

	public static final String SNAPSHOT_DATA = "Snapshot Data";

	private SnapshotView snapshotView;

	IPlottingSystem<?> plottingSystemSnapshot;

	DoubleDataset xCalibratedDataset;
	DoubleDataset yCalibratedDataset;

	Map<Double, Integer> xCalibratedInverted;
	Map<Double, Integer> yCalibratedInverted;

	private IEventBroker eventBroker  = PlatformUI.getWorkbench().getService(IEventBroker.class);

	private EventHandler deleteAnnotationHandler = event -> {
    	String annotationName = (String) event.getProperty(IEventBroker.DATA);
    	IAnnotation annotationWrapper = plottingSystemSnapshot.getAnnotation(annotationName);
    	if (annotationWrapper != null) {
    		plottingSystemSnapshot.removeAnnotation(annotationWrapper);
    	}
    };

	private EventHandler updateLabelAnnotationHandler = event -> {
    	String[] annotationNames = (String[]) event.getProperty(IEventBroker.DATA);
    	String annotationOldName = annotationNames[0];
    	String annotationNewName = annotationNames[1];
    	IAnnotation annotationWrapper = plottingSystemSnapshot.getAnnotation(annotationOldName);
    	if (annotationWrapper != null) {
    		annotationWrapper.setName(annotationNewName);
    	}
	};

	private EventHandler addAnnotationHandler = event -> {
    	String[] eventData = (String[]) event.getProperty(IEventBroker.DATA);

    	String sampleId = eventData[0];
    	String sampleName = eventData[1];
    	double sampleX = Double.parseDouble(eventData[2]);
    	double sampleY = Double.parseDouble(eventData[3]);

    	String annotationName = sampleId + ": " + sampleName;
    	double annotationX = xCalibratedInverted.get(sampleX);
    	double annotationY = yCalibratedInverted.get(sampleY);

    	try {
			Axis xAxis = (Axis) plottingSystemSnapshot.getSelectedXAxis();
			Axis yAxis = (Axis) plottingSystemSnapshot.getSelectedYAxis();

			Annotation a = new Annotation(annotationName, xAxis, yAxis);
			a.setAnnotationColor(new Color(null, 0, 255, 0));
			a.setLocation(annotationX, annotationY);

			a.addAnnotationListener((oldX, oldY, newX, newY) -> {
				double xCalibrated = xCalibratedDataset.getDouble((int) newX);
				double yCalibrated = yCalibratedDataset.getDouble((int) newY);

				eventBroker.post(SamplePlateConstants.TOPIC_UPDATE_POSITION_ANNOTATION, new String[] {
					a.getName(),
					String.valueOf(xCalibrated),
					String.valueOf(yCalibrated)
				});
			});

			CalibratedAxesProvider calibratedAxesProvider = getActiveCameraConfiguration().getCalibratedAxesProvider();
			int midPixelX = calibratedAxesProvider.getXAxisDataset().getSize() / 2;
			int midPixelY = calibratedAxesProvider.getYAxisDataset().getSize() / 2;

			if (annotationX < midPixelX && annotationY < midPixelY) {
				a.setdxdy(40, 40);
			} else if (annotationX < midPixelX && annotationY >= midPixelY) {
				a.setdxdy(40, -40);
			} else if (annotationX >= midPixelX && annotationY < midPixelY) {
				a.setdxdy(-40, 40);
			} else {
				a.setdxdy(-40, -40);
			}

			IAnnotation annotation = new AnnotationWrapper(a);
			annotation.setShowName(true);
			annotation.setShowPosition(false);
			annotation.setShowInfo(false);

			Display.getDefault().syncExec(() -> plottingSystemSnapshot.addAnnotation(annotation));
			plottingSystemSnapshot.repaint();
		} catch (Exception e) {
			logger.error("Error plotting annotation '{}'", annotationName, e);
		}
	};

	private EventHandler clearSnapshotHandler = event -> {
		if (plottingSystemSnapshot != null) {
			plottingSystemSnapshot.reset();
		}
	};

	private EventHandler takeSnapshotHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			try {
				final SnapshotData snapshot = getSnapshot();
				snapshotView = (SnapshotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SnapshotView.ID, null, 3);

				plottingSystemSnapshot = snapshotView.getPlottingSystem();
				Display.getDefault().asyncExec(() -> updateSnapshotView(snapshot));

				xCalibratedDataset = (DoubleDataset) getActiveCameraConfiguration().getCalibratedAxesProvider().getXAxisDataset();
				yCalibratedDataset = (DoubleDataset) getActiveCameraConfiguration().getCalibratedAxesProvider().getYAxisDataset();

				eventBroker.post(SamplePlateConstants.TOPIC_RETURN_CALIBRATED_AXES,
						new ImmutablePair<double[], double[]>(xCalibratedDataset.getData(), yCalibratedDataset.getData()));

				xCalibratedInverted = new HashMap<>();
				for (int key = 0; key < xCalibratedDataset.getSize(); key++) {
					Double value = xCalibratedDataset.getDouble(key);
					xCalibratedInverted.put(value, key);
				}
				yCalibratedInverted = new HashMap<>();
				for (int key = 0; key < yCalibratedDataset.getSize(); key++) {
					Double value = yCalibratedDataset.getDouble(key);
					yCalibratedInverted.put(value, key);
				}

				plottingSystemSnapshot.addClickListener(new IClickListener() {
					@Override
					public void doubleClickPerformed(ClickEvent evt) {
						//No double click action
					}

					@Override
					public void clickPerformed(ClickEvent evt) {
						int xCoord = (int) evt.getxValue();
						int yCoord = (int) evt.getyValue();

						double xCalibratedValue = xCalibratedDataset.getDouble(xCoord);
						double yCalibratedValue = yCalibratedDataset.getDouble(yCoord);

						eventBroker.post(SamplePlateConstants.TOPIC_PICK_POSITION, new ImmutablePair<>(xCalibratedValue, yCalibratedValue));
					}
				});

			} catch (PartInitException | LiveStreamException e) {
				logger.error("View '{}' cannot be initialised", SnapshotView.ID, e);
			}
		}

		private void updateSnapshotView(SnapshotData snapshotData) {
			// perform the update of the snapshot view with the given dataset
			plottingSystemSnapshot.clear();
			plottingSystemSnapshot.updatePlot2D(snapshotData.getDataset(), null, SNAPSHOT_DATA, new NullProgressMonitor());
		}
	};

	@Override
	protected void createLivePlot(final Composite parent, final String secondaryId) {
		Button resetViewButton = new Button(parent, SWT.PUSH);
		resetViewButton.setText("Reset");
		resetViewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				openViewWithSecondaryId(getViewSite().getSecondaryId(), true);
			}
		});

		super.createLivePlot(parent, secondaryId);

		if (getPlottingComposite() != null) {
			resetViewButton.dispose();
		}

		eventBroker.subscribe(SamplePlateConstants.TOPIC_ADD_ANNOTATION, addAnnotationHandler);
		eventBroker.subscribe(SamplePlateConstants.TOPIC_UPDATE_LABEL_ANNOTATION, updateLabelAnnotationHandler);
		eventBroker.subscribe(SamplePlateConstants.TOPIC_DELETE_ANNOTATION, deleteAnnotationHandler);
		eventBroker.subscribe(SamplePlateConstants.TOPIC_TAKE_SNAPSHOT, takeSnapshotHandler);
		eventBroker.subscribe(SamplePlateConstants.TOPIC_CLEAR_SNAPSHOT, clearSnapshotHandler);
	}

	@Override
	public SnapshotData getSnapshot() throws LiveStreamException {
		final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();

		if (plottingSystem == null) {
			throw new LiveStreamException("Cannot get snapshot: plotting system is null");
		}
		final IImageTrace iTrace = getITrace();
		if (iTrace == null) {
			throw new LiveStreamException("Cannot get snapshot: image trace is null");
		}

		final SnapshotData snapshotData = new SnapshotData("", iTrace.getData().clone());
		final List<IDataset> axes = iTrace.getAxes();
		if (axes != null && !axes.isEmpty()) {
			snapshotData.setxAxis(axes.get(0));
			snapshotData.setyAxis(axes.get(1));
		}

		return snapshotData;
	}

	@Override
	protected String getID() {
		return LiveStreamViewForPlate.ID;
	}
}
