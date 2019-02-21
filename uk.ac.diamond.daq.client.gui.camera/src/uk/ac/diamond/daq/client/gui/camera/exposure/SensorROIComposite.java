package uk.ac.diamond.daq.client.gui.camera.exposure;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.Function;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;

public class SensorROIComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(SensorROIComposite.class);
	
	private static final String UNITS = "px";
	private static final String NO_VALUE = "";
	private static final int BOX_SIZE = 80;
	private static final int SPACER_SIZE = 30;
	private static final int BUTTON_SIZE = 80;
	
	private class DataModel {
		int left;
		int width;
		int top;
		int height;
		
		private final PropertyChangeSupport changeSupport =
	            new PropertyChangeSupport(this);

	    @SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.addPropertyChangeListener(listener);
	    }

	    @SuppressWarnings("unused")
		public void removePropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.removePropertyChangeListener(listener);
	    }

	    protected void firePropertyChange(String propertyName, Object oldValue,
	            Object newValue) {
	        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	    }	
	    
		@SuppressWarnings("unused")
		public int getLeft() {
			return left;
		}
		
		public void setLeft(int left) {
			int oldLeft = this.left;
			this.left = left;
			firePropertyChange("left", oldLeft, left);
		}
		
		@SuppressWarnings("unused")
		public int getWidth() {
			return width;
		}
		
		public void setWidth(int width) {
			int oldWidth = this.width;
			this.width = width;
			firePropertyChange("width", oldWidth, width);
		}
		
		@SuppressWarnings("unused")
		public int getTop() {
			return top;
		}
		
		public void setTop(int top) {
			int oldTop = this.top;
			this.top = top;
			firePropertyChange("top", oldTop, top);
		}
		
		@SuppressWarnings("unused")
		public int getHeight() {
			return height;
		}
		
		public void setHeight(int height) {
			int oldHeight = this.height;
			this.height = height;
			firePropertyChange("height", oldHeight, height);
		}
	}
	
	private class ROIListener extends CameraConfigurationAdapter {
		@Override
		public void setROI(RectangularROI roi) {
			updateClearButton();
			
			dataModel.setLeft(roi.getIntPoint()[0]);
			dataModel.setTop(roi.getIntPoint()[1]);
			dataModel.setWidth(roi.getIntLength(0));
			dataModel.setHeight(roi.getIntLength(1));
		}
		
		@Override
		public void clearRegionOfInterest() {
			clearButton.setEnabled(false);
			
			dataModel.setLeft(0);
			dataModel.setTop(0);
			dataModel.setWidth(0);
			dataModel.setHeight(0);
		}
	}
	
	private class ChangeListener implements IChangeListener {
		@Override
		public void handleChange(ChangeEvent event) {
			boolean error = false;
			leftText.setForeground(validColor);
			topText.setForeground(validColor);
			widthText.setForeground(validColor);
			heightText.setForeground(validColor);
			
			RectangularROI max;
			try {
				max = controller.getMaximumSizedROI();
			} catch (DeviceException e) {
				log.error("Cannot get camera size", e);
				return;
			}
			if (dataModel.left < max.getIntPoint()[0]) {
				leftText.setForeground(invalidColor);
				error = true;
			}
			
			if (dataModel.top < max.getIntPoint()[1]) {
				topText.setForeground(invalidColor);
				error = true;
			}
			
			if (dataModel.width <= 0 || dataModel.left + dataModel.width > max.getIntPoint()[0] + max.getIntLength(0)) {
				widthText.setForeground(invalidColor);
				error = true;
			}
			
			if (dataModel.height <= 0 || dataModel.top + dataModel.height > max.getIntPoint()[1] + max.getIntLength(1)) {
				heightText.setForeground(invalidColor);
				error = true;
			}
			
			if (error) {
				applyButton.setEnabled(false);
				return;
			}
			
			if (dataModel.left == 0.0 
					&& dataModel.top == 0.0
					&& dataModel.width == 0.0
					&& dataModel.height == 0.0) {
				applyButton.setEnabled(false);
				return;
			}
			
			RectangularROI current = controller.getCurrentRoi();
			if (dataModel.left == current.getIntPoint()[0] 
					&& dataModel.top == current.getIntPoint()[1]
					&& dataModel.width == current.getIntLength(0)
					&& dataModel.height == current.getIntLength(1)) {
				applyButton.setEnabled(false);
				return;
			}
			applyButton.setEnabled(true);
			
			RectangularROI roi = new RectangularROI(dataModel.left, dataModel.top, dataModel.width, dataModel.height, 0.0);
			controller.setROI(roi);
		}
	}
	
	private DataModel dataModel = new DataModel ();
	
	private DataBindingContext dbc = new DataBindingContext();
	private AbstractCameraConfigurationController controller;
	
	private Text topText;
	private Text leftText;
	private Text widthText;
	private Text heightText;
	
	private Button applyButton;
	private Button clearButton;
	
	private Color validColor;
	private Color invalidColor;
	
	public SensorROIComposite(Composite parent, AbstractCameraConfigurationController controller, int style) {
		super(parent, style);
		
		validColor = SWTResourceManager.getColor(SWT.COLOR_BLACK);
		invalidColor = SWTResourceManager.getColor(SWT.COLOR_RED);

		this.controller = controller;
		ROIListener roiListener = new ROIListener();
		controller.addListener(roiListener);
		
		addListener(SWT.Dispose, e -> controller.removeListener(roiListener));
		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Sensor ROI");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(group);
		
		Composite roiEditComposite = new Composite(group, SWT.SHADOW_NONE);
		GridDataFactory.swtDefaults().grab(true, true).applyTo(roiEditComposite);

		GridLayoutFactory.swtDefaults().numColumns(7).applyTo(roiEditComposite);

		Label label;
		Label spacer;

		/*  Top Row **************************************************************************/
		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText("Left");
		GridDataFactory.swtDefaults().applyTo(label);

		leftText = new Text(roiEditComposite, SWT.RIGHT);
		leftText.setText(NO_VALUE);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, SWT.DEFAULT).applyTo(leftText);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		spacer = new Label(roiEditComposite, SWT.NONE);
		GridDataFactory.swtDefaults().hint(SPACER_SIZE, SWT.DEFAULT).applyTo(spacer);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText("Width");
		GridDataFactory.swtDefaults().applyTo(label);

		widthText = new Text(roiEditComposite, SWT.RIGHT);
		widthText.setText(NO_VALUE);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, SWT.DEFAULT).applyTo(widthText);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		/*  Bottom Row ***********************************************************************/
		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText("Top");
		GridDataFactory.swtDefaults().applyTo(label);

		topText = new Text(roiEditComposite, SWT.RIGHT);
		topText.setText(NO_VALUE);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, SWT.DEFAULT).applyTo(topText);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);
		
		spacer = new Label(roiEditComposite, SWT.NONE);
		GridDataFactory.swtDefaults().hint(SPACER_SIZE, SWT.DEFAULT).applyTo(spacer);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText("Height");
		GridDataFactory.swtDefaults().applyTo(label);

		heightText = new Text(roiEditComposite, SWT.RIGHT);
		heightText.setText(NO_VALUE);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, SWT.DEFAULT).applyTo(heightText);

		label = new Label(roiEditComposite, SWT.LEFT);
		label.setText(UNITS);
		GridDataFactory.swtDefaults().applyTo(label);

		/* Button Composite ******************************************************************/
		
		Composite buttonComposite = new Composite(group, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(buttonComposite);
		
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(buttonComposite);
		
		applyButton = new Button(buttonComposite, SWT.PUSH);
		applyButton.setText("Apply");
		applyButton.addListener(SWT.Selection, e -> {
			try {
				controller.applyROI ();
			} catch (DeviceException ex) {
				String message = "Unable to set region of interest to " + 
						"left: " + dataModel.left + ", top: " + dataModel.top + ", width: " + dataModel.width + 
						", height: " + dataModel.height;
				log.error(message, ex);
				MessageDialog.openError(this.getShell(), "Camera Configuration", message);
			}
		});
		applyButton.setEnabled (false);
		
		clearButton = new Button(buttonComposite, SWT.PUSH);
		clearButton.setText("Clear ROI");
		clearButton.addListener(SWT.Selection, e -> {
			try {
				controller.deleteROI();
			} catch (DeviceException ex) {
				String message = "Unable to clear ROI";
				log.error(message, ex);
				MessageDialog.openError(this.getShell(), "Camera Configuration", message);
			}
		});
		updateClearButton();
		GridDataFactory.swtDefaults().hint(BUTTON_SIZE, SWT.DEFAULT).applyTo(clearButton);
		
		spacer = new Label(buttonComposite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(spacer);

		/* Data Bindings ********************************************************************/
		
		createBinding (leftText, "left");
		createBinding (topText, "top");
		createBinding (widthText, "width");
		createBinding (heightText, "height");
	}
	
	private void updateClearButton () {
		try {
			RectangularROI currentRoi = controller.getCurrentRoi();
			RectangularROI maxRoi = controller.getMaximumSizedROI();
			if (currentRoi.getLength(0) == maxRoi.getLength(0) && currentRoi.getLength(1) == maxRoi.getLength(1)) {
				clearButton.setEnabled(false);
			} else if (currentRoi.getLength(0) <= 0.0 && currentRoi.getLength(1) <= 0.0) {
				clearButton.setEnabled(false);
			} else {
				clearButton.setEnabled(true);
			}
		} catch (DeviceException e) {
			log.error("Cannot find ROI Info", e);
		}
	}
	
	private Function<String, Integer> fromTextToDistance = (String text) -> {
		if (NO_VALUE.equals(text)) {
			return 0;
		}
		return Integer.parseInt((String)text);
	};
	
	private Function<Integer, String> fromDistanceToText = (Integer distance) -> {
		if (distance == 0) {
			return NO_VALUE;
		}
		return String.format("%d", distance);
	};
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createBinding (Text component, String beanProperty) {
		IObservableValue target = WidgetProperties.text(SWT.Modify).observe(component);
		UpdateValueStrategy targetUpdateStrategy = 
				UpdateValueStrategy.create(IConverter.create(String.class, Integer.class, fromTextToDistance));
		
		IObservableValue model = BeanProperties.value(beanProperty).observe(dataModel);
		UpdateValueStrategy modelUpdateStrategy = 
				UpdateValueStrategy.create(IConverter.create(Integer.class, String.class, fromDistanceToText));
		model.addChangeListener(new ChangeListener());

		dbc.bindValue(target, model, targetUpdateStrategy, modelUpdateStrategy);
	}
}
