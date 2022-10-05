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

package uk.ac.diamond.daq.sample.plate.management.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.sample.plate.management.ui.PathscanConfigConstants;

public class AnalyserComposite extends Composite {

	private IEventBroker eventBroker;

	private Map<String, CTabFolder> paramTabFolders = new HashMap<>();

	private Button newParamCollectionButton;

	private List analysersList;

	private String currentAnalyser;

	public AnalyserComposite(Composite parent, int style, IEventBroker eventBroker) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		this.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(false,  false).span(4,1).applyTo(this);

		this.eventBroker = eventBroker;

		addLabel(this, "Sequence files", span(4));
		analysersList = new List(this,  SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 4).applyTo(analysersList);

		Button createSequenceButton = addButton(this, "Create...", span(1).align(SWT.FILL, SWT.FILL), true);
		Button loadSequenceButton = addButton(this, "Load...", span(1).align(SWT.FILL, SWT.FILL), true);
		Button noSequenceButton = addButton(this, "No Sequence", span(1).align(SWT.FILL, SWT.FILL), true);
		Button deleteSequenceButton = addButton(this, "Delete", span(1).align(SWT.FILL, SWT.FILL), true);

		newParamCollectionButton = addButton(this, "New Parameter Set", span(4).align(SWT.BEGINNING, SWT.FILL), true);
		newParamCollectionButton.setEnabled(false);
		newParamCollectionButton.addSelectionListener(new SelectionAdapter() {
			Map<CTabFolder, Integer> id = new HashMap<>();
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem paramTabItem = new CTabItem(paramTabFolders.get(currentAnalyser), SWT.CLOSE);
				id.putIfAbsent(paramTabFolders.get(currentAnalyser), 0);
				id.put(paramTabFolders.get(currentAnalyser), id.get(paramTabFolders.get(currentAnalyser)) + 1);
				String paramName = "Parameter set " + id.get(paramTabFolders.get(currentAnalyser));
				paramTabItem.setText(paramName);
				paramTabItem.setControl(new ParamComposite(paramTabFolders.get(currentAnalyser), SWT.NONE,
						!currentAnalyser.equals(PathscanConfigConstants.NO_ANALYSER), currentAnalyser));
				paramTabFolders.get(currentAnalyser).setSelection(paramTabItem);
				eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null);
			}
		});

		analysersList.addSelectionListener(getAnalysersListAdapter(this));
		createSequenceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event ->
			eventBroker.post(PathscanConfigConstants.TOPIC_OPEN_SPECS, null)
		));

		loadSequenceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.MULTI);
			fileDialog.setFilterExtensions(new String[] {"*.seq"});
			String firstFile = fileDialog.open();
			if (firstFile != null) {
				String[] selectedFiles = fileDialog.getFileNames();
				for (String selectedFile: selectedFiles) {
					String analyserSequenceFile = fileDialog.getFilterPath() + "/" + selectedFile;
					if (!isDuplicate(analyserSequenceFile))
						analysersList.add(analyserSequenceFile);
				}
			}

			eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null);
			newParamCollectionButton.setEnabled(true);
		}));

		noSequenceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			if (!isDuplicate(PathscanConfigConstants.NO_ANALYSER))
				analysersList.add(PathscanConfigConstants.NO_ANALYSER);
			eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null);
			newParamCollectionButton.setEnabled(true);
		}));

		deleteSequenceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			paramTabFolders.get(analysersList.getSelection()[0]).dispose();
			paramTabFolders.remove(analysersList.getSelection()[0]);
			analysersList.remove(analysersList.getSelectionIndices()[0]);
			eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null);
			if (analysersList.getItemCount() == 0)
				newParamCollectionButton.setEnabled(false);
		}));
	}

	protected boolean isDuplicate(String analyserSequenceFile) {
		for (String item: analysersList.getItems()) {
			if (item.equals(analyserSequenceFile)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, CTabFolder> getParamTabFolders() {
		return paramTabFolders;
	}

	public Button getNewParamCollectionButton() {
		return newParamCollectionButton;
	}

	public List getAnalysersList() {
		return analysersList;
	}

	private SelectionAdapter getAnalysersListAdapter(Composite parent) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List list = (List) event.getSource();
				String[] selections = list.getSelection();
				if (selections.length == 1) {
					String analyser = selections[0];
					currentAnalyser = analyser;
					if (paramTabFolders.get(analyser) == null) {
						CTabFolder paramTabFolder = new CTabFolder(parent, SWT.CLOSE);
						paramTabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						paramTabFolder.addCTabFolder2Listener(CTabFolder2Listener.closeAdapter(event1 ->
							eventBroker.post(PathscanConfigConstants.TOPIC_SYNC_SUMMARY, null)
						));
						GridDataFactory.fillDefaults().grab(true, false).span(4,1).applyTo(paramTabFolder);
						paramTabFolders.put(analyser, paramTabFolder);
						newParamCollectionButton.setSelection(true);
						newParamCollectionButton.notifyListeners(SWT.Selection, new Event());
						paramTabFolders.get(currentAnalyser).setSelection(0);
					}
					hideAllTabFolders();
					setShowTabFolder(paramTabFolders.get(analyser), true);
				}
			}
		};
	}

	private void setShowTabFolder(CTabFolder tabFolder, boolean show) {
		GridData data = (GridData) tabFolder.getLayoutData();
		data.exclude = !show;
		tabFolder.setVisible(show);
		tabFolder.layout(true, true);
		tabFolder.getParent().layout(true, true);							// AnalyserComposite
		tabFolder.getParent().getParent().layout(true, true);				// ShapeComposite
		tabFolder.getParent().getParent().getParent().layout(true, true);		// shapeTabFolder
//		tabFolder.getParent().getParent().getParent().getParent().pack();			// child
		eventBroker.post(PathscanConfigConstants.TOPIC_RESIZE_SCROLL, null);
	}

	private void hideAllTabFolders() {
		for (CTabFolder tabFolder: paramTabFolders.values()) {
			setShowTabFolder(tabFolder, false);
		}
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		layout.applyTo(label);
		return label;
	}

	private Button addButton(Composite parent, String buttonText, GridDataFactory layout, boolean buttonEnabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(buttonText);
		button.setEnabled(buttonEnabled);
		layout.applyTo(button);
		return button;
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.swtDefaults().span(span, 1);
	}
}