package uk.ac.diamond.daq.experiment.ui.driver;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AbortConditionsPage extends WizardPage {
	
	AbortConditionsPage() {
		super("Abort Conditions");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		
		Label title = new Label(composite, SWT.NONE);
		title.setText("Abort Conditions");
		GridDataFactory.swtDefaults().applyTo(title);
		
		FontData[] fontData = title.getFont().getFontData();
		fontData[0].setHeight(14);
		title.setFont(new Font(Display.getDefault(), fontData[0]));

		Composite conditions = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(20, 20).applyTo(conditions);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(conditions);
		
		Button failure = new Button(conditions, SWT.RADIO);
		failure.setText("Failure");
		failure.setSelection(true);
		skipCells(conditions, 2);
		
		new Button(conditions, SWT.RADIO).setText("Target load");
		
		new Text(conditions, SWT.BORDER);
		new Label(conditions, SWT.NONE).setText("N");
		
		new Button(conditions, SWT.RADIO).setText("Target displacement");
		new Text(conditions, SWT.BORDER);
		new Label(conditions, SWT.NONE).setText("mm");
		
		new Button(conditions, SWT.RADIO).setText("Custom profile complete");
		skipCells(conditions, 2);
		
		setControl(composite);
	}
	
	private void skipCells(Composite parent, int howMany) {
		for (int i = 0; i < howMany; i++) {
			new Label(parent, SWT.NONE);
		}
	}

}
