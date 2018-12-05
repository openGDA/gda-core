package uk.ac.diamond.daq.client.gui.camera;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DiadConfigurationComposite<D extends DiadConfigurationModel> extends Composite {
	private static final Logger log = LoggerFactory.getLogger(DiadConfigurationListener.class);

	private static final int BUTTON_WIDTH = 80;

	private DiadConfigurationListener<D> listener;

	public DiadConfigurationComposite(Composite parent, DiadConfigurationListener<D> listener, int style) {
		super(parent, style);

		this.listener = listener;

		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

		Composite loadSavePanel = new Composite(this, SWT.None);
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
		Label label = new Label(this, SWT.None);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		Composite okCancelPanel = new Composite(this, SWT.None);
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
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		String filename = fileDialog.open();
		if (filename != null) {
			try (FileReader fileReader = new FileReader(filename)) {
				Gson gson = new Gson();
				Type type = new TypeToken<D>() {
				}.getType();
				D data = gson.fromJson(fileReader, type);
				listener.setModel(data);
			} catch (IOException e) {
				log.error("hard time loading data", e);
			}
		}
	}

	private void save() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		String filename = fileDialog.open();
		if (filename != null) {
			try (FileWriter fileWriter = new FileWriter(filename)) {
				Gson gson = new Gson();
				fileWriter.write(gson.toJson(listener.getModel()));
			} catch (IOException e) {
				log.error("hard time saving data", e);
			}
		}
	}
}
