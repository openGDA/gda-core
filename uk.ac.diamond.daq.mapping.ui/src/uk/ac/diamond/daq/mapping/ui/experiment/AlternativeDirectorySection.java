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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientMessages.ALTERNATIVE_DIRECTORY_USE;
import static uk.ac.gda.ui.tool.ClientMessages.ALTERNATIVE_DIRECTORY_USE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.BROWSE;
import static uk.ac.gda.ui.tool.ClientMessages.BROWSE_DIRECTORY;
import static uk.ac.gda.ui.tool.ClientMessages.CHOOSE_DIRECTORY;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.NOT_A_DIRECTORY;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

/**
 * UI to allow the user to specify that scans should be written to an alternative directory.
 * <p>
 * This can be used for example during testing to write scans to a temporary directory so they do not get mixed up with
 * files from "real" scans.
 */
public class AlternativeDirectorySection extends AbstractMappingSection {
	private static final int NUM_COLUMNS = 3;

	private final DirectoryModel directoryModel = new DirectoryModel();
	private Binding directoryBinding;

	/**
	 * Check box to indicate whether to use an alternative directory for output
	 */
	private Button useCheck;
	/**
	 * Path to the alternative directory
	 */
	private Text directoryText;
	/**
	 * Button opens a dialog to browse for directory
	 */
	private Button browseButton;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		dataBindingContext = new DataBindingContext();
		final IMappingExperimentBean mappingBean = getMappingBean();

		final Composite tempDirectoryComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, NUM_COLUMNS);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tempDirectoryComposite);

		useCheck = createClientButton(tempDirectoryComposite, SWT.CHECK, ALTERNATIVE_DIRECTORY_USE, ALTERNATIVE_DIRECTORY_USE_TP);
		createClientGridDataFactory().applyTo(useCheck);
		useCheck.setSelection(mappingBean.isUseAlternativeDirectory());
		useCheck.addSelectionListener(widgetSelectedAdapter(e -> setEnabledState()));

		directoryText = createClientText(tempDirectoryComposite, SWT.NONE, EMPTY_MESSAGE);
		createClientGridDataFactory().grab(true, true).applyTo(directoryText);
		directoryText.setText(mappingBean.getAlternativeDirectory());

		browseButton = createClientButton(tempDirectoryComposite, SWT.PUSH, BROWSE, BROWSE_DIRECTORY);
		createClientGridDataFactory().applyTo(browseButton);
		browseButton.addSelectionListener(widgetSelectedAdapter(e -> selectDirectory()));

		createDataBindings();
		setEnabledState();
	}


	private void createDataBindings() {
		// Data binding for the check box
		final IObservableValue<Boolean> useCheckBoxModelObservable = BeanProperties.value(DirectoryModel.class, "use", Boolean.class).observe(directoryModel);
		final IObservableValue<Boolean> useCheckBoxWidgetObservable = WidgetProperties.buttonSelection().observe(useCheck);
		dataBindingContext.bindValue(useCheckBoxWidgetObservable, useCheckBoxModelObservable);

		// When the user changes the text box, check that the directory exists
		final UpdateValueStrategy<String, String> setDirectoryStrategy = new UpdateValueStrategy<>();
		setDirectoryStrategy.setBeforeSetValidator(value -> {
			// Make sure tooltip is up to date
			final String newDirectoryString = value;
			directoryText.setToolTipText(newDirectoryString);
			// If the "use" check box is not selected, we don't care whether the path is valid
			if (!useCheck.getSelection()) {
				return ValidationStatus.ok();
			}
			// Otherwise, check that the text box contains a valid directory path
			final File newDirectory = new File(newDirectoryString);
			return newDirectory.isDirectory() ? ValidationStatus.ok() : ValidationStatus.error(getMessage(NOT_A_DIRECTORY));
		});

		// Nothing particular to check when binding from text box
		final UpdateValueStrategy<String, String> setTextBoxStrategy = new UpdateValueStrategy<>();

		// Data binding for the text box
		final IObservableValue<String> directoryModelObservable = BeanProperties.value(DirectoryModel.class, "directory", String.class).observe(directoryModel);
		final IObservableValue<String> directoryWidgetObservable = WidgetProperties.text(SWT.Modify).observe(directoryText);
		directoryBinding = dataBindingContext.bindValue(directoryWidgetObservable, directoryModelObservable, setDirectoryStrategy, setTextBoxStrategy);
		ControlDecorationSupport.create(directoryBinding, SWT.ARROW_LEFT | SWT.TOP);
	}

	/**
	 * Enable/disable text & browse button when the check box is checked/unchecked
	 */
	private void setEnabledState() {
		final boolean enabled = useCheck.getSelection();
		directoryText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	/**
	 * Open a dialog to allow the user to select the alternative directory
	 */
	private void selectDirectory() {
		final Shell shell = Display.getDefault().getActiveShell();
		final DirectoryDialog chooseDirectoryDialog = new DirectoryDialog(shell);
		chooseDirectoryDialog.setText(getMessage(CHOOSE_DIRECTORY));
		chooseDirectoryDialog.setFilterPath(InterfaceProvider.getPathConstructor().getClientVisitDirectory());
		final String selectedDirectory = chooseDirectoryDialog.open();
		if (selectedDirectory != null) {
			directoryText.setText(selectedDirectory);
		}
	}

	/**
	 * Class to bind the values of the "Use alternative directory" check box and the text box containing the directory
	 * path to the mapping bean.
	 * <p>
	 * Functions marked as "unused" are in fact required for data binding to work.
	 */
	private class DirectoryModel {
		private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	    @SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.addPropertyChangeListener(listener);
	    }

	    @SuppressWarnings("unused")
		public void removePropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.removePropertyChangeListener(listener);
	    }

		@SuppressWarnings("unused")
		public String getDirectory() {
			return getMappingBean().getAlternativeDirectory();
		}

		@SuppressWarnings("unused")
		public void setDirectory(String directory) {
			final IMappingExperimentBean mappingBean = getMappingBean();
			final String oldDirectory = mappingBean.getAlternativeDirectory();
			mappingBean.setAlternativeDirectory(directory);
			changeSupport.firePropertyChange("directory", oldDirectory, directory);
		}

		@SuppressWarnings("unused")
		public boolean isUse() {
			return getMappingBean().isUseAlternativeDirectory();
		}

		@SuppressWarnings("unused")
		public void setUse(boolean use) {
			getMappingBean().setUseAlternativeDirectory(use);
			AlternativeDirectorySection.this.directoryBinding.validateTargetToModel();
		}
	}
}
