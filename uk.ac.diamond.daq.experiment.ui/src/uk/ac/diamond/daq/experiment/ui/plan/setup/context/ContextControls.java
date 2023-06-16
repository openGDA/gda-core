/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan.setup.context;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.PlanTreeComponent;
import uk.ac.diamond.daq.experiment.ui.plan.setup.ComponentEditor;
import uk.ac.gda.ui.tool.ClientSWTElements;

public class ContextControls {

	private final Map<Class<? extends PlanTreeComponent>, ComponentEditor> editors =
			Map.of(
				SegmentDescriptor.class, new SegmentEditor(),
				TriggerDescriptor.class, new TriggerEditor());

	/** Do not dispose this! Caching to recreate contextComposite internally */
	private Composite parent;
	private Composite contextComposite;
	private ComponentEditor editor;

	private Set<String> prioritySignals;

	public ContextControls() {
		prioritySignals = Collections.emptySet();
	}

	public void createControls(Composite parent) {
		this.parent = parent;
		createContextComposite();
		new NoSelection().createControls(contextComposite, null);
	}

	public void setPrioritySignals(Set<String> signals) {
		prioritySignals = signals;
		if (editor != null) {
			editor.setPrioritySignals(signals);
		}
	}

	public void showContextControls(PlanTreeComponent component) {
		if (component == null) {
			editor = new NoSelection();
		} else {
			editor = editors.getOrDefault(component.getClass(), new NoSelection());
		}
		createContextComposite();
		editor.setPrioritySignals(prioritySignals);
		editor.createControls(contextComposite, component);
		parent.layout(true, true);
	}

	private class NoSelection implements ComponentEditor {

		@Override
		public void createControls(Composite parent, PlanTreeComponent ignoredSelection) {
			var noSelection = ClientSWTElements.composite(parent, 1);
			ClientSWTElements.label(noSelection, "Select an experiment plan component to edit");
		}

		@Override
		public void setPrioritySignals(Set<String> signals) {
			// Don't need them.
		}
	}

	private void createContextComposite() {
		if (contextComposite != null) {
			contextComposite.dispose();
			contextComposite = null;
		}
		contextComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(contextComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(contextComposite);

	}

}
