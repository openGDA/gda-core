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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Logpanel extends Composite {
	
	private static final Logger logger = LoggerFactory.getLogger(Logpanel.class);
	
	
	// log server connection
	
	/**
	 * Convenience method for LogpanelView
	 */
	public String getLogServerAddress() {
		return String.format("%s:%d", getLogServerHost(), getLogServerOutPort());
	}
	
	private String logServerHost = LocalProperties.get(LogbackUtils.GDA_LOGSERVER_HOST, LogbackUtils.GDA_LOGSERVER_HOST_DEFAULT);
	public String getLogServerHost() {
		return logServerHost;
	}
	protected void setLogServerHost(String logServerHost) {
		this.logServerHost = logServerHost;
	}
	
	private Integer logServerOutPort = LocalProperties.getInt(LogbackUtils.GDA_LOGSERVER_OUT_PORT, LogbackUtils.GDA_LOGSERVER_OUT_PORT_DEFAULT);
	public Integer getLogServerOutPort() {
		return logServerOutPort;
	}
	protected void setLogServerOutPort(Integer logServerOutPort) {
		this.logServerOutPort = logServerOutPort;
	}
	
	/**
	 * Convenience method that connects using property values which are correct 
	 * for both live and dummy modes.
	 */
	protected void connectToLogServer() {
		//TODO remove:
//		String logServerHost = LocalProperties.get(LogbackUtils.GDA_LOGSERVER_HOST, LogbackUtils.GDA_LOGSERVER_HOST_DEFAULT);
//		int logServerOutPort = LocalProperties.getInt(LogbackUtils.GDA_LOGSERVER_OUT_PORT, LogbackUtils.GDA_LOGSERVER_OUT_PORT_DEFAULT);
		connectToLogServer(getLogServerHost(), getLogServerOutPort());
	}
		
	protected void connectToLogServer(String logServerHost, Integer logServerOutPort) {
		setLogServerHost(logServerHost);
		setLogServerOutPort(logServerOutPort);
		
		// setup clean logger context
		LoggerContext logpanelContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		logpanelContext.reset();
		logpanelContext.setName("Logpanel");
		this.setLogpanelContext(logpanelContext);
		
		// setup receiver to connect to log server's ServerSocketAppender
		SocketReceiver receiver = new SocketReceiver();
		receiver.setContext(logpanelContext);
		receiver.setRemoteHost(logServerHost);
		receiver.setPort(logServerOutPort);
		receiver.setReconnectionDelay(10000);
		
		// setup patternLayout using current messagePattern
		//TODO remove:
//		patternLayout = new PatternLayout();
//		patternLayout.setPattern(getMessagePattern());
//		patternLayout.setContext(logpanelContext);
		setMessagePatternCreatingPatternLayout(messagePattern);
		
		// setup appender which updates IObservableList<ILoggingEvent> input in UI thread
		Appender<ILoggingEvent> loggingEventsAppender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(final ILoggingEvent loggingEvent) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
//						input.add(loggingEvent);
						addLoggingEvent(loggingEvent); //TODO remove?
						if (!scrollLockChecked && viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
							viewer.reveal(loggingEvent);
						}
					}
				});
			}
		};
		loggingEventsAppender.setContext(logpanelContext);
		
		// register receiver and appender
		logpanelContext.register(receiver);
		logpanelContext.register(loggingEventsAppender);
		
		// add appender to root logger
		ch.qos.logback.classic.Logger rootLogger = logpanelContext.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(loggingEventsAppender);
		
		// start receiving from the log server and appending to input
		logpanelContext.start();
		patternLayout.start();
		receiver.start();
		loggingEventsAppender.start();
		
		logger.info("Receiving from log server {}", getLogServerAddress());
	}
	
	
	// Logging events and messages
	
	protected LoggerContext logpanelContext;
	public LoggerContext getLogpanelContext() {
		return logpanelContext;
	}
	public void setLogpanelContext(LoggerContext logpanelContext) {
		this.logpanelContext = logpanelContext;
	}
	
	/**
	 * http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout
	 */
	protected String messagePattern = "%d %-5level %logger - %m%n%ex";
	public String getMessagePattern() {
		return messagePattern;
	}
	public void setMessagePattern(String messagePattern) {
		this.messagePattern = messagePattern;
	}
	/**
	 * Convenience method
	 */
	public void setMessagePatternCreatingPatternLayout(String messagePattern) {
		PatternLayout patternLayout = new PatternLayout();
		patternLayout.setPattern(messagePattern);
		this.setPatternLayout(patternLayout);
	}
	
	protected PatternLayout patternLayout;
//	public PatternLayout getPatternLayout() {
//		return patternLayout;
//	}
	public void setPatternLayout(PatternLayout patternLayout) {
		this.patternLayout = patternLayout;
		patternLayout.setContext(getLogpanelContext());
		setMessagePattern(patternLayout.getPattern());
	}
	
	public String layoutMessage(ILoggingEvent loggingEvent) {
		return patternLayout.doLayout(loggingEvent).trim();
	}
	
	protected Function<ILoggingEvent, String> layoutMessage = new Function<ILoggingEvent, String>() {
		@Override
		public String apply(ILoggingEvent loggingEvent) {
			return layoutMessage(loggingEvent);
		}
	};
	
	// transforming selected subsequences of logging events to copyable text 
	
	@SuppressWarnings("unchecked")
	public List<ILoggingEvent> getSelectedLoggingEvents() { 
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		return ImmutableList.copyOf((List<ILoggingEvent>) selection.toList());
	}
	
	public List<String> getSelectedMessages() {
		return ImmutableList.copyOf(Lists.transform(getSelectedLoggingEvents(), layoutMessage));
	}
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	public static final Joiner newLineJoiner = Joiner.on(NEW_LINE);
	public static final Splitter newLineSplitter = Splitter.on(NEW_LINE); // used by LogpanelView
	
	private Joiner messageJoiner = newLineJoiner;
	public Joiner getMessageJoiner() {
		return messageJoiner;
	}
	public void setMessageJoiner(Joiner messageJoiner) {
		this.messageJoiner = messageJoiner;
	}
	
	protected static boolean appendNewLineToJoined = true;
	
	public String getSelectedMessagesJoined(Joiner messageJoiner) {
		return messageJoiner.join(getSelectedMessages()) + (appendNewLineToJoined ? NEW_LINE : "");
	}
	
	protected static boolean appendNewLineToLatest = true;
	
	public String getLatestMessage() {
		String latestMessage = layoutMessage(loggingEvents.get(loggingEvents.size()-1)).trim();
		if (appendNewLineToLatest) {
			return latestMessage + NEW_LINE;
		}
		else {
			return latestMessage;
		}
	}
	
	public String getLatestMessageFirstLine() {
		List<String> lines = newLineSplitter.splitToList(getLatestMessage());
		return lines.get(0);
	}
	
	
	/**
	 * Convenience method using current joiner
	 */
	public String getSelectedMessagesJoined() {
		return getSelectedMessagesJoined(messageJoiner);
	}
	
	//TODO public String getSelectedLoggingEventMessagesStringWithInterveningEllipses() {} // should only put ellipses between non-sequential logging events so can't use Joiner.on("\n...\n")
	
	//TODO public String getSelectedLoggingEventMessagesStringWithInterveningCounted() {}
	
	
	// copying text to clipboard
	
	/**
	 * @return true if text copied to clipboard else false
	 */
	public boolean copyToClipboard(String text) {
		if (text.length() > 0) {
			final Clipboard cb = new Clipboard(display);
			cb.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
			return true;
		}
		return false;
	}
	
	/**
	 * Convenience method
	 */
	public boolean copySelectedMessagesToClipboard(Joiner messageJoiner) {
		return copyToClipboard(getSelectedMessagesJoined(messageJoiner));
	}
	/**
	 * Convenience method using current joiner
	 */
	public boolean copySelectedMessagesToClipboard() {
		return copySelectedMessagesToClipboard(messageJoiner);
	}	
	
	
	// logging events buffering
	
	/**
	 * Maximum size to which loggingEvents is allowed to grow before eviction 
	 * of earliest events occurs.
	 */
	protected static int maxSize = 12345; //TODO uninformed guess ~ Windows XP widget possibly 10,000

	/**
	 * Collection of logging events received since connectToLogServer ran for the first time.
	 */
	private List<ILoggingEvent> loggingEvents = new LinkedList<ILoggingEvent>();
	
	/**
	 * Bridge between logging events buffer and viewer in GUI
	 */
	final IObservableList input = Properties.selfList(ILoggingEvent.class).observe(loggingEvents);	// wrap a writable list into an IObservableList: http://www.vogella.com/tutorials/EclipseDataBinding/article.html#jfacedb_viewer
	public IObservableList getInput() {
		return input;
	}
	
	protected void addLoggingEvent(ILoggingEvent loggingEvent) {
		if (input.size() == maxSize) {
			input.remove(0);
		}
		input.add(loggingEvent);
	}
	
	
	// widgets 
	
	private TableViewer viewer; // to set with input
	
	public TableViewer getViewer() {
		return viewer;
	}
	
	
	protected boolean scrollLockChecked = false;
	public void setScrollLockChecked(final boolean isChecked) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				scrollLockChecked = isChecked;
			}
		});
	}
	
	
	// LabelProvider
	
	final Display display = getDisplay();
	
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
	
	private class ILoggingEventLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
		
		Color debugForeground = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		Color errorForeground = display.getSystemColor(SWT.COLOR_WHITE);
		Color errorBackground = display.getSystemColor(SWT.COLOR_DARK_RED);
		Color infoForeground = display.getSystemColor(SWT.COLOR_BLACK);
		Color warnForeground = display.getSystemColor(SWT.COLOR_BLACK);
		Color warnBackground = display.getSystemColor(SWT.COLOR_YELLOW);
		
		@Override
		public String getColumnText(Object element, int columnIndex) {
			ILoggingEvent loggingEvent = (ILoggingEvent) element;
			return layoutMessage(loggingEvent);
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
	
	
	public class MatchingFilter extends ViewerFilter {
		private boolean isCaseInsensitive = true;
		public void setCaseInsensitive(boolean isCaseInsensitive) {
			this.isCaseInsensitive = isCaseInsensitive;
		}
		private Pattern pattern;
		/**
		 * Ensure that the value can be used for matching.
		 * @param matching
		 */
		public void setMatching(String matching) {
			String regex = ".*" + matching + ".*"; // ensure any value can be used for matching //TODO may cause problems with '|', etc.
			try {
				if (isCaseInsensitive) {
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
			String message = layoutMessage(loggingEvent);
			return pattern.matcher(message).matches();
		}
	};
	
	private MatchingFilter matchingFilter;
	
	
	public static final BiMap<Level, String> LOG_LEVELS = ImmutableBiMap.<Level, String>of(
			Level.ALL, "All", 
			Level.DEBUG, "DEBUG", 
			Level.INFO, "INFO", 
			Level.WARN, "WARN", 
			Level.ERROR, "ERROR");
	
	public static Level getLogLevel(String level) {
		return LOG_LEVELS.inverse().get(level);
	}
	
	public static String getLogLevelText(Level level) {
		return LOG_LEVELS.get(level);
	}
	
	private Level minLogLevel = Level.INFO;
	
	public Level getMinLogLevel() {
		return minLogLevel;
	}
	
	public void setMinLogLevel(Level level) {
		if (!LOG_LEVELS.keySet().contains(level)) {
			throw new IllegalArgumentException(LOG_LEVELS.get(level));
		}
		minLogLevel = level;
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}
	
	public class MinLogLevelFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ((ILoggingEvent) element).getLevel().isGreaterOrEqual(minLogLevel) ? true : false;
		}
	};
	
	private MinLogLevelFilter minLogLevelFilter;
	
	
	public Logpanel(Composite parent, int style) {
		super(parent, style);
		
		connectToLogServer();
		
		
		Label filterLabel = new Label(this, SWT.NONE);
		filterLabel.setText("Filter:");
		
		final Text filterText = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filterText.setFont(getFont());
		filterText.setMessage("... matching ...");
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent keyEvent) {
				matchingFilter.setMatching(filterText.getText());
				viewer.refresh();
			}
		});
		filterText.setFocus();
		
		viewer = new TableViewer(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(new ILoggingEventLabelProvider());
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setUseHashlookup(true); //TODO test for possible speedup and increased memory usage
		viewer.setInput(input);
		
		matchingFilter = new MatchingFilter();
		viewer.addFilter(matchingFilter);
		
		minLogLevelFilter = new MinLogLevelFilter();
		viewer.addFilter(minLogLevelFilter);
		
		
		// copy selection to X buffer when Copy command not explicitly invocation by user
		// do not copy to clipboard automatically as this can overwrite it's contents
		final Text invisibleSelectionText = new Text(this, SWT.READ_ONLY);
		// widget must be visible and not .exclude(true) to enable automatic copying to X cut buffer
		GridDataFactory.swtDefaults().span(3, 1).hint(0, 0).applyTo(invisibleSelectionText);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0){
					String selectedMessagesJoined = getSelectedMessagesJoined();
					invisibleSelectionText.setText(selectedMessagesJoined);
					invisibleSelectionText.setSelection(0,selectedMessagesJoined.length());
				}
			}
		}); 
		
		
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);
		GridDataFactory.swtDefaults().span(1, 1).applyTo(filterLabel);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(filterText);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(viewer.getControl());
		
		
		// former controls supplanted by command buttons in LogpanelView toolbar
		// methods still useful for other Composites embedding Logpanel 
		//createScrollLockCheckBox(this);
		//createClearButton(this);
		//createCopyButton(this);
	}
	
	
	// controls for Logpanel behaviour outside LogpanelView
	
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
	
	//TODO public Button createCopyButton(Composite parent) {}
	public Button createCopyButton(Composite parent) {
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
	
}
