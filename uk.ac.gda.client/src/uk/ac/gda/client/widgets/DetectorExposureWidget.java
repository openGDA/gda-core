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

package uk.ac.gda.client.widgets;

import static uk.ac.gda.ui.tool.ClientSWTElements.getImage;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveDoubleText;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Simple exposure controls consisting of text box and button to update text value with exposure from hardware.
 * Instantiated with consumer/supplier for binding, and initial state may be set with {@link #updateFromModel(double)}
 * or {@link #updateFromHardware()}.
 */
public class DetectorExposureWidget {

	private final DoubleConsumer consumer;
	private final DoubleSupplier supplier;

	private Text exposure;

	/**
	 * @param parent Composite on which to create this widget.
	 * @param consumer Setter in model, or some intermediate method which consumes the specified exposure. Invoked on SWT.Modify events.
	 * @param supplier Source of current exposure. Used to synchronise with hardware.
	 */
	public DetectorExposureWidget(Composite parent, DoubleConsumer consumer, DoubleSupplier supplier) {
		this.consumer = consumer;
		this.supplier = supplier;

		createComposite(parent);
	}

	private Composite createComposite(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(control);

		var grabHorizontally = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		grabHorizontally.applyTo(control);

		exposure = new Text(control, SWT.BORDER);
		exposure.setToolTipText("Detector exposure in seconds");
		exposure.addVerifyListener(verifyOnlyPositiveDoubleText);
		grabHorizontally.applyTo(exposure);

		var fetch = new Button(control, SWT.PUSH);
		fetch.setToolTipText("Fetch current exposure from detector");
		var icon = getImage(ClientImages.CAMERA);
		fetch.setImage(icon);
		fetch.addDisposeListener(dispose -> icon.dispose());

		exposure.addModifyListener(modify -> consumer.accept(Double.parseDouble(exposure.getText())));
		fetch.addSelectionListener(SelectionListener.widgetSelectedAdapter(selection -> updateFromHardware()));

		return control;
	}

	/**
	 * Updates exposure in text box according from the configured supplier.
	 * Programmatic equivalent of selecting the "fetch" button
	 */
	public void updateFromHardware() {
		updateExposureWidget(supplier.getAsDouble());
	}

	/**
	 * Updates exposure in text box with given exposure
	 */
	public void updateFromModel(double exposure) {
		updateExposureWidget(exposure);
	}

	private void updateExposureWidget(double newExposure) {
		exposure.setText(String.valueOf(newExposure));
	}

}
