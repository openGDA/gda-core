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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class NcdMemoryUsage extends ViewPart {
	public static final String ID = "gda.rcp.ncd.views.NcdMemoryUsage"; //$NON-NLS-1$
	private ProgressBar saxsProgressBar;
	private ProgressBar waxsProgressBar;
	private Group group;

	private static int minUsage = 0;
	private static int maxUsage = 100;
	private double saxsMemoryRatio = 0.0;
	private double waxsMemoryRatio = 0.0;
	private int timeFrameCount = 1;
	private double saxsPercentage = 0.0;
	private double waxsPercentage = 0.0;
	
	public NcdMemoryUsage() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));

		Composite fillComposite = new Composite(container, SWT.NONE);
		fillComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

//		Composite container = new Composite(fillComposite, SWT.NONE);
//		container.setLayout(new RowLayout(SWT.VERTICAL));

		group = new Group(fillComposite, SWT.SHADOW_ETCHED_IN);
		group.setText("Memory Usage");
//		group.setLayout(new GridLayout(1, false));
		group.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(group, SWT.NONE);
//		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayout(new GridLayout(2, false));
		{
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			label.setText("Saxs");
			saxsProgressBar = new ProgressBar(composite, SWT.NONE);
			saxsProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		{
			Label label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			label.setText("Waxs");
			waxsProgressBar = new ProgressBar(composite, SWT.NONE);
			waxsProgressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		saxsProgressBar.setMinimum(minUsage);
		saxsProgressBar.setMaximum(maxUsage);
		waxsProgressBar.setMinimum(minUsage);
		waxsProgressBar.setMaximum(maxUsage);
	}

	@Override
	public void setFocus() {
	}

	public void setTimeFrameCount(int value) {
		timeFrameCount = value;

		saxsPercentage = timeFrameCount * saxsMemoryRatio * 100.0;
		setMemoryUsageValue(saxsProgressBar, saxsPercentage);

		waxsPercentage = timeFrameCount * waxsMemoryRatio * 100.0;
		setMemoryUsageValue(waxsProgressBar, waxsPercentage);
	}

	/**
	 * set ratio for SAXS or WAXS
	 * @param type SAXS or WAXS
	 * @param memoryRatio
	 */
	public void setMemoryRatio(String type, double memoryRatio) {
		if ("SAXS".equals(type)) setSaxsMemoryRatio(memoryRatio);
		if ("WAXS".equals(type)) setWaxsMemoryRatio(memoryRatio);
	}
	
	/**
	 * @param saxsMemoryRatio
	 */
	public void setSaxsMemoryRatio(double saxsMemoryRatio) {
		this.saxsMemoryRatio = saxsMemoryRatio;

		saxsPercentage = timeFrameCount * saxsMemoryRatio * 100.0;
		setMemoryUsageValue(saxsProgressBar, saxsPercentage);
	}

	/**
	 * @param waxsMemoryRatio
	 */
	public void setWaxsMemoryRatio(double waxsMemoryRatio) {
		this.waxsMemoryRatio = waxsMemoryRatio;

		waxsPercentage = timeFrameCount * waxsMemoryRatio * 100.0;
		setMemoryUsageValue(waxsProgressBar, waxsPercentage);
	}

	private void actionOnMemoryOverflow() {
		NcdRapidButtonView nbpv = (NcdRapidButtonView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("gda.rcp.ncd.views.NcdRapidButtonView");
		if (saxsPercentage > 100.0 || waxsPercentage > 100.0)
			nbpv.disableCollection();
		else
			nbpv.enableCollection();
	}

	/**
	 * Sets memory usage value in experiment
	 * 
	 * @param memoryUsage
	 *            the progress bar instance
	 * @param pcent
	 *            the new percentage complete value
	 */
	private void setMemoryUsageValue(ProgressBar memoryUsage, double pcent) {
		// Update the memory progress bar. The indicator will normally
		// be green but will display red when the memory capacity is exceeded.
		if (pcent > 100.0) {
			pcent = 100.0;
//			memoryUsage.setForeground();
		} else {
//			memoryUsage.setForeground(Color.green);
		}
		memoryUsage.setSelection((int)pcent);
		actionOnMemoryOverflow();
	}
	
}