package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationController;
import uk.ac.gda.api.camera.CameraRegionOfInterest;

public class SensorROIComposite extends Composite {
	private static final String UNITS = "pix";
	
	private class DataModel {
		int left;
		int width;
		int top;
		int height;
	}
	
	private class ROIListener extends CameraConfigurationAdapter {
		@Override
		public void setRegionOfInterest(CameraRegionOfInterest regionOfInterest) {
			dataModel.left = regionOfInterest.getLeft();
			dataModel.top = regionOfInterest.getTop();
			dataModel.width = regionOfInterest.getWidth();
			dataModel.height = regionOfInterest.getHeight();
		}
		
		@Override
		public void clearRegionOfInterest() {
			leftText.setText("");
			topText.setText("");
			widthText.setText("");
			heightText.setText("");
		}
	}
	
	private DataModel dataModel = new DataModel ();
	
	private DataBindingContext dbc = new DataBindingContext();
	private CameraConfigurationController controller;
	
	private Text topText;
	private Text leftText;
	private Text widthText;
	private Text heightText;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SensorROIComposite(Composite parent, CameraConfigurationController controller, int style) {
		super(parent, style);

		this.controller = controller;
		ROIListener roiListener = new ROIListener();
		controller.addListener(roiListener);
		
		addListener(SWT.Dispose, e -> controller.removeListener(roiListener));
		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		Group panel = new Group(this, SWT.SHADOW_NONE);
		panel.setText("Sensor ROI");

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(panel);

		Label label;

		label = new Label(panel, SWT.LEFT);
		label.setText("Top");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Height");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		topText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(topText);

		label = new Label(panel, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		heightText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(heightText);

		label = new Label(panel, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Left");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Width");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		leftText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftText);

		label = new Label(panel, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		widthText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(widthText);

		label = new Label(panel, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		GridDataFactory.fillDefaults().applyTo(panel);
		
		/* Data Bindings *******************************************************************/
		IObservableValue leftTarget = WidgetProperties.text(SWT.Modify).observe(leftText);
		leftTarget.addValueChangeListener(e -> updateText(e.getObservableValue().getValue().toString(), leftText, true));
		IObservableValue leftModel = BeanProperties.value("left").observe(dataModel);
		dbc.bindValue(leftTarget, leftModel);
	}
	
	private void updateText (String textValue, Text component, boolean sendToController) {
		try {
			Integer.parseInt(textValue);
			component.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			if (sendToController) {
				sendToController ();
			}
		} catch (NumberFormatException e) {
			component.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		}
	}
	
	private void sendToController () {
		try {
			Integer.parseInt(leftText.getText());
			Integer.parseInt(topText.getText());
			Integer.parseInt(widthText.getText());
			Integer.parseInt(heightText.getText());
			
			CameraRegionOfInterest region = CameraRegionOfInterest.getInstanceFromWidthHeight(
					dataModel.left, dataModel.top, dataModel.width, dataModel.height);
			controller.setROIRegion(region);
		} catch (NumberFormatException e) {
			//do nothing
		} catch (DeviceException e) {
			MessageDialog.openError(this.getShell(), "Camera Configuration", "Unable to set region of interest to " + 
					"left: " + dataModel.left + "top: " + dataModel.top + "width: " + dataModel.width + "height: " + dataModel.height);
		}
	}
}
