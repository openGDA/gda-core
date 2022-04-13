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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ScanMetadataSection extends AbstractTomoViewSection {

	private Text sampleNameText;

	private Text backgroundFilePathText;

	protected ScanMetadataSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
	}

	@Override
	public void createControls(Composite parent) {
		createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		createSampleNameControls(composite);
		createBackgroundFilePathControls(composite);

		bindTextControls();
	}

	private void createSampleNameControls(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Sample Name:");
		GridDataFactory.swtDefaults().applyTo(label);

		sampleNameText = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);
	}

	private void createBackgroundFilePathControls(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Background file:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Composite composite = createComposite(parent, 2, false);

		backgroundFilePathText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(backgroundFilePathText);

		final Button selectFileButton = new Button(composite, SWT.PUSH);
		selectFileButton.setText("Browse..."); // TODO use image instead of text?
		GridDataFactory.swtDefaults().applyTo(selectFileButton);
		selectFileButton.addSelectionListener(widgetSelectedAdapter(e -> chooseBackgroundFile()));
		// TODO do we also need a choose within workspace button?
	}

	private void chooseBackgroundFile() {
		final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		// TODO any particular file extension (existing UI doesn't apply one)
		dialog.setFilterPath(getVisitConfigDir());
		dialog.setOverwrite(true);

		final String fileName = dialog.open();
		if (fileName != null) {
			backgroundFilePathText.setText(fileName);
		}
	}

	private void bindTextControls() {
		disposeOldBindings();

		final IObservableValue<String> sampleNameTextValue = WidgetProperties.text(SWT.Modify).observe(sampleNameText);
		final IObservableValue<String> sampleNameModelValue = PojoProperties.value("sampleName", String.class).observe(getTomoBean());
		dataBindingContext.bindValue(sampleNameTextValue, sampleNameModelValue);

		final IObservableValue<String> backgroundFilePathTextValue = WidgetProperties.text(SWT.Modify).observe(backgroundFilePathText);
		final IObservableValue<String> backgroundFilePathModelValue = PojoProperties.value("backgroundFilePath", String.class).observe(getTomoBean());
		dataBindingContext.bindValue(backgroundFilePathTextValue, backgroundFilePathModelValue);
	}

	@Override
	protected void updateControls() {
		bindTextControls();
	}

}
