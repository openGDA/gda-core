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

package uk.ac.gda.richbeans.xml.string;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringInput implements IStorageEditorInput {

    private IStorage storage;
    
    public StringInput(IStorage storage) {this.storage = storage;}
    
    @Override
    public boolean exists() {return true;}
    @Override
    public ImageDescriptor getImageDescriptor() {return null;}
    @Override
    public String getName() {
       return storage.getName();
    }
    @Override
    public IPersistableElement getPersistable() {return null;}
    @Override
    public IStorage getStorage() {
       return storage;
    }
    @Override
    public String getToolTipText() {
       return "String-based file: " + storage.getName();
    }
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
      return null;
    }

}
