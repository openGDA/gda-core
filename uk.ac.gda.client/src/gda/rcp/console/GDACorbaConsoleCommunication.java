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

package gda.rcp.console;

import gda.jython.JythonServerFacade;
import gda.jython.Terminal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.osgi.framework.Bundle;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.IToken;
import org.python.pydev.editor.codecompletion.AbstractPyCodeCompletion;
import org.python.pydev.editor.codecompletion.PyCalltipsContextInformation;
import org.python.pydev.editor.codecompletion.PyCodeCompletionImages;
import org.python.pydev.editor.codecompletion.PyLinkedModeCompletionProposal;
import org.python.pydev.shared_core.callbacks.ICallback;
import org.python.pydev.shared_core.structure.Tuple;
import org.python.pydev.shared_interactive_console.console.IScriptConsoleCommunication;
import org.python.pydev.shared_interactive_console.console.InterpreterResponse;
import org.python.pydev.shared_ui.proposals.IPyCompletionProposal;
import org.python.pydev.shared_ui.proposals.PyCompletionProposal;

public class GDACorbaConsoleCommunication implements IScriptConsoleCommunication, Terminal {

	/* the command server to run the jython in */
	JythonServerFacade commandserver;

	/* for previously partial commands, the command in progress */
	String commandInProgress = "";

	/* the data that has come in since newData was last cleared */
	String newData = "";

	public GDACorbaConsoleCommunication() {
		Bundle bundle = Platform.getBundle("ac.uk.gda.core");
		Path path = new Path("scripts/PySrc");
		URL pySrcUrl;
		try {
			pySrcUrl = new URL(bundle.getEntry("/"), path.toString());
			URL localURL = FileLocator.toFileURL(pySrcUrl);

			String file = new File(localURL.getFile()).getCanonicalFile().toString();

			commandserver = JythonServerFacade.getInstance();
			commandserver.addIObserver(this); //FIXME: potential race condition

			// rather than ensure the completer code is included in the -Dpython.path definition
			commandserver.runsource("sys.path.append(\"" + file + "\")", "GDACorbaConsoleCommunication");
			commandserver.runsource("from _completer import Completer", "GDACorbaConsoleCommunication");
		} catch (MalformedURLException e) {
			// TODO Completions Not Available error needs to be piped to the correct place
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Completions Not Available error needs to be piped to the correct place
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		commandserver.deleteIObserver(this);
	}

	public InterpreterResponse execInterpreter(String command) {

		/*
		 * append the newest "bit" to the command in progress. This is different than how execInterpreter in
		 * PydevConsoleCommunication works because the rpcxml server does "push" see
		 * http://www.jython.org/docs/api/org/python/util/InteractiveConsole.html#push(java.lang.String) but the gda
		 * server does "runsource" see
		 * http://www.jython.org/docs/api/org/python/util/InteractiveInterpreter.html#runsource(java.lang.String)
		 */
		commandInProgress += "\n" + command;

		boolean needMore = commandserver.runsource(commandInProgress, "JythonTerminal");
		// InterpreterResponse x = new InterpreterResponse(newData, "", needMore, false);
		InterpreterResponse x = new InterpreterResponse("", "", needMore, false);
		newData = "";

		/* if we don't need anymore, the command is done, so start fresh */
		if (!needMore) {
			commandInProgress = "";
		}
		return x;
	}

	@Override
	public String getDescription(String text) throws Exception {
		return "";
	}

	@Override
	public ICompletionProposal[] getCompletions(String text, String othertext, int offset, boolean showForTabCompletion) throws Exception {

		String savedNewData = newData;
		newData = "";
		String command = "myabc = Completer(locals(), globals())";
		commandserver.runsource(command, "GDACorbaConsoleCommunication");
		command = "myabc.complete(\"" + text + "\")";
		commandserver.runsource(command, "GDACorbaConsoleCommunication");
		String fromServerString = newData;
		newData = savedNewData;

		List<ICompletionProposal> ret = new ArrayList<ICompletionProposal>();

		int length = text.lastIndexOf('.');
		if (length == -1) {
			length = text.length();
		} else {
			length = text.length() - length - 1;
		}
		// On Windows we get the string terminated in \r\n whilst on Linux we get \n
		if (fromServerString.charAt(fromServerString.length() - 1) == '\n') {
			fromServerString = fromServerString.substring(0, fromServerString.length() - 1);
		}
		if (fromServerString.charAt(fromServerString.length() - 1) == '\r') {
			fromServerString = fromServerString.substring(0, fromServerString.length() - 1);
		}

		if (fromServerString.length() < 3 || fromServerString.charAt(0) != '['
				|| fromServerString.charAt(fromServerString.length() - 1) != ']') {
			return new ICompletionProposal[0];
		}

		// remove starting and trailing [()] as we split on the inner brackets, remove the outer ones
		fromServerString = fromServerString.substring(2, fromServerString.length() - 2);

		// split into individual completeions
		String[] fromServer = fromServerString.split("\\), \\(");

		for (String s : fromServer) {

			// name, doc, args, type
			String[] comp = s.split(", ");

			String name = comp[0].substring(1, comp[0].length() - 1);
			String docStr = comp[1].substring(1, comp[1].length() - 1);

			String docStr2 = docStr.replaceAll("\\\\n", "\n");
			int type;
			try {
				type = Integer.parseInt(comp[3].substring(1, comp[3].length() - 1));
			} catch (NumberFormatException e) {
				type = 0;
			}
			String argsReceived = comp[2].substring(1, comp[2].length() - 1);
			String args = AbstractPyCodeCompletion.getArgs(argsReceived, type,
					ICompletionState.LOOKING_FOR_INSTANCED_VARIABLE);
			String nameAndArgs = name + args;

			int priority = IPyCompletionProposal.PRIORITY_DEFAULT;
			if (type == IToken.TYPE_PARAM) {
				priority = IPyCompletionProposal.PRIORITY_LOCALS;
			}

			int cursorPos = name.length();
			if (args.length() > 1) {
				cursorPos += 1;
			}		// TODO

			int replacementOffset = offset - length;
			PyCalltipsContextInformation pyContextInformation = null;
			if (args.length() > 2) {
				pyContextInformation = new PyCalltipsContextInformation(args, replacementOffset + name.length() + 1); // just
																														// after
																														// the
																														// parenthesis
			}

			ret.add(new PyLinkedModeCompletionProposal(nameAndArgs, replacementOffset, length, cursorPos,
					PyCodeCompletionImages.getImageForType(type), nameAndArgs, pyContextInformation, docStr2, priority,
					PyCompletionProposal.ON_APPLY_DEFAULT, args, false));

		}

		ICompletionProposal[] proposals = ret.toArray(new ICompletionProposal[ret.size()]);
		return proposals;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
	}

	@Override
	public void write(byte[] output) {
		try {
			write(new String(output, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(String output) {
		newData += output;
	}

	@Override
	public void execInterpreter(String command, ICallback<Object, InterpreterResponse> arg1,
			ICallback<Object, Tuple<String, String>> arg2) {

		/*
		 * append the newest "bit" to the command in progress. This is different than how execInterpreter in
		 * PydevConsoleCommunication works because the rpcxml server does "push" see
		 * http://www.jython.org/docs/api/org/python/util/InteractiveConsole.html#push(java.lang.String) but the gda
		 * server does "runsource" see
		 * http://www.jython.org/docs/api/org/python/util/InteractiveInterpreter.html#runsource(java.lang.String)
		 */
		commandInProgress += "\n" + command;

		boolean needMore = commandserver.runsource(commandInProgress, "JythonTerminal");
		// InterpreterResponse x = new InterpreterResponse(newData, "", needMore, false);
		InterpreterResponse x = new InterpreterResponse("", "", needMore, false);
		newData = "";

		/* if we don't need anymore, the command is done, so start fresh */
		if (!needMore) {
			commandInProgress = "";
		}
		arg1.call(x);
	}

	@Override
	public void linkWithDebugSelection(boolean arg0) {
		//not sure what to do here so provide default implementation
	}
}