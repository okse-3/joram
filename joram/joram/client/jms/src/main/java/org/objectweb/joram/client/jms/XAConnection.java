/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.jms;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

/**
 * Connection used within global transactions; an instance of this class
 * acts as a resource manager.
 * <p>
 * The XAConnection class extends the capability of Connection by providing an
 * XASession. This class offers support to transactional environments. Client
 * programs are strongly encouraged to use the transactional support available
 * in their environment, rather than use these XA interfaces directly.
 */
public class XAConnection extends Connection 
    implements javax.jms.XAConnection {

  /** Resource manager instance. */
  private XAResourceMngr rm;

  /**
   * Creates a <code>XAConnection</code> instance.
   */
  public XAConnection() {
    super();
    rm = new XAResourceMngr(this);
  }

  /** 
   * Creates a non-XA session.
   * 
   * @param transacted  indicates whether the session is transacted.
   * @param acknowledgeMode indicates whether the consumer or the client will acknowledge any messages
   *                        it receives; ignored if the session is transacted. Legal values are
   *                        Session.AUTO_ACKNOWLEDGE, Session.CLIENT_ACKNOWLEDGE, and Session.DUPS_OK_ACKNOWLEDGE.
   * @return A newly created session.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException           In case of an invalid acknowledge mode.
   */
  public javax.jms.Session
         createSession(boolean transacted, int acknowledgeMode)
         throws JMSException {
    return super.createSession(transacted, acknowledgeMode);
  }

  /** 
   * Creates a XA session.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.XASession createXASession() throws JMSException {
    checkClosed();
    Session s = new Session(this, true, 0, getRequestMultiplexer());
    XASession xas = new XASession(this, s, rm);
    addSession(s);
    return xas;
  }

  /**
   * return XAResourceMngr of this connection.
   * see connector
   */
  public XAResourceMngr getXAResourceMngr() {
    return rm;
  }
}
