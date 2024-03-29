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

package uk.ac.diamond.daq.mapping.ui.experiment.copyscan;

import static uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanWizard.PROPERTY_NAME_CLASS_NAME;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_CLASS_NAME;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_ENTER_NAME_DESCRIPTION;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_ENTER_NAME_INVALID;
import static uk.ac.gda.ui.tool.ClientMessages.COPY_SCAN_ENTER_NAME_TITLE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Displays a text input box to allow the user to specify the name of the class to be generated from the current scan
 * definition.<br>
 * The class name must conform to VALID_CLASS_NAME_REGEX.
 */
class ChooseClassNameWizardPage extends WizardPage {
	private static final int NUM_COLUMNS = 2;
	private static final int TEXT_LENGTH = 500;

	private static final String VALID_CLASS_NAME_REGEX = "[A-Z][a-zA-Z0-9_]*";

	private final DataBindingContext bindingContext = new DataBindingContext();

	protected ChooseClassNameWizardPage() {
		super(ChooseClassNameWizardPage.class.getSimpleName());
		setTitle(ClientMessagesUtility.getMessage(COPY_SCAN_ENTER_NAME_TITLE));
		setDescription(ClientMessagesUtility.getMessage(COPY_SCAN_ENTER_NAME_DESCRIPTION));
	}

	@Override
	public void createControl(Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(mainComposite);

		final Label classNameLabel = createClientLabel(mainComposite, SWT.NONE, COPY_SCAN_CLASS_NAME);
		classNameLabel.setFont(CopyScanWizard.DEFAULT_FONT);

		final Text classNameText = createClientText(mainComposite, SWT.LEFT, COPY_SCAN_CLASS_NAME);
		GridDataFactory.swtDefaults().hint(TEXT_LENGTH, SWT.DEFAULT).applyTo(classNameText);
		classNameText.setFont(CopyScanWizard.DEFAULT_FONT);

		final IObservableValue<String> classNameModel = PojoProperties.value(PROPERTY_NAME_CLASS_NAME, String.class).observe(getWizard());
		final IObservableValue<String> classNameWidget = WidgetProperties.text(SWT.Modify).observe(classNameText);

		final UpdateValueStrategy<String, String> strategy = new UpdateValueStrategy<>();
		strategy.setBeforeSetValidator(this::validateClassName);
		final Binding bindValue = bindingContext.bindValue(classNameWidget, classNameModel, strategy, null);
		ControlDecorationSupport.create(bindValue, SWT.ON_TOP | SWT.LEFT);

		setControl(mainComposite);
		setPageComplete(validateClassName(classNameModel.getValue()).isOK());
	}

	private IStatus validateClassName(Object value) {
		if (!(value instanceof String)) {
			return handleInvalidClassName();
		}
		final String valueString = (String) value;
		if (valueString.isBlank()) {
			// An empty class name is not flagged as an error, but you cannot go to the next page
			setPageComplete(false);
			return ValidationStatus.ok();
		}
		if (valueString.matches(VALID_CLASS_NAME_REGEX)) {
			setPageComplete(true);
			return ValidationStatus.ok();
		}
		return handleInvalidClassName();
	}

	private IStatus handleInvalidClassName() {
		setPageComplete(false);
		return ValidationStatus.error(ClientMessagesUtility.getMessage(COPY_SCAN_ENTER_NAME_INVALID));
	}
}
