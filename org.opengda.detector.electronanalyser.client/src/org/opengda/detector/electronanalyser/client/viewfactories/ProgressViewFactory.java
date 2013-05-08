package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.ProgressView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ProgressViewFactory.class);
	private String viewPartName;
	private String name;
	private String currentPointPV;
	private String totalPointsPV;
	private String currentIterationPV;
	private String totalIterationsPV;
	private String leadPointsPV;
	private String endPointsPV;
	private String currentLeadPointPV;
	private String currentDataPointPV;
	private String regionProgressPV;
	private String inLeadPV;
	
	@Override
	public Object create() throws CoreException {
		logger.info("Creating Progress view");
		ProgressView progressView = new ProgressView();
		progressView.setViewPartName(viewPartName);
		if (leadPointsPV != null) {
			progressView.setLeadPointsPV(leadPointsPV);
		}
		if (endPointsPV != null) {
			progressView.setEndPointsPV(endPointsPV);
		}
		if (currentLeadPointPV != null) {
			progressView.setCurrentLeadPointPV(currentLeadPointPV);
		}
		if (currentDataPointPV != null) {
			progressView.setCurrentDataPointPV(currentDataPointPV);
		}
		if (regionProgressPV != null) {
			progressView.setRegionProgressPV(regionProgressPV);
		}
		if (inLeadPV != null) {
			progressView.setInLeadPV(inLeadPV);
		}
		if (getCurrentIterationPV() != null) {
			progressView.setCurrentIterationPV(getCurrentIterationPV());
		}
		if (getTotalIterationsPV() != null) {
			progressView.setTotalIterationsPV(getTotalIterationsPV());
		}
		if (currentPointPV != null) {
			progressView.setCurrentPointPV(currentPointPV);
		}
		if (totalPointsPV != null) {
			progressView.setTotalPointsPV(totalPointsPV);
		}
		
		return progressView;
	}

	@Override
	public void setName(String name) {
		this.name=name;		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (leadPointsPV == null ) {
			throw new IllegalArgumentException("Lead points PV cannot be null in progress View.");
		}
		if (endPointsPV == null ) {
			throw new IllegalArgumentException("End points PV cannot be null in progress View.");
		}
		if (currentLeadPointPV == null ) {
			throw new IllegalArgumentException("Current lead point PV cannot be null in progress View.");
		}
		if (currentDataPointPV == null ) {
			throw new IllegalArgumentException("Current data point PV cannot be null in progress View.");
		}
		if (regionProgressPV == null ) {
			throw new IllegalArgumentException("Region progress PV cannot be null in progress View.");
		}
		if (inLeadPV == null ) {
			throw new IllegalArgumentException("In lead PV cannot be null in progress View.");
		}
		if (currentIterationPV == null ) {
			throw new IllegalArgumentException("Current iteration PV cannot be null in progress View.");
		}
		if (totalIterationsPV == null ) {
			throw new IllegalArgumentException("Total iterations PV cannot be null in progress View.");
		}
		if (currentPointPV == null ) {
			throw new IllegalArgumentException("Current point PV cannot be null in progress View.");
		}
		if (totalPointsPV == null ) {
			throw new IllegalArgumentException("Total points PV cannot be null in progress View.");
		}
		
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
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

	public String getLeadPointsPV() {
		return leadPointsPV;
	}

	public void setLeadPointsPV(String leadPointsPV) {
		this.leadPointsPV = leadPointsPV;
	}

	public String getEndPointsPV() {
		return endPointsPV;
	}

	public void setEndPointsPV(String endPointsPV) {
		this.endPointsPV = endPointsPV;
	}

	public String getCurrentLeadPointPV() {
		return currentLeadPointPV;
	}

	public void setCurrentLeadPointPV(String currentLeadPointPV) {
		this.currentLeadPointPV = currentLeadPointPV;
	}

	public String getCurrentDataPointPV() {
		return currentDataPointPV;
	}

	public void setCurrentDataPointPV(String currentDataPointPV) {
		this.currentDataPointPV = currentDataPointPV;
	}

	public String getRegionProgressPV() {
		return regionProgressPV;
	}

	public void setRegionProgressPV(String regionProgressPV) {
		this.regionProgressPV = regionProgressPV;
	}

	public String getInLeadPV() {
		return inLeadPV;
	}

	public void setInLeadPV(String inLeadPV) {
		this.inLeadPV = inLeadPV;
	}

}
