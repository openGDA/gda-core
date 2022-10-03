package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.ProgressView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;

public class ProgressViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ProgressViewFactory.class);
	private String viewPartName;
	private String name;
	private String currentIterationRemainingTimePV;
	private String iterationLeadPointsPV;
	private String iterationProgressPV;
	private String totalDataPointsPV;
	private String iterationCurrentPointPV;

	private String totalRemianingTimePV;
	private String totalProgressPV;
	private String totalPointsPV;
	private String currentPointPV;

	private String currentIterationPV;
	private String totalIterationsPV;

//	private String iterationTotalPointsPV;
//	private String currentDataPointPV;
//	private String inLeadPV;
//	private String currentLeadPointPV;

	@Override
	public Object create() throws CoreException {
		logger.info("Creating Progress view");
		ProgressView progressView = new ProgressView();
		progressView.setViewPartName(viewPartName);
		if (currentIterationRemainingTimePV != null) {
			progressView.setCurrentIterationRemainingTimePV(currentIterationRemainingTimePV);
		}
		if (iterationLeadPointsPV != null) {
			progressView.setIterationLeadPointsPV(iterationLeadPointsPV);
		}
		if (iterationProgressPV != null) {
			progressView.setIterationProgressPV(iterationProgressPV);
		}
		if (totalPointsPV != null) {
			progressView.setTotalPointsPV(totalPointsPV);
		}
		if (iterationCurrentPointPV != null) {
			progressView.setIterationCurrentPointPV(iterationCurrentPointPV);
		}
		if (totalRemianingTimePV != null) {
			progressView.setTotalRemianingTimePV(totalRemianingTimePV);
		}
		if (totalProgressPV != null) {
			progressView.setTotalProgressPV(totalProgressPV);
		}
		if (currentPointPV != null) {
			progressView.setCurrentPointPV(currentPointPV);
		}
		if (currentIterationPV != null) {
			progressView.setCurrentIterationPV(currentIterationPV);
		}
		if (totalIterationsPV != null) {
			progressView.setTotalIterationsPV(totalIterationsPV);
		}
		if (totalDataPointsPV != null) {
			progressView.setTotalDataPointsPV(totalDataPointsPV);
		}
//		if (iterationTotalPointsPV != null) {
//			progressView.setTotalPointsPV(iterationTotalPointsPV);
//		}
//		if (currentDataPointPV != null) {
//			progressView.setCurrentDataPointPV(currentDataPointPV);
//		}
//		if (inLeadPV != null) {
//			progressView.setInLeadPV(inLeadPV);
//		}
//		if (currentLeadPointPV != null) {
//			progressView.setCurrentLeadPointPV(currentLeadPointPV);
//		}

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
		if (currentIterationRemainingTimePV == null ) {
			throw new IllegalArgumentException("Current iteration remaining time PV cannot be null in progress View.");
		}
		if (iterationLeadPointsPV == null ) {
			throw new IllegalArgumentException("Iteration lead points PV cannot be null in progress View.");
		}
		if (iterationProgressPV == null ) {
			throw new IllegalArgumentException("Iteration progress PV cannot be null in progress View.");
		}
		if (iterationCurrentPointPV == null ) {
			throw new IllegalArgumentException("Iteration current point PV cannot be null in progress View.");
		}
		if (totalRemianingTimePV == null ) {
			throw new IllegalArgumentException("Total remaining time PV cannot be null in progress View.");
		}
		if (totalProgressPV == null ) {
			throw new IllegalArgumentException("Total progress PV cannot be null in progress View.");
		}
		if (totalPointsPV == null ) {
			throw new IllegalArgumentException("Total points PV cannot be null in progress View.");
		}
		if (currentPointPV == null ) {
			throw new IllegalArgumentException("Current point PV cannot be null in progress View.");
		}
		if (currentIterationPV == null ) {
			throw new IllegalArgumentException("Current iteration PV cannot be null in progress View.");
		}
		if (totalIterationsPV == null ) {
			throw new IllegalArgumentException("Total iterations PV cannot be null in progress View.");
		}
		if (totalDataPointsPV == null ) {
			throw new IllegalArgumentException("Total number of data points PV cannot be null in progress View.");
		}
//		if (iterationTotalPointsPV == null ) {
//			throw new IllegalArgumentException("Iteration total points PV cannot be null in progress View.");
//		}
//		if (currentDataPointPV == null ) {
//			throw new IllegalArgumentException("Current data point PV cannot be null in progress View.");
//		}
//		if (inLeadPV == null ) {
//			throw new IllegalArgumentException("In lead PV cannot be null in progress View.");
//		}
//		if (currentLeadPointPV == null ) {
//			throw new IllegalArgumentException("Current lead point PV cannot be null in progress View.");
//		}
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public String getIterationCurrentPointPV() {
		return iterationCurrentPointPV;
	}

	public void setIterationCurrentPointPV(String currentPointPV) {
		this.iterationCurrentPointPV = currentPointPV;
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

	public String getIterationLeadPointsPV() {
		return iterationLeadPointsPV;
	}

	public void setIterationLeadPointsPV(String leadPointsPV) {
		this.iterationLeadPointsPV = leadPointsPV;
	}

	public String getTotalDataStringPointsPV() {
		return totalDataPointsPV;
	}

	public void setTotalDataPointsPV(String endPointsPV) {
		this.totalDataPointsPV = endPointsPV;
	}

	public String getIterationProgressPV() {
		return iterationProgressPV;
	}

	public void setIterationProgressPV(String regionProgressPV) {
		this.iterationProgressPV = regionProgressPV;
	}

	public String getCurrentIterationRemainingTimePV() {
		return currentIterationRemainingTimePV;
	}

	public void setCurrentIterationRemainingTimePV(
			String currentIterationRemainingTime) {
		this.currentIterationRemainingTimePV = currentIterationRemainingTime;
	}

	public String getTotalRemianingTimePV() {
		return totalRemianingTimePV;
	}

	public void setTotalRemianingTimePV(String totalRemianingTimePV) {
		this.totalRemianingTimePV = totalRemianingTimePV;
	}

	public String getTotalProgressPV() {
		return totalProgressPV;
	}

	public void setTotalProgressPV(String totalProgressPV) {
		this.totalProgressPV = totalProgressPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentPointPV) {
		this.currentPointPV = currentPointPV;
	}

//	public String getTotalPointsPV() {
//		return iterationTotalPointsPV;
//	}
//
//	public void setIterationTotalPointsPV(String totalPointsPV) {
//		this.iterationTotalPointsPV = totalPointsPV;
//	}
//	public String getInLeadPV() {
//		return inLeadPV;
//	}
//
//	public void setInLeadPV(String inLeadPV) {
//		this.inLeadPV = inLeadPV;
//	}
//	public String getCurrentLeadPointPV() {
//		return currentLeadPointPV;
//	}
//
//	public void setCurrentLeadPointPV(String currentLeadPointPV) {
//		this.currentLeadPointPV = currentLeadPointPV;
//	}
//
//	public String getCurrentDataPointPV() {
//		return currentDataPointPV;
//	}
//
//	public void setCurrentDataPointPV(String currentDataPointPV) {
//		this.currentDataPointPV = currentDataPointPV;
//	}


}
