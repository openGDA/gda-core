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

package uk.ac.gda.rcp.views.sample;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.swtdesigner.SWTResourceManager;

public class SampleMetadataView extends ViewPart {

	protected Text subDirectory;
	protected Text currentDirectory;
	protected Text scanFile;
	protected Text sampleName;

	protected Label scanStatus;
	protected Label scanNumber;
	protected Label remainingTime;
	protected Label elapsedTime;
	protected Label scanPoint;

	protected ProgressBar progressBar;

	@Override
	public void createPartControl(Composite parent) {
		// Increase vertical spacing to 10px
		GridLayoutFactory.swtDefaults().numColumns(5).spacing(5, 10).equalWidth(true).applyTo(parent);

		// Status Row
		scanStatus = createStatusSection(parent, "Scan Status", "Cycle: Running, [Paused], Completed", "UNKNOWN");
		scanNumber = createStatusSection(parent, "Running Scan Number", "Before Scan: Number of the next file to be written; During scan: number of file being written", "UNKNOWN");
		scanPoint = createStatusSection(parent, "Scan Point", null, "/");
		elapsedTime = createStatusSection(parent, "Elapsed", null,  "--:--:--");
		remainingTime = createStatusSection(parent, "Remaining", "Estimate of time remaining based of average time taken so far", "--:--:--");

		// Text rows (full width)
		progressBar = createProgressBar(parent);
		scanFile = createTextSection(parent, "Last Scan File", false);
		currentDirectory = createTextSection(parent, "Current Directory", false);
		subDirectory = createTextSection(parent, "Subdirectory", true);
		sampleName = createTextSection(parent, "Sample Name", true);

		// This is attaching the logic
		new MetadataUpdater(this);
	}

	private Label createStatusSection(Composite parent, String title, String toolTip, String initialStatus) {
		Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		group.setText(title);
		group.setToolTipText(toolTip);
		group.setLayout(new FillLayout(SWT.HORIZONTAL));
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label label = new Label(group, SWT.NONE);
		label.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		label.setAlignment(SWT.CENTER);
		label.setToolTipText(toolTip);
		label.setText(initialStatus);
		return label;
	}

	private Text createTextSection(Composite parent, String label, boolean editable) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Text textbox = new Text(parent, SWT.BORDER);
		textbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		textbox.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		textbox.setText("");
		textbox.setEditable(editable);
		return textbox;
	}

	private ProgressBar createProgressBar(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Progress");

		ProgressBar progBar = new ProgressBar(parent, SWT.NONE);
		progBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		progBar.setMaximum(10000);
		progBar.setMinimum(0);

		return progBar;
	}

	@Override
	public void setFocus() {
		sampleName.setFocus();
	}
}