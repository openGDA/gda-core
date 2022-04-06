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

package uk.ac.diamond.daq.mapping.ui.tomo;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SampleNameSection extends AbstractTomoViewSection {

	private Text sampleNameText;
	private Binding sampleNameBinding;

	protected SampleNameSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
	}

	@Override
	public void createControls(Composite parent) {
		createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		final Label sampleNameLabel = new Label(composite, SWT.NONE);
		sampleNameLabel.setText("Sample Name:");
		GridDataFactory.swtDefaults().applyTo(sampleNameLabel);

		sampleNameText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);
		bindSampleName();
	}

	private void bindSampleName() {
		if (sampleNameBinding != null) sampleNameBinding.dispose();
		final IObservableValue<String> sampleNameTextValue = WidgetProperties.text(SWT.Modify).observe(sampleNameText);
		final IObservableValue<String> sampleNameModelValue = PojoProperties.value("sampleName", String.class).observe(getTomoBean());
		sampleNameBinding = dataBindingContext.bindValue(sampleNameTextValue, sampleNameModelValue);
	}

	@Override
	protected void updateControls() {
		bindSampleName();
	}

}
