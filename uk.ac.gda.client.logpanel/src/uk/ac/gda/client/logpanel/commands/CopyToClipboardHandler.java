package uk.ac.gda.client.logpanel.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.base.Joiner;

import uk.ac.gda.client.logpanel.view.Logpanel;
import uk.ac.gda.client.logpanel.view.LogpanelView;

public class CopyToClipboardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LogpanelView logpanelView = (LogpanelView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("uk.ac.gda.client.logpanel.view");
		Logpanel logpanel = logpanelView.getLogpanel(); // needed for display and selected messages
		List<String> selectedMessageStrings = logpanel.getSelectedMessageStrings();
		String selectedMessages = Joiner.on('\n').join(selectedMessageStrings);
		final Clipboard cb = new Clipboard(logpanel.getDisplay());
		if (selectedMessages.length() > 0) {
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[]{selectedMessages}, new Transfer[]{textTransfer});
		}
		return null;
	}

}
