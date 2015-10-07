/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.wizards;

import gda.configuration.properties.LocalProperties;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.reflection.IBeanService;
import org.eclipse.richbeans.widgets.file.FileBox;
import org.eclipse.richbeans.widgets.file.FileBox.ChoiceType;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.exafs.ExafsActivator;

public abstract class ImportROIWizardPage extends WizardPage {

	protected Button addButton;
	protected Button addToAllButton;
	protected Composite mainComposite;
	protected ScrolledComposite scrolledComp;
	protected ListEditor currentDetectorList;


	public ImportROIWizardPage() {
		super("Import Regions Of Interest");
		setDescription("Import Regions Of Interest");
	}

	public abstract List<? extends DetectorROI> getBeansToAdd();

	protected abstract void createSourceControls(Composite parent);
	protected abstract void createDestinationControls(Composite parent);
	protected abstract void newSourceSelected(IPath path);
	protected abstract void performAdd();
	protected abstract void performAddAll();
	protected abstract boolean currentSourceValid();
	protected abstract void updateEnables();

	/**
	 * Override to set an initial value for the file source box.
	 * <p>
	 * e.g. On have getInitialSourceValue() return the contents of a property
	 * such as gda.gui.import.xspress.defaultValue
	 * @return the initial value to use in the dialog
	 */
	protected String getInitialSourceValue() {
		String initialSource = LocalProperties.get("gda.spec.windows.location", null);
		if(initialSource == null)
			return "";
		return initialSource;
	}

	@Override
	public void createControl(Composite incoming) {
		scrolledComp = new ScrolledComposite(incoming, SWT.H_SCROLL | SWT.V_SCROLL);
		GridLayoutFactory.swtDefaults().applyTo(scrolledComp);

		mainComposite = new Composite(scrolledComp, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(mainComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(mainComposite);
		setControl(mainComposite);


		Composite fileSelectionArea = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(fileSelectionArea);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(fileSelectionArea);


		Label configLabel = new Label(fileSelectionArea, SWT.NONE);
		configLabel.setText("Select Detector File to import");

		final FileBox configFileName = new FileBox(fileSelectionArea, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(configFileName);
		configFileName.setChoiceType(ChoiceType.FULL_PATH);
		configFileName.setFilterExtensions(new String[] { "*.xml" });
		configFileName.addValueListener(new ValueAdapter("file import file name") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				final String name = (String)e.getValue();
				newSourceSelected(new Path(name));
				updateEnables();
			}
		});
		configFileName.on();

		Composite main = new Composite(mainComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).applyTo(main);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(main);


		Composite left = new Composite(main, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(left);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(false, true).applyTo(left);


		Composite centre = new Composite(main, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(centre);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(false, true).applyTo(centre);


		Composite right = new Composite(main, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(right);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(right);


		createSourceControls(left);
		createControlButtons(centre);
		createDestinationControls(right);

		scrolledComp.setContent(mainComposite);
		scrolledComp.setExpandHorizontal(true);
		scrolledComp.setExpandVertical(true);
		mainComposite.layout(true, true);
		scrolledComp.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// set an initial value for the import wizard.
		// this is where you may want to point at a pre-existing file stored for the entire beamline
		configFileName.setText(getInitialSourceValue());
	}

	private void createControlButtons(Composite centre) {
		addButton = new Button(centre, SWT.NONE);
		addButton.setText("Copy Selected >>>");
		GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(addButton);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performAdd();
			}
		});
		addToAllButton = new Button(centre, SWT.NONE);
		addToAllButton.setText("Copy to All >>>");
		GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(addToAllButton);
		addToAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performAddAll();
			}
		});
	}


	protected void setEnables(Control control, boolean enabled) {
		control.setEnabled(enabled);
		if (control instanceof Composite) {
			Composite composite = (Composite)control;
			for (Control child : composite.getChildren()) {
				setEnables(child, enabled);
			}
		}
	}

	public void setListEditor(ListEditor detectorList) {
		this.currentDetectorList = detectorList;

	}

	protected void beanToUI(Object ui, Object bean) throws Exception {
		IBeanService service = ExafsActivator.getService(IBeanService.class);
		IBeanController control = service.createController(ui, bean);
		control.switchState(false);
		control.beanToUI();
		control.switchState(true);
	}

}
