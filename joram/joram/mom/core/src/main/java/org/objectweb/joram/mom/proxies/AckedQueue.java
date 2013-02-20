/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

import java.util.*;

public class AckedQueue implements java.io.Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** logger */
  public static Logger logger = Debug.getLogger(AckedQueue.class.getName());

  private Vector<ProxyMessage> list;

  public AckedQueue() {
    list = new Vector<ProxyMessage>();
  }
  
  // JORAM_PERF_BRANCH
  public int size() {
    return list.size();
  }

  public void push(ProxyMessage msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AckedQueue.push(" + msg + ')');
    
    synchronized (list) {
      list.addElement(msg);
      list.notify();
    }
  }
  
  public static final boolean GET_ARRAY = true;

  public Object get() throws InterruptedException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AckedQueue.get()");
    
    synchronized (list) { 
      while (list.size() == 0) {
        list.wait();
      }      
      // JORAM_PERF_BRANCH:
      if (GET_ARRAY) {
        ProxyMessage[] res = new ProxyMessage[list.size()];
        list.copyInto(res);
        list.clear();
        return res;
      } else {
        ProxyMessage msg = list.remove(0);
        return msg;
      }
      // JORAM_PERF_BRANCH.
    }
  }

  /* JORAM_PERF_BRANCH:
  public void ack(long ackId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AckedQueue.ack(" + ackId + ')');
    synchronized (list) {
      while (list.size() > 0) {
        ProxyMessage m = list.elementAt(0);
        if (ackId < m.getId())  return;

        // acked
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "AckedQueue acked " + m.getId());
        
        list.removeElementAt(0);
        if (current > 0) current--;
      }
    }
  }
  */

  public void reset() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AckedQueue.reset()");
  }
}
