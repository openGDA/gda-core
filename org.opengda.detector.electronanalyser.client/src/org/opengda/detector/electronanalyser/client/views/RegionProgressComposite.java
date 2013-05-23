package org.opengda.detector.electronanalyser.client.views;

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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private String currentPointPV;
	private String totalPointsPV;
	private String currentIterationPV;
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

	private String totalIterationsPV;
	private LeadPointsListener leadPointsListener;
	private EndPointsListener endPointsListener;
	private RegionProgressListener regionProgressListener;
	private static final Logger logger=LoggerFactory.getLogger(RegionProgressComposite.class);

	public RegionProgressComposite(Composite parent, int style) {
		super(parent, style);
		
		controller=new EpicsChannelManager(this);
		this.setLayout(new FillLayout());
		Composite rootComposite = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);
		Label lblIteration=new Label(rootComposite, SWT.None);
		lblIteration.setText("Iteration: ");
		
		lblIterationValue = new Label(rootComposite, SWT.None);
		lblIterationValue.setAlignment(SWT.RIGHT);
		GridData gd_lblIterationValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblIterationValue.widthHint = 38;
		lblIterationValue.setLayoutData(gd_lblIterationValue);
		updateIterationDispay(currentiteration, totalIterations);
		
		lblMin = new Label(rootComposite, SWT.NONE);
		lblMin.setAlignment(SWT.RIGHT);
		GridData gd_lblMin = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblMin.widthHint = 28;
		lblMin.setLayoutData(gd_lblMin);
		
		progressBar = new ProgressBar(rootComposite, SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		lblMin.setText(String.valueOf(progressBar.getMinimum()));
		
		lblMax = new Label(rootComposite, SWT.NONE);
		GridData gd_lblMax = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblMax.widthHint = 56;
		lblMax.setLayoutData(gd_lblMax);
		lblMax.setText(String.valueOf(progressBar.getMaximum()));
		
		Label lblStep = new Label(rootComposite, SWT.NONE);
		GridData gd_lblStep = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblStep.widthHint = 36;
		lblStep.setLayoutData(gd_lblStep);
		lblStep.setText("Step:");
		
		txtCurrentStep = new Text(rootComposite, SWT.BORDER);
		txtCurrentStep.setForeground(ColorConstants.green);
		txtCurrentStep.setEditable(false);
		txtCurrentStep.setBackground(ColorConstants.black);
		txtCurrentStep.setText("currentStep");
		GridData gd_txtCurrentStep = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtCurrentStep.widthHint = 82;
		txtCurrentStep.setLayoutData(gd_txtCurrentStep);
		
//		Button btnStop = new Button(rootComposite, SWT.CENTER);
//		btnStop.setImage(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_STOP));
//		btnStop.setToolTipText("Stop current region collection");
	}

	public void initialise() {
		if (getCurrentPointPV()==null || getTotalPointsPV() == null || getCurrentIterationPV()==null || getTotalIterationsPV()== null) {
			throw new IllegalStateException("required parameters for 'currentPointPV', 'totalPointsPV', currentIterationPV, and/or totalIterationsPV are missing.");
		}
		leadPointsListener = new LeadPointsListener();
		endPointsListener = new EndPointsListener();
		regionProgressListener=new RegionProgressListener();
		currentPointListener = new CurrentPointListener();
		totalPointsListener = new TotalPointsListener();
		currentIterationListener=new CurrentIterationListener();
		totalIterationsListener=new TotalIterationsListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create all required channels", e1);
		}
	}

	private CurrentPointListener currentPointListener;
	private TotalPointsListener totalPointsListener;
	private EpicsChannelManager controller;
	private Channel currentIterationChannel;
	private Channel totalIterationsChannel;
	private CurrentIterationListener currentIterationListener;
	private TotalIterationsListener totalIterationsListener;
	private Channel leadPointsChannel;
	private Channel endPointsChannel;
	private Channel currentLeadPointChannel;
	private Channel currentDataPointChannel;
	private Channel regionProgressChannel;
	private Channel inLeadChannel;
	
	public void createChannels() throws CAException, TimeoutException {
		leadPointsChannel = controller.createChannel(leadPointsPV, leadPointsListener, MonitorType.NATIVE,false);
		endPointsChannel = controller.createChannel(endPointsPV,endPointsListener,MonitorType.NATIVE, false);
		regionProgressChannel = controller.createChannel(regionProgressPV, regionProgressListener, MonitorType.NATIVE,false);
		currentPointChannel=controller.createChannel(currentPointPV,currentPointListener,MonitorType.NATIVE,false );
		totalPointsChannel = controller.createChannel(totalPointsPV,totalPointsListener,MonitorType.NATIVE, false);
		currentIterationChannel=controller.createChannel(currentIterationPV,currentIterationListener,MonitorType.NATIVE,false );
		totalIterationsChannel=controller.createChannel(totalIterationsPV,totalIterationsListener, MonitorType.NATIVE,false );
		controller.creationPhaseCompleted();
		logger.debug("channels are created");
	}
	
	public void disposeChannels() {
		leadPointsChannel.dispose();
		endPointsChannel.dispose();
		currentLeadPointChannel.dispose();
		currentDataPointChannel.dispose();
		regionProgressChannel.dispose();
		inLeadChannel.dispose();
		currentIterationChannel.dispose();
		totalIterationsChannel.dispose();
		currentPointChannel.dispose();
		totalPointsChannel.dispose();
		
		logger.debug("all channels are disposed");
		
	}

	int totalSteps = 0;
	private Channel currentPointChannel;
	private Channel totalPointsChannel;
	private Text txtCurrentStep;
	private ProgressBar progressBar;
	public int totalIterations=0;
	public int currentiteration=0;
	private Label lblIterationValue;
	private String leadPointsPV;
	private String endPointsPV;
	private String currentLeadPointPV;
	private String currentDataPointPV;
	private String regionProgressPV;
	private String inLeadPV;
	private Label lblMin;
	private Label lblMax;

	private class LeadPointsListener implements MonitorListener {

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

	private class EndPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int endPoints = (int)((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							lblMax.setText(String.valueOf(endPoints));
						}
					});
				}
				logger.debug("End points updated to {}", endPoints);
			}
		}
	}

	private class RegionProgressListener implements MonitorListener {

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
						}
					});
				}
				logger.debug("percentage completed updated to {}", percentage);
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
		lblIterationValue.setText(String.valueOf(currentiteration)+"/"+String.valueOf(totalIterations));
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
	private class CurrentPointListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				final int currentpoint =(int) ((DBR_Double) dbr).getDoubleValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							txtCurrentStep.setText(String.valueOf(currentpoint));
							// reset progress bar when completed
							if (currentpoint == totalSteps) {
								progressBar.setSelection(0);
							}
						}
					});
				}
				logger.debug("current point number updated to {}", currentpoint);
			}
		}
	}

	private class TotalPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				totalSteps = (int)((DBR_Double) dbr).getDoubleValue()[0];
				logger.debug("total number of points updated to {}", totalSteps);
			}
		}
	}

	@Override
	public void dispose() {
		disposeChannels();
		super.dispose();
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

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.info("Region Progress EPICS Channels initialisation completed!");
		
	}

	public void setLeadPointsPV(String leadPointsPV) {
		this.leadPointsPV=leadPointsPV;
		
	}

	public void setEndPointsPV(String endPointsPV) {
		this.endPointsPV=endPointsPV;		
	}

	public void setCurrentLeadPointPV(String currentLeadPointPV) {
		this.currentLeadPointPV=currentLeadPointPV;
	}

	public void setCurrentDataPointPV(String currentDataPointPV) {
		this.currentDataPointPV=currentDataPointPV;
	}

	public void setRegionProgressPV(String regionProgressPV) {
		this.regionProgressPV=regionProgressPV;
		
	}

	public void setInLeadPV(String inLeadPV) {
		this.inLeadPV=inLeadPV;
		
	}
}
