/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs.ui;

import static uk.ac.gda.devices.hatsaxs.SampleListStats.getRuntimeString;
import static uk.ac.gda.devices.hatsaxs.ui.Column.ColumnType.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import uk.ac.gda.devices.hatsaxs.beans.ManualBean;
import uk.ac.gda.devices.hatsaxs.ui.Column.ColumnType;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class ManualSampleFieldComposite extends FieldComposite {

	private static final Logger logger = LoggerFactory.getLogger(ManualSampleFieldComposite.class);
	private static final String SAMPLE_TIME_OVERHEAD = "gda.devices.manual.overhead";

	Object value = null;
	private Table table;
	private Label sampleCount;
	private Label totalRuntime;
	private final TableViewer tableViewer;
	private Composite composite_1;
	private final RichBeanEditorPart rbeditor;

	/** Collection of colours to be disposed of when the view is disposed */
	private Collection<Runnable> colourClearing = new ArrayList<>();

	private boolean isStaff;

	private Map<String, Column<ManualBean, ?>> columns;

	Color okay, warning;

	private static final SimpleObjectTransfer TRANSFER = new SimpleObjectTransfer() {
		private final String TYPE_NAME = "uk.ac.gda.devices.manual.ui.ManualBeanTransfer" + System.currentTimeMillis(); //$NON-NLS-1$
		private final int TYPE_ID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPE_ID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}
	};

	public ManualSampleFieldComposite(Composite parent, int style, RichBeanEditorPart editor) {
		super(parent, style);
		this.rbeditor = editor;
		this.isStaff = InterfaceProvider.getBatonStateProvider().getMyDetails().getAuthorisationLevel() >= 3;
		if (isStaff) {
			logger.debug("Running biosaxs editor with staff authorisation");
		}
		final Display display = Display.getCurrent();
		okay = null;
		warning = new Color(display, 255, 160, 30);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		final Composite comp = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(layoutData);
		TableColumnLayout layout = new TableColumnLayout();
		comp.setLayout(layout);

		tableViewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		table = tableViewer.getTable();
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		new HatsaxsMenu<ManualBean>(tableViewer);

		// This listener lets the background colour of cells be kept even when the row
		// is selected
		// (instead of being replaced by the selected foreground)
		table.addListener(SWT.EraseItem, (event) -> {
			event.detail &= ~SWT.HOT;
			if ((event.detail & SWT.SELECTED) == 0)
				return;
			GC gc = event.gc;
			Rectangle rect = event.getBounds();
			gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
			gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
			gc.fillRectangle(rect);
			event.detail &= ~SWT.SELECTED;
		});

		columns = new LinkedHashMap<>();
		columns.put("Sample Name", new Column<ManualBean, String>(100, tableViewer, rbeditor, TEXT) {
			@Override
			public String getRealValue(ManualBean element) {
				return element.getSampleName();
			}

			@Override
			public void setNewValue(ManualBean element, String value) {
				element.setSampleName(value);
			}
		});
		columns.put("Time per\n  Frame", new Column<ManualBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(ManualBean element) {
				return element.getTimePerFrame();
			}

			@Override
			public void setNewValue(ManualBean element, Double value) {
				element.setTimePerFrame(value);
				updateTotals();
			}
		});
		columns.get("Time per\n  Frame").setOutputFormat("%5.3f s");
		columns.put("Frames", new Column<ManualBean, Integer>(40, tableViewer, rbeditor, INTEGER) {
			@Override
			public Integer getRealValue(ManualBean element) {
				return element.getFrames();
			}

			@Override
			public void setNewValue(ManualBean element, Integer value) {
				element.setFrames(value);
				updateTotals();
			}
		});
		columns.put("Temperature", new Column<ManualBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(ManualBean element) {
				return element.getTemperature();
			}

			@Override
			public void setNewValue(ManualBean element, Double value) {
				element.setTemperature(value);
			}
		});
		columns.get("Temperature").setOutputFormat("%4.1f \u00B0C");
		columns.put("Delay", new Column<ManualBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(ManualBean element) {
				return element.getDelay();
			}

			@Override
			public void setNewValue(ManualBean element, Double value) {
				element.setDelay(value);
			}
		});
		// Using Object here is a bit awkward but it allows the tool tip text to
		// be passed in as strings. Bitfield cell editors always return integers
		columns.put("Illumination\n  Channels", new Column<ManualBean, Object>(40, tableViewer, rbeditor,
				ColumnType.BITFIELD, "Channel 1", "Channel 2", "Channel 3", "Channel 4") {
			@Override
			public Integer getRealValue(ManualBean element) {
				return element.getChannels();
			}

			@Override
			public void setNewValue(ManualBean element, Object value) {
				element.setChannels((int) value);
			}

			@Override
			protected String getStringValue(ManualBean element) {
				var v = element.getChannels();
				var sb = new StringBuilder(4);
				for (int i = 0; i < 4; i++) {
					sb.append((v & 1 << i) > 0 ? "✓" : "✗");
				}
				return sb.toString();
			}
		});
		columns.put("Illumination\n    Time", new Column<ManualBean, Double>(40, tableViewer, rbeditor, DOUBLE) {
			@Override
			public Double getRealValue(ManualBean element) {
				return element.getIllumination();
			}

			@Override
			public void setNewValue(ManualBean element, Double value) {
				element.setIllumination(value);
			}
		});
		columns.put("  Light\nExpose", new Column<ManualBean, Boolean>(40, tableViewer, rbeditor, BOOL) {
			@Override
			public Boolean getRealValue(ManualBean element) {
				return element.isLightExpose();
			}

			@Override
			public void setNewValue(ManualBean element, Boolean value) {
				element.setLightExpose(value);
			}
		});

		if (isStaff) {
			columns.put("Visit", new Column<ManualBean, String>(70, tableViewer, rbeditor, TEXT) {
				private boolean validVisit(ManualBean element) {
					String visit = element.getVisit();
					HashMap<String, String> overrides = new HashMap<>();
					overrides.put("visit", visit);
					String visitPath = InterfaceProvider.getPathConstructor()
							.createFromProperty(LocalProperties.GDA_VISIT_DIR, overrides);
					File visitDir = new File(visitPath);
					return visitDir.exists() && visitDir.isDirectory();
				}

				@Override
				public String getRealValue(ManualBean element) {
					return element.getVisit();
				}

				@Override
				public void setNewValue(ManualBean element, String value) {
					element.setVisit(value);
				}

				@Override
				protected Color getColour(ManualBean element) {
					if (!validVisit(element)) {
						logger.error("visit doesn't exist");
						return warning;
					} else {
						return super.getColour(element);
					}
				}

				@Override
				protected String getToolTip(ManualBean element) {
					if (!validVisit(element)) {
						return "Visit directory does not exist\n     (or can't be written to)";
					}
					return super.getToolTip(element);
				}
			});
		}

		columns.forEach((name, column) -> column.addToTable(name, tableViewer));
		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(TRANSFER, TextTransfer.getInstance());

		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem[] selection = table.getSelection();

				if (TRANSFER.isSupportedType(event.dataType)) {
					List<ManualBean> data = new ArrayList<>();
					try {
						for (TableItem element : selection) {
							ManualBean oldBean = (ManualBean) element.getData();
							ManualBean copiedBean = (ManualBean) BeanUtils.cloneBean(oldBean);
							data.add(copiedBean);
						}
					} catch (Exception e) {
						logger.error("error cloning manual bean for outgoing drag and drop", e);
					}
					event.data = data;
				} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					var buff = new StringBuilder();
					for (TableItem element : selection) {
						buff.append(((ManualBean) element.getData()).getSampleName());
					}
					event.data = buff.toString();
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				super.dragFinished(event);
				if ((event.detail & DND.DROP_MOVE) != 0) {
					logger.debug(event.toString());
					DragSource ds = (DragSource) event.widget;
					Table table = (Table) ds.getControl();
					TableItem[] selection = table.getSelection();
					if (selection.length == 0)
						logger.debug("selection empty");
					for (TableItem element : selection) {
						if (!getList().remove(element.getData()))
							logger.debug("data not there or not removed");
					}
					updateTotals();
					tableViewer.refresh();
				} else {
					logger.debug("not move");
				}
			}
		});

		// Create the drop target
		var target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(TRANSFER);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE : DND.DROP_NONE;
				}

				for (TransferData dataType : event.dataTypes) {
					if (TRANSFER.isSupportedType(dataType)) {
						event.currentDataType = dataType;
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (TRANSFER.isSupportedType(event.currentDataType)) {
					@SuppressWarnings("unchecked")
					List<ManualBean> data = (List<ManualBean>) event.data;
					List<ManualBean> list = getList();

					int before = 0;

					Widget intoitem = event.item;
					if (intoitem == null) {
						before = list.size();
					} else {
						before = list.indexOf(intoitem.getData());
					}

					list.addAll(before, data);

					updateTotals();
					tableViewer.refresh();
				}
			}
		});
//
		tableViewer.setContentProvider(new ArrayContentProvider());

		composite_1 = new Composite(this, SWT.FILL);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		rowLayout.spacing = 5;
		composite_1.setLayout(rowLayout);

		Label label = new Label(composite_1, SWT.NONE);
		label.setText("Number of Samples:");
		sampleCount = new Label(composite_1, SWT.NONE);
		sampleCount.setText("000000000000");// ensures label is long enough
		sampleCount.setText("0");

		Label runtimeLabel = new Label(composite_1, SWT.NONE);
		runtimeLabel.setText("Total Runtime");

		totalRuntime = new Label(composite_1, SWT.NONE);
		totalRuntime.setText("00:00:00");
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		updateTotals();
		tableViewer.setInput(value);
	}

	@SuppressWarnings("unchecked")
	private List<ManualBean> getList() {
		if (value == null)
			setValue(new ArrayList<>());
		return (List<ManualBean>) value;
	}

	public void deleteSelection() {
		if (table.getSelectionIndices().length == 0) {
			return;
		}
		int[] selectionIndices = table.getSelectionIndices();
		Arrays.sort(selectionIndices);
		for (int i = selectionIndices.length - 1; i >= 0; i--) {
			getList().remove(selectionIndices[i]);
		}
		rbeditor.valueChangePerformed(new ValueEvent("", ""));
		updateTotals();
		tableViewer.refresh();
	}

	public void addSample() {
		if (table.getSelectionIndices().length == 0) {
			var list = getList();
			ManualBean e = new ManualBean();
			e.setSampleName("Sample " + (list.size() + 1));
			list.add(e);
		} else {
			int[] selectionIndices = table.getSelectionIndices();
			List<ManualBean> toadd = new ArrayList<>(table.getSelectionIndices().length);
			for (int i : selectionIndices) {
				try {
					ManualBean oldBean = getList().get(i);
					ManualBean copiedBean = (ManualBean) BeanUtils.cloneBean(oldBean);
					toadd.add(copiedBean);
				} catch (Exception e) {
				}
			}
			getList().addAll(selectionIndices[selectionIndices.length - 1] + 1, toadd);
		}
		updateTotals();
		tableViewer.refresh();
		rbeditor.valueChangePerformed(new ValueEvent("", ""));
	}

	private void updateTotals() {
		List<ManualBean> samples = getList();
		sampleCount.setText(String.valueOf(samples.size()));
		int overheadPerSample = LocalProperties.getAsInt(SAMPLE_TIME_OVERHEAD, 90);
		totalRuntime.setText(getRuntimeString(samples, tb -> tb.getTimePerFrame() * tb.getFrames(), overheadPerSample));
	}

	@Override
	public void dispose() {
		super.dispose();
		colourClearing.forEach(Runnable::run);
	}
}
