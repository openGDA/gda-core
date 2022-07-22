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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.dawnsci.common.widgets.periodictable.PeriodicTableComposite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodic table UI to allow a list of Element-Edge strings to be created.
 *
 */
public class XRFLinesCellEditor extends TextCellEditor {

	private static final Logger logger = LoggerFactory.getLogger(XRFLinesCellEditor.class);

	private Set<ElementAndEdge> elements = new HashSet<>();
	private boolean isBrowsing = false;

	public XRFLinesCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected Control createControl(Composite parent) {

		final Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		GridUtils.removeMargins(comp);

		Control c = super.createControl(comp);
		text.setEditable(false);

		c.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());

		// Couple of buttons
		final ToolBarManager toolbar = new ToolBarManager(SWT.FLAT);

		IAction periodicTableButton = new Action("XRF") {

			@Override
			public void run() {
				isBrowsing = true;
				try {
					XRFLinesDialog d = new XRFLinesDialog(XRFLinesCellEditor.this.getControl().getShell());
					int open = d.open();

					if (open == Window.OK) {
						String xrfLinesString = d.getXRFLinesString();

						doSetValue(xrfLinesString);
					}
				} finally {
					isBrowsing = false;
				}

			}
		};
		toolbar.add(periodicTableButton);

		Control tb = toolbar.createControl(comp);
		tb.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).create());

		return comp;

	}

	@Override
	protected void focusLost() {
		if (isActivated() && !isBrowsing) {
			fireApplyEditorValue();
		}
	}

	public class XRFLinesDialog extends Dialog {

		public XRFLinesDialog(Shell parent) {
			super(parent);
		}

		public String getXRFLinesString() {
			if (elements.isEmpty()) {
				return "";
			}

			StringBuilder b = new StringBuilder();
			for (ElementAndEdge e : elements) {
				b.append(e.toString());
				b.append(", ");
			}

			b.setLength(b.length() - 2);

			return b.toString();
		}

		@Override
		public Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);

			try {
				PeriodicTableComposite ptc = new PeriodicTableComposite(container, 106);
				ptc.addPeriodicTableButtonPressedListener(event -> {
					Button mendeleevButton = event.getButton();
					if (mendeleevButton.getMenu() != null)
						mendeleevButton.getMenu().dispose();
					int z = event.getZ();
					Menu popupMenu = new Menu(mendeleevButton);

					String[] edges = new String[] { "Ka", "Kb", "La", "Lb", "Ma" };

					for (String ed : edges) {
						MenuItem popupItem = new MenuItem(popupMenu, SWT.CHECK);
						popupItem.setText(ed);
						ElementAndEdge el = new ElementAndEdge(z, event.getElement(), ed);
						popupItem.setSelection(elements.contains(el));
						popupItem.addSelectionListener(new SelectionAdapter() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								if (elements.contains(el)) {
									elements.remove(el);
									ptc.getButton(z).setBackground(null);
								} else {
									elements.add(el);
									ptc.getButton(z)
											.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
								}
							}
						});
					}

					mendeleevButton.setMenu(popupMenu);
					popupMenu.setVisible(true);
				});
			} catch (Exception e) {
				logger.error("Could not create periodic table widget");
			}

			return container;
		}

	}

	public class ElementAndEdge {

		int z;
		String edge;
		String element;

		public ElementAndEdge(int z, String element, String edge) {
			this.z = z;
			this.edge = edge;
			this.element = element;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Objects.hash(edge, z);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ElementAndEdge other = (ElementAndEdge) obj;
			return Objects.equals(edge, other.edge) && z == other.z;
		}

		@Override
		public String toString() {
			return element + "-" + edge;
		}
	}
}
