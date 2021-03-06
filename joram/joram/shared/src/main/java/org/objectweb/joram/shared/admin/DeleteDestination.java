/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>DeleteDestination</code> instance requests the deletion of
 * a given destination.
 */
public class DeleteDestination extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the destination to delete. */
  private String id;

  /**
   * Constructs a <code>DeleteDestination</code> instance.
   *
   * @param id  The identifier of the destination to delete.
   */
  public DeleteDestination(String id) {
    this.id = id;
  }
  
  public DeleteDestination() { }

  /** Returns the identifier of the destination to delete. */
  public String getId() {
    return id;
  }
  
  protected int getClassId() {
    return DELETE_DESTINATION;
  }
  
  public void readFrom(InputStream is) throws IOException {
    id = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(id, os);
  }
}
