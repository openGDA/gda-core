package org.opengda.detector.electronanalyser.client.views;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to display data colection progress for a
 * {@link org.opengda.detector.electronanalyser.model.Region} It monitors EPICS
 * progress PVs to update the progress bar. It also provide 'Stop' button to
 * abort current region collection. Users must provide 'analyser' object to enable the monitoring.
 * 
 * @author fy65
 * 
 */
public class RegionProgressComposite extends Composite {
	private String currentPointPV;
	private String totalPointsPV;
	private static final Logger logger=LoggerFactory.getLogger(RegionProgressComposite.class);

	public RegionProgressComposite(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new FillLayout());
		Composite rootComposite = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(6, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);
		
		Label lblMin = new Label(rootComposite, SWT.NONE);
		
		progressBar = new ProgressBar(rootComposite, SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		lblMin.setText(String.valueOf(progressBar.getMinimum()));
		
		Label lblMax = new Label(rootComposite, SWT.NONE);
		lblMax.setText(String.valueOf(progressBar.getMaximum()));
		
		Label lblStep = new Label(rootComposite, SWT.NONE);
		lblStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStep.setText("Step:");
		
		txtCurrentStep = new Text(rootComposite, SWT.BORDER);
		txtCurrentStep.setText("currentStep");
		txtCurrentStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Button btnStop = new Button(rootComposite, SWT.CENTER);
		btnStop.setImage(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_STOP));
		btnStop.setToolTipText("Stop current region collection");
	}

	public void initialise() {
		if (getCurrentPointPV()==null || getTotalPointsPV() == null) {
			throw new IllegalStateException("required parameters for 'currentPointPV' and/or 'totalPointsPV' are missing.");
		}
		currentPointListener = new CurrentPointListener();
		totalPointsListener = new TotalPointsListener();
		try {
			addMonitors();
		} catch (Exception e) {
			logger.error("Region progress composite failed to add monitors to the electron analyser.", e);
		}
		addMonitorListeners();
	}

	private CurrentPointListener currentPointListener;
	private TotalPointsListener totalPointsListener;
	private Monitor currentPointMonitor;
	private Monitor totalPointsMonitor;
	private EpicsController controller = EpicsController.getInstance();

	public void addMonitors() throws Exception {
		currentPointChannel = controller.createChannel(currentPointPV);
		currentPointMonitor = controller.addMonitor(currentPointChannel);
		totalPointsChannel = controller.createChannel(totalPointsPV);
		totalPointsMonitor = controller.addMonitor(totalPointsChannel);
	}

	public void removeMonitors() throws CAException {
		currentPointMonitor.clear();
		totalPointsMonitor.clear();
	}

	public void addMonitorListeners() {
		currentPointMonitor.addMonitorListener(currentPointListener);
		totalPointsMonitor.addMonitorListener(totalPointsListener);
	}

	public void removeMonitorListeners() {
		currentPointMonitor.removeMonitorListener(currentPointListener);
		totalPointsMonitor.removeMonitorListener(totalPointsListener);
	}

	int totalSteps = 0;
	private Channel currentPointChannel;
	private Channel totalPointsChannel;
	private Text txtCurrentStep;
	private ProgressBar progressBar;

	private class CurrentPointListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			// TODO Auto-generated method stub
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				int currentpoint = ((DBR_Int) dbr).getIntValue()[0];
				progressBar.setSelection(currentpoint);
				txtCurrentStep.setText(String.valueOf(currentpoint));
				// reset progress bar when completed
				if (progressBar.getSelection() == totalSteps) {
					progressBar.setSelection(0);
				}
			}
		}
	}

	private class TotalPointsListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				totalSteps = ((DBR_Int) dbr).getIntValue()[0];
				progressBar.setMaximum(totalSteps);
			}
		}
	}

	@Override
	public void dispose() {
		removeMonitorListeners();
		try {
			removeMonitors();
		} catch (CAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
