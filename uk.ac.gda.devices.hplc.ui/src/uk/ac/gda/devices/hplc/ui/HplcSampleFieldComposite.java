/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hplc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.jython.InterfaceProvider;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hatsaxs.beans.Plate;
import uk.ac.gda.devices.hatsaxs.ui.Column;
import uk.ac.gda.devices.hatsaxs.ui.Column.ColumnHelper;
import uk.ac.gda.devices.hatsaxs.ui.Column.ColumnType;
import uk.ac.gda.devices.hplc.beans.HplcBean;
import uk.ac.gda.devices.hplc.beans.HplcSessionBean;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class HplcSampleFieldComposite extends FieldComposite {

	private static final Logger logger = LoggerFactory.getLogger(HplcSampleFieldComposite.class);

	Object value = null;
	private Table table;
	private Label sampleCount;
	private final TableViewer tableViewer;
	private Composite composite_1;
	private final RichBeanEditorPart rbeditor;

	private Map<String, Column<HplcBean,?>> columns;

	Color okay, warning;

	private boolean isStaff;

	private static final SimpleObjectTransfer TRANSFER = new SimpleObjectTransfer() {
		private final String TYPE_NAME = "uk.ac.gda.devices.hplc.ui.HplcBeanTransfer" + System.currentTimeMillis(); //$NON-NLS-1$
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

	public HplcSampleFieldComposite(Composite parent, int style, RichBeanEditorPart editor) {
		super(parent, style);
		this.rbeditor = editor;

		final Display display = Display.getCurrent();
		okay = null;
		warning = new Color(display, 255, 160, 30);
		this.isStaff = InterfaceProvider.getBatonStateProvider().getMyDetails().getAuthorisationLevel() >= 3;

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
		table.addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.detail &= ~SWT.HOT;
				if ((event.detail & SWT.SELECTED) == 0) {
					return;
				}
				GC gc = event.gc;
				Rectangle rect = event.getBounds();
				gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
				gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
				gc.fillRectangle(rect);
				event.detail &= ~SWT.SELECTED;
			}
		});


		columns = new LinkedHashMap<>();
		columns.put("Location", new Column<HplcBean, String>(70, tableViewer, rbeditor, ColumnType.TEXT) {
			@Override
			public String getRealValue(HplcBean element) {
				return element.getLocation();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				element.setLocation(value);
			}
		});
		columns.put("Sample Name",new Column<HplcBean, String>(100, tableViewer, rbeditor, ColumnType.TEXT) {
			@Override
			public String getRealValue(HplcBean element) {
				return element.getSampleName();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				element.setSampleName(value);
			}
		});
		columns.put("Concentration", new Column<HplcBean, Double>(70, tableViewer, rbeditor,ColumnType.DOUBLE) {
			@Override
			public Double getRealValue(HplcBean element) {
				return element.getConcentration();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				double conc = Double.valueOf(value);
				element.setConcentration(conc);
			}
		});
		columns.get("Concentration").setOutputFormat("%5.5f mg/ml");
		columns.put("Molecular\n  Weight", new Column<HplcBean, Double>(50, tableViewer, rbeditor, ColumnType.DOUBLE) {
			@Override
			public Double getRealValue(HplcBean element) {
				return element.getMolecularWeight();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				double weight = Double.valueOf(value);
				element.setMolecularWeight(weight);
			}
		});
		columns.get("Molecular\n  Weight").setOutputFormat("%s kDa");

		columns.put("Time per\n  Frame", new Column<HplcBean, Double>(50, tableViewer, rbeditor, ColumnType.DOUBLE) {
			@Override
			public Double getRealValue(HplcBean element) {
				return element.getTimePerFrame();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				try {
					double time = Double.valueOf(value);
					element.setTimePerFrame(time);
				} catch (NumberFormatException nfe) {
				}
			}
		});
		columns.get("Time per\n  Frame").setOutputFormat("%5.3f s");
		columns.put("Total\nDuration", new Column<HplcBean, Double>(50, tableViewer, rbeditor, ColumnType.DOUBLE) {
			@Override
			public Double getRealValue(HplcBean element) {
				return element.getTotalDuration();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				try {
					double time = Double.valueOf(value);
					element.setTotalDuration(time);
				} catch (NumberFormatException nfe) {
				}
			}
		});
		columns.get("Total\nDuration").setOutputFormat("%.1f min");
		columns.put("Buffers", new Column<HplcBean, String>(100, tableViewer, rbeditor, ColumnType.TEXT) {
			@Override
			public String getRealValue(HplcBean element) {
				return element.getBuffers();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				element.setBuffers(value);
			}
		});
		columns.put("Column\nType", new Column<HplcBean, String>(100, tableViewer, rbeditor, ColumnType.CHOICE) {

			@Override
			public String getRealValue(HplcBean element) {
				return element.getColumnType();
			}

			@Override
			public void setNewValue(HplcBean element, String value) {
				element.setColumnType(value);
			}

		});
		columns.get("Column\nType").setInput(HplcSessionBean.HPLC_PLATES.getAvailableCapillaries());
		columns.put("Comment", new Column<HplcBean, String>(100, tableViewer, rbeditor, ColumnType.TEXT) {
			@Override
			public String getRealValue(HplcBean element) {
				return element.getComment();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				element.setComment(value);
			}
		});
		if (isStaff) {
			columns.put("Visit", new Column<HplcBean, String>(70, tableViewer, rbeditor, ColumnType.TEXT) {
				private boolean validVisit(HplcBean element) {
					String visit = element.getVisit();
					HashMap<String, String> overrides = new HashMap<>();
					overrides.put("visit", visit);
					String visitPath = PathConstructor.createFromProperty(LocalProperties.GDA_VISIT_DIR, overrides);
					File visitDir = new File(visitPath);
					return visitDir.exists() && visitDir.isDirectory() && visitDir.canWrite();
				}
				@Override
				public String getRealValue(HplcBean element) {
					return element.getVisit();
				}
				@Override
				public void setNewValue(HplcBean element, String value) {
					element.setVisit(value);
				}
				@Override
				protected Color getColour(HplcBean element) {
					if (!validVisit(element)) {
						logger.error("visit doesn't exist");
						return warning;
					} else {
						return super.getColour(element);
					}
				}
				@Override
				protected String getToolTip(HplcBean element) {
					if (!validVisit(element)) {
						return "Visit directory does not exist\n     (or can't be written to)";
					}
					return super.getToolTip(element);
				}
			});
			columns.put("Username", new Column<HplcBean, String>(70, tableViewer, rbeditor, ColumnType.TEXT) {
				@Override
				public String getRealValue(HplcBean element) {
					return element.getUsername();
				}
				@Override
				public void setNewValue(HplcBean element, String value) {
					element.setUsername(value);
				}
			});
		}

		for (Entry<String, Column<HplcBean,?>> column : columns.entrySet()) {
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.CENTER);
			int width = column.getValue().getWidth();
			col.getColumn().setWidth(width);
			col.getColumn().setText(column.getKey());
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
			layout.setColumnData(col.getColumn(), new ColumnWeightData(width, width));
			col.setLabelProvider(column.getValue().getLabelProvider());
			col.setEditingSupport(column.getValue().getEditor());
		}

		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(new Transfer[] { TRANSFER, TextTransfer.getInstance() });

		source.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem[] selection = table.getSelection();

				if (TRANSFER.isSupportedType(event.dataType)) {
					List<HplcBean> data = new ArrayList<HplcBean>();
					try {
						for (TableItem element : selection) {
							HplcBean oldBean = (HplcBean) element.getData();
							HplcBean copiedBean = (HplcBean) BeanUtils.cloneBean(oldBean);
							data.add(copiedBean);
						}
					} catch (Exception e) {
						logger.error("error cloning titrationbean for outgoing drag and drop", e);
					}
					event.data = data;
				} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					StringBuffer buff = new StringBuffer();
					for (TableItem element : selection) {
						buff.append(((HplcBean) element.getData()).getSampleName());
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
					if (selection.length == 0) {
						logger.debug("selection empty");
					}
					for (TableItem element : selection) {
						if (!getList().remove(element.getData())) {
							logger.debug("data not there or not removed");
						}
					}

					sampleCount.setText(String.valueOf(getList().size()));
					tableViewer.refresh();
				} else {
					logger.debug("not move");
				}
			}
		});

		// Create the drop target
		DropTarget target = new DropTarget(table, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { TRANSFER });
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
					List<HplcBean> data = (List<HplcBean>) event.data;
					List<HplcBean> list = getList();

					int before = 0;

					Widget intoitem = event.item;
					if (intoitem == null) {
						before = list.size();
					} else {
						before = list.indexOf(intoitem.getData());
					}

					list.addAll(before, data);

					sampleCount.setText(String.valueOf(getList().size()));
					tableViewer.refresh();
				}
			}
		});
//
		tableViewer.setContentProvider(new ArrayContentProvider());

		composite_1 = new Composite(this, SWT.NONE);
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
		sampleCount.setText("00000000");//ensure label is long enough
		sampleCount.setText("0");
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.setInput(value);
	}

	@SuppressWarnings("unchecked")
	private List<HplcBean> getList() {
		if (value == null) {
			setValue(new ArrayList<HplcBean>());
		}
		return (List<HplcBean>) value;
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
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.refresh();
	}

	public void addSample() {
		if (table.getSelectionIndices().length == 0) {
			getList().add(new HplcBean());
		} else {
			int[] selectionIndices = table.getSelectionIndices();
			List<HplcBean> toadd = new ArrayList<HplcBean>(table.getSelectionIndices().length);
			for (int i : selectionIndices) {
				try {
					HplcBean oldBean = getList().get(i);
					HplcBean copiedBean = (HplcBean) BeanUtils.cloneBean(oldBean);
					toadd.add(copiedBean);
				} catch (Exception e) {
				}
			}
			getList().addAll(selectionIndices[selectionIndices.length - 1] + 1, toadd);
		}
		sampleCount.setText(String.valueOf(getList().size()));
		tableViewer.refresh();
		rbeditor.valueChangePerformed(new ValueEvent("", ""));
	}

	private Map<String,Column<HplcBean,?>> getLocationColumns(final String prefix, final ColumnHelper<HplcBean, LocationBean> helper) {
		Map<String,Column<HplcBean,?>> columns = new LinkedHashMap<>();
		if (HplcSessionBean.HPLC_PLATES.getPlates().size() != 1) {
			Column<HplcBean, String> plateColumn = new Column<HplcBean, String>(40, tableViewer, rbeditor, Column.ColumnType.CHOICE) {
				private ColumnHelper<HplcBean, LocationBean> help = helper;
				@Override
				public String getRealValue(HplcBean element) {
					LocationBean loc = help.getValue(element);
					return loc == null ? "--" : LocationBean.getPlateText(loc.getPlate());
				}
				@Override
				public void setNewValue(HplcBean element, String value) {
					LocationBean loc = helper.getValue(element);
					if (loc == null) {
						loc = new LocationBean(HplcSessionBean.HPLC_PLATES);
						helper.setValue(element,loc);
					}
					loc.setPlate(value);
				}
				@Override
				protected String getStringValue(Object element) {
					String value = getRealValue((HplcBean)element);
					return value;
				}
				@Override
				protected Color getColour(HplcBean tb) {
					return helper.bGColor(tb);
				}
				@Override
				protected String getToolTip(HplcBean tb) {
					return helper.toolTip(tb);
				}
			};
			String[] plateArray = HplcSessionBean.HPLC_PLATES.getAvailablePlates();
			plateColumn.setInput(plateArray);
			columns.put(prefix + "Plate", plateColumn);
		}
		columns.put(prefix + "Row", new Column<HplcBean,Character>(30, tableViewer, rbeditor, Column.ColumnType.CHOICE ) {
			@Override
			public Character getRealValue(HplcBean element) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					return null;
				}
				short currentPlate = loc.getPlate();
				Plate plate = HplcSessionBean.HPLC_PLATES.getPlate(currentPlate);
				setInput(plate.getRows());
				return loc.getRow();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					loc = new LocationBean(HplcSessionBean.HPLC_PLATES);
					helper.setValue(element, loc);
				}
				if (value.length() == 1) {
					loc.setRow(value.charAt(0));
				}
			}
			@Override
			protected String getStringValue(Object element) {
				Character row = getRealValue((HplcBean)element);
				return row == null ? "--" : String.valueOf(row);
			}
			@Override
			protected Color getColour(HplcBean tb) {
				return helper.bGColor(tb);
			}
			@Override
			protected String getToolTip(HplcBean tb) {
				return helper.toolTip(tb);
			}
		});
		columns.put(prefix + "Column", new Column<HplcBean,Integer>(50,tableViewer, rbeditor, Column.ColumnType.CHOICE) {
			@Override
			public Integer getRealValue(HplcBean element) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					return null;
				}
				short currentPlate = loc.getPlate();
				Plate plate = HplcSessionBean.HPLC_PLATES.getPlate(currentPlate);
				setInput(plate.getColumns());
				return (int) loc.getColumn();
			}
			@Override
			public void setNewValue(HplcBean element, String value) {
				LocationBean loc = helper.getValue(element);
				if (loc == null) {
					loc = new LocationBean(HplcSessionBean.HPLC_PLATES);
					helper.setValue(element,loc);
				}
				short col = Short.valueOf(value);
				loc.setColumn(col);
			}
			@Override
			protected String getStringValue(Object element) {
				Integer plate = getRealValue((HplcBean)element);
				return plate == null ? "--" : String.valueOf(plate);
			}
			@Override
			protected Color getColour(HplcBean tb) {
				return helper.bGColor(tb);
			}
			@Override
			protected String getToolTip(HplcBean tb) {
				return helper.toolTip(tb);
			}
		});
		return columns;
	}
}
