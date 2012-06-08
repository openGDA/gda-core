package gda.jython.logger;


import gda.jython.ITerminalOutputProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;

/**
 * 	An  {@link OutputTerminalAdapter} listens for terminal input/output and logs it to the specified {@link LineLogger}.
 */
public class OutputTerminalAdapter extends TerminalAdapter  {

	/**
	 * 
	 * @param logger
	 * @param terminalOutputProvider The GDA's {@link JythonServerFacade} singleton is often a good choice.
	 */
	public OutputTerminalAdapter(LineLogger logger, ITerminalOutputProvider terminalOutputProvider) {
		super(logger);
		terminalOutputProvider.addOutputTerminal(this);
	}
}