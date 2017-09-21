package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * class to display data colection progress for a
 * {@link org.opengda.detector.electronanalyser.model.Region} It monitors EPICS
 * progress PVs to update the progress bar. It also provides 'Stop' button to
 * abort current region collection. Users must provide 'analyser' object to enable the monitoring.
 *
 * @author fy65
 *
 */
public class RegionProgressComposite extends Composite implements InitializationListener {

	private String currentIterationRemainingTimePV;
	private String iterationLeadPointsPV;
	private String iterationProgressPV;
	private String totalDataPointsPV;
	private String iterationCurrentPointPV;

//	private String iterationTotalPointsPV;
//	private String currentDataPointPV;

	private String totalRemianingTimePV;
	private String totalProgressPV;
	private String totalPointsPV;
	private String currentPointPV;

	private String currentIterationPV;
	private String totalIterationsPV;

//	private String inLeadPV;
//	private String currentLeadPointPV;

	private static final Logger logger=LoggerFactory.getLogger(RegionProgressComposite.class);
	private EpicsChannelManager controller;

	private IterationRemainingTimeListener iterationRemainingTimeListener;
	private IterationLeadPointsListener iterationLeadPointsListener;
	private IterationProgressListener iterationProgressListener;
	private IterationTotalDataPointsListener endPointsListener;
	private IterationCurrentPointListener iterationCurrentPointListener;

//	private IterationTotalPointsListener iterationTotalPointsListener; //total steps in iteration
//	private IterationCurrentDataPointListener iterationCurrentDataPointListener; //current data point in iteration

	private TotalRemainingTimeListener totalTimeRemainingListener;
	private TotalProgressListener totalProgressListener;
	private TotalPointsListener totalPointsListener;
	private CurrentPointListener currentPointListener;

	private CurrentIterationListener currentIterationListener;
	private TotalIterationsListener totalIterationsListener;

	private Channel iterationTimeRemainingChannel;
	private Channel leadPointsChannel;
	private Channel interationProgressChannel;
	private Channel endPointsChannel;
	private Channel interationCurrentPointChannel;

	private Channel totalTimeRemainingChannel;
	private Channel totalProgressChannel;
	private Channel totalPointsChannel;
	private Channel currentPointChannel;

	private Channel currentIterationChannel;
	private Channel totalIterationsChannel;

//	private Channel currentDataPointChannel;
//	private Channel inLeadChannel;
//	private Channel currentLeadPointChannel;

	private int iterationTotalDataPoints = 0;
	private int totalSteps;
	private int totalIterations=0;
	private int currentiteration=0;

	private Text txtTextIterationValue;
	private Text txtCurrentPoint;
	private Text txtIterationTimeRemaining;
	private Label lblMin;
	private ProgressBar progressBar;
	private Label lblMax;

	private Text txtTextTotalStepsValue;
	private Text txtCurrentStepValue;
	private Text txtTotalTimeRemaining;
	protected ProgressBar totalProgressBar;

	public RegionProgressComposite(Composite parent, int style) {
		super(parent, style);

		controller=new EpicsChannelManager(this);
		setLayout(new GridLayout(1, false));

		Composite rootComposite = new Composite(this, SWT.NONE);
		GridData gd_rootComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_rootComposite.widthHint = 456;
		rootComposite.setLayoutData(gd_rootComposite);
		GridLayout layout = new GridLayout(6, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);

		Label lblIteration=new Label(rootComposite, SWT.None);
		lblIteration.setText("Iteration: ");

		txtTextIterationValue = new Text(rootComposite, SWT.BORDER);
		GridData gd_lblIterationValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblIterationValue.widthHint = 40;
		txtTextIterationValue.setLayoutData(gd_lblIterationValue);
		txtTextIterationValue.setEditable(false);
		updateIterationDispay(currentiteration, totalIterations);

		Label lblCurrentPoint = new Label(rootComposite, SWT.NONE);
		lblCurrentPoint.setText("Point:");

		txtCurrentPoint = new Text(rootComposite, SWT.BORDER);
		txtCurrentPoint.setEditable(false);
		txtCurrentPoint.setText("0");
		GridData gd_txtCurrentPoint = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_txtCurrentPoint.widthHint = 40;
		txtCurrentPoint.setLayoutData(gd_txtCurrentPoint);

		Label lblTimeRemaining = new Label(rootComposite, SWT.NONE);
		lblTimeRemaining.setText("Iter Time Remaining:");

		txtIterationTimeRemaining = new Text(rootComposite, SWT.BORDER);
		txtIterationTimeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		txtIterationTimeRemaining.setText("0.0000");
		txtIterationTimeRemaining.setEditable(false);

		Label lblIterationProgress = new Label(rootComposite, SWT.NONE);
		lblIterationProgress.setText("Iter progress:");

		lblMin = new Label(rootComposite, SWT.NONE);
		lblMin.setAlignment(SWT.RIGHT);
		GridData gd_lblMin = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblMin.widthHint = 28;
		lblMin.setLayoutData(gd_lblMin);

		progressBar = new ProgressBar(rootComposite, SWT.HORIZONTAL);
		GridData gd_progressBar = new GridData(GridData.FILL_HORIZONTAL);
		gd_progressBar.grabExcessHorizontalSpace = false;
		gd_progressBar.horizontalSpan = 3;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);

		lblMax = new Label(rootComposite, SWT.NONE);
		lblMax.setText("100");
		GridData gd_lblMax = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMax.widthHint = 56;
		lblMax.setLayoutData(gd_lblMax);

		lblMax.setText(String.valueOf(progressBar.getMaximum()));
		lblMin.setText(String.valueOf(progressBar.getMinimum()));

		Label horizontalSeparator = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1);
		gd_label.widthHint = 447;
		horizontalSeparator.setLayoutData(gd_label);

		Label lblTotalSteps = new Label(rootComposite, SWT.NONE);
		lblTotalSteps.setText("Total Steps:");

		txtTextTotalStepsValue = new Text(rootComposite, SWT.BORDER);
		GridData gd_txtTextTotalStepsValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtTextTotalStepsValue.widthHint = 40;
		txtTextTotalStepsValue.setLayoutData(gd_txtTextTotalStepsValue);
		txtTextTotalStepsValue.setText("0");
		txtTextTotalStepsValue.setEditable(false);

		Label lblCurrentStep = new Label(rootComposite, SWT.NONE);
		lblCurrentStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCurrentStep.setText("Step:");

		txtCurrentStepValue = new Text(rootComposite, SWT.BORDER);
		GridData gd_txtCurrentStepValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtCurrentStepValue.widthHint = 40;
		txtCurrentStepValue.setLayoutData(gd_txtCurrentStepValue);
		txtCurrentStepValue.setText("0");
		txtCurrentStepValue.setEditable(false);

		Label lblTotalTimeRemaining = new Label(rootComposite, SWT.NONE);
		lblTotalTimeRemaining.setText("Total time remaining:");

		txtTotalTimeRemaining = new Text(rootComposite, SWT.BORDER);
		txtTotalTimeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		txtTotalTimeRemaining.setText("0.0000");
		txtTotalTimeRemaining.setEditable(false);

		Label lblTotalProgress = new Label(rootComposite, SWT.NONE);
		lblTotalProgress.setText("Total progress:");

		totalProgressBar = new ProgressBar(rootComposite, SWT.NONE);
		GridData gd_progressBar_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1);
		gd_progressBar_1.widthHint = 340;
		totalProgressBar.setLayoutData(gd_progressBar_1);
	}

	public void initialise() {
		if (getCurrentIterationRemainingTimePV()==null ) {
			throw new IllegalStateException("required PV for Iteration Time Remaining missing.");
		}
		if (getIterationLeadPointsPV()==null ) {
			throw new IllegalStateException("required PV for Iteration Lead Points missing.");
		}
		if (getTotalDataPointsPV()==null ) {
			throw new IllegalStateException("required PV for Iteration Total Data Points missing.");
		}
		if (getIterationProgressPV()==null ) {
			throw new IllegalStateException("required PV for Iteration Progress missing.");
		}
		if (getIterationCurrentPointPV()==null ) {
			throw new IllegalStateException("required PV for Iteration Current Point missing.");
		}
		if (getTotalRemianingTimePV()==null ) {
			throw new IllegalStateException("required PV for Total Time Remaining missing.");
		}
		if (getTotalProgressPV()==null ) {
			throw new IllegalStateException("required PV for Total Progress missing.");
		}
		if (getTotalPointsPV()==null ) {
			throw new IllegalStateException("required PV for Total Points missing.");
		}
		if (getCurrentPointPV()==null ) {
			throw new IllegalStateException("required PV for Current Point missing.");
		}
		if (getCurrentIterationPV()==null ) {
			throw new IllegalStateException("required PV for Current Iteration number missing.");
		}
		if (getTotalIterationsPV()==null ) {
			throw new IllegalStateException("required PV for Total iteration number missing.");
		}
		iterationRemainingTimeListener=new IterationRemainingTimeListener();
		iterationLeadPointsListener = new IterationLeadPointsListener();
		endPointsListener = new IterationTotalDataPointsListener();
		iterationProgressListener=new IterationProgressListener();
		iterationCurrentPointListener = new IterationCurrentPointListener();

		totalTimeRemainingListener=new TotalRemainingTimeListener();
		totalProgressListener=new TotalProgressListener();
		totalPointsListener = new TotalPointsListener();
		currentPointListener=new CurrentPointListener();

		currentIterationListener=new CurrentIterationListener();
		totalIterationsListener=new TotalIterationsListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create all required channels", e1);
		}
	}

	public void createChannels() throws CAException, TimeoutException {
		iterationTimeRemainingChannel = controller.createChannel(getCurrentIterationRemainingTimePV(), iterationRemainingTimeListener, MonitorType.NATIVE,false);
		leadPointsChannel = controller.createChannel(getIterationLeadPointsPV(), iterationLeadPointsListener, MonitorType.NATIVE,false);
		endPointsChannel = controller.createChannel(getTotalDataPointsPV(),endPointsListener,MonitorType.NATIVE, false);
		interationProgressChannel = controller.createChannel(getIterationProgressPV(), iterationProgressListener, MonitorType.NATIVE,false);
		interationCurrentPointChannel=controller.createChannel(getIterationCurrentPointPV(),iterationCurrentPointListener,MonitorType.NATIVE,false );

		totalTimeRemainingChannel = controller.createChannel(getTotalRemianingTimePV(),totalTimeRemainingListener,MonitorType.NATIVE, false);
		totalProgressChannel = controller.createChannel(getTotalProgressPV(),totalProgressListener,MonitorType.NATIVE, false);
		totalPointsChannel = controller.createChannel(getTotalPointsPV(),totalPointsListener,MonitorType.NATIVE, false);
		currentPointChannel = controller.createChannel(getCurrentPointPV(),currentPointListener,MonitorType.NATIVE, false);

		currentIterationChannel=controller.createChannel(getCurrentIterationPV(),currentIterationListener,MonitorType.NATIVE,false );
		totalIterationsChannel=controller.createChannel(getTotalIterationsPV(),totalIterationsListener, MonitorType.NATIVE,false );
		controller.creationPhaseCompleted();
		logger.debug("channels are created");
	}

	private class IterationRemainingTimeListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final double timeremaining = ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtIterationTimeRemaining.setText(String.format("%.3f",timeremaining));
						}
					});
				}
				logger.debug("iteration time remaining changed to {}", timeremaining);
			}
		}
	}

	private class IterationLeadPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int leadPoints = -(int)((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							lblMin.setText(String.valueOf(leadPoints));
						}
					});
				}
				logger.debug("lead points changed to {}", leadPoints);
			}
		}
	}
	private class IterationProgressListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int percentage =(int) ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							progressBar.setSelection(percentage);
//							if (percentage==100) {
//								progressBar.setSelection(0);
//							}
						}
					});
				}
				logger.debug("percentage completed updated to {}", percentage);
			}
		}
	}
	private class IterationTotalDataPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				iterationTotalDataPoints = (int)((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							lblMax.setText(String.valueOf(iterationTotalDataPoints));
						}
					});
				}
				logger.debug("End points updated to {}", iterationTotalDataPoints);
			}
		}
	}
	private class IterationCurrentPointListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int currentpoint =(int) ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtCurrentPoint.setText(String.valueOf(currentpoint));
							// reset progress bar when completed
							if (currentpoint == iterationTotalDataPoints) {
								progressBar.setSelection(0);
							}
						}
					});
				}
				logger.debug("current point number updated to {}", currentpoint);
			}
		}
	}

	public class TotalIterationsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				totalIterations = ((DBR_Int) dbr).getIntValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							updateIterationDispay(currentiteration, totalIterations);
						}
					});
				}
				logger.debug("total iterations changed to {}", totalIterations);
			}
		}
	}
	private void updateIterationDispay(int currentiteration, int totalIterations) {
		txtTextIterationValue.setText(String.valueOf(currentiteration)+"/"+String.valueOf(totalIterations));
	}
	public class CurrentIterationListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				currentiteration = ((DBR_Int) dbr).getIntValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							updateIterationDispay(currentiteration, totalIterations);
						}
					});
				}
				logger.debug("current iteration is {}", currentiteration);
			}
		}
	}

	private class TotalRemainingTimeListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final double timeremaining = ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtTotalTimeRemaining.setText(String.format("%.3f",timeremaining));
						}
					});
				}
				logger.debug("iteration time remaining changed to {}", timeremaining);
			}
		}
	}
	private class TotalProgressListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int percentage =(int) ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							totalProgressBar.setSelection(percentage);
//							if (percentage==100) {
//								totalProgressBar.setSelection(0);
//							}
						}
					});
				}
				logger.debug("total percentage completed updated to {}", percentage);
			}
		}
	}
	private class TotalPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				totalSteps = (int)((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtTextTotalStepsValue.setText(String.valueOf(totalSteps));
						}
					});
				}
				logger.debug("total number of steps updated to {}", totalSteps);
			}
		}
	}
	private class CurrentPointListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int currentstep =(int) ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtCurrentStepValue.setText(String.valueOf(currentstep));
							// reset progress bar when completed
							if (currentstep == totalSteps) {
								totalProgressBar.setSelection(0);
							}
						}
					});
				}
				logger.debug("current step number updated to {}", currentstep);
			}
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.info("Region Progress EPICS Channels initialisation completed!");

	}

	public String getCurrentIterationRemainingTimePV() {
		return currentIterationRemainingTimePV;
	}

	public void setCurrentIterationRemainingTimePV(
			String currentIterationRemainingTimePV) {
		this.currentIterationRemainingTimePV = currentIterationRemainingTimePV;
	}

	public String getIterationLeadPointsPV() {
		return iterationLeadPointsPV;
	}

	public void setIterationLeadPointsPV(String iterationLeadPointsPV) {
		this.iterationLeadPointsPV = iterationLeadPointsPV;
	}

	public String getIterationProgressPV() {
		return iterationProgressPV;
	}

	public void setIterationProgressPV(String iterationProgressPV) {
		this.iterationProgressPV = iterationProgressPV;
	}

	public String getIterationCurrentPointPV() {
		return iterationCurrentPointPV;
	}

	public void setIterationCurrentPointPV(String iterationCurrentPointPV) {
		this.iterationCurrentPointPV = iterationCurrentPointPV;
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

	public String getTotalPointsPV() {
		return totalPointsPV;
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

	public String getCurrentIterationPV() {
		return currentIterationPV;
	}

	public void setCurrentIterationPV(String currentIterationPV) {
		this.currentIterationPV = currentIterationPV;
	}

	public String getTotalIterationsPV() {
		return totalIterationsPV;
	}

	public void setTotalIterationsPV(String totalIterationsPV) {
		this.totalIterationsPV = totalIterationsPV;
	}

	public String getTotalDataPointsPV() {
		return totalDataPointsPV;
	}

	public void setTotalDataPointsPV(String totalDataPointsPV) {
		this.totalDataPointsPV = totalDataPointsPV;
	}

//	public String getCurrentDataPointPV() {
//		return currentDataPointPV;
//	}
//
//	public void setCurrentDataPointPV(String currentDataPointPV) {
//		this.currentDataPointPV = currentDataPointPV;
//	}
//
//	public String getInLeadPV() {
//		return inLeadPV;
//	}
//
//	public void setInLeadPV(String inLeadPV) {
//		this.inLeadPV = inLeadPV;
//	}
//
//	public String getCurrentLeadPointPV() {
//		return currentLeadPointPV;
//	}
//
//	public void setCurrentLeadPointPV(String currentLeadPointPV) {
//		this.currentLeadPointPV = currentLeadPointPV;
//	}
//	public String getIterationTotalPointsPV() {
//	return iterationTotalPointsPV;
//}
//
//public void setIterationTotalPointsPV(String iterationTotalPointsPV) {
//	this.iterationTotalPointsPV = iterationTotalPointsPV;
//}


}
