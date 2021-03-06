/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): Tachker Nicolas (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.sub;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;

import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;


/**
 * Test the flow control mechanism with topic subscription and listener:
 *  - after reactivation of the subscription the supply of the listener's queue is resumed only
 *  if new messages arrive.
 * 
 * @see     Joram/JORAM-22
 */
public class Sub4 extends TestCase implements MessageListener {
  
  public static void main(String[] args) {
    new Sub4().run();
  }

  Object lock = new Object();
  Topic topic;
  ConnectionFactory cf;
  Connection cnx;
  Session sess1, sess2;
  User anonymous;
  String subName;
  
  static final int NB_SENT_MSG = 200;
  static final int SLEEPING = 10;
  
  public void run() {
    try {
      System.out.println("server start");
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      Thread.sleep(1000);

      cf = TcpConnectionFactory.create("localhost", 2560);

      AdminModule.connect(cf);

      // create topics
      topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      // create a user
      anonymous = User.create("anonymous", "anonymous");

      //AdminModule.disconnect();
      System.out.println("admin config ok");

      // connection for subscriber
      cnx = (Connection) cf.createConnection();
      
      sess1 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess1.createProducer(topic);
      
      sess2 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      sess2.setTopicActivationThreshold(10);
      sess2.setTopicPassivationThreshold(30);
      MessageConsumer cons = sess2.createConsumer(topic);
      subName = ((org.objectweb.joram.client.jms.MessageConsumer) cons).getTargetName();
      
      cnx.start();
      
      for (int i=0; i<NB_SENT_MSG; i++) {
        Message msg = sess1.createMessage();
        msg.setIntProperty("idx", i);
        prod.send(msg);
      }
      
      // set the listener
      cons.setMessageListener(this);
      
      synchronized (lock) {
        lock.wait(60000);
      }
      
      assertTrue("Received " + nbMsgReceived + ", should be " + NB_SENT_MSG, (nbMsgReceived == NB_SENT_MSG));
      
      cnx.close();
    } catch (Throwable exc) {
		  exc.printStackTrace();
		  error(exc);
	  } finally {
		  System.out.println("Server stop");
      AgentServer.stop();
		  endTest();
	  }
  }

  int nbMsgReceived = 0;
  
  public void onMessage(Message msg) {
    try {
     System.out.println("Recv msg#" + msg.getIntProperty("idx"));
      nbMsgReceived += 1;
      
      System.out.println("pending = " + checkPending());
      
      if (nbMsgReceived == NB_SENT_MSG) {
        synchronized (lock) {
          lock.notifyAll();
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  int pendingAckMax = 0;
  int pendingMsgMax = 0;
  
  public String checkPending() throws Exception {
    Subscription sub = anonymous.getSubscription(subName);
    int pendingMsg = 0;
    int pendingAck = 0;
    pendingMsg = sub.getMessageCount();
    pendingAck = sub.getDeliveredMessageCount();
    
    if (nbMsgReceived == 1) {
      pendingAckMax = pendingAck;
      pendingMsgMax = pendingMsg;
    }
    if (pendingAckMax > 0 && pendingMsgMax > pendingMsg && pendingMsg > 0) {
      pendingMsgMax = pendingMsg;
      assertTrue("PendingAck " + pendingAck + ", should be 31.", (pendingAck == 31));
    }
    
    return "msg=" + pendingMsg + ", ack=" + pendingAck;
  }
}
