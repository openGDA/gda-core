/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.device.Scannable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class MetadataComposite extends Composite {

	private FindableNameWrapper scannableName;
	private Label scannableNameLabel;

	public MetadataComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		scannableNameLabel = new Label(this, SWT.NONE);
		scannableNameLabel.setText("Scannable Name");

		scannableName = new FindableNameWrapper(this, SWT.BORDER, Scannable.class);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint=260;
		scannableName.setLayoutData(gd);
		scannableName.addValueListener(new ValueAdapter("Metadata Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {

			}
		});
	}

	public void selectionChanged(MetadataParameters metParams) {
		String name = scannableName.getValue().toString();
		if (name != null && metParams != null) {
			metParams.setScannableName(name);
			scannableName.refresh();
		}
	}
	
	/**
	 * @return s
	 */
	public TextWrapper getScannableName() {
		return scannableName;
	}
}
