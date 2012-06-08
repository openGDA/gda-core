/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.Timer;

import org.eclipse.swt.SWT;

/**
 * This code is adopted from https://bugs.eclipse.org/bugs/show_bug.cgi?id=171432#c15
 *   
------- Comment  #15 From Karl Tauber  2008-06-24 09:51:39 -0400  -------

Created an attachment (id=105699) [details]
Workaround that avoids the StackOverflowError

This workaround (or hack?) avoids the StackOverflowError by periodically
monitoring and fixing the X11 error handler fields in sun.awt.X11.XlibWrapper
and sun.awt.X11.XToolkit. This is not a real fix. It just tries to avoid the
StackOverflowError, but does not avoid the concurrent changes of the X11 error
handler done in the SWT/GTK and AWT threads.

See comment #5 for details about the problem. We use this workaround
successfully in JFormDesigner.

To install the workaround, invoke initX11ErrorHandlerFix() once in the AWT
thread after invoking SWT_AWT.new_Frame(). E.g.:

if( !x11ErrorHandlerFixInstalled && "gtk".equals( SWT.getPlatform() ) ) {
  x11ErrorHandlerFixInstalled = true;
  EventQueue.invokeLater( new Runnable() {
    public void run() {
      initX11ErrorHandlerFix();
    }
  } );
}

 */
public class WorkaroundSWT_AWT_BridgeOnGTK {
	private static boolean x11ErrorHandlerFixInstalled;

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=171432
	 */
	@SuppressWarnings("unchecked")
	private static void initX11ErrorHandlerFix() {
		assert EventQueue.isDispatchThread();

		try {
			// get XlibWrapper.SetToolkitErrorHandler() and XSetErrorHandler() methods
			@SuppressWarnings("rawtypes")
			Class xlibwrapperClass = Class.forName( "sun.awt.X11.XlibWrapper" );
			final Method setToolkitErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod( "SetToolkitErrorHandler", null );
			final Method setErrorHandlerMethod = xlibwrapperClass.getDeclaredMethod( "XSetErrorHandler", new Class[] { Long.TYPE } );
			setToolkitErrorHandlerMethod.setAccessible( true );
			setErrorHandlerMethod.setAccessible( true );

			// get XToolkit.saved_error_handler field
			@SuppressWarnings("rawtypes")
			Class xtoolkitClass = Class.forName( "sun.awt.X11.XToolkit" );
			final Field savedErrorHandlerField = xtoolkitClass.getDeclaredField( "saved_error_handler" );
			savedErrorHandlerField.setAccessible( true );

			// determine the current error handler and the value of XLibWrapper.ToolkitErrorHandler
			// (XlibWrapper.SetToolkitErrorHandler() sets the X11 error handler to
			// XLibWrapper.ToolkitErrorHandler and returns the old error handler)
			final Object defaultErrorHandler = setToolkitErrorHandlerMethod.invoke( null, null );
			final Object toolkitErrorHandler = setToolkitErrorHandlerMethod.invoke( null, null );
			setErrorHandlerMethod.invoke( null, new Object[] { defaultErrorHandler } );

			// create timer that watches XToolkit.saved_error_handler whether its value is equal
			// to XLibWrapper.ToolkitErrorHandler, which indicates the start of the trouble
			Timer timer = new Timer( 200, new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					try {
						Object savedErrorHandler = savedErrorHandlerField.get( null );
						if( toolkitErrorHandler.equals( savedErrorHandler ) ) {
							// Last saved error handler in XToolkit.WITH_XERROR_HANDLER
							// is XLibWrapper.ToolkitErrorHandler, which will cause
							// the StackOverflowError when the next X11 error occurs.
							// Workaround: restore the default error handler.
							// Also update XToolkit.saved_error_handler so that
							// this is done only once.
							setErrorHandlerMethod.invoke( null, new Object[] { defaultErrorHandler } );
							savedErrorHandlerField.setLong( null, ((Long)defaultErrorHandler).longValue() );
						}
					} catch( Exception ex ) {
						ex.printStackTrace();
					}
					
				}
			} );
			timer.start();
		} catch( Exception ex ) {
			// ignore
		}
	}

	/**
	 * To install the workaround, invoke workAroundIssue() once in the AWT
	 * thread after invoking SWT_AWT.new_Frame().
	 */
	public static void workAroundIssue() {
		if (!x11ErrorHandlerFixInstalled && "gtk".equals(SWT.getPlatform())) {
			x11ErrorHandlerFixInstalled = true;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					initX11ErrorHandlerFix();
				}
			});
		}
	}
}
