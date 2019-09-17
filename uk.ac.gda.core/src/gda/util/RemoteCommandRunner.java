package gda.util;

import java.io.Serializable;

import gda.factory.Findable;

/** Command Runner to allow clients to run commands that take arguments */
public interface RemoteCommandRunner extends Findable {
	void runCommand(Serializable... arguments);
}