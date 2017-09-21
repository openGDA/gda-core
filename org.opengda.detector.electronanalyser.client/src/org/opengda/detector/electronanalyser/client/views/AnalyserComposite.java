package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * class to display analyser status and permit manual zero supplies in the analyser device
 *
 * @author fy65
 *
 */
public class AnalyserComposite extends Composite implements InitializationListener {

	private String statePV;
	private String acquirePV;
	private String messagePV;
	private String zeroSuppliesPV;

	private static final Logger logger = LoggerFactory.getLogger(AnalyserComposite.class);
	private EpicsChannelManager channelManager;
	private EpicsController controller;

	private StateListener stateListener;
	private AcquireListener acquireListener;
	private MessageListener messageListener;

	private Channel stateChannel;
	private Channel acquireChannel;
	private Channel messageChannel;
	private Channel zeroSppliesChannel;

	private Text txtStateValue;
	private Text txtAcquireState;
	private Text txtMessage;
	private String[] stateLabels;

	public AnalyserComposite(Composite parent, int style) {
		super(parent, style);

		channelManager = new EpicsChannelManager(this);
		controller=EpicsController.getInstance();
		setLayout(new GridLayout(1, false));

		Composite rootComposite = new Composite(parent, SWT.NONE);
		GridData gd_rootComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_rootComposite.heightHint = 55;
		gd_rootComposite.widthHint = 451;
		rootComposite.setLayoutData(gd_rootComposite);
		GridLayout layout = new GridLayout(5, false);
		layout.horizontalSpacing = 15;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		rootComposite.setLayout(layout);

		Label lblState = new Label(rootComposite, SWT.None);
		lblState.setText("State: ");

		txtStateValue = new Text(rootComposite, SWT.BORDER);
		GridData gd_txtStateValue = new GridData(SWT.LEFT, SWT.CENTER, false,false, 1, 1);
		gd_txtStateValue.widthHint = 120;
		txtStateValue.setLayoutData(gd_txtStateValue);
		txtStateValue.setEditable(false);

		Label lblAcquire = new Label(rootComposite, SWT.NONE);
		lblAcquire.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAcquire.setText("Acquire:");

		txtAcquireState = new Text(rootComposite, SWT.BORDER);
		txtAcquireState.setBackground(ColorConstants.darkGreen);
		GridData gd_txtAcquireState = new GridData(SWT.CENTER, SWT.CENTER,false, false, 1, 1);
		gd_txtAcquireState.widthHint = 15;
		txtAcquireState.setLayoutData(gd_txtAcquireState);

		Button btnZeroSupplies = new Button(rootComposite, SWT.NONE);
		btnZeroSupplies.setText("Zero Supplies");
		btnZeroSupplies.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					controller.caput(zeroSppliesChannel, 1);
				} catch (CAException | InterruptedException e1) {
					logger.error("Failed to zero the supplies on {}", zeroSppliesChannel.getName());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					controller.caput(zeroSppliesChannel, 1);
				} catch (CAException | InterruptedException e1) {
					logger.error("Failed to zero the supplies on {}", zeroSppliesChannel.getName());
				}
			}
		});

		Label lblMessage = new Label(rootComposite, SWT.NONE);
		lblMessage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,false, 1, 1));
		lblMessage.setText("Message:");

		txtMessage = new Text(rootComposite, SWT.BORDER);
		txtMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false, 4, 1));
	}

	public void initialise() {
		if (getStatePV() == null) {
			throw new IllegalStateException(
					"required PV for Analyser State missing.");
		}
		if (getAcquirePV() == null) {
			throw new IllegalStateException(
					"required PV for Analyser Acquire missing.");
		}
		if (getMessagePV() == null) {
			throw new IllegalStateException(
					"required PV for Analyser Message missing.");
		}
		if (getZeroSuppliesPV() == null) {
			throw new IllegalStateException(
					"required PV for Analyser Zero Supplies missing.");
		}
		stateListener = new StateListener();
		acquireListener = new AcquireListener();
		messageListener = new MessageListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create all required channels", e1);
		}
	}

	public void createChannels() throws CAException, TimeoutException {
		stateChannel = channelManager.createChannel(getStatePV(), false);
		acquireChannel = channelManager.createChannel(getAcquirePV(),acquireListener, MonitorType.NATIVE, false);
		zeroSppliesChannel = channelManager.createChannel(getZeroSuppliesPV(),false);
		messageChannel = channelManager.createChannel(getMessagePV(), false);
		channelManager.creationPhaseCompleted();
		logger.debug("channels are created");
	}

	public class StateListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				final short value = ((DBR_Enum) dbr).getEnumValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtStateValue.setText(stateLabels[value]);
							if (stateLabels[value].equalsIgnoreCase("Error")) {
								txtStateValue.setForeground(ColorConstants.red);
							} else {
								txtStateValue.setForeground(ColorConstants.black);
							}
						}
					});
				}
				logger.debug("Analyser state changed to {}", stateLabels[value]);
			}else {
				logger.error("Analyser state PV type is {}", dbr.getType());
			}
		}
	}

	public class AcquireListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				final short value = ((DBR_Enum) dbr).getEnumValue()[0];
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (value==1) {
								txtAcquireState.setBackground(ColorConstants.green);
							} else {
								txtAcquireState.setBackground(ColorConstants.darkGreen);
							}
						}
					});
				}
				logger.debug("Analyser Acquire update to {}", value);
			}
		}
	}

	private class MessageListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isBYTE()) {
				final byte[] message1 = ((DBR_Byte) dbr).getByteValue();
				final String message=new String(message1).trim();
				if (!getDisplay().isDisposed()) {
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							txtMessage.setText(message);
						}
					});
				}
				logger.debug("Analyser message updated to {}", message);
			} else {
				logger.error("Analyser message PV type is {}", dbr.getType());
			}
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException,
			DeviceException, TimeoutException, CAException {
		try {
			stateLabels = controller.cagetLabels(stateChannel);
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Failed to get Analyser State labels!");
		}
		controller.setMonitor(stateChannel, DBRType.ENUM, gov.aps.jca.Monitor.VALUE, stateListener, stateChannel.getElementCount());
		controller.setMonitor(messageChannel,DBRType.BYTE, gov.aps.jca.Monitor.VALUE, messageListener, messageChannel.getElementCount());
		logger.info("Analyser IOC EPICS Channels initialisation completed!");

	}

	public String getStatePV() {
		return statePV;
	}

	public void setStatePV(String statePV) {
		this.statePV = statePV;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public String getMessagePV() {
		return messagePV;
	}

	public void setMessagePV(String messagePV) {
		this.messagePV = messagePV;
	}

	public String getZeroSuppliesPV() {
		return zeroSuppliesPV;
	}

	public void setZeroSuppliesPV(String zeroSuppliesPV) {
		this.zeroSuppliesPV = zeroSuppliesPV;
	}
}
