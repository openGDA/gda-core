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

package gda.rcp.ncd.views;

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

public class NcdStatus extends ViewPart {
	public NcdStatus() {
	}
	protected Text subDirectory;
	protected Text currentDirectory;
	protected Text scanFile;
	protected Label parameterFile;
	protected Label saxsCountRate, waxsCountRate, waxsPeakRate, saxsPeakRate, saxsPeak, waxsPeak, saxsCount, waxsCount;
	protected Label elapsedTime;
	protected Label frameStatus;
	protected Label frameNumber;
	protected Label cycleNumber;
	protected Label totalCycleCount;
	protected ProgressBar progressBar;
	protected Label i0Normalisation;
	protected Label itNormalisation;


	@Override
	public void createPartControl(Composite parent) {
//		parent.setLayout(new RowLayout(SWT.VERTICAL));
		createFirstHalf(parent);
	}

	private void createFirstHalf(Composite parent) {
//		Composite parent = new Composite(up, SWT.NONE);
		GridLayout gl_parent = new GridLayout(4, false);
		gl_parent.verticalSpacing = 12;
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		parent.setLayoutData(gridData);
		parent.setLayout(gl_parent);
		{
			Group grpFrame = new Group(parent, SWT.NONE);
			grpFrame.setText("Frame");
			GridData gd_grpElapsedTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_grpElapsedTime.widthHint = 60;
			grpFrame.setLayoutData(gd_grpElapsedTime);
			grpFrame.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				frameNumber = new Label(grpFrame, SWT.NONE);
				frameNumber.setAlignment(SWT.CENTER);
				frameNumber.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
				frameNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
				frameNumber.setText("0");
			}
		}
		{
			Group grpCycle = new Group(parent, SWT.NONE);
			grpCycle.setText("Cycle");
			GridData gd_grpElapsedTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_grpElapsedTime.widthHint = 60;
			grpCycle.setLayoutData(gd_grpElapsedTime);
			grpCycle.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				cycleNumber = new Label(grpCycle, SWT.NONE);
				cycleNumber.setAlignment(SWT.CENTER);
				cycleNumber.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
				cycleNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
				cycleNumber.setText("0");
			}
		}
		{
			Group grpElapsedTime = new Group(parent, SWT.NONE);
			GridData gd_grpElapsedTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_grpElapsedTime.widthHint = 120;
			grpElapsedTime.setLayoutData(gd_grpElapsedTime);
			grpElapsedTime.setText("Tfg Status");
			grpElapsedTime.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				frameStatus = new Label(grpElapsedTime, SWT.NONE);
				frameStatus.setText("BORED");
				frameStatus.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
				frameStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
				frameStatus.setAlignment(SWT.CENTER);
			}
		}
		{
			Group grpElapsedTime = new Group(parent, SWT.NONE);
			GridData gd_grpElapsedTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_grpElapsedTime.widthHint = 120;
			grpElapsedTime.setLayoutData(gd_grpElapsedTime);
			grpElapsedTime.setText("Elapsed Time");
			grpElapsedTime.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				elapsedTime = new Label(grpElapsedTime, SWT.NONE);
				elapsedTime.setText("00:00:00");
				elapsedTime.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
				elapsedTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
				elapsedTime.setAlignment(SWT.CENTER);
			}
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			label.setText("Progress");
		}
		{
			progressBar = new ProgressBar(parent, SWT.NONE);
			progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		}
		{
			Group normalisation = new Group(parent, SWT.NONE);
			normalisation.setText("Normalisation");
			GridData gd_normalisation = new GridData(SWT.FILL, SWT.CENTER, true, false, 4,1);
			normalisation.setLayoutData(gd_normalisation);
			normalisation.setLayout(new FillLayout(SWT.HORIZONTAL));
			{
				i0Normalisation = new Label(normalisation, SWT.NONE);
				i0Normalisation.setText("I0: -- Counts");
				i0Normalisation.setAlignment(SWT.CENTER);
			}
			{
				itNormalisation = new Label(normalisation, SWT.NONE);
				itNormalisation.setText("It: -- Counts");
				itNormalisation.setAlignment(SWT.CENTER);
			}
		}
		{
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 2));
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 0;
			gridLayout.marginHeight = 0;
			gridLayout.horizontalSpacing = 10;
			composite.setLayout(gridLayout);
			{
				Group grpElapsedTime = new Group(composite, SWT.NONE);
				grpElapsedTime.setText("Saxs Peak");
				grpElapsedTime.setLayout(new FillLayout(SWT.VERTICAL));
				grpElapsedTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				{
					saxsPeak = new Label(grpElapsedTime, SWT.NONE);
					saxsPeak.setText("-- counts");
					saxsPeak.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					saxsPeak.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					saxsPeak.setAlignment(SWT.CENTER);
				}
				{
					saxsPeakRate = new Label(grpElapsedTime, SWT.NONE);
					saxsPeakRate.setText("-- cps");
					saxsPeakRate.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					saxsPeakRate.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					saxsPeakRate.setAlignment(SWT.CENTER);
				}
			}
			{
				Group grpElapsedTime = new Group(composite, SWT.NONE);
				grpElapsedTime.setText("Waxs Peak");
				grpElapsedTime.setLayout(new FillLayout(SWT.VERTICAL));
				grpElapsedTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		{
					waxsPeak = new Label(grpElapsedTime, SWT.NONE);
					waxsPeak.setText("-- counts");
					waxsPeak.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					waxsPeak.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					waxsPeak.setAlignment(SWT.CENTER);
				}
		{
			waxsPeakRate = new Label(grpElapsedTime, SWT.NONE);
			waxsPeakRate.setText("-- cps");
			waxsPeakRate.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
			waxsPeakRate.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
			waxsPeakRate.setAlignment(SWT.CENTER);
		}
			}

			{
				Group grpElapsedTime = new Group(composite, SWT.NONE);
				grpElapsedTime.setText("Saxs Integrated");
				grpElapsedTime.setLayout(new FillLayout(SWT.VERTICAL));
				grpElapsedTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				{
					saxsCount = new Label(grpElapsedTime, SWT.NONE);
					saxsCount.setText("-- counts");
					saxsCount.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					saxsCount.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					saxsCount.setAlignment(SWT.CENTER);
				}
				{
					saxsCountRate = new Label(grpElapsedTime, SWT.NONE);
					saxsCountRate.setText("-- cps");
					saxsCountRate.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					saxsCountRate.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					saxsCountRate.setAlignment(SWT.CENTER);
				}
			}
			{
				Group grpElapsedTime = new Group(composite, SWT.NONE);
				grpElapsedTime.setText("Waxs Integrated");
				grpElapsedTime.setLayout(new FillLayout(SWT.VERTICAL));
				grpElapsedTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				{
					waxsCount = new Label(grpElapsedTime, SWT.NONE);
					waxsCount.setText("-- counts");
					waxsCount.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					waxsCount.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					waxsCount.setAlignment(SWT.CENTER);
				}
				{
					waxsCountRate = new Label(grpElapsedTime, SWT.NONE);
					waxsCountRate.setText("-- cps");
					waxsCountRate.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
					waxsCountRate.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
					waxsCountRate.setAlignment(SWT.CENTER);
				}
			}
		}

		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Total Cycle Count");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		}
		{
			totalCycleCount = new Label(parent, SWT.NONE);
			totalCycleCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			totalCycleCount.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			totalCycleCount.setText("");
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Parameter File");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		}
		{
			parameterFile = new Label(parent, SWT.NONE);
			GridData gd_parameterFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			parameterFile.setLayoutData(gd_parameterFile);
			parameterFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			parameterFile.setText("");
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Scan File");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		}
		{
			scanFile = new Text(parent, SWT.NONE);
			GridData gd_scanFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			scanFile.setLayoutData(gd_scanFile);
			scanFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			scanFile.setText("");
			scanFile.setEditable(false);
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Current Directory");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		}
		{
			currentDirectory = new Text(parent, SWT.NONE);
			GridData gd_currentDirectory = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			currentDirectory.setLayoutData(gd_currentDirectory);
			currentDirectory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			currentDirectory.setText("");
			currentDirectory.setEditable(false);
		}
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Subdirectory");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		}
		{
			subDirectory = new Text(parent, SWT.BORDER);
			GridData gd_subDirectory = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			subDirectory.setLayoutData(gd_subDirectory);
			subDirectory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			subDirectory.setText("");
		}

		new NcdStatusUpdater(this);
	}

	@Override
	public void setFocus() {
	}
}