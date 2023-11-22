/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views;

import static org.eclipse.jface.viewers.LabelProvider.createTextProvider;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

public class HistorySearch {
	private static final KeyStroke CTRL_R = KeyStroke.getInstance(SWT.CTRL, 'r');

	private final Text txtInput;
	private ContentProposalAdapter ctrlr;

	private IContentProposalListener contentProposalListener = p -> acceptHisorySelection(p.getLabel());
	private List<String> history;

	public HistorySearch(Text txtInput, List<String> history) {
		this.txtInput = txtInput;
		this.history = history;
		var adapter = new TextContentAdapter();
		var acceptKeys = Set.of(SWT.CR, SWT.LF);
		ctrlr = new ContentProposalAdapter(txtInput, adapter, this::getProposals, CTRL_R, acceptKeys);
		ctrlr.addContentProposalListener(contentProposalListener);
		ctrlr.setLabelProvider(createTextProvider(e -> {
			if (e instanceof IContentProposal el) {
				return el.getLabel();
			}
			return null;
		}));
	}

	private void acceptHisorySelection(String replacement) {
		if (replacement != null) {
			txtInput.setText(replacement);
			txtInput.setSelection(replacement.length());
			txtInput.setFocus();
		}
		// if key to open proposals != key to accept proposal, popup can remain open
		ctrlr.closeProposalPopup();
	}


	/**
	 * Return an array of Objects representing the valid content proposals for a field.
	 *
	 * @param contents
	 *            the current contents of the field (only consulted if filtering is set to <code>true</code>)
	 * @param position
	 *            the current cursor position within the field (ignored)
	 * @return the array of Objects that represent valid proposals for the field given its current content.
	 */
	public IContentProposal[] getProposals(String contents, @SuppressWarnings("unused") int position) {
		return history.stream()
				.filter(s -> s.contains(contents))
				.distinct()
				.map(HistoryAdapter::new)
				.toArray(HistoryAdapter[]::new);
	}

	public void dispose() {
		if (ctrlr != null) {
			ctrlr.removeContentProposalListener(contentProposalListener);
		}
	}
}

class HistoryAdapter implements IContentProposal {
	private String text;

	public HistoryAdapter(String text) {
		this.text = text;
	}

	@Override
	public String getContent() { return text; }

	@Override
	public int getCursorPosition() { return text.length(); }

	@Override
	public String getLabel() { return text; }

	@Override
	public String getDescription() { return ""; }

}