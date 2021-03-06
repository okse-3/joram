/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.stream.Properties;

/**
 * A notification sent in response of a {@link PingNot}.
 * Should contain the metrics about current behavior of the remote destination.
 */
public class PongNot extends Notification {
  private static final long serialVersionUID = 1L;

  Properties stats;
  
  public PongNot(Properties stats) {
    this.stats = stats;
  }
  
  public Object get(String name) {
    if (stats == null)
      return null;
    return stats.get(name);
  }
}
