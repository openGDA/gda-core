/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.commandqueue;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.server.servlet.AbstractConsumerServlet;
import org.eclipse.scanning.server.servlet.ScanProcess;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;

/**
 * A scan servlet, based on {@link ScanServlet}, but which can run GDA8 scans based
 * on {@link Command}  as well as GD9 {@link ScanBean}s.
 */
public class HeterogeneousScanServlet extends AbstractConsumerServlet<StatusBean> {

	@Override
	protected String getName() {
		return "Heterogeneous Scan Consumer";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IConsumerProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> response)
			throws EventException {
		if (bean instanceof ScanBean) {
			// scan beans are handled just as by ScanServlet (new scanning framework)
			return (IConsumerProcess) createScanProcess((ScanBean) bean, (IPublisher) response);
		}

		if (bean instanceof CommandBean) {
			// create a process for the command bean containing the legacy command
			return (IConsumerProcess) new CommandProcess((CommandBean) bean, (IPublisher) response);
		}

		// only ScanBean and CommandBean are supported
		throw new IllegalArgumentException("Unsupported bean class: " + bean.getClass().getName());
	}

	private ScanProcess createScanProcess(ScanBean scanBean, IPublisher<ScanBean> response) throws EventException {
		// copied from ScanServlet
		if (scanBean.getScanRequest() == null) throw new EventException("The scan must include a request to run something!");
		preprocess(scanBean);

		return new ScanProcess(scanBean, response, isBlocking());
	}

	private void preprocess(ScanBean scanBean) throws ProcessingException {
		// copied from ScanServlet
		ScanRequest<?> req = scanBean.getScanRequest();
		if (req.isIgnorePreprocess()) {
			return;
		}
		for (IPreprocessor processor : Services.getPreprocessors()) {
			req = processor.preprocess(req);
		}
		scanBean.setScanRequest(req);
	}

}
