package gda.util;

import java.io.Serializable;

import gda.device.Stoppable;

/** Command Runner to allow clients to run commands that take arguments */
public interface RemoteCommandRunner extends Stoppable {
	void runCommand(Serializable... arguments);
}