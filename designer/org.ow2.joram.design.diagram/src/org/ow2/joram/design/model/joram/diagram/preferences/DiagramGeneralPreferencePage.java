/*
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 * 
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 * 
 */
package org.ow2.joram.design.model.joram.diagram.preferences;

import org.eclipse.gmf.runtime.diagram.ui.preferences.DiagramsPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.ow2.joram.design.model.joram.diagram.part.JoramDiagramEditorPlugin;

/**
 * @generated
 */
public class DiagramGeneralPreferencePage extends DiagramsPreferencePage {

  public static final String PREF_JORAM_EXTENSION_FILE = "JoramExtensionFilePref";

  private FileFieldEditor joramExtensionFile;

  /**
   * @generated
   */
  public DiagramGeneralPreferencePage() {
    setPreferenceStore(JoramDiagramEditorPlugin.getInstance().getPreferenceStore());
  }

  /**
   * @generated NOT
   */
  protected void addFields(Composite parent) {
    joramExtensionFile = new FileFieldEditor(PREF_JORAM_EXTENSION_FILE, "Joram extension file", parent);
    addField(joramExtensionFile);
    super.addFields(parent);
  }
}
