/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditorUI;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.exafs.ui.dialogs.GainWizard;
import uk.ac.gda.exafs.util.GainCalculation;

public class DrainCurrentComposite extends Composite implements ListEditorUI {

	private TextWrapper deviceName;
	private TextWrapper name;
	private TextWrapper currentAmplifierName;
	private SpinnerWrapper channel;
	private ComboWrapper gain;

	private ExpandableComposite advancedExpandableComposite;

	private ExpansionAdapter expansionListener;
	private BooleanWrapper changeSensitivity;

	/**
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("unused")
	public DrainCurrentComposite(Composite parent, int style, final IBeanController control) {


		super(parent, style);
		setLayout(new GridLayout());

		final Composite main = new Composite(this, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 140;
		main.setLayoutData(gd);
		main.setLayout(new GridLayout(2, true));

		final Composite gainProperties = new Composite(main, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gainProperties.setLayout(gridLayout);
		gainProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		changeSensitivity = new BooleanWrapper(gainProperties, SWT.NONE);
		changeSensitivity.setText("Change the sensitivity during data collection");
		changeSensitivity
				.setToolTipText("Select for the amplifier sensitivity to be adjust during data collection.\nIf unselected then the current sensitivity will be left unchanged.");
		changeSensitivity.setValue(false);

		new Label(gainProperties, SWT.NONE);

		final Label gainLabel = new Label(gainProperties, SWT.NONE);
		gainLabel.setText("Sensitivity");
		gainLabel
				.setToolTipText("The gain setting on the amplifier.\n(This cannot be linked to get the gain as the Stanford Amplifier does not have a get for the gain, only a set.)");

		gain = new ComboWrapper(gainProperties, SWT.READ_ONLY);
		gain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final List<String> notches = GainCalculation.getGainNotches();
		gain.setItems(notches.toArray(new String[notches.size()]));
		gain.addButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog dialog = new WizardDialog(getShell(), new GainWizard(control));
				dialog.setPageSize(new Point(780, 550));
				dialog.create();
				dialog.open();
			}
		});
	}

	@Override
	public void dispose() {
		advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}

	/**
	 * @return variable
	 */
	public ComboWrapper getGain() {
		return gain;
	}
	/**
	 * @return variable
	 */
	public SpinnerWrapper getChannel() {
		return channel;
	}
	/**
	 * @return variable
	 */
	public TextWrapper getCurrentAmplifierName() {
		return currentAmplifierName;
	}
	/**
	 * @return variable
	 */
	// suppressing that getName is in the superclass hierarchy as a private
	@SuppressWarnings("all")
	public TextWrapper getName() {
		return name;
	}

	public BooleanWrapper getChangeSensitivity() {
		return changeSensitivity;
	}

	/**
	 * @return variable
	 */
	public TextWrapper getDeviceName() {
		return deviceName;
	}

	@Override
	public boolean isAddAllowed(ListEditor listEditor) {
		return false;
	}

	@Override
	public boolean isDeleteAllowed(ListEditor listEditor) {
		return false;
	}

	@Override
	public boolean isReorderAllowed(ListEditor listEditor) {
		return false;
	}

	@Override
	public void notifySelected(ListEditor listEditor) {
	}

}

