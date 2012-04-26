/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.partlistener;

import java.lang.reflect.Field;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.menus.CommandContributionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Part listener to add force text on the view toolbar - this is done by changing the "FORCE_TEXT" mode on the toolbar
 * menu.
 */
public class MenuDisplayPartListener implements IPartListener {
	private static final Logger logger = LoggerFactory.getLogger(MenuDisplayPartListener.class);

	@Override
	public void partActivated(IWorkbenchPart part) {
		logger.info("Part partActivated:{}", part.getTitle());
		updateViews(part);
	}

	public void updateViews(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			IViewPart view = (IViewPart) part;
			IContributionItem[] items = view.getViewSite().getActionBars().getToolBarManager().getItems();
			for (IContributionItem iContributionItem : items) {
				if (iContributionItem instanceof CommandContributionItem) {
					// As per https://bugs.eclipse.org/bugs/show_bug.cgi?id=256340 the CommandContributionItem
					// doesn't expose the setMode() method. Therefore, setting the mode using reflection.
					CommandContributionItem commandContributionItem = (CommandContributionItem) iContributionItem;
					commandContributionItem.getData().mode = CommandContributionItem.MODE_FORCE_TEXT;
					commandContributionItem.getId();
					Class<? extends CommandContributionItem> class1 = commandContributionItem.getClass();
					try {
						Field field = class1.getDeclaredField("mode");
						field.setAccessible(true);
						field.set(commandContributionItem, new Integer(1));
					} catch (SecurityException e) {
						logger.error("SecurityException - Problem setting mode", e);
					} catch (IllegalArgumentException e) {
						logger.error("IllegalArgumentException - Problem setting mode", e);
					} catch (IllegalAccessException e) {
						logger.error("IllegalAccessException - Problem setting mode", e);
					} catch (NoSuchFieldException e) {
						logger.error("NoSuchFieldException - Problem setting mode", e);
					}
					commandContributionItem.update();
				}
				if (iContributionItem instanceof ActionContributionItem) {
					ActionContributionItem commandContributionItem = (ActionContributionItem) iContributionItem;
					commandContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
				}
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// Do nothing
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// Do nothing
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// Do nothing
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		updateViews(part);
	}
}
