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

package uk.ac.gda.client.widgets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.observable.IObserver;

/**
 * Control the position of a ControlPoint, warning the user if they have input an invalid value
 */
public class PositionControlComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(PositionControlComposite.class);

	/**
	 * Width of {@link #positionText}
	 */
	private static final int TEXT_WIDTH = 40;

	/**
	 * Text box to edit the position
	 */
	private final Text positionText;

	private final ControlPoint positionControlPoint;

	private final PositionControlBinding positionReadbackBinding;

	private final DataBindingContext dataBindingContext;

	public PositionControlComposite(Composite parent, int style, ControlPoint positionControlPoint) {
		super(parent, style);
		Objects.requireNonNull(positionControlPoint, "Position control point must not be null");
		this.positionControlPoint = positionControlPoint;
		GridLayoutFactory.swtDefaults().spacing(1,1).applyTo(this);

		dataBindingContext = new DataBindingContext();
		positionReadbackBinding = new PositionControlBinding();

		positionText = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(positionText);

		// Check the value entered for position
		final UpdateValueStrategy setPositionStrategy = new UpdateValueStrategy();
		setPositionStrategy.setBeforeSetValidator(value -> {
			try {
				Double.parseDouble(positionText.getText());
				return ValidationStatus.ok();
			} catch (Exception e) {
				return ValidationStatus.error("Invalid number for position, must be a number");
			}
		});

		// Nothing particular to check when binding from hardware value to text box
		final UpdateValueStrategy setTextBoxStrategy = new UpdateValueStrategy();
		// TODO: Switch to typed version after platform update
		//final UpdateValueStrategy<String, Double> setTextBoxStrategy = new UpdateValueStrategy<>();

		// Set up data binding to keep position and text box in sync
		@SuppressWarnings("unchecked")
		final IObservableValue<Double> positionReadbackObservable = BeanProperties.value(PositionControlBinding.class, "position", Double.class).observe(positionReadbackBinding);
		@SuppressWarnings("unchecked")
		final IObservableValue<String> positionObservable = WidgetProperties.text(SWT.Modify).observe(positionText);
		final Binding positionBinding = dataBindingContext.bindValue(positionObservable, positionReadbackObservable, setPositionStrategy, setTextBoxStrategy);
		ControlDecorationSupport.create(positionBinding, SWT.LEFT | SWT.TOP);

		positionReadbackBinding.getPosition();

		// positionReadbackBinding needs to listen for changes from the hardware
		positionControlPoint.addIObserver(positionReadbackBinding);
		parent.addDisposeListener(e -> positionControlPoint.deleteIObserver(positionReadbackBinding));
	}

	private void displayError(final String message) {
		MessageDialog.openError(Display.getDefault().getActiveShell(), "Position control error", message);
	}

	/**
	 * Class for data binding to use to mediate between the position control and the text box
	 * <p>
	 * Functions marked as "unused" are in fact required for data binding to work.
	 * <p>
	 * As the position can also be set in the console or directly in Epics, this class needs
	 * to observe the hardware and respond to {@link Double} events.
	 */
	private class PositionControlBinding implements IObserver {
		// Allow for inaccuracy in floating point values
		private static final double FP_TOLERANCE = 1e-12;

		private double position;

		private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		@SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		@SuppressWarnings("unused")
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		public double getPosition() {
			try {
				// Always refresh position from the hardware
				position = positionControlPoint.getValue();
			} catch (DeviceException e) {
				final String message = String.format("Error getting position on %s", positionControlPoint.getName());
				logger.error(message, e);
				displayError(message);
			}
			return position;
		}

		@SuppressWarnings("unused")
		public void setPosition(double position) {
			try {
				positionControlPoint.setValue(position);
			} catch (Exception e) {
				final String message = String.format("Error setting position on %s", positionControlPoint.getName());
				logger.error(message, e);
				displayError(message);
			}
		}

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof Double) {
				final double oldPosition = position;
				position = (Double) arg;
				// Update the text box if the position has been changed by an external event e.g. on the command line
				if (Math.abs(position - oldPosition) > FP_TOLERANCE) {
					logger.debug("Position of changed from {} to {}", oldPosition, position);
					changeSupport.firePropertyChange("position", oldPosition, position);
				}
			}
		}

		@Override
		public String toString() {
			return "PositionControlBinding [position=" + position + "]";
		}
	}
}
