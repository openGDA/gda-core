/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.pydev.extension.builder;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.ILocalScope;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.docutils.PySelection.ActivationTokenAndQual;
import org.python.pydev.dltk.console.ui.IScriptConsoleViewer;
import org.python.pydev.editor.codecompletion.CompletionRequest;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant2;

/**
 * TODO
 */
public class ExtendedSyntaxCompleter implements IPyDevCompletionParticipant, IPyDevCompletionParticipant2 {

	@Override
	public Collection<Object> getArgsCompletion(ICompletionState arg0, ILocalScope arg1, Collection<IToken> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IToken> getCompletionsForMethodParameter(ICompletionState arg0, ILocalScope arg1,
			Collection<IToken> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IToken> getCompletionsForTokenWithUndefinedType(ICompletionState arg0, ILocalScope arg1,
			Collection<IToken> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> getGlobalCompletions(CompletionRequest arg0, ICompletionState arg1)
			throws MisconfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> getStringGlobalCompletions(CompletionRequest arg0, ICompletionState arg1)
			throws MisconfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ICompletionProposal> computeConsoleCompletions(ActivationTokenAndQual arg0,
			List<IPythonNature> arg1, IScriptConsoleViewer arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}
