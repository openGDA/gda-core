package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

public class ExposureLengthComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(ExposureLengthComposite.class);
	
	private static final int MAX_EXPOSURE = 1000;
	private static final int MIN_EXPOSURE = 5;
	
	@SuppressWarnings("unused")
	private class Exposure {
		double exposure;
		
		double getExposure () {
			return exposure;
		}
		
		void setExposure(double exposure) {
			this.exposure = exposure;
		}
	}
	
	private class SliderListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			double value = (double)(exposureSlider.getSelection() - 1);
			value = exposureSlider.getMaximum() - (value * exposureSlider.getMinimum());
			value /= exposureSlider.getMaximum();
			value = Math.log10(value);
			value *= -1 * (MAX_EXPOSURE - MIN_EXPOSURE);
			value += MIN_EXPOSURE;
			exposureText.setText(String.format("%.0f", value));
		}
	}
	
	private Exposure exposure;
	private DataBindingContext dbc;
	private Slider exposureSlider;
	private Text exposureText;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExposureLengthComposite(Composite parent, int style) {
		super(parent, style);
		
		exposure = new Exposure();
		dbc = new DataBindingContext();

		GridLayoutFactory.fillDefaults().applyTo(this);
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Exposure:");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(group);

		exposureSlider = new Slider(group, SWT.HORIZONTAL);
		exposureSlider.setValues(0, 1, 20, 1, 1, 1);
		exposureSlider.addListener(SWT.Selection, new SliderListener());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(exposureSlider);

		exposureText = new Text(group, SWT.RIGHT | SWT.BORDER);
		exposureText.setText("0");
		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(exposureText);
		IObservableValue targetValue = WidgetProperties.text(SWT.Modify).observe(exposureText);
		targetValue.addChangeListener(e -> {
			try {
				setExposure (Double.parseDouble(exposureText.getText().trim()));
				exposureText.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			} catch (NumberFormatException ex) {
				exposureText.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		});
		IObservableValue modelValue = BeanProperties.value("exposure").observe(exposure);
		dbc.bindValue(targetValue, modelValue);

		Label label = new Label(group, SWT.LEFT);
		label.setText("ms");
		GridDataFactory.swtDefaults().applyTo(label);
	}

	private void setExposure (double exposure) {
		double selection = exposure - exposureSlider.getMinimum();
		selection *= -1.0 / (MAX_EXPOSURE - MIN_EXPOSURE);
		selection = Math.pow(10, selection);
		selection = exposureSlider.getMaximum() - (selection * exposureSlider.getMaximum());
		selection /= exposureSlider.getMinimum();
		selection += 1;
		log.info("Selection: {}", (int)Math.round(selection));
		exposureSlider.setSelection((int)Math.round(selection));
	}
}
