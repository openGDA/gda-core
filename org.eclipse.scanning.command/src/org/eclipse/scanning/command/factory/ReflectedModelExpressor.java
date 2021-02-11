package org.eclipse.scanning.command.factory;

import org.eclipse.scanning.api.device.models.IReflectedModel;

/**
 * @deprecated for removal in 9.21. See DAQ-3292
 */
@Deprecated
public class ReflectedModelExpressor extends PyModelExpresser<IReflectedModel> {

	@Override
	String pyExpress(IReflectedModel model, boolean verbose) throws Exception {
		return model.getCommandString(verbose);
	}
}
