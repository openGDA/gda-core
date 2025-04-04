package uk.ac.gda.client.experimentdefinition.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.components.ExperimentRunEditor;

public class CopyValuesCommandHandler extends AbstractExperimentCommandHandler {
    private final static Logger logger = LoggerFactory.getLogger(CopyValuesCommandHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        copyValuesToRows();
        return null;
    }

    private void copyValuesToRows() {
    	ExperimentRunEditor runEditor = getEditorManager().getActiveRunEditor();
		if (runEditor == null) {
			logger.warn("Can't get reference to the ExperimentRunEditor - does the multi-scan view have the current focus");
			return;
		}

        int selectedRow = runEditor.getTableViewer().getTable().getSelectionIndex();

        String selectedType = runEditor.getSelectedBeanType();

		// Selected scan parameters
		var selectedScan = getEditorManager().getSelectedScan();

		// Get the file name corresponding to the selected type
		IFile selectedFile = selectedScan.getFile(selectedType);
		if (selectedFile == null) {
			logger.debug("No file for selected column type : {}", selectedType);
			return ;
		}

		String filenameToCopy = selectedFile.getName();

        CustomParameterSetterDialog dialog = new CustomParameterSetterDialog(Display.getDefault().getActiveShell(), selectedRow, runEditor.getRunObjectManager().getExperimentList().size() - 1);
        if (dialog.open() != Window.OK) {
            return;
        }

        List<IExperimentObject> expObjects = runEditor.getRunObjectManager().getExperimentList();
        for (int i = dialog.getStartRow(); i <= dialog.getEndRow(); i++) {
        	IExperimentObject expObject = expObjects.get(i);
        	try {
        		expObject.setFileName(selectedType, selectedFile.getName());
				logger.debug("Applying filename {} to row {}", filenameToCopy, i);
			} catch (Exception e) {
				logger.error("Error applying filename to row {}",i, e);
			}
        }

        runEditor.runChangePerformed(null);
    }

    private class CustomParameterSetterDialog extends Dialog {
        private int maxRow;
        private int startRow;
        private int endRow;
        private Spinner endSpinner;
        private Spinner startSpinner;

        public CustomParameterSetterDialog(Shell parentShell, int initialRow, int maxRow) {
            super(parentShell);
            this.maxRow = maxRow;
            this.startRow = initialRow;
            this.endRow = Math.min(initialRow + 1, maxRow);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            createComposite(container);
            return parent;
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText("Apply value to range of rows");
        }

        private void createComposite(Composite parent) {
            GridLayout layout = new GridLayout();
            layout.numColumns = 4;
            parent.setLayout(layout);

            Label infoLabel = new Label(parent, SWT.NONE);
            infoLabel.setText("Select the range of rows the value should be applied to:");
            GridDataFactory.fillDefaults().span(4, 1).applyTo(infoLabel);

            Label startTextLabel = new Label(parent, SWT.NONE);
            startTextLabel.setText("Start row:");
            startSpinner = new Spinner(parent, SWT.BORDER);
            startSpinner.setValues(startRow, 0, maxRow, 0, 1, 10);

            Label endTextLabel = new Label(parent, SWT.NONE);
            endTextLabel.setText("End row:");
            endSpinner = new Spinner(parent, SWT.BORDER);
            endSpinner.setValues(endRow, 0, maxRow, 0, 1, 10);
        }

        @Override
        public boolean close() {
            startRow = Math.min(startSpinner.getSelection(), endSpinner.getSelection());
            endRow = Math.max(startSpinner.getSelection(), endSpinner.getSelection());
            return super.close();
        }

        public int getStartRow() {
            return startRow;
        }

        public int getEndRow() {
            return endRow;
        }
    }
}