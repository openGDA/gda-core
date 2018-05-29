/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.ui.view;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ScanDetailsDialog extends Dialog {

	private static final int HORIZONTAL_WIDTH = 800;

	private final StatusBean statusBean;

	public ScanDetailsDialog(Shell parentShell, StatusBean scanBean) {
		super(parentShell);
		this.statusBean = scanBean;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Scan details");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogComposite = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().applyTo(dialogComposite);

		createDetailsView(dialogComposite);
		return dialogComposite;
	}

	private void createDetailsView(Composite parent) {
		final Composite detailsComposite = new Composite(parent, SWT.NONE);

		// Overall layout is a 2-column grid
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detailsComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(5,0).spacing(10, 10).applyTo(detailsComposite);

		createLabel(detailsComposite, "Name");
		createTextBox(detailsComposite, statusBean.getName());

		createLabel(detailsComposite, "Status");
		createTextBox(detailsComposite, statusBean.getStatus().toString());

		createLabel(detailsComposite, "Complete");
		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setRoundingMode(RoundingMode.DOWN);
		createTextBox(detailsComposite, percentFormat.format(statusBean.getPercentComplete() / 100d));

		createLabel(detailsComposite, "Date submitted");
		createTextBox(detailsComposite, DateFormat.getDateTimeInstance().format(new Date(statusBean.getSubmissionTime())));

		createLabel(detailsComposite, "Message");
		createTextBox(detailsComposite, statusBean.getMessage());

		createLabel(detailsComposite, "Location");
		createTextBox(detailsComposite, getLocation(statusBean));

		createLabel(detailsComposite, "Host");
		createTextBox(detailsComposite, statusBean.getHostName());

		createLabel(detailsComposite, "User name");
		createTextBox(detailsComposite, statusBean.getUserName());

		createLabel(detailsComposite, "Start time");
		final long startTime = statusBean.getStartTime();
		final String startTimeString = (startTime == 0) ? "" : DateFormat.getTimeInstance().format(new Date(startTime));
		createTextBox(detailsComposite, startTimeString);

		createLabel(detailsComposite, "Estimated end time");
		final long estimatedEndTime = statusBean.getStartTime() + statusBean.getEstimatedTime();
		final String estimatedEndTimeString = (estimatedEndTime == 0) ? "" : DateFormat.getTimeInstance().format(new Date(estimatedEndTime));
		createTextBox(detailsComposite, estimatedEndTimeString);
}

	private static void createLabel(Composite dialogComposite, String text) {
		final Label label = new Label(dialogComposite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
		label.setText(text);
	}

	private static void createTextBox(Composite dialogComposite, String text) {
		final Text textBox = new Text(dialogComposite, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).hint(HORIZONTAL_WIDTH, SWT.DEFAULT).applyTo(textBox);
		textBox.setText(text);
	}

	private static String getLocation(final StatusBean statusBean) {
		if (statusBean instanceof ScanBean) {
			return ((ScanBean) statusBean).getFilePath();
		}
		return statusBean.getRunDirectory();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
	}

}
