/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.rcp.ncd.samplerack.views;

import static java.util.Arrays.stream;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.COLOR_RED;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.LEFT;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.OPEN;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.RIGHT;
import static org.eclipse.swt.SWT.SAVE;
import static org.eclipse.swt.SWT.SINGLE;
import static org.eclipse.swt.SWT.TRAIL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import gda.jython.InterfaceProvider;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.server.ncd.samplerack.Sample;
import uk.ac.gda.server.ncd.samplerack.SampleConfiguration;
import uk.ac.gda.server.ncd.samplerack.SampleRack;
import uk.ac.gda.server.ncd.samplerack.SampleRackService;

public class SampleRackView {

	private final class SampleRackContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return ((SampleRackService) inputElement).getRacks().toArray();
		}
	}

	private Color error = new Color(Display.getDefault(), 255, 240, 240);

	@Inject
	private SampleRackService rackService;

	private SampleConfiguration model = new SampleConfiguration();
	private TableViewer sampleTable;

	private SampleRack rack;

	private Button runButton;

	private Label runtimeLabel;

	private Label statusLabel;

	private Text backgroundFileEntry;

	/** Utility class to prevent endless casting anonymous classes */
	private class TypedLabelProvider<T> extends ColumnLabelProvider {
		private Function<T, String> textFunc;
		private Function<T, Color> colorFunc = e -> null;
		private Function<T, String> tooltip = e -> null;

		public TypedLabelProvider(Function<T, String> extractor) {
			textFunc = extractor;
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getText(Object element) {
			return textFunc.apply((T) element);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Color getBackground(Object element) {
			return colorFunc.apply((T) element);
		}

		@SuppressWarnings("unchecked")
		@Override
		public String getToolTipText(Object element) {
			return tooltip.apply((T) element);
		}

		public TypedLabelProvider<T> background(Function<T, Color> func) {
			this.colorFunc = func;
			return this;
		}

		public TypedLabelProvider<T> tooltip(Function<T, String> tooltip) {
			this.tooltip = tooltip;
			return this;
		}
	}

	/** Utility class to prevent endless casting in anonymous classes */
	private class TypedEditingSupport<T, V> extends EditingSupport {
		public TypedEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		private CellEditor editor = null;
		private Function<T, V> getter = e -> null;
		private BiConsumer<T, V> setter = (e, v) -> {
		};

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Object getValue(Object element) {
			return getter.apply((T) element);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void setValue(Object element, Object value) {
			setter.accept((T) element, (V) value);
			getViewer().update(element, null);
			refresh();
		}

		public TypedEditingSupport<T, V> editor(CellEditor editor) {
			this.editor = editor;
			return this;
		}

		public TypedEditingSupport<T, V> getter(Function<T, V> getter) {
			this.getter = getter;
			return this;
		}

		public TypedEditingSupport<T, V> setter(BiConsumer<T, V> setter) {
			this.setter = setter;
			return this;
		}
	}

	@PostConstruct
	public void createPartControl(Composite parent) {

		var root = new Composite(parent, NONE);
		root.setLayout(new GridLayout(4, false));

		createImportOptions(root);

		var selectLabel = new Label(root, NONE);
		selectLabel.setText("Select Rack");

		var rackCombo = new ComboViewer(new CCombo(root, BORDER));
		rackCombo.setContentProvider(new SampleRackContentProvider());
		rackCombo.setLabelProvider(new TypedLabelProvider<>(Object::toString));
		rackCombo.addSelectionChangedListener(event -> {
			rack = (SampleRack) event.getStructuredSelection().getFirstElement();
			refresh();
		});
		rackCombo.setInput(rackService);
		rackCombo.getCCombo().select(0);
		rack = rackService.getRacks().iterator().next();

		createRackConfigureButton(root);

		sampleTable = buildTable(root);
		createSampleControls(root);

		createMetadataFileControls(root);

		createRunControls(root);
		parent.addDisposeListener(e -> error.dispose());
		refresh();
	}

	private void createRackConfigureButton(Composite root) {
		final Button configButton = new Button(root, SWT.PUSH);
		configButton.setText("Rack Configure");
		configButton.addListener(SWT.Selection, event -> {
				RackConfigureDialog configRackDialog = new RackConfigureDialog(rack, rackService);
				configRackDialog.setBlockOnOpen(true);
				configRackDialog.open();
		});
		GridDataFactory.fillDefaults().applyTo(configButton);
	}

	private void createMetadataFileControls(Composite root) {
		var controls = new Composite(root, NONE);
		controls.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(controls);

		var entryLayoutData = GridDataFactory.fillDefaults().grab(true, false);

		var backgroundLabel = new Label(controls, NONE);
		backgroundLabel.setText("Background file");
		backgroundFileEntry = new Text(controls, SINGLE | BORDER);
		backgroundFileEntry.addModifyListener(e -> {
			var text = backgroundFileEntry.getText();
			if (text.isBlank()) {
				backgroundFileEntry.setBackground(null);
				if (model.getBackground() != null) {
					model.setBackground(null);
					refresh();
				}
			} else if (Files.exists(Paths.get(text))) {
				backgroundFileEntry.setBackground(null);
				if (!text.equals(model.getBackground())) {
					model.setBackground(text);
					refresh();
				}
			} else {
				backgroundFileEntry.setBackground(error);
			}
		});
		entryLayoutData.applyTo(backgroundFileEntry);
		var backgroundChoiceButton = new Button(controls, PUSH);
		backgroundChoiceButton.setImage(GDAClientActivator.getImageDescriptor("icons/folder.png").createImage());
		backgroundChoiceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var dialog = new FileDialog(Display.getCurrent().getActiveShell(), OPEN);
				dialog.setText("Choose Background File");
				dialog.setFilterExtensions(new String[] { "*.nxs" });
				String proc = InterfaceProvider.getPathConstructor().getClientVisitDirectory();
				dialog.setFilterPath(proc);
				var file = dialog.open();
				if (file != null) {
					model.setBackground(file);
					refresh();
				}
			}
		});
	}

	private void createRunControls(Composite root) {

		var controls = new Composite(root, NONE);
		controls.setLayout(new GridLayout(5, false));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(controls);

		var fill = GridDataFactory.fillDefaults();
		var saveButton = new Button(controls, PUSH);
		saveButton.setText("Save");
		fill.applyTo(saveButton);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveConfig();
			}
		});
		var loadButton = new Button(controls, PUSH);
		loadButton.setText("Load");
		fill.applyTo(loadButton);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadConfig();
			}
		});

		statusLabel = new Label(controls, TRAIL);
		statusLabel.setForeground(Display.getCurrent().getSystemColor(COLOR_RED));
		fill.copy().grab(true, false).align(FILL, CENTER).applyTo(statusLabel);

		runButton = new Button(controls, PUSH);
		runButton.setText("Run");
		runButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				statusLabel.setText("Sample Rack scan is running ...");
				runButton.setEnabled(false);

				Thread background = new Thread() {
					@Override
					public void run() {
						try {
							rackService.runSamples(rack, model);
						} finally {
							Display.getDefault().asyncExec(() -> {
								statusLabel.setText("");
								runButton.setEnabled(true);
							});
						}
					}
				};
				background.start();
			}
		});
		fill.applyTo(runButton);

	}

	private void createImportOptions(Composite root) {
		var importOptions = new Composite(root, NONE);
		GridDataFactory.fillDefaults().span(4, 1).applyTo(importOptions);
		importOptions.setLayout(new GridLayout(4, false));
		var importData = GridDataFactory.fillDefaults();

		var importLabel = new Label(importOptions, CENTER | RIGHT);
		importLabel.setText("Import Samples");
		importData.copy().align(RIGHT, CENTER).grab(true, false).applyTo(importLabel);

		var ispybButton = new Button(importOptions, PUSH);
		ispybButton.setText("From ISpyB");
		ispybButton.setEnabled(false);
		importData.applyTo(ispybButton);

		var excelButton = new Button(importOptions, PUSH);
		excelButton.setEnabled(false);
		excelButton.setText("From Excel");
		importData.applyTo(excelButton);

		var previousButton = new Button(importOptions, PUSH);
		previousButton.setText("From Previous");
		importData.applyTo(previousButton);
		previousButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importLocal();
			}
		});
	}

	private void createSampleControls(Composite root) {
		var controls = new Composite(root, NONE);
		controls.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().span(4, 1).applyTo(controls);

		runtimeLabel = new Label(controls, LEFT);
		runtimeLabel.setText("Total runtime: 0:00:00");
		runtimeLabel.setToolTipText("Runtime is sum of individual samples and does not include motion of rack");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(runtimeLabel);

		var removeButton = new Button(controls, PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (var sample : sampleTable.getStructuredSelection()) {
					model.removeSample((Sample) sample);
				}
				refresh();
			}
		});
		GridDataFactory.fillDefaults().applyTo(removeButton);

		var duplicateButton = new Button(controls, PUSH);
		duplicateButton.setText("Copy");
		duplicateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (var sample : sampleTable.getStructuredSelection()) {
					model.addSample(((Sample) sample).copy());
				}
				refresh();
			}
		});
		GridDataFactory.fillDefaults().applyTo(duplicateButton);

		var addButton = new Button(controls, PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.addSample(new Sample("", "New Sample", 1, 1));
				refresh();
			}
		});
		GridDataFactory.fillDefaults().applyTo(addButton);
	}

	protected void refresh() {
		refreshHeadings(sampleTable.getTable().getColumn(1));
		sampleTable.refresh();
		var totalSeconds = stream(model.samples()).filter(Sample::isActive).mapToDouble(s -> s.getFrames() * s.getTpf()).sum();
		var runtime = Duration.ofSeconds((long) totalSeconds);
		var time = String.format("Total runtime: %d:%02d:%02d", runtime.toHours(), runtime.toMinutesPart(), runtime.toSecondsPart());
		runtimeLabel.setText(time);

		if (model.getBackground() != null) {
			backgroundFileEntry.setText(model.getBackground());
		}

		if (rack == null) {
			statusLabel.setText("Sample rack must be selected before running samples");
			runButton.setEnabled(false);
		} else if (stream(model.samples()).filter(Sample::isActive).count() == 0) {
			statusLabel.setText("No samples active");
			runButton.setEnabled(false);
		} else {
			var allValid = stream(model.samples())
					.filter(Sample::isActive)
					.allMatch(s -> rack.validLocation(s.getCell()));
			statusLabel.setText(allValid ? "" : "Active sample locations are not all valid");
			runButton.setEnabled(allValid);
		}
	}

	private TableViewer buildTable(Composite root) {
		var comp = new Composite(root, SWT.NONE);
		GridDataFactory.fillDefaults().span(4, 1).grab(true, true).applyTo(comp);
		var layout = new TableColumnLayout();
		comp.setLayout(layout);

		comp.setBackground(Display.getCurrent().getSystemColor(3));

		var tableViewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		var table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		ColumnViewerToolTipSupport.enableFor(tableViewer);

		var handleColumn = new TableViewerColumn(tableViewer, CENTER);
		layout.setColumnData(handleColumn.getColumn(), new ColumnPixelData(30, false));
		handleColumn.getColumn().setResizable(false);
		handleColumn.setLabelProvider(new TypedLabelProvider<>(s -> null));

		var activeColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		activeColumn.getColumn().setText("☐");
		activeColumn.setLabelProvider(new TypedLabelProvider<Sample>(s -> s.isActive() ? "✓" : ""));
		activeColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var c = (TableColumn) e.getSource();
				if (Arrays.stream(model.samples()).allMatch(Sample::isActive)) {
					for (var s : model.samples()) {
						s.setActive(false);
					}
				} else {
					for (var s : model.samples()) {
						s.setActive(true);
					}
				}
				refreshHeadings(c);
				tableViewer.refresh();
			}
		});
		activeColumn.setEditingSupport(new TypedEditingSupport<Sample, Boolean>(activeColumn.getViewer())
				.editor(new CheckboxCellEditor(table))
				.getter(Sample::isActive)
				.setter((s, v) -> {
					s.setActive(v);
					refreshHeadings(activeColumn.getColumn());
				}));
		layout.setColumnData(activeColumn.getColumn(), new ColumnPixelData(30, false));
		activeColumn.getColumn().setResizable(false);

		var locationColumn = new TableViewerColumn(tableViewer, CENTER);
		locationColumn.getColumn().setText("Cell");
		locationColumn.setLabelProvider(new TypedLabelProvider<Sample>(Sample::getCell)
				.background(s -> (!s.isActive() || rack == null || rack.validLocation(s.getCell())) ? null : error)
				.tooltip(s -> (!s.isActive() || rack == null || rack.validLocation(s.getCell())) ? null : "Sample location is not valid for selected rack"));
		locationColumn.setEditingSupport(new TypedEditingSupport<Sample, String>(locationColumn.getViewer())
				.editor(new TextCellEditor(table))
				.setter(Sample::setCell)
				.getter(Sample::getCell));
		layout.setColumnData(locationColumn.getColumn(), new ColumnPixelData(50, false));

		var nameColumn = new TableViewerColumn(tableViewer, CENTER);
		nameColumn.getColumn().setText("Sample Name");
		nameColumn.setLabelProvider(new TypedLabelProvider<Sample>(Sample::getName));
		nameColumn.setEditingSupport(new TypedEditingSupport<Sample, String>(nameColumn.getViewer())
				.editor(new TextCellEditor(table))
				.getter(Sample::getName)
				.setter(Sample::setName));
		layout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(2, 30));

		var frameColumn = new TableViewerColumn(tableViewer, CENTER);
		frameColumn.getColumn().setText("Frames");
		frameColumn.setLabelProvider(new TypedLabelProvider<Sample>(s -> Integer.toString(s.getFrames())));
		frameColumn.setEditingSupport(new TypedEditingSupport<Sample, String>(frameColumn.getViewer())
				.editor(new TextCellEditor(table))
				.getter(s -> Integer.toString(s.getFrames()))
				.setter((s, v) -> {
					try {
						var frames = Integer.valueOf(v);
						if (frames > 0) {
							s.setFrames(frames);
						}
					} catch (NullPointerException | NumberFormatException e) {
						// Default back to previous value
					}
				}));
		layout.setColumnData(frameColumn.getColumn(), new ColumnPixelData(60, false));

		var tpfColumn = new TableViewerColumn(tableViewer, CENTER);
		tpfColumn.getColumn().setText("Time/Frame");
		tpfColumn.setLabelProvider(new TypedLabelProvider<Sample>(s -> Double.toString(s.getTpf())));
		tpfColumn.setEditingSupport(new TypedEditingSupport<Sample, String>(frameColumn.getViewer())
				.editor(new TextCellEditor(table))
				.getter(s -> Double.toString(s.getTpf()))
				.setter((s, v) -> {
					try {
						var tpf = Double.valueOf(v);
						if (tpf > 0) {
							s.setTpf(tpf);
						}
					} catch (NullPointerException | NumberFormatException e) {
						// Default back to previous value
					}
				}));
		layout.setColumnData(tpfColumn.getColumn(), new ColumnPixelData(100, false));
		tableViewer.setContentProvider((IStructuredContentProvider) (e -> ((SampleConfiguration) e).samples()));
		tableViewer.setInput(model);
		return tableViewer;
	}

	private void refreshHeadings(TableColumn column) {
		var samples = model.samples();
		var active = Arrays.stream(model.samples()).filter(Sample::isActive).count();
		if (active == 0) {
			column.setText("☐");
		} else if (active < samples.length) {
			column.setText("⊡");
		} else {
			column.setText("⊠");
		}
	}

	@PreDestroy
	private void close() {
		error.dispose();
	}

	private void saveConfig() {
		var dialog = new FileDialog(Display.getCurrent().getActiveShell(), SAVE);
		dialog.setText("Save current configuration");
		dialog.setFilterExtensions(new String[] { "*.json" });
		String proc = InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("processing");
		dialog.setFilterPath(proc);
		dialog.setOverwrite(true);
		var file = dialog.open();
		if (file != null) {
			try {
				model.saveTo(file);
			} catch (IOException e) {
				UIHelper.showError("Error saving rack configuration", e);
			}
		}
	}

	private void loadConfig() {
		var dialog = new FileDialog(Display.getCurrent().getActiveShell(), OPEN);
		dialog.setText("Load existing configuration");
		dialog.setFilterExtensions(new String[] { "*.json" });
		String proc = InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("processing");
		dialog.setFilterPath(proc);
		var file = dialog.open();
		if (file != null) {
			try {
				model = SampleConfiguration.fromFile(file);
				sampleTable.setInput(model);
				refresh();
			} catch (IOException | RuntimeException  e) {
				UIHelper.showError("Error loading rack configuration", e);
			}
		}
	}

	private void importLocal() {
		var dialog = new FileDialog(Display.getCurrent().getActiveShell(), OPEN);
		dialog.setText("Save current configuration");
		dialog.setFilterExtensions(new String[] { "*.json" });
		String proc = InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("processing");
		dialog.setFilterPath(proc);
		var file = dialog.open();
		if (file != null) {
			try {
				var newSamples = SampleConfiguration.fromFile(file);
				for (var sample : newSamples.samples()) {
					model.addSample(sample);
				}
				refresh();
			} catch (IOException e) {
				UIHelper.showError("Error saving rack configuration", e);
			}
		}
	}
}
