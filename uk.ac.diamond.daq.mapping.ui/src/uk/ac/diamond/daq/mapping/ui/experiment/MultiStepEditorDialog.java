/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.widgets.selector.BeanConfigurator;
import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.ui.IScannableUIPreferencesService;
import org.eclipse.scanning.api.ui.ScannableUIPreferences;
import org.eclipse.scanning.device.ui.composites.MultiStepComposite;
import org.eclipse.scanning.device.ui.model.ModelPersistAction;
import org.eclipse.scanning.device.ui.model.ModelPersistAction.PersistType;
import org.eclipse.scanning.device.ui.model.TypeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.Activator;

public class MultiStepEditorDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(MultiStepEditorDialog.class);

	private TypeEditor<AxialMultiStepModel> ed;
	private AxialMultiStepModel model;
	private String scannableName;

	public MultiStepEditorDialog(Shell parentShell, String scannableName) {
		super(parentShell);
		this.scannableName = scannableName;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Editing multi-step scan for "+scannableName);
	}

	@Override
	public Control createDialogArea(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);

		Button loadButton = new Button(comp, SWT.PUSH);
		loadButton.setImage(Activator.getImage("icons/folder-import.png"));
		loadButton.setToolTipText("Load multi-step model");

		Button saveButton = new Button(comp, SWT.PUSH);
		saveButton.setImage(Activator.getImage("icons/folder-export.png"));
		saveButton.setToolTipText("Save multi-step model");

		if (model==null) {
			model = new AxialMultiStepModel();
			model.setName(scannableName);
		}
		final IModelProvider<AxialMultiStepModel> modelProvider = new IModelProvider<AxialMultiStepModel>() {

			@Override
			public AxialMultiStepModel getModel() throws Exception {
				return model;
			}

			@Override
			public void updateModel(AxialMultiStepModel model) throws Exception {
				// updateModel is not needed for this implementation
				// Overriding because super throws IllegalArgumentException
			}
		};
		ed = new TypeEditor<>(modelProvider, comp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2,1).applyTo(ed);

		try {
			ed.setModel(model);
		} catch (Exception e) {
			logger.error("Could not set model:",e);
		}

		((MultiStepComposite) ed.getUI()).updateUnits(scannableName);

		// Disabling scannable name combo since dialog is tied to single scannable
		((MultiStepComposite) ed.getUI()).setNameComboEnabled(false);

		// Load default values
		((MultiStepComposite) ed.getUI()).getStepModels().setBeanConfigurator(getBeanConfigurator());

		// save & load logic
		ModelPersistAction<AxialMultiStepModel> load = new ModelPersistAction<>(ed,PersistType.LOAD);
		loadButton.addListener(SWT.Selection, event->load.run());

		ModelPersistAction<AxialMultiStepModel> save = new ModelPersistAction<>(ed,PersistType.SAVE);
		saveButton.addListener(SWT.Selection, event->save.run());

		return ed;
	}

	private BeanConfigurator<AxialStepModel> getBeanConfigurator() {
		ScannableUIPreferences prefs = IScannableUIPreferencesService.DEFAULT.getPreferences(scannableName);
		return (bean, previous, context) -> {
			bean.setStart(previous != null ? previous.getStop() : prefs.getStepModelStart());
			bean.setStop(bean.getStart() + prefs.getStepModelWidth());
			bean.setStep(prefs.getStepModelStep());
		};
	}

	public TypeEditor<AxialMultiStepModel> getEditor() {
		return ed;
	}

	public void setModel(AxialMultiStepModel model) {
		this.model = model;
	}
}
