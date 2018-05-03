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

package uk.ac.gda.beamline.synoptics.composites;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.icat.IcatProvider;
import gda.device.DeviceException;
import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
/**
 * Reusable composite that displays a label on the left and a drop down selection on the right.
 */
public class VisitSelectionComposite extends Composite implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(VisitSelectionComposite.class);
	private static final String UNKNOWN = "UNKNOWN";
	private ComboViewer viewer;
	private Collection<String> list;
	private final Metadata metadata;
	private final String metaName;

	public VisitSelectionComposite(Composite parent, int style, String label, String entryName) {
		super(parent, style);
		list = getCurrentVisits();
		requireNonNull(entryName, "Metadata entry is required");
		metaName = entryName;
		metadata = GDAMetadataProvider.getInstance();
		metadata.addIObserver(this);
		if (label == null) {
			label = metaName;
		}

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);

		Label lbl = new Label(this, SWT.NONE | SWT.CENTER);
		lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true));
		lbl.setText(label);
		lbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		viewer = new ComboViewer(parent, SWT.READ_ONLY);
		viewer.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		// the ArrayContentProvider  object does not store any state,
		// therefore you can re-use instances
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return String.format("%-20s", element);
				}
				return super.getText(element);
			}
		});
		viewer.setInput(list);
		// react to the selection change of the viewer
		// note that the viewer returns the actual object
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			//implement property change by this viewer
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0) {
					String firstElement = (String) selection.getFirstElement();
					if (!firstElement.equals(getCurrentVisit())) {
						setVisit(firstElement);
					}
					logger.info("Selected {}", firstElement);
				}
			}
		});
		viewer.setSelection(new StructuredSelection(getCurrentVisit()));
		parent.addDisposeListener((e) -> dispose());
	}

	protected void updateSelection() {
		if (!isDisposed()) {
			Display.getDefault().asyncExec(() -> {
				String visit = getCurrentVisit();
				if (!list.contains(visit)) {
					list.add(String.valueOf(visit));
					viewer.add(visit);
					viewer.getControl().redraw();
				}
				viewer.setSelection(new StructuredSelection(visit));
			});
		}
	}

	private String getCurrentVisit() {
		try {
			return metadata.getMetadataValue(metaName);
		} catch (DeviceException e) {
			logger.error("Could not get visit metadata", e);
			return UNKNOWN;
		}
	}

	private boolean setVisit(String firstElement) {
		try {
			metadata.setMetadataValue(metaName, firstElement);
			return true;
		} catch (DeviceException e) {
			logger.error("Could not set visit", e);
			return false;
		}
	}

	private Collection<String> getCurrentVisits() {
		IBatonStateProvider bsp = InterfaceProvider.getBatonStateProvider();
		String fedid = bsp.getMyDetails().getUserID();
		try {
			Set<String> visits = stream(IcatProvider.getInstance().getMyValidVisits(fedid))
					.map(v -> v.getVisitID())
					.filter(s -> !s.isEmpty())
					.collect(toSet());
			visits.add(bsp.getMyDetails().getVisitID());
			return visits;
		} catch (Exception e) {
			logger.error("Could not get current visits", e);
		}

		return new HashSet<>(asList(bsp.getMyDetails().getVisitID()));
	}

	@Override
	public void dispose() {
		metadata.deleteIObserver(this);
		super.dispose();
	}

	/**
	 * handling distributed event - i.e property change on the server side.
	 */
	@Override
	public void update(Object source, Object arg) {
		logger.trace("Update {} from {}", arg, source);
		if (source instanceof MetadataEntry && metaName.equals(((MetadataEntry)source).getName())) {
			updateSelection();
		}
	}
}
