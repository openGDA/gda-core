package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;

import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

/**
 * class to display data colection progress for a
 * {@link org.opengda.detector.electronanalyser.model.Region} It monitors EPICS
 * progress PVs to update the progress bar. It also provides 'Stop' button to
 * abort current region collection. Users must provide 'analyser' object to enable the monitoring.
 *
 * @author fy65
 *
 */
public class ScanProgressComposite extends Composite implements IObserver {

	private Text txtPointValue;
	private Text txtRegionValue;
	private Text txtTimeRemaining;
	private ProgressBar progressBar;

	private int currentPointNumber;
	private int totalNumberOfPoints;
	private int totalActiveRegions;
	private int crrentRegionNumber;
	private Scriptcontroller scriptcontroller;

	public ScanProgressComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(1, false));

		Composite rootComposite = new Composite(this, SWT.NONE);
		GridData gd_rootComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_rootComposite.widthHint = 456;
		rootComposite.setLayoutData(gd_rootComposite);
		GridLayout layout = new GridLayout(6, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);

		Label lblPoint=new Label(rootComposite, SWT.None);
		lblPoint.setText("Point: ");

		txtPointValue = new Text(rootComposite, SWT.BORDER);
		GridData gd_lblIterationValue = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblIterationValue.widthHint = 40;
		txtPointValue.setLayoutData(gd_lblIterationValue);
		txtPointValue.setForeground(ColorConstants.green);
		txtPointValue.setEditable(false);
		txtPointValue.setBackground(ColorConstants.black);
		updateScanPointNumber(currentPointNumber, totalNumberOfPoints);

		Label lblRegion = new Label(rootComposite, SWT.NONE);
		lblRegion.setText("Region:");

		txtRegionValue = new Text(rootComposite, SWT.BORDER);
		txtRegionValue.setForeground(ColorConstants.green);
		txtRegionValue.setEditable(false);
		txtRegionValue.setBackground(ColorConstants.black);
		txtRegionValue.setText("0");
		GridData gd_txtCurrentPoint = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_txtCurrentPoint.widthHint = 40;
		txtRegionValue.setLayoutData(gd_txtCurrentPoint);
		updateRegionNumber(crrentRegionNumber, totalActiveRegions);

		Label lblTimeRemaining = new Label(rootComposite, SWT.NONE);
		lblTimeRemaining.setText("Time Remaining:");

		txtTimeRemaining = new Text(rootComposite, SWT.BORDER);
//		gd_txtIterationTimeRemaining.widthHint=40;
		txtTimeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		txtTimeRemaining.setForeground(ColorConstants.green);
		txtTimeRemaining.setBackground(ColorConstants.black);
		txtTimeRemaining.setText("0.0000");
		txtTimeRemaining.setEditable(false);

		Label lblProgress = new Label(rootComposite, SWT.NONE);
		lblProgress.setText("progress:");

		progressBar = new ProgressBar(rootComposite, SWT.HORIZONTAL);
		GridData gd_progressBar = new GridData(GridData.FILL_HORIZONTAL);
		gd_progressBar.grabExcessHorizontalSpace = false;
		gd_progressBar.horizontalSpan = 5;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);

	}
	public void initialise() {
		if (getScriptcontroller()==null) {
			throw new IllegalStateException("Event admin object must not null");
		}
	}
	private void updateRegionNumber(int currentRegionNumber,
			int totalActiveRegions) {
		txtRegionValue.setText(String.valueOf(currentRegionNumber)+'/'+String.valueOf(totalActiveRegions));
	}

	private void updateScanPointNumber(int currentPointNumber,
			int totalNumberOfPoints) {
		txtPointValue.setText(String.valueOf(currentPointNumber)+'/'+String.valueOf(totalNumberOfPoints));
	}

	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}

	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}
	@Override
	public void update(Object source, Object arg) {
		if (source==getScriptcontroller()) {
			if (arg instanceof ScanStartEvent) {
				ScanStartEvent evt=(ScanStartEvent)arg;
				totalNumberOfPoints=evt.getNumberOfPoints();
				String scanFilename = evt.getScanFilename();
				int scanNumber = evt.getScanNumber();
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							updateScanPointNumber(currentPointNumber, totalNumberOfPoints);
						}
					});
				}
			}
		}

	}

}
