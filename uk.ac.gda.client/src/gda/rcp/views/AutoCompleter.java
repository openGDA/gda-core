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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.completion.AutoCompleteOption;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.TextCompleter;
import gda.rcp.GDAClientActivator;
import gda.rcp.ImageConstants;

public class AutoCompleter {

	private final Text txtInput;
	private static final Logger logger = LoggerFactory.getLogger(AutoCompleter.class);
	private final TextCompleter completer = InterfaceProvider.getCompleter();

	private ContentProposalAdapter ctrlSpaceAdapter;
	private ContentProposalAdapter tabAdapter;

	private IContentProposalListener contentProposalListener = p -> finishAutoCompletion(p.getLabel());
	private JythonTerminalContentProposalProvider contentProposalProvider = new JythonTerminalContentProposalProvider();

	public AutoCompleter(final Text txtInput) {
		this.txtInput = txtInput;
		try {
			installContentProposalAdapter(txtInput, new TextContentAdapter());
		} catch (ParseException e) {
			logger.error("Problem installing content proposal adapter", e);
		}
	}

	private void installContentProposalAdapter(Control control, IControlContentAdapter contentAdapter) throws ParseException {
		char[] autoActivationCharacters = null;
		KeyStroke tabStroke = KeyStroke.getInstance("Tab");
		tabAdapter = new ContentProposalAdapter(control, contentAdapter, contentProposalProvider, tabStroke, autoActivationCharacters);
		setupContentProposalAdapter(tabAdapter);

		KeyStroke ctrlSpaceKeyStroke = KeyStroke.getInstance("Ctrl+Space");
		ctrlSpaceAdapter = new ContentProposalAdapter(control, contentAdapter, contentProposalProvider, ctrlSpaceKeyStroke, autoActivationCharacters);
		setupContentProposalAdapter(ctrlSpaceAdapter);
	}

	private void setupContentProposalAdapter(ContentProposalAdapter cpa) {
		boolean propagate = true;
		int autoActivationDelay = 0;
		cpa.setInfoPopupRequired(false);
		cpa.addContentProposalListener(contentProposalListener);
		cpa.setLabelProvider(prv);
		cpa.setAutoActivationDelay(autoActivationDelay);
		cpa.setPropagateKeys(propagate);
		cpa.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
	}


	private ILabelProvider prv = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof IContentProposal) {
				return ((IContentProposal) element).getLabel();
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof AutoCompletionAdapter) {
				int type = ((AutoCompletionAdapter) element).getType();
				ImageRegistry imageRegistry = GDAClientActivator.getDefault().getImageRegistry();
				switch (type) {
				case 0:
					return imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_0);
				case 1:
					return imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_1);
				case 2:
					return imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_2);
				case 3:
					return imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_3);
				case 4:
					return imageRegistry.get(ImageConstants.IMG_JYTHON_TERMINAL_TYPE_4);
				}
			}
			return null;
		}
	};

	/*
	 * called by either the popup menu
	 */
	private void finishAutoCompletion(String replacement) {
		if (replacement != null) {
			txtInput.setText(contentProposalProvider.buildContent(replacement));
			final int posn = contentProposalProvider.completionOptions.getPosition() + replacement.length();
			txtInput.setFocus();
			txtInput.setSelection(posn, posn);
		}
		// if key to open proposals != key to accept proposal, popup can remain open
		tabAdapter.closeProposalPopup();
		ctrlSpaceAdapter.closeProposalPopup();
	}

	private class JythonTerminalContentProposalProvider implements IContentProposalProvider {

		private AutoCompletion completionOptions;

		/**
		 * Return an array of Objects representing the valid content proposals for a field.
		 *
		 * @param contents
		 *            the current contents of the field (only consulted if filtering is set to <code>true</code>)
		 * @param position
		 *            the current cursor position within the field (ignored)
		 * @return the array of Objects that represent valid proposals for the field given its current content.
		 */
		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			completionOptions = completer.getCompletionsFor(contents, position);
			return completionOptions.getOptions()
					.stream()
					.map(o -> new AutoCompletionAdapter(o, completionOptions.getPosition()))
					.toArray(AutoCompletionAdapter[]::new);
		}

		public String buildContent(String replacement) {
			return completionOptions.getBefore() + replacement + completionOptions.getAfter();
		}
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
	private final int posn;

	public AutoCompletionAdapter(AutoCompleteOption option, int posn) {
		this.option = option;
		this.posn = posn;
	}

	public int getType() {
		return option.type.ordinal();
	}

	@Override
	public String getContent() { return option.text; }

	@Override
	public int getCursorPosition() { return posn; }

	@Override
	public String getLabel() { return option.text; }

	@Override
	public String getDescription() { return ""; }

}