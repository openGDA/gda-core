package uk.ac.gda.client.live.stream.controls.custom.widgets;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.swtdesigner.SWTResourceManager;

/**
 * A class which provides a GUI composite to display progress of a count down timer.
 *
 * @author fy65
 *
 */
public class CountdownProgressComposite extends Composite implements Observer {

	// GUI Elements
	private Label displayNameLabel;
	private ProgressBar progressBar;

	private String displayName;    // Allow a different prettier name be used if required
	private Observable observable; // must be set to obtain text to be displayed
	private Integer barWidth;
	private GridData gd_progressBar;

	public CountdownProgressComposite(Composite parent, int style) {
		super(parent, style);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		progressBar = new ProgressBar(this, SWT.HORIZONTAL);
		gd_progressBar = new GridData(GridData.FILL_HORIZONTAL);
		gd_progressBar.grabExcessHorizontalSpace = true;
		gd_progressBar.minimumWidth=SWT.DEFAULT;
		progressBar.setLayoutData(gd_progressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);

	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets a different name to be used in the GUI instead of the default which is the scannable name
	 * <p>
	 * After calling this method the control will be automatically redrawn.
	 *
	 * @param displayName
	 *            The name to be used in the GUI
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		displayNameLabel.setText(displayName);
		this.redraw();
	}
	public Observable getObservable() {
		return observable;
	}

	public void setObservable(Observable observable) {
		this.observable = observable;
		this.observable.addObserver(this);
	}	
	@Override
	public void update(Observable o, Object arg) {
		if (o == observable) {
			Display.getDefault().asyncExec(()-> progressBar.setSelection(Integer.parseInt(arg.toString())));
		}
	}

	public Integer getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(Integer barWidth) {
		this.barWidth = barWidth;
		gd_progressBar.widthHint=barWidth;
		this.redraw();
	}
	@Override
	public void dispose() {
		SWTResourceManager.disposeColors();
		super.dispose();
	}

}
