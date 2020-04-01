package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.RealMatrix;
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
import org.springframework.stereotype.Component;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Displays a button to execute
 * {@link BeamCameraMapping_#calibrate(ICameraConfiguration)} on a
 * {@link ICameraConfiguration}. This class listen to
 * {@link ChangeActiveCameraEvent} in order to know on which camera execute the
 * mapping.
 * <p>
 * Note that the {@link Composite} returned by
 * {@link #createComposite(Composite, int)} and the one publishing the
 * {@link ChangeActiveCameraEvent} have to be part of the same root
 * {@link Component}.
 * </p>
 * 
 * @author Maurizio Nagni
 *
 */
public class BeamCameraCalibrationComposite implements CompositeFactory {
	private Button doCalibration;
	private UUID uuidRoot;
	private ICameraConfiguration cameraConfiguration;
	private CameraMappingTable mappingTable;

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraCalibrationComposite.class);

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<ChangeActiveCameraEvent> changeCameraListener = new ApplicationListener<ChangeActiveCameraEvent>() {
		@Override
		public void onApplicationEvent(ChangeActiveCameraEvent event) {
			// if the event arrives from a component with a different common parent, rejects
			// the event
			if (!event.getRootComposite().orElse(UUID.randomUUID()).equals(uuidRoot)) {
				return;
			}
			updateCamera(event.getActiveCamera().getIndex());
		}
	};

	@Override
	public Composite createComposite(final Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, style);
		doCalibration = ClientSWTElements.createButton(container, style, ClientMessages.BEAM_CAMERA_MAPPING,
				ClientMessages.BEAM_CAMERA_MAPPING_TP);
		uuidRoot = ClientSWTElements.findParentUUID(parent).orElse(null);
		updateCamera(CameraHelper.getDefaultCameraProperties().getIndex());
		mappingTable = new CameraMappingTable(container);
		mappingTable.createComposite();
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BeamCameraMapping ic = SpringApplicationContextProxy.getBean(BeamCameraMapping.class);
				ic.calibrate(cameraConfiguration);
			}
		};
		doCalibration.addSelectionListener(listener);
		try {
			SpringApplicationContextProxy.addApplicationListener(changeCameraListener);
			SpringApplicationContextProxy.addApplicationListener(cameraMappingEventListener);
		} catch (GDAClientException e) {
			String msg = ClientMessagesUtility.getMessage(ClientMessages.CANNOT_LISTEN_CAMERA_PUBLISHER);
			logger.error(msg, e);
			UIHelper.showError(msg, e);
		}
		return container;
	}

	private void updateCamera(int cameraIndex) {
		cameraConfiguration = CameraHelper.createICameraConfiguration(cameraIndex);
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
			CameraHelper.createICameraConfiguration(event.getCameraIndex()).getBeamCameraMap()
					.ifPresent(c -> Display.getDefault().asyncExec(() -> mappingTable.displayMatrix(c)));
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
