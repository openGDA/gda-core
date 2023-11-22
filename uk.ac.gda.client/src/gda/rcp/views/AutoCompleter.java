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

import java.util.Set;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;

import gda.jython.InterfaceProvider;
import gda.jython.completion.AutoCompleteOption;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.TextCompleter;
import gda.rcp.GDAClientActivator;
import gda.rcp.ImageConstants;

public class AutoCompleter {
	private static final Set<Character> ACCEPT_KEYS = Set.of(SWT.TAB, SWT.CR, SWT.LF, SWT.SPACE);
	private static final KeyStroke TAB = KeyStroke.getInstance(SWT.NONE, SWT.TAB);
	private static final KeyStroke CTRL_SPACE = KeyStroke.getInstance(SWT.CTRL, SWT.SPACE);

	private final Text txtInput;
	private final TextCompleter completer = InterfaceProvider.getCompleter();

	private ContentProposalAdapter ctrlSpaceAdapter;
	private ContentProposalAdapter tabAdapter;

	private IContentProposalListener contentProposalListener = this::acceptCompletion;

	public AutoCompleter(final Text txtInput) {
		this.txtInput = txtInput;
		tabAdapter = createContentProposalAdapter(TAB);
		ctrlSpaceAdapter = createContentProposalAdapter(CTRL_SPACE);
	}

	private ContentProposalAdapter createContentProposalAdapter(KeyStroke trigger) {
		var cpa = new ContentProposalAdapter(txtInput, new TextContentAdapter(), this::getProposals, trigger, ACCEPT_KEYS);
		cpa.addContentProposalListener(contentProposalListener);
		cpa.setLabelProvider(prv);
		return cpa;
	}

	private ILabelProvider prv = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IContentProposal proposal) {
				return proposal.getLabel();
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof AutoCompletionAdapter adapter) {
				ImageRegistry imageRegistry = GDAClientActivator.getDefault().getImageRegistry();
				return switch (adapter.getType()) {
				case 0 -> imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_0);
				case 1 -> imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_1);
				case 2 -> imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_2);
				case 3 -> imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_3);
				case 4 -> imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_4);
				default -> null;
				};
			}
			return null;
		}
	};

	private void acceptCompletion(IContentProposal completion) {
		if (completion instanceof AutoCompletionAdapter comp) {
			txtInput.setText(comp.getContent());
			final int posn = comp.getCursorPosition();
			txtInput.setFocus();
			txtInput.setSelection(posn, posn);
		}
		// if key to open proposals != key to accept proposal, popup can remain open
		tabAdapter.closeProposalPopup();
		ctrlSpaceAdapter.closeProposalPopup();
	}

	public IContentProposal[] getProposals(String contents, int position) {
		var completionOptions = completer.getCompletionsFor(contents, position);
		return completionOptions.getOptions()
				.stream()
				.map(o -> new AutoCompletionAdapter(o, completionOptions))
				.toArray(AutoCompletionAdapter[]::new);
	}

	public void dispose() {
		if (ctrlSpaceAdapter != null) {
			ctrlSpaceAdapter.removeContentProposalListener(contentProposalListener);
		}
		if (tabAdapter != null) {
			tabAdapter.removeContentProposalListener(contentProposalListener);
		}
	}
}

class AutoCompletionAdapter implements IContentProposal {

	private final AutoCompleteOption option;
	private AutoCompletion completionOptions;

	public AutoCompletionAdapter(AutoCompleteOption option, AutoCompletion completionOptions) {
		this.option = option;
		this.completionOptions = completionOptions;
	}

	public int getType() {
		return option.type.ordinal();
	}

	@Override
	public String getContent() { return completionOptions.getBefore() + option.text + completionOptions.getAfter(); }

	@Override
	public int getCursorPosition() { return completionOptions.getPosition() + option.text.length(); }

	@Override
	public String getLabel() { return option.text; }

	@Override
	public String getDescription() { return ""; }

}