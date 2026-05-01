package uk.ac.diamond.daq.arpes.ui.e4.addons;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.arpes.ui.e4.context.SpringContextHolder;
import uk.ac.diamond.daq.arpes.ui.e4.dispatcher.MbsArpesLiveDataDispatcherE4;

class RegisterDispatcherInContextAddon {
	@Inject
	private IEclipseContext context;

	private static final Logger logger = LoggerFactory.getLogger(RegisterDispatcherInContextAddon.class);

	@PostConstruct
	public void init() {
		Map<String, MbsArpesLiveDataDispatcherE4> dispatchers = SpringContextHolder
				.getContext()
				.getBeansOfType(MbsArpesLiveDataDispatcherE4.class);

			logger.debug("Adding dispatchers {} to eclipse context", dispatchers);
			context.set("dispatcherMap", dispatchers);

	}
}
