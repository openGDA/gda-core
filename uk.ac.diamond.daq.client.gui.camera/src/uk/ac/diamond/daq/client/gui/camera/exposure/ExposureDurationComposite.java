package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class ExposureDurationComposite implements CompositeFactory {

	private final AbstractCameraConfigurationController controller;
	private double exposure;
	private DataBindingContext dbc;
	private Slider exposureSlider;
	private Text exposureText;

	private static final Logger logger = LoggerFactory.getLogger(ExposureDurationComposite.class);

	public ExposureDurationComposite(AbstractCameraConfigurationController controller) {
		this.controller = controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = ClientSWTElements.createComposite(parent, style);
		createElements(composite, style);
		bindElements();
		initialiseElements();
		return composite;
	}

	private void createElements(Composite parent, int style) {
		Group group = ClientSWTElements.createGroup(parent, 3, ClientMessages.EXPOSURE, false);
		exposureSlider = ClientSWTElements.createSlider(group, SWT.HORIZONTAL);
		exposureText = ClientSWTElements.createText(group, SWT.NONE, ClientVerifyListener.verifyOnlyIntegerText, null,
				ClientMessages.EMPTY_MESSAGE, GridDataFactory.fillDefaults().minSize(50, 10));
		ClientSWTElements.createLabel(group, SWT.LEFT, ClientMessages.MILLISECONDS_MS, null,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));
	}

	private void initialiseElements() {
		int startExposure = 0;
		try {
			startExposure = (int) (controller.getExposure() * 1000);
		} catch (DeviceException e2) {
			logger.error("Error reading detector exposure", e2);
		}
		exposureSlider.setValues(startExposure, (int) controller.getMinExposure(), (int) controller.getMaxExposure(), 1,
				1, 1);
		exposureText.setText(Integer.toString(startExposure));
	}

	private void bindElements() {
		dbc = new DataBindingContext();
		exposureSlider.addListener(SWT.Selection, new SliderListener());
		IObservableValue targetValue = WidgetProperties.text(SWT.Modify).observe(exposureText);
		targetValue.addChangeListener(getTextListener());

		IObservableValue modelValue = BeanProperties.value("exposure").observe(this);
		dbc.bindValue(targetValue, modelValue);
	}

	private class SliderListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			int value = exposureSlider.getSelection() - 1;
			exposureText.setText(Integer.toString(value));
			try {
				controller.setExposure(value / 1000.0);
			} catch (DeviceException e) {
				logger.error("Error adjusting detector exposure", e);
			}
		}
	}

	private IChangeListener getTextListener() {
		return event -> {
			try {
				exposureSlider.setSelection(Integer.parseInt(exposureText.getText()));
				exposureText.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			} catch (NumberFormatException ex) {
				exposureText.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		};
	}
}
