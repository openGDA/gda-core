package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ProgressView.class);
	private String currentPointPV;
	private String totalPointsPV;

	public ProgressView() {
		setTitleToolTip("display progress view");
		// setContentDescription("A view for displaying progresses.");
		setPartName("Progress");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);

		try {
			RegionProgressComposite regionProgressComposite = new RegionProgressComposite(rootComposite, SWT.None);
			regionProgressComposite.setCurrentPointPV(currentPointPV);
			regionProgressComposite.setTotalPointsPV(totalPointsPV);
			regionProgressComposite.initialise();
			regionProgressComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		} catch (Exception e) {
			logger.error("Cannot create region progress composite.", e);
		}
	}

	@Override
	public void setFocus() {

	}


	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);

	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentPointPV) {
		this.currentPointPV = currentPointPV;
	}

	public String getTotalPointsPV() {
		return totalPointsPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

}
