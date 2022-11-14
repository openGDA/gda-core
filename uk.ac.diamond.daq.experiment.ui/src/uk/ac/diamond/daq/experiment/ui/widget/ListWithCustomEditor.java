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

package uk.ac.diamond.daq.experiment.ui.widget;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.DOWN_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.MINUS_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.PLUS_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.UP_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.getImage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

/**
 * This widget will show a list of {@link EditableWithListWidget} items, each represented by a single String.
 * Elements can be added, removed, and reordered by interacting with the buttons under the table.
 *
 * A custom {@link ElementEditor} can be provided, which will be given the selected element from the list to edit further.
 *
 */
public class ListWithCustomEditor {

	private TableViewer viewer;
	private int minimumElements;
	private int listHeight = 300;

	private List<EditableWithListWidget> list;
	private EditableWithListWidget template;
	private ElementEditor elementEditor;

	private Button add;
	private Button delete;
	private Button moveUp;
	private Button moveDown;

	private Set<Consumer<EditableWithListWidget>> addHooks = new CopyOnWriteArraySet<>();
	private Set<Consumer<EditableWithListWidget>> deleteHooks = new CopyOnWriteArraySet<>();

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private PropertyChangeListener elementChangeListener = e -> {
		if (e.getPropertyName().equals(EditableWithListWidget.REFRESH_PROPERTY)) {
			viewer.refresh();
		}
	};

	private ISelectionChangedListener selectionChangedListener;

	public void setList(List<EditableWithListWidget> list) {
		Objects.requireNonNull(list, "I don't want a null list thank you");
		getList().forEach(thing -> thing.removePropertyChangeListener(elementChangeListener));
		list.forEach(thing -> thing.addPropertyChangeListener(elementChangeListener));
		this.list = list;
		if (viewer != null) {
			viewer.setInput(this.list);
		}
	}

	public void setElementEditor(ElementEditor editor) {
		elementEditor = editor;
	}

	/**
	 * Set an instance of the {@link EditableWithListWidget} so we can instantiate
	 * further when the 'add' button is pressed
	 */
	public void setTemplate(EditableWithListWidget template) {
		this.template = template;
	}

	public void setMinimumElements(int minimumElements) {
		this.minimumElements = minimumElements;
	}

	public void create(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().applyTo(composite);

		// Viewer

		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).
			hint(SWT.DEFAULT, listHeight).applyTo(viewer.getControl());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(getList());

		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EditableWithListWidget) element).getLabel();
			}
		});

		selectionChangedListener = event -> selectionChanged((IStructuredSelection) event.getSelection());
		viewer.addSelectionChangedListener(selectionChangedListener);

		// Action buttons

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(buttonsComposite);
		GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).applyTo(buttonsComposite);

		add = new Button(buttonsComposite, SWT.PUSH);
		add.setText("Add");
		setImage(add, PLUS_ICON);
		GridDataFactory.fillDefaults().applyTo(add);
		add.addListener(SWT.Selection, e -> add());

		delete = new Button(buttonsComposite, SWT.PUSH);
		delete.setText("Delete");
		setImage(delete, MINUS_ICON);
		GridDataFactory.fillDefaults().applyTo(delete);
		delete.addListener(SWT.Selection, e -> delete());

		moveUp = new Button(buttonsComposite, SWT.PUSH);
		moveUp.setText("Move up");
		setImage(moveUp, UP_ICON);
		GridDataFactory.fillDefaults().applyTo(moveUp);
		moveUp.addListener(SWT.Selection, e -> moveUp());

		moveDown = new Button(buttonsComposite, SWT.PUSH);
		moveDown.setText("Move down");
		setImage(moveDown, DOWN_ICON);
		GridDataFactory.fillDefaults().applyTo(moveDown);
		moveDown.addListener(SWT.Selection, e -> moveDown());

		// Custom GUI

		if (elementEditor != null) {
			elementEditor.createControl(parent);
		}

		if (!list.isEmpty()) {
			viewer.getTable().setSelection(0);
			listChanged();
		}

		// If list contains fewer elements than the allowed minimum
		// we add() until this is remedied

		while (list.size() < minimumElements) add();

		updateButtons();
	}

	private void setImage(Button button, String icon) {
		var image = getImage(icon);
		button.setImage(image);
		button.addDisposeListener(dispose -> image.dispose());
	}

	public List<EditableWithListWidget> getList() {
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}

	private void selectionChanged(IStructuredSelection selection) {
		EditableWithListWidget selected = (EditableWithListWidget) selection.getFirstElement();
		if (elementEditor != null) {
			if (selected != null) {
				elementEditor.load(selected);
			} else {
				elementEditor.clear();
			}
		}
		updateButtons();
	}

	private void listChanged() {
		pcs.firePropertyChange("list", null, getList());
		selectionChangedListener.selectionChanged(new SelectionChangedEvent(viewer, viewer.getSelection()));
		updateButtons();
	}

	private void add() {
		final EditableWithListWidget bean;

		if (template == null) {
			if (list.isEmpty()) throw new IllegalStateException("No template model provided");
			bean = list.get(0).createDefault();
		} else {
			bean = template.createDefault();
		}

		int selectionIndex = viewer.getTable().getSelectionIndex();

		bean.addPropertyChangeListener(elementChangeListener);

		list.add(selectionIndex+1, bean);
		viewer.refresh();
		viewer.getTable().setSelection(selectionIndex+1);
		listChanged();

		addHooks.forEach(hook -> hook.accept(bean));
	}

	private void delete() {
		int selectionIndex = viewer.getTable().getSelectionIndex();
		final EditableWithListWidget bean = list.remove(selectionIndex);
		bean.removePropertyChangeListener(elementChangeListener);
		viewer.refresh();
		if (selectionIndex < list.size()) {
			viewer.getTable().setSelection(selectionIndex);
		} else {
			viewer.getTable().setSelection(list.size()-1);
		}
		listChanged();

		deleteHooks.forEach(hook -> hook.accept(bean));
	}

	private void moveUp() {
		int currentIndex = viewer.getTable().getSelectionIndex();
		EditableWithListWidget bean = list.remove(currentIndex);
		list.add(currentIndex - 1, bean);
		viewer.refresh();
		listChanged();
	}

	private void moveDown() {
		int currentIndex = viewer.getTable().getSelectionIndex();
		EditableWithListWidget bean = list.remove(currentIndex);
		list.add(currentIndex+1, bean);
		viewer.refresh();
		listChanged();
	}

	private void updateButtons() {
		boolean anythingSelected = viewer.getTable().getSelectionCount() == 1;

		// adding can be done if we have either a template of a non-empty list
		add.setEnabled(template != null || !list.isEmpty());

		// delete should only be active when there are more items than the minimum allowed
		delete.setEnabled(anythingSelected && viewer.getTable().getItemCount() > minimumElements);

		// up button enabled when there is an item selected and it is not the topmost
		moveUp.setEnabled(anythingSelected && viewer.getTable().getSelectionIndex() > 0);

		// down button enabled when there is an item selected and it is not the bottom-most
		moveDown.setEnabled(anythingSelected && viewer.getTable().getSelectionIndex() < list.size() - 1);
	}

	public void setListHeight(int listHeight) {
		this.listHeight = listHeight;
	}

	public void addListListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addAddHook(Consumer<EditableWithListWidget> hook) {
		addHooks.add(hook);
	}

	public void addDeleteHook(Consumer<EditableWithListWidget> hook) {
		deleteHooks.add(hook);
	}

	public void refresh() {
		viewer.refresh();
		updateButtons();
	}
}
