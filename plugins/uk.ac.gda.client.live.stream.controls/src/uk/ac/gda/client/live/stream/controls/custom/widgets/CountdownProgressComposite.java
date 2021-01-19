package uk.ac.gda.client.live.stream.controls.custom.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.swtdesigner.SWTResourceManager;

import gda.observable.IObservable;
import gda.observable.IObserver;

/**
 * A class which provides a GUI composite to display progress of a count down timer.
 *
 * @author fy65
 *
 */
public class CountdownProgressComposite extends Composite implements IObserver {

	// GUI Elements
	private Label displayNameLabel;
	private ProgressBar progressBar;

	private String displayName;    // Allow a different prettier name be used if required
	private IObservable observable; // must be set to obtain text to be displayed
	private Integer barWidth;
	private GridData gdProgressBar;
	private Color color;

	public CountdownProgressComposite(Composite parent, int style) {
		super(parent, style);

		color = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);
		parent.setBackground(color);
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		progressBar = new ProgressBar(this, SWT.HORIZONTAL);
		gdProgressBar = new GridData(GridData.FILL_HORIZONTAL);
		gdProgressBar.grabExcessHorizontalSpace = true;
		gdProgressBar.minimumWidth=SWT.DEFAULT;
		progressBar.setLayoutData(gdProgressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		if (getObservable()!=null) {
			// required when view containing this being re-opened.
			getObservable().addIObserver(this);
		}
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
	public IObservable getObservable() {
		return observable;
	}

	public void setObservable(IObservable observable) {
		this.observable = observable;
		//required when 1st time this being created in LiveControl
		this.observable.addIObserver(this);
	}	

	public Integer getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(Integer barWidth) {
		this.barWidth = barWidth;
		gdProgressBar.widthHint=barWidth;
		this.redraw();
	}
	@Override
	public void dispose() {
		if (observable!=null) {
			observable.deleteIObserver(this);
		}
		if (color!=null) {
			color.dispose();
		}
		if (progressBar!=null) {
			progressBar.dispose();
		}
		super.dispose();
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == observable && !progressBar.isDisposed()) {
			Display.getDefault().asyncExec(()-> progressBar.setSelection(Integer.parseInt(arg.toString())));
		}
	}

}
