/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * A <code>SessCreateTDReply</code> is used by a JMS proxy for replying
 * to a <code>SessCreate&lt;TQ/TT&gt;Request</code>.
 */
public final class SessCreateTDReply extends AbstractReply {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The string identifier of the temporary destination agent. */
  private String agentId;

  /** Sets the destination identifier. */
  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }

  /** Returns the temporary destination's agent identifier. */
  public String getAgentId() {
    return agentId;
  }

  protected int getClassId() {
    return SESS_CREATE_TDREPLY;
  }

  /**
   * Constructs a <code>SessCreateTDReply</code> instance.
   *
   * @param request  The replied request.
   * @param agentId  String identifier of the destination agent.
   */
  public SessCreateTDReply(AbstractRequest request, String agentId) {
    super(request.getRequestId());
    this.agentId = agentId;
  }

  /**
   * Constructs a <code>SessCreateTDReply</code> instance.
   */
  public SessCreateTDReply() {}

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputXStream os) throws IOException {
    super.writeTo(os);
    os.writeString(agentId);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    agentId = is.readString();
  }
}
