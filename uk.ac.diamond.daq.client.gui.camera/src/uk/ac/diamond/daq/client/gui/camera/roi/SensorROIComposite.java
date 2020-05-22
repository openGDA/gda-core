package uk.ac.diamond.daq.client.gui.camera.roi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.RegionCallback;
import uk.ac.diamond.daq.client.gui.camera.RoiCallback;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.event.DrawableRegionRegisteredEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.diamond.daq.client.gui.camera.event.RegisterDrawableRegionEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.DrawableRegion;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.ClientVerifyListener;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * A Composite which draws a region on an {@link IPlottingSystem} to define the
 * camera ROI
 * 
 * @author Maurizio Nagni
 */
public class SensorROIComposite implements CompositeFactory {
	private static final Logger logger = LoggerFactory.getLogger(SensorROIComposite.class);

	private ICameraConfiguration cameraConfiguration;
	private IRectangularROI roiFromPlottingSystem;
	private final List<ROIRow> rows = new ArrayList<>();
	private final UUID sensorRegionID = UUID.randomUUID();
	private DrawableRegion sensorDrawableRegion;
	
	@Override
	public Composite createComposite(final Composite parent, int style) {
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		createTableColumn(table);

		createCameraRow(table);
		createROIRow(table);

		updateCamera(CameraHelper.getDefaultCameraProperties().getIndex());

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(table, regionRegisteredEventListener(parent));
			SpringApplicationContextProxy.addDisposableApplicationListener(table, getROIChangeListener(parent));
			SpringApplicationContextProxy.addDisposableApplicationListener(table, getChangeCameraListener(parent));

			SpringApplicationContextProxy
					.publishEvent(new RegisterDrawableRegionEvent(this, SWTResourceManager.getColor(SWT.COLOR_GREEN),
							ClientMessages.ROI, ClientSWTElements.findParentUUID(parent), sensorRegionID));
		} catch (GDAClientException e) {
			logger.error("Error", e);
		}
		return table;
	}

	/**
	 * Creates the Camera frame sizes row
	 * 
	 * @param table
	 */
	private void createCameraRow(Table table) {
		RoiCallback uc = () -> {
			try {
				return cameraConfiguration.getMaximumSizedROI();
			} catch (GDAClientException e) {
				logger.error("Error", e);
				return null;
			}
		};
		rows.add(new ROIRow(table, ClientMessages.CAMERA_AREA, false, 0, uc, null));
	}

	/**
	 * Creates the Camera ROI sizes row
	 * 
	 * @param table
	 */
	private void createROIRow(Table table) {
		RoiCallback nuc = () -> {
			return roiFromPlottingSystem;
		};
		RegionCallback rc = () -> {
			return sensorDrawableRegion.getIRegion();
		};
		rows.add(new ROIRow(table, ClientMessages.ROI, false, 1, nuc, rc));
	}

	private void createTableColumn(Table table) {
		ClientMessages[] headers = { ClientMessages.REGION, ClientMessages.X_MIN, ClientMessages.Y_MIN,
				ClientMessages.WIDTH, ClientMessages.HEIGHT };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(120);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}

	private class ROIRow {
		private final TableItem tableItem;
		private final ClientMessages name;
		private final RoiCallback roiCallback;
		private final RegionCallback regionCallback;
		private boolean editable;

		private Label regionName;
		private Text xMin;
		private Text yMin;
		private Text width;
		private Text height;

		public ROIRow(Table table, ClientMessages name, boolean editable, int index, RoiCallback roiCallback,
				RegionCallback regionCallback) {
			this.tableItem = new TableItem(table, SWT.NONE, index);
			this.name = name;
			this.editable = editable;
			this.roiCallback = roiCallback;
			this.regionCallback = regionCallback;
			addColumns();
		}

		private void addColumns() {
			Table table = tableItem.getParent();
			TableEditor editor = new TableEditor(table);

			regionName = ClientSWTElements.createLabel(table, SWT.NONE, name);
			editor.grabHorizontal = true;
			editor.setEditor(regionName, tableItem, 0);

			xMin = ClientSWTElements.createText(table, SWT.NONE, ClientVerifyListener.verifyOnlyIntegerText);
			xMin.setText(Integer.toString(0));
			xMin.setEnabled(editable);
			editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.setEditor(xMin, tableItem, 1);
			editor.horizontalAlignment = SWT.LEFT;

			yMin = ClientSWTElements.createText(table, SWT.NONE, ClientVerifyListener.verifyOnlyIntegerText);
			yMin.setText(Integer.toString(0));
			yMin.setEnabled(editable);
			editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.setEditor(yMin, tableItem, 2);
			editor.horizontalAlignment = SWT.LEFT;

			width = ClientSWTElements.createText(table, SWT.NONE, ClientVerifyListener.verifyOnlyIntegerText);
			width.setText(Integer.toString(0));
			width.setEnabled(editable);
			editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.setEditor(width, tableItem, 3);
			editor.horizontalAlignment = SWT.LEFT;

			height = ClientSWTElements.createText(table, SWT.NONE, ClientVerifyListener.verifyOnlyIntegerText);
			height.setText(Integer.toString(0));
			height.setEnabled(editable);
			editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.setEditor(height, tableItem, 4);
			editor.horizontalAlignment = SWT.LEFT;
		}

		public void updateRow() throws DeviceException {
			if (roiCallback.getROI() == null) {
				return;
			}
			xMin.setText(Integer.toString((int) roiCallback.getROI().getPointX()));
			yMin.setText(Integer.toString((int) roiCallback.getROI().getPointY()));
			width.setText(Integer.toString((int) roiCallback.getROI().getLength(0)));
			height.setText(Integer.toString((int) roiCallback.getROI().getLength(1)));
		}

		public void isEditable(boolean editable) {
			xMin.setEditable(editable);
			xMin.setEnabled(editable);

			yMin.setEditable(editable);
			yMin.setEnabled(editable);

			width.setEditable(editable);
			width.setEnabled(editable);

			height.setEditable(editable);
			height.setEnabled(editable);
		}

		public void addModifyListener() {
			xMin.addModifyListener(ml);
			yMin.addModifyListener(ml);
			width.addModifyListener(ml);
			height.addModifyListener(ml);
		}

		public void removeModifyListener() {
			xMin.removeModifyListener(ml);
			yMin.removeModifyListener(ml);
			width.removeModifyListener(ml);
			height.removeModifyListener(ml);
		}

		public void submitROI() throws GDAClientException {
			try {
				cameraConfiguration.getCameraControl().orElseThrow(() -> new GDAClientException("")).setRoi(
						Integer.parseInt(xMin.getText()), Integer.parseInt(yMin.getText()),
						Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
			} catch (DeviceException e) {
				throw new GDAClientException("Cannot set ROI", e);
			}
		}

		public void updateDrawableRegion() {
			if (regionCallback == null || regionCallback.getRegion() == null) {
				return;
			}
			RectangularROI rectangularROI = new RectangularROI(Integer.parseInt(xMin.getText()),
					Integer.parseInt(yMin.getText()), Integer.parseInt(width.getText()),
					Integer.parseInt(height.getText()), 0);
			regionCallback.getRegion().setROI(rectangularROI);
		}
	}

	private void updateCameraROI() {
		try {
			rows.get(1).submitROI();
		} catch (NumberFormatException e) {
			logger.error("Error", e);
		} catch (GDAClientException e) {
			logger.error("Error", e);
		}
	}

	/**
	 * Events from Text Fields
	 */
	private ModifyListener ml = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateCameraROI();
			getCameraROIRow().updateDrawableRegion();
		}
	};

	/**
	 * Events from the PlottingSystem
	 */
	private ApplicationListener<ROIChangeEvent> getROIChangeListener(Composite parent) {
		return new ApplicationListener<ROIChangeEvent>() {
			@Override
			public void onApplicationEvent(ROIChangeEvent event) {
				if (!event.getRoi().getName().equals(sensorDrawableRegion.getRegionID().toString())) {
					return;
				}
				roiFromPlottingSystem = event.getRoi();
				updateROIRow();
			}

			private void updateROIRow() {
				getCameraROIRow().removeModifyListener();
				try {
					getCameraRow().updateRow();
					getCameraROIRow().updateRow();
					getCameraROIRow().isEditable(true);
					updateCameraROI(); // sets
										// the
										// ROI
										// in
										// the
										// camera
										// control
				} catch (DeviceException e) {
					e.printStackTrace();
				}
				rows.get(1).addModifyListener();
			}
		};
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeCameraListener(Composite parent) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				if (!event.haveSameParent(parent)) {
					return;
				}
				updateCamera(event.getActiveCamera().getIndex());
			}
		};
	}

	private ApplicationListener<DrawableRegionRegisteredEvent> regionRegisteredEventListener(Composite parent) {
		return new ApplicationListener<DrawableRegionRegisteredEvent>() {
			@Override
			public void onApplicationEvent(DrawableRegionRegisteredEvent event) {
				if (!event.haveSameParent(parent)) {
					return;
				}
				sensorDrawableRegion = event.getDrawableRegion();
			}
		};
	}

	private void updateCamera(int cameraIndex) {
		cameraConfiguration = CameraHelper.createICameraConfiguration(cameraIndex);
		updateRoi();
	}

	private void updateRoi() {
		rows.stream().forEach(i -> {
			try {
				i.updateRow();
			} catch (DeviceException e) {
				logger.error("Error", e);
			}
		});
	}

	private ROIRow getCameraRow() {
		return rows.get(0);
	}

	private ROIRow getCameraROIRow() {
		return rows.get(1);
	}
}
