/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */
package a3.cssi;

import fr.dyade.aaa.agent.*;

public class Token extends Notification {
  public int bounce;
  public byte[] ballast;
  public int pause;

  public Token(int bounce, int size, int pause) {
    this.bounce = bounce;
    this.ballast = new byte[size];
    this.pause = pause;
  }

  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",bounce=").append(bounce);
    output.append(",size=").append(ballast.length);
    output.append(",pause=").append(pause);
    output.append(')');
    
    return output;
  }
}
