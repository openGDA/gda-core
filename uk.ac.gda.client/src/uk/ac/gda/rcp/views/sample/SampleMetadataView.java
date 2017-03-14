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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.swtdesigner.SWTResourceManager;

public class SampleMetadataView extends ViewPart {
	public SampleMetadataView() {
	}
	protected Text subDirectory;
	protected Text currentDirectory;
	protected Text scanFile;
	protected Text sampleName;


	protected Label scanStatLbl;
	protected Label scanNumLbl;
	protected Label remainTimeLbl;
	protected Label elapsedTime;

	protected Label scanPntLbl;
	protected ProgressBar progressBar;


	@Override
	public void createPartControl(Composite parent) {
		createFirstHalf(parent);
	}

	private void createFirstHalf(Composite parent) {
		GridLayout gl_parent = new GridLayout(5, true);
		gl_parent.verticalSpacing = 12;
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		parent.setLayoutData(gridData);
		parent.setLayout(gl_parent);
		{
			Group grpScanStat = new Group(parent, SWT.NONE);
			grpScanStat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			grpScanStat.setText("Scan Status");
			grpScanStat.setToolTipText("Cycle: Running, [Paused], Completed");
			grpScanStat.setLayout(new FillLayout(SWT.HORIZONTAL));
			grpScanStat.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			scanStatLbl = new Label(grpScanStat, SWT.NONE);
			scanStatLbl.setText("UNKNOWN");
			scanStatLbl.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
			scanStatLbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			scanStatLbl.setAlignment(SWT.CENTER);
		}
		{
			Group grpScanNum = new Group(parent, SWT.NONE);
			grpScanNum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			grpScanNum.setText("Running Scan Number");
			grpScanNum.setToolTipText("Before Scan: Number of the next file to be written; During scan: number of file being written");
			grpScanNum.setLayout(new FillLayout(SWT.HORIZONTAL));
			grpScanNum.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			scanNumLbl = new Label(grpScanNum, SWT.NONE);
			scanNumLbl.setText("");
			scanNumLbl.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
			scanNumLbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			scanNumLbl.setAlignment(SWT.CENTER);
		}
		{
			Group grpScanPnt = new Group(parent, SWT.NONE);
			grpScanPnt.setText("Scan Point");
			grpScanPnt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			grpScanPnt.setLayout(new FillLayout(SWT.HORIZONTAL));
			grpScanPnt.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			scanPntLbl = new Label(grpScanPnt, SWT.NONE);
			scanPntLbl.setAlignment(SWT.CENTER);
			scanPntLbl.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
			// scanPntLbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			scanPntLbl.setText("[0] / [0]");
		}
		{
			Group grpElapsedTime = new Group(parent, SWT.NONE);
			grpElapsedTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			grpElapsedTime.setText("Elapsed");
			grpElapsedTime.setLayout(new FillLayout(SWT.HORIZONTAL));
			grpElapsedTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			elapsedTime = new Label(grpElapsedTime, SWT.NONE);
			elapsedTime.setText("00:00:00");
			elapsedTime.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
			elapsedTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			elapsedTime.setAlignment(SWT.CENTER);
		}
		{
			Group grpRemainTime = new Group(parent, SWT.NONE);
			grpRemainTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			grpRemainTime.setText("Remaining");
			grpRemainTime.setToolTipText("Estimate of time remaining based of average time taken so far");
			grpRemainTime.setLayout(new FillLayout(SWT.HORIZONTAL));
			grpRemainTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			remainTimeLbl = new Label(grpRemainTime, SWT.NONE);
			remainTimeLbl.setText("00:00:00");
			remainTimeLbl.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
			remainTimeLbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			remainTimeLbl.setAlignment(SWT.CENTER);
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Progress");

			progressBar = new ProgressBar(parent, SWT.NONE);
			progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
			progressBar.setMaximum(10000);
			progressBar.setMinimum(0);
		}
		{
			Label lblLastScanFile = new Label(parent, SWT.NONE);
			lblLastScanFile.setText("Last Scan File");
			lblLastScanFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			scanFile = new Text(parent, SWT.BORDER);
			scanFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			scanFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			scanFile.setText("");
			scanFile.setEditable(false);
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Current Directory");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			currentDirectory = new Text(parent, SWT.BORDER);
			currentDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			currentDirectory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			currentDirectory.setText("");
			currentDirectory.setEditable(false);
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Subdirectory");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			subDirectory = new Text(parent, SWT.BORDER);
			subDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			subDirectory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			subDirectory.setText("");
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Sample Name");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			sampleName = new Text(parent, SWT.BORDER);
			sampleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			sampleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			sampleName.setText("");
		}

		new MetadataUpdater(this);
	}

	@Override
	public void setFocus() {
	}
}