package gda.jython.logger;


import gda.jython.ITerminalInputProvider;
import gda.jython.JythonServer;

/**
 * 	An  {@link InputTerminalAdapter} listens for terminal input and logs it to the specified {@link LineLogger}.
 */
public class InputTerminalAdapter extends TerminalAdapter  {

	/**
 * @param logger
 * @param terminalInputProvider The GDA's {@link JythonServer} named 'command_server' is often a good choice.
 */
	public InputTerminalAdapter(LineLogger logger, ITerminalInputProvider terminalInputProvider) {
		super(logger);
		terminalInputProvider.addInputTerminal(this);
	}
}