/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 */
package org.ow2.joram.jmxconnector.shared;

import java.io.Serializable;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * Class used to send Notification from server to client.
 */
public class NotificationDesc implements Serializable {
  public ObjectName name;
  public Notification not;
  
  /**
   * Initializes a new <code>NotificationDesc</code> object
   * @param name
   * @param not
   */
  public NotificationDesc(ObjectName name, Notification not) {
    this.name = name;
    this.not = not;
  }
}
