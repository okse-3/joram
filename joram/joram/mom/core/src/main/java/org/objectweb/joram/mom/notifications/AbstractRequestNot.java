/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * An <code>AbstractRequest</code> is a request sent by a client agent to
 * a destination agent.
 */
public abstract class AbstractRequestNot extends AbstractNotification {
  /**
   * The <code>requestId</code> field allows a client to identify the request
   * sent to a destination.
   * <p>
   * This field is for the client use only and might not be set.
   */
  private int requestId = -1;

  /**
   * Constructs an <code>AbstractRequest</code>.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Identifies the request.
   */
  public AbstractRequestNot(int clientContext, int requestId) {
    super(clientContext);
    this.requestId = requestId;
  }

  /**
   * Constructs an <code>AbstractRequest</code>.
   */
  public AbstractRequestNot() {}


  /** Returns the request identifier. */
  public int getRequestId() {
    return requestId;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output	buffer to fill in
   * @return		<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",requestId=").append(requestId);
    output.append(')');

    return output;
  }
  
  public int getEncodedSize() throws Exception {
    return super.getEncodedSize() + INT_ENCODED_SIZE;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encode32(requestId);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    requestId = decoder.decode32();
  }
  
}
