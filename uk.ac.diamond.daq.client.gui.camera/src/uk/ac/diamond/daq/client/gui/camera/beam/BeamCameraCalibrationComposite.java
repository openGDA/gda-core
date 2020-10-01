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

package uk.ac.diamond.daq.client.gui.camera.beam;

import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.createICameraConfiguration;
import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getDefaultCameraProperties;
import static uk.ac.gda.ui.tool.ClientMessages.BEAM_CAMERA_MAPPING;
import static uk.ac.gda.ui.tool.ClientMessages.BEAM_CAMERA_MAPPING_TP;
import static uk.ac.gda.ui.tool.ClientMessages.CANNOT_LISTEN_CAMERA_PUBLISHER;
import static uk.ac.gda.ui.tool.ClientMessages.MOTOR_OUT_OF_RANGE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.updateButton;
import static uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy.addDisposableApplicationListener;
import static uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy.getBean;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.event.PlottingSystemUpdateEvent;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * GUI to maps camera pixels to the motors driving the beam. This class listen
 * to events (published by the container it belongs)
 * <ul>
 * <li>{@link ChangeActiveCameraEvent} to set the active camera</li>
 * <li>{@link PlottingSystemUpdateEvent} to set the active plotting system</li>
 * <li>{@link BeamCameraMappingEvent} to draw the camera area where mapping
 * exists</li>
 * </ul>
 * The first two events have to be published by the top parent container or any
 * of its children. See <a href=
 * "https://confluence.diamond.ac.uk/display/DIAD/Camera+Configuration+View">Camera
 * Configuration</a> in Confluence.
 * </p>
 *
 * <p>
 * See <a href=
 * "https://confluence.diamond.ac.uk/display/DIAD/BeamCameraMapping">CameraBeamMapping</a>
 * in Confluence
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
public class BeamCameraCalibrationComposite implements CompositeFactory {
	private Button doCalibration;
	private ICameraConfiguration cameraConfiguration;
	private CameraMappingTable mappingTable;
	private IPlottingSystem<Composite> plottingSystem;
	private Composite container;

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraCalibrationComposite.class);

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent) {
		return CameraHelper.createChangeCameraListener(parent, changeCameraControl);
	}

	private Consumer<ChangeActiveCameraEvent> changeCameraControl = event -> {
		updateCamera(event.getActiveCamera().getIndex());
	};

	@Override
	public Composite createComposite(final Composite parent, int style) {
		Composite container = createClientCompositeWithGridLayout(parent, style, 1);
		doCalibration = createClientButton(container, style, BEAM_CAMERA_MAPPING, BEAM_CAMERA_MAPPING_TP);
		createClientGridDataFactory().applyTo(doCalibration);
		updateCamera(getDefaultCameraProperties().getIndex());
		mappingTable = new CameraMappingTable(container);
		mappingTable.createComposite();
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCalibration.setText("Please wait....");
				doCalibration.setEnabled(false);
				BeamCameraMapping ic = getBean(BeamCameraMapping.class);
				ic.calibrate(cameraConfiguration);
			}
		};
		doCalibration.addSelectionListener(listener);
		try {
			addDisposableApplicationListener(container, getChangeActiveCameraListener(parent));
			addDisposableApplicationListener(container, cameraMappingEventListener);
			addDisposableApplicationListener(container, plottingSystemUpdateListener);
		} catch (GDAClientException e) {
			String msg = ClientMessagesUtility.getMessage(CANNOT_LISTEN_CAMERA_PUBLISHER);
			logger.error(msg, e);
			UIHelper.showError(msg, e);
		}
		return container;
	}

	private void updateCamera(int cameraIndex) {
		cameraConfiguration = createICameraConfiguration(cameraIndex);
		doCalibration.setEnabled(false);
		if (cameraConfiguration.getCameraProperties().isBeamMappingActive()) {
			doCalibration.setEnabled(true);
		}
	}

	/**
	 * Handles {@link BeamCameraMappingEvent}s
	 */
	private ApplicationListener<BeamCameraMappingEvent> cameraMappingEventListener = new ApplicationListener<BeamCameraMappingEvent>() {
		@Override
		public void onApplicationEvent(BeamCameraMappingEvent event) {
			createICameraConfiguration(event.getCameraIndex()).getBeamCameraMap()
					.ifPresent(c -> Display.getDefault().asyncExec(() -> {
						updateComposite(c);
						DrawCameraMappingArea.handleEvent(plottingSystem, event);
					}));
		}

		private void updateComposite(BeamCameraMap beamCameraMap) {
			// Displays the matrix
			mappingTable.displayMatrix(beamCameraMap);
			// Enables the mapping button
			updateButton(doCalibration, BEAM_CAMERA_MAPPING, BEAM_CAMERA_MAPPING_TP, null);
			doCalibration.setEnabled(true);
		}
	};

	private ApplicationListener<PlottingSystemUpdateEvent> plottingSystemUpdateListener = new ApplicationListener<PlottingSystemUpdateEvent>() {
		@Override
		public void onApplicationEvent(PlottingSystemUpdateEvent event) {
			if (!event.haveSameParent(container)) {
				return;
			}
			plottingSystem = event.getPlottingSystem();
			clickEvents(plottingSystem);
		}

		private void clickEvents(IPlottingSystem<Composite> plottingSystem) {
			plottingSystem.addClickListener(new IClickListener() {

				@Override
				public void doubleClickPerformed(final ClickEvent evt) {
					sendEvent(evt, true);
				}

				@Override
				public void clickPerformed(final ClickEvent evt) {
					sendEvent(evt, false);
				}

				private void sendEvent(final ClickEvent event, boolean isDoubleClick) {
					if (event.isShiftDown()) {
						onClickEvent(event);
					}
				}
			});
		}

		private void onClickEvent(ClickEvent event) {
			moveBeam(cameraConfiguration, event);
		}

		private void moveBeam(ICameraConfiguration iConfiguration, ClickEvent event) {
			if (Objects.isNull(iConfiguration.getBeamCameraMap())) {
				return;
			}
			iConfiguration.getBeamCameraMap().ifPresent(bcm -> moveMotors(bcm, event));
		}

		private void moveMotors(BeamCameraMap bcm, ClickEvent event) {
			RealMatrix transformation = bcm.getAffineTransformation();
			LUDecomposition luDecompositionBeamToCamera = new LUDecomposition(transformation);
			LUDecomposition luDecompositionCameraToBeam = new LUDecomposition(luDecompositionBeamToCamera.getSolver().getInverse());
			RealVector cameraVector = new ArrayRealVector(new double[] { event.getxValue(), event.getyValue() }, false);
			try {
				getBean(BeamCameraMapping.class).moveKB(luDecompositionCameraToBeam.getSolver().solve(cameraVector));
			} catch (SingularMatrixException e) {
				e.printStackTrace();
			} catch (GDAClientException e) {
				Display.getDefault().asyncExec(() -> UIHelper.showError(MOTOR_OUT_OF_RANGE, e));
			}
		}
	};

	private class CameraMappingTable {

		private final Composite parent;
		private TableItem row1;
		private TableItem row2;

		public CameraMappingTable(Composite parent) {
			super();
			this.parent = parent;
		}

		public void createComposite() {
			Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
			table.setLinesVisible(true);
			table.setHeaderVisible(false);
			GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			table.setLayoutData(data);
			createTableColumn(table);

			row1 = new TableItem(table, SWT.NULL);
			row1.setText(0, "a00");
			row1.setText(1, "a01");
			row2 = new TableItem(table, SWT.NULL);
			row2.setText(0, "a10");
			row2.setText(1, "a11");
			table.setVisible(true);
		}

		private void createTableColumn(Table table) {
			IntStream.range(0, 2).forEach(c -> {
				TableColumn column = new TableColumn(table, SWT.NONE);
				column.setWidth(100);
			});
		}

		private void displayMatrix(BeamCameraMap beamCameraMap) {
			RealMatrix transformation = beamCameraMap.getAffineTransformation();
			row1.setText(0, Double.toString(transformation.getEntry(0, 0)));
			row1.setText(1, Double.toString(transformation.getEntry(0, 1)));

			row2.setText(0, Double.toString(transformation.getEntry(1, 0)));
			row2.setText(1, Double.toString(transformation.getEntry(1, 1)));
		}
	}
}