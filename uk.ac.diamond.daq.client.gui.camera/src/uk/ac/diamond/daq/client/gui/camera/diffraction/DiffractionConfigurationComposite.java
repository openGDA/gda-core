package uk.ac.diamond.daq.client.gui.camera.diffraction;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.DiffractionCameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.DiffractionCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.exposure.BinningComposite;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureDurationComposite;
import uk.ac.diamond.daq.client.gui.camera.exposure.SensorROIComposite;
import uk.ac.gda.client.exception.GDAClientException;

public class DiffractionConfigurationComposite extends Composite {
	private class CameraPositionListener extends DiffractionCameraConfigurationAdapter
			implements ISelectionChangedListener {
		private boolean updateController = true;
		private String[] values;
		private String lastMovingMessage;

		public CameraPositionListener(String[] values) {
			this.values = values;
			lastMovingMessage = null;
		}

		@Override
		public void setCameraPosition(boolean moving, String from, String to) {
			getDisplay().asyncExec(() -> {
				if (moving) {
					cameraPositionCombo.getCombo().setEnabled(false);
					cameraPositionCombo.remove(values);
					lastMovingMessage = "Moving from " + from + " to " + to;
					updateController = false;
					cameraPositionCombo.add(lastMovingMessage);
					cameraPositionCombo.setSelection(new StructuredSelection(lastMovingMessage));
				} else {
					cameraPositionCombo.getCombo().setEnabled(true);
					if (lastMovingMessage != null) {
						cameraPositionCombo.remove(lastMovingMessage);
					}
					updateController = false;
					cameraPositionCombo.add(values);
					cameraPositionCombo.setSelection(new StructuredSelection(to));
				}
			});
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (!updateController) {
				updateController = true;
				return;
			}
			IStructuredSelection selection = event.getStructuredSelection();
			if (selection.size() > 0) {
				try {
					controller.setCameraPosition((String) selection.getFirstElement());
				} catch (DeviceException e) {
					getDisplay().asyncExec(() -> cameraPositionCombo.getCombo()
							.setText("Cannot move to " + (String) selection.getFirstElement()));
				}
			}
		}
	}

	private DiffractionCameraConfigurationController controller;
	private ComboViewer cameraPositionCombo;
	private CameraPositionListener cameraPositionListener;

	public DiffractionConfigurationComposite(Composite parent, DiffractionCameraConfigurationController controller,
			int style) throws GDAClientException {
		super(parent, style);

		this.controller = controller;
		try {
			cameraPositionListener = new CameraPositionListener(controller.getPosibleCameraPositions());
		} catch (DeviceException e) {
			throw new GDAClientException("Error", e);
		}
		controller.addListener(cameraPositionListener);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		Composite exposureLengthComposite = new ExposureDurationComposite(controller).createComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(exposureLengthComposite);

		Composite binningComposite = new BinningComposite(this, controller, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(binningComposite);

		Composite sensorROIPanel = new SensorROIComposite(this, controller, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(sensorROIPanel);

		Composite cameraPositionComposite;
		try {
			cameraPositionComposite = createCameraPositionComposite();
		} catch (DeviceException e) {
			throw new GDAClientException("Error", e);
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(cameraPositionComposite);

		Label spacer = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).applyTo(spacer);
	}

	private Composite createCameraPositionComposite() throws DeviceException {
		Composite composite = new Composite(this, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(composite);

		Group group = new Group(composite, SWT.NONE);
		group.setText("Camera Position");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

		cameraPositionCombo = new ComboViewer(group, SWT.READ_ONLY);
		cameraPositionCombo.add(controller.getPosibleCameraPositions());
		cameraPositionCombo.setSelection(new StructuredSelection(controller.getCameraPosition()));
		cameraPositionCombo.addSelectionChangedListener(cameraPositionListener);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(cameraPositionCombo.getControl());

		Label spacer = new Label(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(spacer);

		return composite;
	}
}
