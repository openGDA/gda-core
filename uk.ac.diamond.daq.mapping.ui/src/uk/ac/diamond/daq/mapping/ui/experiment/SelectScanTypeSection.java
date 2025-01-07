/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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


import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SelectScanTypeSection extends AbstractMappingSection {

	private SubmitScanSelector submitScanSelector;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		final Composite sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(sectionComposite);

		setupGui(sectionComposite, submitScanSelector);
	}

	private void setupGui(Composite parent, SubmitScanSelector selector) {
		if (selector == null) {
			Label l = new Label(parent, SWT.NONE);
			l.setText("Not creating scan selection controls - SubmitScanSelector has not been set.");
			return;
		}

		Label label = new Label(parent, SWT.NONE);
		label.setText("Select scan type : ");

		String[] allItems = selector.getDescriptions().toArray(new String[] {});
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.addListener(SWT.MouseVerticalWheel, ev -> ev.doit = false); // disable mouse scroll behaviour
		combo.setItems(allItems);
		combo.select(0);
		combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> selector.showSection(combo.getSelectionIndex())));
	}

	public void setSubmitScanSelector(SubmitScanSelector submitScanSelector) {
		this.submitScanSelector = submitScanSelector;
	}
}
