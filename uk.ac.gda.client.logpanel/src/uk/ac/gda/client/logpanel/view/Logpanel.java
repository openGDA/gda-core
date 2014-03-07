package uk.ac.gda.client.logpanel.view;

import gda.configuration.properties.LocalProperties;
import gda.util.logging.LogbackUtils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

public class Logpanel extends Composite {

	String pattern = "%d %-5level %logger - %m%n%ex";

	PatternLayout patternLayout;

	List<ILoggingEvent> loggingEvents = new LinkedList<ILoggingEvent>();

	/**
	 * wrap a writable list into an IObservableList: 
	 *  http://www.vogella.com/tutorials/EclipseDataBinding/article.html#jfacedb_viewer
	 */
	final IObservableList input = Properties.selfList(ILoggingEvent.class).observe(loggingEvents);
	
	TableViewer viewer;
	
	boolean scrollLockChecked = false;
	
	Display display = getDisplay();

	Color debugForeground = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	Color errorForeground = display.getSystemColor(SWT.COLOR_WHITE);
	Color errorBackground = display.getSystemColor(SWT.COLOR_DARK_RED);
	Color infoForeground = display.getSystemColor(SWT.COLOR_BLACK);
	Color warnForeground = display.getSystemColor(SWT.COLOR_BLACK);
	Color warnBackground = display.getSystemColor(SWT.COLOR_YELLOW);

//	public PatternLayout getPatternLayout() {
//		return patternLayout;
//	}

//	public void setPattern(String pattern) {
//		getPatternLayout().setPattern(pattern);
//	}

//	public String getMessage(ILoggingEvent loggingEvent) {
////		return getPatternLayout().doLayout(loggingEvent).trim();
//		return patternLayout.doLayout(loggingEvent).trim();
//	}

	class ILoggingEventLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
//			return getMessage(loggingEvent);
			return patternLayout.doLayout(loggingEvent).trim();
		}
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		@Override
		public Color getForeground(Object element, int columnIndex) {
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
			Level level = loggingEvent.getLevel();
			if (level == Level.INFO)
				return infoForeground;
			else if (level == Level.WARN)
				return warnForeground;
			else if (level == Level.ERROR)
				return errorForeground;
			else if (level == Level.DEBUG)
				return debugForeground;
			return null;
		}
		@Override
		public Color getBackground(Object element, int columnIndex) {
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
			Level level = loggingEvent.getLevel();
			if (level == Level.WARN)
				return warnBackground;
			else if (level == Level.ERROR)
				return errorBackground;
			return null;
		}
	}	

	public Logpanel(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);

		Label connectionLabel = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().span(3, 1).grab(true, false).applyTo(connectionLabel);

		String logServerHost = LocalProperties.get(LogbackUtils.GDA_LOGSERVER_HOST, LogbackUtils.GDA_LOGSERVER_HOST_DEFAULT);
		int logServerOutPort = LocalProperties.getInt(LogbackUtils.GDA_LOGSERVER_OUT_PORT, LogbackUtils.GDA_LOGSERVER_OUT_PORT_DEFAULT);
		
		String connectionLabelText; 
		Color connectionLabelForeground; 

		try {
			connectToLogServer(logServerHost, logServerOutPort); //TODO doesn't throw an Exception if host doesn't exist

			connectionLabelText = String.format("Connected to log server %s:%d", logServerHost, logServerOutPort);
			connectionLabelForeground = debugForeground;//display.getSystemColor(SWT.COLOR_GREEN);
		}
		catch (Exception exception) {
			connectionLabelText = String.format("Failed to connect to log server %s:%d\n%s", logServerHost, logServerOutPort, exception.getMessage());
			connectionLabelForeground = display.getSystemColor(SWT.COLOR_RED);
		}
		
		connectionLabel.setForeground(connectionLabelForeground);
		connectionLabel.setText(connectionLabelText);

		viewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(viewer.getControl());

		viewer.setLabelProvider(new ILoggingEventLabelProvider());

		// http://www.vogella.com/tutorials/EclipseDataBinding/article.html#jfacedb_viewer
		viewer.setContentProvider(new ObservableListContentProvider());
		
		// set the IObservableList as input for the viewer
		viewer.setInput(input); 

		Composite spacer = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().hint(1, 1).grab(true, false).applyTo(spacer);

		Label filterLabel = new Label(spacer, SWT.NONE);
		filterLabel.setText("Filter:");
		
		Text filterText = new Text(spacer, SWT.SINGLE);
		GridDataFactory.swtDefaults().hint(1, 1).grab(true, false).applyTo(filterText);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				//TODO implement updating filter
			}
		});

		final Button scrollLockCheckBox = new Button(this, SWT.CHECK); //TODO move to view toolbar
//		GridDataFactory.swtDefaults().applyTo(scrollLockCheckBox);
		scrollLockCheckBox.setText("Scroll Lock");
		scrollLockCheckBox.setSelection(scrollLockChecked);
		scrollLockCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollLockChecked = scrollLockCheckBox.getSelection();
			}
		});

		Button clearButton = new Button(this, SWT.PUSH); //TODO move to view toolbar
		GridDataFactory.swtDefaults().applyTo(clearButton);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				input.clear();
			}
		});
	}

	protected void connectToLogServer(String logServerHost, int logServerOutPort) throws Exception {

		// in Log view forwarding context
		LoggerContext logViewForwardingContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		logViewForwardingContext.reset();
		logViewForwardingContext.setName("logViewForwarding");

		// receive from log server ServerSocketAppender
		SocketReceiver receiver = new SocketReceiver();
		receiver.setContext(logViewForwardingContext);
		receiver.setRemoteHost(logServerHost);
		receiver.setPort(logServerOutPort);
		receiver.setReconnectionDelay(10000);

		// and using layout
		patternLayout = new PatternLayout();
		patternLayout.setPattern(pattern);
		patternLayout.setContext(logViewForwardingContext);

		// append to loggingEvents and update UI
		Appender<ILoggingEvent> loggingEventsAppender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(final ILoggingEvent loggingEvent) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						input.add(loggingEvent);
						if (!scrollLockChecked && viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
							viewer.reveal(loggingEvent);
						}
					}
				});
			}
		};

		loggingEventsAppender.setContext(logViewForwardingContext);

		logViewForwardingContext.register(receiver);
		logViewForwardingContext.register(loggingEventsAppender);

		Logger rootLogger = logViewForwardingContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(loggingEventsAppender);

		receiver.start();
		patternLayout.start();
		loggingEventsAppender.start();
		logViewForwardingContext.start();
	}

}
