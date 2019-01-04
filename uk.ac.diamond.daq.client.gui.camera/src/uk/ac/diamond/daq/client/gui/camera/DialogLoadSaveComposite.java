package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialogLoadSaveComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(DialogLoadSaveComposite.class);

	private static final int BUTTON_WIDTH = 80;

	public DialogLoadSaveComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

		Composite loadSavePanel = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(loadSavePanel);
		RowLayoutFactory.swtDefaults().applyTo(loadSavePanel);

		Button loadButton = new Button(loadSavePanel, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.addListener(SWT.Selection, e -> load());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(loadButton);

		Button saveButton = new Button(loadSavePanel, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addListener(SWT.Selection, e -> save());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(saveButton);

		// Spacing label between panels
		Label label = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		Composite okCancelPanel = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(okCancelPanel);
		RowLayoutFactory.swtDefaults().applyTo(okCancelPanel);

		Button cancelButton = new Button(okCancelPanel, SWT.PUSH);
		cancelButton.setText("Cancel");
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(cancelButton);

		Button okButton = new Button(okCancelPanel, SWT.PUSH);
		okButton.setText("OK");
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(okButton);
	}

	private void load() {
		log.info("I would have produced an open dialog");
	}

	private void save() {
		log.info("I would have produced a save dialog");
	}
}
