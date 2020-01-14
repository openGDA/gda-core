/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.stream.Collectors.toList;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * An section of the mapping view for choosing template files to be applied to the
 * Nexus file produced from the mapping scan.
 */
public class NexusTemplatesSection extends AbstractMappingSection {

	private static final String NO_TEMPLATES_SELECTED = "<no template files selected>";

	private Label templatesLabel;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		// create the composite
		final Composite templatesComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).applyTo(templatesComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(templatesComposite);

		// create the label
		new Label(templatesComposite, SWT.NONE).setText("Template Files:");

		// create the read only text field showing the template files
		templatesLabel = new Label(templatesComposite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(300, SWT.DEFAULT).grab(true, false).applyTo(templatesLabel);
		updateTemplatesLabel();

		// create the edit button
		final Button editTemplatesButton = new Button(templatesComposite, SWT.NONE);
		editTemplatesButton.setImage(getImage("icons/pencil.png"));
		editTemplatesButton.setToolTipText("Select Nexus Template Files");
		editTemplatesButton.addListener(SWT.Selection, event -> editTemplateFiles());
		GridDataFactory.swtDefaults().align(SWT.TRAIL, SWT.CENTER).applyTo(editTemplatesButton);
	}

	private void updateTemplatesLabel() {
		final String labelText = getTemplateLabel();

		templatesLabel.setText(labelText);
		templatesLabel.setToolTipText(labelText);
	}

	private String getTemplateLabel() {
		final List<String> templateFilePaths = getMappingBean().getTemplateFilePaths();
		if (templateFilePaths == null || templateFilePaths.isEmpty()) {
			return NO_TEMPLATES_SELECTED;
		}

		return String.join(", ", templateFilePaths.stream()
				.map(NexusTemplatesSection::getFileName)
				.collect(toList()));
	}

	private static String getFileName(String filePath) {
		return Paths.get(filePath).getFileName().toString();
	}

	private void editTemplateFiles() {
		final List<String> templateFilePaths = new ArrayList<>(getMappingBean().getTemplateFilePaths());
		final TemplateFilesSelectionDialog dialog = new TemplateFilesSelectionDialog(getShell(), templateFilePaths);

		if (dialog.open() == Window.OK) {
			getMappingBean().setTemplateFilePaths(dialog.getTemplateFilePaths());
			updateTemplatesLabel();
		}
	}

	@Override
	public void updateControls() {
		updateTemplatesLabel();
	}

	/**
	 * A dialog to select template files.
	 */
	private static final class TemplateFilesSelectionDialog extends Dialog {

		private List<String> templateFilePaths;
		private ListViewer filesListViewer;

		public TemplateFilesSelectionDialog(Shell shell, List<String> templateFilePaths) {
			super(shell);
			setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
			this.templateFilePaths = new ArrayList<>(templateFilePaths);
		}

		@Override
		public void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Select Template Files");
		}

		@Override
		public Control createDialogArea(Composite parent) {
			final Composite composite = (Composite) super.createDialogArea(parent);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

			filesListViewer = new ListViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			filesListViewer.setContentProvider(ArrayContentProvider.getInstance());
			filesListViewer.setLabelProvider(new LabelProvider() {
				@Override
				public Image getImage(Object element) {
					return null;
				}

				@Override
				public String getText(Object element) {
					return getFileName((String) element);
				}

			});

			GridDataFactory.fillDefaults().grab(true, true).hint(300, 200).applyTo(filesListViewer.getControl());
			filesListViewer.setInput(templateFilePaths);

			final int buttonWidth = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.CENTER).hint(buttonWidth, SWT.DEFAULT);

			final Composite buttonArea = new Composite(composite, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.TOP).applyTo(buttonArea);
			GridLayoutFactory.swtDefaults().applyTo(buttonArea);

			final Button addButton = new Button(buttonArea, SWT.PUSH);
			addButton.setText("Add...");
			addButton.addListener(SWT.Selection, event -> addTemplateFile());
			buttonGridDataFactory.applyTo(addButton);

			final Button removeButton = new Button(buttonArea, SWT.PUSH);
			removeButton.setText("Remove");
			removeButton.addListener(SWT.Selection, event -> removeTemplateFiles());
			buttonGridDataFactory.applyTo(removeButton);

			final Button removeAllButton = new Button(buttonArea, SWT.PUSH);
			removeAllButton.setText("Remove All");
			removeAllButton.addListener(SWT.Selection, event -> removeAllTemplateFiles());
			buttonGridDataFactory.applyTo(removeAllButton);

			return composite;
		}

		private void addTemplateFile() {
			final FileDialog dialog = new FileDialog(getShell(), SWT.APPLICATION_MODAL | SWT.MULTI);
			dialog.setFilterNames(new String[] { "Template Files (YAML)" });
			dialog.setFilterExtensions(new String[] { "*.yaml" });
			final IFilePathService filePathService = Activator.getService(IFilePathService.class);
			final String templatesDir = filePathService.getPersistenceDir();
			dialog.setFilterPath(templatesDir);

			if (dialog.open() != null) {
				final String dir = dialog.getFilterPath() + IPath.SEPARATOR;
				Arrays.stream(dialog.getFileNames())
					.forEach(name -> templateFilePaths.add(dir + name));
				filesListViewer.refresh();
			}
		}

		private void removeTemplateFiles() {
			final Object[] selectedItems = filesListViewer.getStructuredSelection().toArray();
			templateFilePaths.removeAll(Arrays.asList(selectedItems));
			filesListViewer.refresh();
		}

		private void removeAllTemplateFiles() {
			templateFilePaths.clear();
			filesListViewer.refresh();
		}

		private List<String> getTemplateFilePaths() {
			return templateFilePaths;
		}

	}

}
