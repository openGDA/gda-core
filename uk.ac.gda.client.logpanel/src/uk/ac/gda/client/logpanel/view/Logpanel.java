/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.logpanel.view;

import gda.configuration.properties.LocalProperties;
import gda.util.logging.LogbackUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

public class Logpanel extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(Logpanel.class);

	private String patternLayoutPattern = "%d %-5level %logger - %m%n%ex";

	private PatternLayout patternLayout; //TODO enable customisation

	protected String applyPatternLayout(ILoggingEvent loggingEvent) {
		return patternLayout.doLayout(loggingEvent).trim();
	}

	private List<ILoggingEvent> loggingEvents = new LinkedList<ILoggingEvent>();

	/**
	 * Wrap a writable list into an IObservableList: 
	 *  http://www.vogella.com/tutorials/EclipseDataBinding/article.html#jfacedb_viewer
	 */
	final IObservableList input = Properties.selfList(ILoggingEvent.class).observe(loggingEvents);

	public IObservableList getInput() {
		return input;
	}

	private TableViewer viewer; // to set with input

	boolean scrollLockChecked = false;

	public void setScrollLockChecked(final boolean isChecked) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				scrollLockChecked = isChecked;
			}
		});
	}

	final Display display = getDisplay();

	private Color debugForeground = display.getSystemColor(SWT.COLOR_DARK_GRAY);
	private Color errorForeground = display.getSystemColor(SWT.COLOR_WHITE);
	private Color errorBackground = display.getSystemColor(SWT.COLOR_DARK_RED);
	private Color infoForeground = display.getSystemColor(SWT.COLOR_BLACK);
	private Color warnForeground = display.getSystemColor(SWT.COLOR_BLACK);
	private Color warnBackground = display.getSystemColor(SWT.COLOR_YELLOW);

	private class ILoggingEventLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
			return applyPatternLayout(loggingEvent);
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
		@Override
		public Font getFont(Object element, int columnIndex) {
			return MONOSPACE;
		}
	}

	private boolean isMatchingFilterCaseInsensitive = true;

	public void setIsMatchingFilterCaseInsensitive(boolean isMatchingFilterCaseInsensitive) {
		this.isMatchingFilterCaseInsensitive = isMatchingFilterCaseInsensitive;
	}

	private class MatchingFilter extends ViewerFilter {

		private Pattern pattern;

		/**
		 * Ensure that the value can be used for matching.
		 * @param matching
		 */
		public void setMatching(String matching) {
			String regex = ".*" + matching + ".*"; // ensure any value can be used for matching
			try {
				if (isMatchingFilterCaseInsensitive) {
					pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE); // UNICODE_CASE essential
				}
				else {
					pattern = Pattern.compile(regex);
				}
			}
			catch (PatternSyntaxException e) {
				pattern = null;
			}
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (pattern == null) {
				return true;
			}
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
			String message = applyPatternLayout(loggingEvent);
			return pattern.matcher(message).matches();
		}
	};

	private MatchingFilter filter;

	public static final Font MONOSPACE = new Font(Display.getDefault(), new FontData("Monospace", 10, SWT.NORMAL));

	protected Font font = MONOSPACE;
	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public void setFont(String name, int size) {
		FontData data = new FontData(name, size, SWT.NORMAL);
		setFont(new Font(display, data));
	}

	public Logpanel(Composite parent, int style) {
		super(parent, style);

		connectToLogServer();

		final Text filterText = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filterText.setFont(getFont());
		filterText.setMessage("Matching ...");
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent keyEvent) {
				filter.setMatching(filterText.getText());
				viewer.refresh();
			}
		});
		filterText.setFocus();

		viewer = new TableViewer(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(new ILoggingEventLabelProvider());
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setUseHashlookup(true); //TODO test for possible speedup and increased memory usage
		viewer.setInput(input); 

		filter = new MatchingFilter();
		viewer.addFilter(filter);

		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(filterText);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(viewer.getControl());

		// supplanted by LogpanelView toolbar commands
		//createScrollLockCheckBox(this);
		//createClearButton(this);
	}

	/**
	 * For use of Logpanel behaviour outside LogpanelView.
	 */
	public Button createScrollLockCheckBox(Composite parent) {
		final Button scrollLockCheckBox = new Button(parent, SWT.CHECK);
		scrollLockCheckBox.setText("Scroll Lock");
		scrollLockCheckBox.setSelection(scrollLockChecked);
		scrollLockCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollLockChecked = scrollLockCheckBox.getSelection();
			}
		});
		return scrollLockCheckBox;
	}

	/**
	 * For control of Logpanel behaviour outside LogpanelView.
	 */
	public Button createClearButton(Composite parent) {
		Button clearButton = new Button(parent, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				input.clear();
			}
		});
		return clearButton;
	}

	private String logServerHost;
	public String getLogServerHost() {
		return logServerHost;
	}

	private Integer logServerOutPort;
	public Integer getLogServerOutPort() {
		return logServerOutPort;
	}

	protected void connectToLogServer() {
		String logServerHost = LocalProperties.get(LogbackUtils.GDA_LOGSERVER_HOST, LogbackUtils.GDA_LOGSERVER_HOST_DEFAULT);
		int logServerOutPort = LocalProperties.getInt(LogbackUtils.GDA_LOGSERVER_OUT_PORT, LogbackUtils.GDA_LOGSERVER_OUT_PORT_DEFAULT);
		connectToLogServer(logServerHost, logServerOutPort);
	}

	protected void connectToLogServer(String logServerHost, Integer logServerOutPort) {
		this.logServerHost = logServerHost;
		this.logServerOutPort = logServerOutPort;

		// in Logpanel context
		LoggerContext logpanelContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		logpanelContext.reset();
		logpanelContext.setName("Logpanel");

		// receive from log server ServerSocketAppender
		SocketReceiver receiver = new SocketReceiver();
		receiver.setContext(logpanelContext);
		receiver.setRemoteHost(logServerHost);
		receiver.setPort(logServerOutPort);
		receiver.setReconnectionDelay(10000);

		// and using layout
		patternLayout = new PatternLayout();
		patternLayout.setPattern(patternLayoutPattern);
		patternLayout.setContext(logpanelContext);

		// append to loggingEvents and update IObservableList in UI thread
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
		loggingEventsAppender.setContext(logpanelContext);

		logpanelContext.register(receiver);
		logpanelContext.register(loggingEventsAppender);

		ch.qos.logback.classic.Logger rootLogger = logpanelContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(loggingEventsAppender);

		receiver.start();
		patternLayout.start();
		loggingEventsAppender.start();
		logpanelContext.start();

//		logger.info("Receiving from log server {}:{}", logServerHost, logServerOutPort);
	}

}
