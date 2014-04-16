package uk.ac.gda.client.logpanel.commands;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.client.logpanel.view.Logpanel;
import uk.ac.gda.client.logpanel.view.LogpanelView;

public class CopyToClipboardHandler extends AbstractHandler {

	public static final String ID = "uk.ac.gda.client.logpanel.commands.copy";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logpanel logpanel = ((LogpanelView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(LogpanelView.ID)).getLogpanel();
		logpanel.copySelectedMessagesToClipboard();
		return null;
	}

}
