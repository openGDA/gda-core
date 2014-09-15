package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressView extends ViewPart {

	public static String ID = "org.opengda.detector.electronanalyser.client.views.progressview";
	private static final Logger logger=LoggerFactory .getLogger(ProgressView.class);
	private String currentIterationRemainingTimePV;
	private String iterationLeadPointsPV;
	private String iterationProgressPV;
	private String iterationTotalPointsPV;
	private String iterationCurrentPointPV;
	
	private String totalRemianingTimePV;
	private String totalProgressPV;
	private String totalPointsPV;
	private String currentPointPV;
	
	private String currentIterationPV;
	private String totalIterationsPV;
	
	private String totalDataPointsPV;
	private String currentDataPointPV;

	private String inLeadPV;
	private String currentLeadPointPV;
	
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
			regionProgressComposite.setCurrentIterationRemainingTimePV(currentIterationRemainingTimePV);
			regionProgressComposite.setIterationLeadPointsPV(iterationLeadPointsPV);
			regionProgressComposite.setIterationProgressPV(iterationProgressPV);
			regionProgressComposite.setTotalDataPointsPV(totalDataPointsPV);
			regionProgressComposite.setIterationCurrentPointPV(iterationCurrentPointPV);
			regionProgressComposite.setTotalRemianingTimePV(totalRemianingTimePV);
			regionProgressComposite.setTotalProgressPV(totalProgressPV);
			regionProgressComposite.setTotalPointsPV(totalPointsPV);
			regionProgressComposite.setCurrentPointPV(currentPointPV);
			regionProgressComposite.setCurrentIterationPV(currentIterationPV);
			regionProgressComposite.setTotalIterationsPV(totalIterationsPV);
//			regionProgressComposite.setIterationTotalPointsPV(iterationTotalPointsPV);
//			regionProgressComposite.setCurrentDataPointPV(currentDataPointPV);
//			regionProgressComposite.setInLeadPV(inLeadPV);
//			regionProgressComposite.setCurrentLeadPointPV(currentLeadPointPV);
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

	public String getTotalIterationsPV() {
		return totalIterationsPV;
	}

	public void setTotalIterationsPV(String totalIterationsPV) {
		this.totalIterationsPV = totalIterationsPV;
	}

	public String getCurrentIterationPV() {
		return currentIterationPV;
	}

	public void setCurrentIterationPV(String currentIterationPV) {
		this.currentIterationPV = currentIterationPV;
	}

	public void setCurrentLeadPointPV(String currentLeadPointPV) {
		this.currentLeadPointPV=currentLeadPointPV;
	}

	public void setCurrentDataPointPV(String currentDataPointPV) {
		this.currentDataPointPV=currentDataPointPV;
	}

	public void setInLeadPV(String inLeadPV) {
		this.inLeadPV=inLeadPV;
		
	}
	
	public String getCurrentIterationRemainingTimePV() {
		return this.currentIterationRemainingTimePV;
	}

	public void setCurrentIterationRemainingTimePV(String currentIterationRemainingTimePV) {
		this.currentIterationRemainingTimePV=currentIterationRemainingTimePV;
	}
	public String getTotalRemianingTimePV() {
		return this.totalRemianingTimePV;
	}
	public void setTotalRemianingTimePV(String totalRemianingTimePV) {
		this.totalRemianingTimePV=totalRemianingTimePV;
	}
	public String getTotalProgressPV() {
		return this.totalProgressPV;
	}
	public void setTotalProgressPV(String totalProgressPV) {
		this.totalProgressPV=totalProgressPV;
	}

	public String getIterationProgressPV() {
		return iterationProgressPV;
	}

	public void setIterationProgressPV(String iterationProgressPV) {
		this.iterationProgressPV = iterationProgressPV;
	}

	public String getIterationLeadPointsPV() {
		return iterationLeadPointsPV;
	}

	public void setIterationLeadPointsPV(String iterationLeadPointsPV) {
		this.iterationLeadPointsPV = iterationLeadPointsPV;
	}

	public String getIterationTotalPointsPV() {
		return iterationTotalPointsPV;
	}

	public void setIterationTotalPointsPV(String iterationTotalPointsPV) {
		this.iterationTotalPointsPV = iterationTotalPointsPV;
	}

	public String getIterationCurrentPointPV() {
		return iterationCurrentPointPV;
	}

	public void setIterationCurrentPointPV(String iterationCurrentPointPV) {
		this.iterationCurrentPointPV = iterationCurrentPointPV;
	}

	public String getTotalDataPointsPV() {
		return totalDataPointsPV;
	}

	public void setTotalDataPointsPV(String totalDataPointsPV) {
		this.totalDataPointsPV = totalDataPointsPV;
	}

}
