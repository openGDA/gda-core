package org.opengda.detector.electronanalyser.client.views;

import gda.epics.connection.EpicsController;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.server.VGScientaController;

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

	private ProgressBar progressBar;
	private VGScientaAnalyser analyser;

	public RegionProgressComposite(Composite parent, int style) {
		super(parent, style);
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new GridLayout(6, false));

		Label lblMin = new Label(rootComposite, SWT.None);

		progressBar = new ProgressBar(rootComposite, SWT.SMOOTH
				| SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Rectangle clientArea = rootComposite.getClientArea();
		progressBar.setBounds(clientArea.x, clientArea.y, 200, 20);

		lblMin.setText(String.valueOf(progressBar.getMinimum()));

		Label lblMax = new Label(rootComposite, SWT.None);
		lblMax.setText(String.valueOf(progressBar.getMaximum()));

		Label lblStep = new Label(rootComposite, SWT.None);
		lblStep.setText("Step");

		txtCurrentStep = new Text(rootComposite, SWT.BORDER);
		txtCurrentStep.setToolTipText("Display current step");
		txtCurrentStep.setEditable(false);
		GridData gd_txtCurrentStep = new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1);
		gd_txtCurrentStep.widthHint = 30;
		txtCurrentStep.setLayoutData(gd_txtCurrentStep);
		txtCurrentStep.setText("000");
		txtCurrentStep.setBackground(ColorConstants.black);
		txtCurrentStep.setForeground(ColorConstants.green);

		Button btnStop = new Button(rootComposite, SWT.None);
		btnStop.setToolTipText("Stop current region collection");
		btnStop.setImage(ElectronAnalyserClientPlugin.getDefault()
				.getImageRegistry().get(ImageConstants.ICON_STOP));
		initialise();
	}

	private void initialise() {
		if (getAnalyser() == null) {
			// Analyser must be called 'analyser' in Spring configuration
			analyser = (VGScientaAnalyser) (Finder.getInstance()
					.find("analyser"));
		}
		currentPointListener = new CurrentPointListener();
		totalPointsListener = new TotalPointsListener();
		try {
			addMonitors();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addMonitorListeners();
	}

	private CurrentPointListener currentPointListener;
	private TotalPointsListener totalPointsListener;
	private Monitor currentPointMonitor;
	private Monitor totalPointsMonitor;
	private EpicsController controller = EpicsController.getInstance();

	public void addMonitors() throws Exception {
		getAnalyser().getController();
		currentPointChannel = getAnalyser().getController().getChannel(
				VGScientaController.CURRENTPOINT);
		currentPointMonitor = controller.addMonitor(currentPointChannel);
		getAnalyser().getController();
		totalPointsChannel = getAnalyser().getController().getChannel(
				VGScientaController.TOTALPOINTS);
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

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
}
