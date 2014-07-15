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

package uk.ac.gda.client.plotting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.ResourceComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.plotting.model.DataNode;
import uk.ac.gda.client.plotting.model.LineTraceProvider;
import uk.ac.gda.client.plotting.model.ScanDataItemNode;
import uk.ac.gda.client.plotting.model.ScanDataNode;
import uk.ac.gda.client.plotting.model.LineTraceProvider.TraceStyleDetails;

public class ScanDataPlotterComposite extends ResourceComposite {
	private static final int HIGHLIGHTED_LINE_WIDTH = 2;

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPlotterComposite.class);

	private IPlottingSystem plottingSystem;

	private DataPlotterCheckedTreeViewer dataTreeViewer;
	private final DataNode rootDataNode;

	private final DataBindingContext dataBindingCtx = new DataBindingContext();
	private final IObservableList selectedList = new WritableList(new ArrayList<DataNode>(), DataNode.class);

	private Binding selectionBinding;

	private boolean clearPlotOnStartOfScan = true;


	public ScanDataPlotterComposite(Composite parent, int style, ViewPart parentView, DataNode rootDataNode) {
		super(parent, style);
		this.rootDataNode = rootDataNode;
		this.setLayout(UIHelper.createGridLayoutWithNoMargin(1, false));
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			UIHelper.showError("Unable to create plotting system", e.getMessage());
			logger.error("Unable to create plotting system", e);
			return;
		}
		final SashForm composite = new SashForm(this, SWT.HORIZONTAL);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite plot = new Composite(composite, SWT.None);
		plot.setLayout(new FillLayout());
		plottingSystem.createPlotPart(plot, parentView.getTitle(),
				parentView.getViewSite().getActionBars(), PlotType.XY, parentView);
		plottingSystem.getSelectedXAxis().setAxisAutoscaleTight(true);

		createDataTree(composite);
		composite.setWeights(new int[] {3, 1});
		setupDataSelection();
	}

	private void setupDataSelection() {
		rootDataNode.getChildren().addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						//
					}
					@Override
					public void handleAdd(int index, Object element) {
						if (clearPlotOnStartOfScan) {
							for (Object obj : dataTreeViewer.getCheckedElements()) {
								dataTreeViewer.updateCheckSelection(obj, false);
							}
						}
					}
				});
			}
		});
		rootDataNode.addPropertyChangeListener(DataNode.DATA_ADDED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				DataNode node = (DataNode) evt.getNewValue();
				dataTreeViewer.expandToLevel(node.getParent(), AbstractTreeViewer.ALL_LEVELS);
				if (node instanceof LineTraceProvider && dataTreeViewer.getChecked(node)) {
					addTrace((LineTraceProvider) node);
				}
			}
		});

		rootDataNode.addPropertyChangeListener(DataNode.DATA_CHANGED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				DataNode node = (DataNode) evt.getNewValue();
				if (node instanceof LineTraceProvider && dataTreeViewer.getChecked(node)) {
					updateTrace((LineTraceProvider) node);
				}
			}
		});
	}

	private void updateTrace(LineTraceProvider lineTraceProvider) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(((DataNode) lineTraceProvider).getIdentifier());
		if (trace != null) {
			trace.setData(lineTraceProvider.getXAxisDataset(), lineTraceProvider.getYAxisDataset());
			plottingSystem.repaint();
		}
	}

	private void addTrace(LineTraceProvider lineTraceProvider) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(((DataNode) lineTraceProvider).getIdentifier());
		if (trace == null) {
			trace = plottingSystem.createLineTrace(((DataNode) lineTraceProvider).getIdentifier());
			TraceStyleDetails traceDetails = lineTraceProvider.getTraceStyle();
			if (traceDetails.getColorHexValue() != null) {
				trace.setTraceColor(getTraceColor(traceDetails.getColorHexValue()));
			}
			int lineWidth = traceDetails.getLineWidth();
			if (lineTraceProvider.isHighlighted()) {
				lineWidth += HIGHLIGHTED_LINE_WIDTH;
			}
			trace.setLineWidth(lineWidth);
			trace.setTraceType(traceDetails.getTraceType());
			trace.setPointSize(traceDetails.getPointSize());
			trace.setPointStyle(traceDetails.getPointStyle());
			plottingSystem.addTrace(trace);
		}
		trace.setData(lineTraceProvider.getXAxisDataset(), lineTraceProvider.getYAxisDataset());
		plottingSystem.getSelectedXAxis().setTitle(lineTraceProvider.getXAxisDataset().getName());
		plottingSystem.repaint();
	}

	private final Map<String, Color> nodeColors = new HashMap<String, Color>();

	private Color getTraceColor(String colorValue) {
		Color color = null;
		if (!nodeColors.containsKey(colorValue)) {
			color = UIHelper.convertHexadecimalToColor(colorValue, Display.getDefault());
			nodeColors.put(colorValue, color);
		} else {
			color = nodeColors.get(colorValue);
		}
		return color;
	}

	IObservableFactory dataObservableFactory = new IObservableFactory() {
		@Override
		public IObservable createObservable(Object target) {
			if (target instanceof LineTraceProvider) {
				if (((LineTraceProvider) target).isPlotByDefault()) {
					dataTreeViewer.updateCheckSelection(target, true);
				}
			}
			if (target instanceof DataNode) {
				return (((DataNode) target).getChildren());
			}
			return null;
		}
	};

	private void createDataTree(final SashForm parent) {
		Composite dataTreeParent = new Composite(parent, SWT.None);
		dataTreeParent.setLayout(UIHelper.createGridLayoutWithNoMargin(1, false));
		CoolBar composite = new CoolBar(dataTreeParent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		CoolItem toolbarCoolItem = new CoolItem(composite, SWT.NONE);
		ToolBar tb = new ToolBar(composite, SWT.FLAT);

		ToolItem backToolItem = new ToolItem(tb, SWT.NONE);
		backToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		backToolItem.setToolTipText("Collapse all");

		ToolItem forwardToolItem = new ToolItem(tb, SWT.NONE);
		forwardToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		forwardToolItem.setToolTipText("Expand all");

		ToolItem highlightOnSelectionToolItem = new ToolItem(tb, SWT.CHECK);
		highlightOnSelectionToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED));
		highlightOnSelectionToolItem.setToolTipText("Highlight selected line trace");

		ToolItem clearPlotOnStartOfScanToolItem = new ToolItem(tb, SWT.CHECK);
		clearPlotOnStartOfScanToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_CLEAR));
		clearPlotOnStartOfScanToolItem.setSelection(clearPlotOnStartOfScan);
		clearPlotOnStartOfScanToolItem.setToolTipText("Clear plot on start of scan");

		Point p = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		tb.setSize(p);
		Point p2 = toolbarCoolItem.computeSize(p.x, p.y);
		toolbarCoolItem.setControl(tb);
		toolbarCoolItem.setSize(p2);

		backToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dataTreeViewer.collapseAll();
			}
		});

		forwardToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dataTreeViewer.expandAll();
			}
		});

		highlightOnSelectionToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!((ToolItem) event.widget).getSelection()) {
					selectedList.clear();
					if (selectionBinding != null) {
						dataBindingCtx.removeBinding(selectionBinding);
						selectionBinding.dispose();
						selectionBinding = null;
					}
				} else {
					selectionBinding = dataBindingCtx.bindList(
							ViewersObservables.observeMultiSelection(dataTreeViewer), selectedList);
				}
			}
		});

		clearPlotOnStartOfScanToolItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				clearPlotOnStartOfScan = ((ToolItem) event.widget).getSelection();
			}
		});


		dataTreeViewer = new DataPlotterCheckedTreeViewer(dataTreeParent, SWT.MULTI);
		dataTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dataTreeViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				cell.setText(element.toString());
				if (element instanceof LineTraceProvider) {
					LineTraceProvider item = ((LineTraceProvider) element);
					String color = item.getTraceStyle().getColorHexValue();
					if (color != null) {
						cell.setForeground(getTraceColor(color));
					}
					return;
				}
			}
		});
		dataTreeViewer.setContentProvider(new ObservableListTreeContentProvider(dataObservableFactory, null));
		dataTreeViewer.setInput(rootDataNode);
		dataTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				DataNode dataNode = (DataNode) event.getElement();
				updateSelection(dataNode, event.getChecked());
			}

			private void updateSelection(DataNode dataNode, boolean checked) {
				if (dataNode.getChildren() == null) {
					updateDataItemNode(dataNode, checked);
				} else {
					for (Object childDataNode : dataNode.getChildren()) {
						updateSelection((DataNode) childDataNode, checked);
					}
				}
			}
		});

		selectedList.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				event.diff.accept(new ListDiffVisitor() {
					@Override
					public void handleRemove(int index, Object element) {
						updateSelection(element, false);
					}

					private void updateSelection(Object element, boolean highlighted) {
						if (element instanceof LineTraceProvider && dataTreeViewer.getChecked(element)) {
							LineTraceProvider lineTraceProvider = (LineTraceProvider) element;
							lineTraceProvider.setHighlighted(highlighted);
							removeTrace(lineTraceProvider.getIdentifier());
							addTrace(lineTraceProvider);
						}
					}

					@Override
					public void handleAdd(int index, Object element) {
						updateSelection(element, true);
					}
				});
			}
		});
		registerViewerContextMenu();
	}

	private void registerViewerContextMenu() {
		final MenuManager menuMgr = new MenuManager();

		Menu menu = menuMgr.createContextMenu(dataTreeViewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (!rootDataNode.getChildren().isEmpty()) {
					menuMgr.add(new Action("Remove All") {
						@Override
						public void run() {
							for(Object obj : rootDataNode.getChildren()) {
								DataNode nodeToRemove = (DataNode) obj;
								if (dataTreeViewer.getChecked(nodeToRemove)) {
									dataTreeViewer.updateCheckSelection(nodeToRemove, false);
								}
							}
							rootDataNode.getChildren().clear();
						}
					});
				}
				if (dataTreeViewer.getSelection().isEmpty() || !isSelectedOnSameNodeType(dataTreeViewer.getSelection())) {
					return;
				}
				final IStructuredSelection selection = (IStructuredSelection) dataTreeViewer.getSelection();
				// FIXME shouldn't have ScanDataNode reference here
				if (selection.getFirstElement() instanceof ScanDataNode) {
					menuMgr.add(new Action("Remove") {
						@Override
						public void run() {
							Iterator<?> iterator = selection.iterator();
							while(iterator.hasNext()) {
								DataNode nodeToRemove = (DataNode) iterator.next();
								if (dataTreeViewer.getChecked(nodeToRemove)) {
									dataTreeViewer.updateCheckSelection(nodeToRemove, false);
								}
								rootDataNode.removeChild(nodeToRemove);
							}
						}
					});
				} else if (selection.getFirstElement() instanceof LineTraceProvider) {
					menuMgr.add(new Action("Change appearance") {
						@Override
						public void run() {
							TraceStyleDetails traceStyle = null;
							if (selection.size() == 1) {
								traceStyle = ((LineTraceProvider) selection.getFirstElement()).getTraceStyle();
							}
							TraceStyleDialog dialog = new TraceStyleDialog(ScanDataPlotterComposite.this.getShell(), traceStyle);
							dialog.create();
							if (dialog.open() == Window.OK) {
								Iterator<?> iterator = selection.iterator();
								while(iterator.hasNext()) {
									ScanDataItemNode nodeToChange = (ScanDataItemNode) iterator.next();
									try {
										TraceStyleDetails newTraceStyleDetails = new TraceStyleDetails();
										BeanUtils.copyProperties(newTraceStyleDetails, dialog.getTraceStyle());
										nodeToChange.setTraceStyle(newTraceStyleDetails);
										if (dataTreeViewer.getChecked(nodeToChange)) {
											removeTrace(nodeToChange.getIdentifier());
											addTrace(nodeToChange);
										}
									} catch (IllegalAccessException | InvocationTargetException e) {
										logger.error("Unable to copy properties", e);
									}
								}
							}
						}
					});
				}
			}

			private boolean isSelectedOnSameNodeType(ISelection iSelection) {
				if (!(iSelection instanceof IStructuredSelection)) {
					return false;
				}
				IStructuredSelection selection = (IStructuredSelection) dataTreeViewer.getSelection();
				Object firstSelection = selection.getFirstElement();
				Iterator<?> iterator = selection.iterator();
				while(iterator.hasNext()) {
					if (!(iterator.next().getClass() == firstSelection.getClass())) {
						return false;
					}
				}
				return true;
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		dataTreeViewer.getControl().setMenu(menu);
	}

	private void updateDataItemNode(DataNode dataItemNode, boolean isAdded) {
		if (dataItemNode instanceof LineTraceProvider) {
			if (isAdded) {
				addTrace((LineTraceProvider) dataItemNode);
			} else {
				removeTrace(dataItemNode.getIdentifier());
			}
		}
	}

	private void removeTrace(String identifier) {
		ILineTrace trace = (ILineTrace) plottingSystem.getTrace(identifier);
		if (trace != null) {
			plottingSystem.removeTrace(trace);
		}
	}

	@Override
	protected void disposeResource() {
		plottingSystem.dispose();
		for (Color color : nodeColors.values()) {
			color.dispose();
		}
	}
}
