/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.view;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.IScanResolutionLookupProvider;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigContent;

public class AdditionalInfoDialogCellEditor extends DialogCellEditor {

	private final TomoConfigContent configContent;
	private final IScanResolutionLookupProvider scanResolutionProvider;

	public AdditionalInfoDialogCellEditor(Table table, TomoConfigContent configContent,
			IScanResolutionLookupProvider scanResolutionProvider) {
		super(table);
		this.configContent = configContent;
		this.scanResolutionProvider = scanResolutionProvider;
	}

	@Override
	protected Button createButton(Composite parent) {
		Button btn = super.createButton(parent);
		btn.setText("Click...");
		return btn;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		AdditionalInfoDialog dialog = new AdditionalInfoDialog(cellEditorWindow.getShell(), "Additional Information",
				configContent, scanResolutionProvider);
		return dialog.open() == Window.OK ? null : null;
	}

	public static class AdditionalInfoDialog extends Dialog {

		private static final String NO_SCAN_MSG = "No Scans run for this configuration yet.";
		private static final String NUMBER_OF_PROJECTIONS = "Number of projections: ";
		private static final String SCAN_NUMBER = "Scan Number: ";
		private static final String SCAN_START_TIME = "Scan start time: ";
		private static final String SCAN_END_TIME = "Scan End Time: ";
		private final String header;
		private final TomoConfigContent tomoConfigContent;
		private final IScanResolutionLookupProvider scanResolutionProvider;
		private static final Logger logger = LoggerFactory.getLogger(AdditionalInfoDialog.class);

		protected AdditionalInfoDialog(Shell parentShell, String header, TomoConfigContent configContent,
				IScanResolutionLookupProvider scanResolutionProvider) {
			super(parentShell);
			this.header = header;
			this.tomoConfigContent = configContent;
			this.scanResolutionProvider = scanResolutionProvider;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(header);
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			Control createButtonBar = super.createButtonBar(parent);
			createButtonBar.setBackground(ColorConstants.white);
			return createButtonBar;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			parent.setBackground(ColorConstants.white);
			composite.setBackground(ColorConstants.white);
			FormToolkit formToolkit = new FormToolkit(composite.getDisplay());

			formToolkit.setBackground(ColorConstants.white);

			Composite infoComposite = formToolkit.createComposite(composite);

			GridLayout layout = new GridLayout(2, false);

			infoComposite.setLayout(layout);

			formToolkit.createLabel(infoComposite, NUMBER_OF_PROJECTIONS);

			try {
				int numberOfProjections = scanResolutionProvider.getNumberOfProjections(RESOLUTION.get(
						tomoConfigContent.getResolution()).getResolutionNumber());
				formToolkit.createLabel(infoComposite, Integer.toString(numberOfProjections));

			} catch (Exception e) {
				logger.error("Problem getting number of projections", e);
			}

			int mostRecentScanNumber = tomoConfigContent.getMostRecentScanNumber();
			if (mostRecentScanNumber > 0) {
				formToolkit.createLabel(infoComposite, SCAN_NUMBER);
				formToolkit.createLabel(infoComposite, Integer.toString(mostRecentScanNumber));

				formToolkit.createLabel(infoComposite, SCAN_START_TIME);
				formToolkit.createLabel(infoComposite, tomoConfigContent.getMostRecentStartTime());

				formToolkit.createLabel(infoComposite, SCAN_END_TIME);
				formToolkit.createLabel(infoComposite, tomoConfigContent.getMostRecentEndTime());
			} else {
				Label lblNoScansRun = formToolkit.createLabel(infoComposite, NO_SCAN_MSG);
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				lblNoScansRun.setLayoutData(gd);
			}
			return composite;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}

	}

}
