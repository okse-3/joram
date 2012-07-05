/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.mom.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.objectweb.joram.mom.dest.AdminTopicMBean;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.DestinationMBean;
import org.objectweb.joram.mom.messages.MessageView;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.objectweb.joram.shared.DestinationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.ow2.joram.shell.ShellDisplay;

import fr.dyade.aaa.agent.AgentMBean;
import fr.dyade.aaa.agent.AgentServer;

public class MOMCommandsImpl implements MOMCommands {
  
  public static final String NAMESPACE = "joram:mom";
  private static final int TIMEOUT = 1000;
//  private static final int DEFAULT_SID = 0;
 
  private BundleContext bundleContext;
  private ServiceTracker destinationTracker;
  private ServiceTracker queueTracker;
  private ServiceTracker topicTracker;
  private ServiceTracker userTracker;
  private ServiceTracker adminTracker;
  private ServiceTracker clientSubTracker;

  public MOMCommandsImpl(BundleContext context) {
    this.bundleContext = context;
    this.destinationTracker = new ServiceTracker
                (bundleContext, DestinationMBean.class, null);
    this.queueTracker = new ServiceTracker
                (bundleContext, QueueMBean.class,       null);
    this.topicTracker = new ServiceTracker
                (bundleContext, TopicMBean.class,       null);
    this.userTracker = new ServiceTracker
                (bundleContext, UserAgentMBean.class,   null);
    this.adminTracker = new ServiceTracker
                (bundleContext, AdminTopicMBean.class,  null);
    this.clientSubTracker = new ServiceTracker
                (bundleContext, ClientSubscriptionMBean.class,  null);

    destinationTracker.open();
    queueTracker.open();
    topicTracker.open();
    userTracker.open();
    adminTracker.open();
    clientSubTracker.open();
  }
  
  /**
   * Print the descrption of the given command
   * @param command Name of the command to describe
   */
  private static void help(String command) {
    StringBuffer buf = new StringBuffer();
    String fullCommand = "["+NAMESPACE+":]"+command;
    buf.append("Usage: ").append(fullCommand).append(" ");
    if(command.equals("list")) {
      buf.append("<category> [username]");
      buf.append("\n\tPossible categories: destination, topic, queue, user, subscription");
      buf.append("\n\tNB: For the subscription category, you must provide the user name.");     
    } else if(command.equals("create")) {
      buf.append("<topic|queue> <name> [option...]\n");
      buf.append("Options: -sid <server id>\tSpecifies on which server the destination is to be created\n");
      buf.append("                         \tDefault: This server\n");
      buf.append("         -ext <extension>\tSpecifies which extension class to instanciate\n");
      buf.append("                         \tDefault: None");
    } else if(command.equals("queueLoad")) {
      buf.append("<queueName>");
    } else if(command.equals("subscriptionLoad")) {
      buf.append("<userName> <subscriptionName>");
    } else if(command.equals("delete")) {
      buf.append("<topic|queue|user> <name>");
    } else if(command.equals("addUser")) {
      buf.append("[name]");
    } else if(command.equals("info")) {
      buf.append("<queue|topic> <name>");
    } else if(command.equals("lsMsg")) {
      buf.append("<queue> [[first msg idx]:[last msg idx]]");
    } else {
      System.err.println("Unknown command: "+command);
      return;
    }
    System.out.println(buf.toString());
  }

  public void list(String[] args) {
    String category = args[0];
    if(category.equals("destination")
    || category.equals("queue")
    || category.equals("topic")) {
      listDestination(category);
    } else if(category.equals("user")) {
      listUser();
    } else if (category.equals("subscription") && args.length==2) {
      listSubscription(args[1]);
    } else {
      help("list");
    }
  }
  
  private void listDestination(String category) {
    Object[] obj;
    
    if(category.equals("destination")) {
      obj = destinationTracker.getServices();
    } else if(category.equals("queue")) {
      obj = queueTracker.getServices();
    } else {
      obj = topicTracker.getServices();
    }
    
    if(obj == null || obj.length ==0) {
      System.err.println("Error: There is no "+category);
      return;
    }
    
    HashMap<String, DestinationMBean> dests = new HashMap<String, DestinationMBean>();
    for(Object o : obj) {
      DestinationMBean d = (DestinationMBean) o;
      dests.put(d.getDestinationId(), d);
    }

    String[][] table = new String[dests.size()+1][];
    table[0] = new String[] {"Id","Name","Type","Creation Date","Nb Rec Msg","Nb Del Msg","Read","Write"};
    int i = 1;
    for(DestinationMBean d : dests.values()) {
      String type = "NA";
      if(d instanceof TopicMBean)
        type="Topic";
      else if(d instanceof QueueMBean)
        type="Queue";
      table[i++] = new String[] { d.getDestinationId(),
                                d.getName(),
                                type,
                                d.getCreationDate(),
                                Long.toString(d.getNbMsgsReceiveSinceCreation()),
                                Long.toString(d.getNbMsgsDeliverSinceCreation()),
                                (d.isFreeReading()?"Yes":"No"),
                                (d.isFreeWriting()?"Yes":"No")};
    }
    int n = dests.size();
    if(n < 2)
      System.out.println("There is " + dests.size() + " "+category+".");
    else
      System.out.println("There are " + dests.size() + " "+category+"s.");
    System.out.println();
    ShellDisplay.displayTable(table, true);
  }
  
  private void listUser() {
    Object[] objs = userTracker.getServices();
    if(objs == null || objs.length ==0) {
      System.err.println("Error: There is no user.");
      return;
    }
    HashMap<String,UserAgentMBean> users = new HashMap<String, UserAgentMBean>();
    for(int i = 0; i < objs.length; i++) {
      UserAgentMBean u = (UserAgentMBean) objs[i];
      users.put(u.getAgentId(), u);
    }
    
    String[][] table = new String[users.size()+1][];
    table[0] = new String[]{"User Id","Name"};
    int i = 1;
    for(UserAgentMBean u : users.values()) {
      table[i++] = new String[] { u.getAgentId(),
                                  u.getName()};
    }
    ShellDisplay.displayTable(table, true);
  }

  private void listSubscription(String userName) {
    //Step 1: retrieve the user
    UserAgentMBean user = null;
    Object[] objs = userTracker.getServices();
    if(objs==null) {
      System.err.println("Error: No user found.");
      return;
    }
    for(Object o : objs) {
      UserAgentMBean u = (UserAgentMBean) o;
      if(u.getName().equals(userName)) {
        user = u;
        break;
      }
    }
    if(user == null) {
      System.err.println("Error: The user "+userName+" does not exist.");
      return;
    }
    //Step 2: retrieve the user's subscriptions
    String[] names = user.getSubscriptionNames();
    HashSet<ClientSubscriptionMBean> subs =
        new HashSet<ClientSubscriptionMBean>();
    Object[] clients = clientSubTracker.getServices();
    if(clients==null) {
      System.err.println("Error: No subscription found.");
      return;
    }
    for(Object o : clients) {
      ClientSubscriptionMBean s = (ClientSubscriptionMBean) o;
      for(String n : names)
        if(n.equals(s.getName())) {
          subs.add(s);
          break;
        }
    }
    if(subs.size()==0) {
      System.err.println("Error: The user "+userName+" has no subscription.");
      return;
    }
    
    //Step 3: Display
    String[][] table = new String[subs.size()+1][8];
    table[0] = new String[]{"Name",
                            "TopicIdAsString",
                            "PendingMessageCount",
                            "Delivered Message Count",
                            "Selector",
                            "NbMsgMax",
                            "NbMsgDeliveredSinceCreation",
                            "NbMsgSendToDMQSinceCreation"};
    int i=1;
    for(ClientSubscriptionMBean sub : subs) {
      table[i] = new String[] {
        sub.getName(),
        sub.getTopicIdAsString(),
        String.valueOf(sub.getPendingMessageCount()),
        String.valueOf(sub.getDeliveredMessageCount()),
        sub.getSelector()!=null?"Oui":"Non",
        String.valueOf(sub.getNbMaxMsg()),
        String.valueOf(sub.getNbMsgsDeliveredSinceCreation()),
        String.valueOf(sub.getNbMsgsSentToDMQSinceCreation())
      };
    }
    ShellDisplay.displayTable(table, true);
  }

  public void create(String[] args) {
    if(args == null || args.length<2)  {
      help("create");
      return;
    }
    byte type = 0;
    String name= null;
    int sid = AgentServer.getServerId();
    String ext = null;
    
    if(args[0].equals("topic")) {
      type=DestinationConstants.TOPIC_TYPE;
    }  else if(args[0].equals("queue")) {
      type=DestinationConstants.QUEUE_TYPE;
    } else {
      help("create");
      return;
    }
    
    name=args[1];
   
    for(int i = 2; i < args.length; i++) {
      if(args[i].equals("-sid") && args.length>i+1) {
        sid=Integer.parseInt(args[++i]);
      } else if(args[i].equals("ext") && args.length>i+1) {
        ext = args[++i];
      } else {
        help("create");
        return;
     }
    }
    
    AdminTopicMBean admin;
    try {
      admin = (AdminTopicMBean) adminTracker.waitForService(TIMEOUT);
      if(admin==null) {
        System.err.println("Error: AdminTopic not found.");
        return;
      }
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      return;
    }
    // TODO: checks whether the destination has been properly created
    switch(type) {
      case DestinationConstants.QUEUE_TYPE:
        if(ext==null)
          admin.createQueue(name,sid);
        else
          admin.createQueue(name,ext,sid);
        System.out.println(
            "Queue "+name+" created on server "+sid+
            (ext==null?".":" with the class "+ext+"."));
        break;
      case DestinationConstants.TOPIC_TYPE:
        if(ext==null)
          admin.createTopic(name,sid);
        else
          admin.createTopic(name,ext,sid);
        System.out.println(
            "Topic "+name+" created on server "+sid+
            (ext==null?".":" with the class "+ext+"."));
        break;
    }
  }
  
  public void delete(String[] args) {
    if(args.length != 2) {
      help("delete");
      return;
    }
    String category = args[0];
    ServiceTracker tracker;
    if(category.equalsIgnoreCase("queue")) {
      tracker = queueTracker;
    } else if(category.equalsIgnoreCase("topic")) {
      tracker = topicTracker;
    } else if(category.equalsIgnoreCase("user")) {
      tracker = userTracker;
    } else {
      System.err.println("Error: Unknwon category.");
      return;
    }
    Object[] objs = tracker.getServices();
    if(objs==null) {
      System.err.println("Error: No "+category+" found.");
      return;
    }
    AgentMBean a;
    for(int i = 0; i < objs.length; i++) {
      a = (AgentMBean) objs[i];
      if(a.getName().equals(args[1])) {
        a.delete();
        return;
      }
    }
  }
  
  public void addUser(String[] args) {
    String userName = null;
    Scanner s = new Scanner(System.in);
    if(args.length==0) {
      System.out.print("User name: "); System.out.flush();
      userName = s.nextLine();
    } else if(args.length==1) {
      userName = args[0];
    } else {
      help("addUser");
      return;
    }
    if(!userName.matches("[A-Za-z][A-Za-z0-9]{2,}?")) {
      System.out.println("The user name must begin with a letter and contain at least 3 alhpa-numeric caracters.");
      return;
    }
    
    System.out.print("Password: "); System.out.flush();
    String pwd = s.nextLine();
    if(userName.length()<5) {
      System.out.println("The password must be at least 6 caracters long.");
      return;
    }
   
    AdminTopicMBean admin;
    try {
      admin = (AdminTopicMBean) adminTracker.waitForService(TIMEOUT);
      if(admin==null) {
        System.err.println("Error: AdminTopic not found.");
        return;
      }      
      //TODO: No info about the result of the creation... Now assuming it works.
      admin.createUser(userName, pwd);
      System.out.println("User succesfully created.");
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      return;
    } catch (Exception e) {
      System.err.println("Error: "+e.getMessage());
      e.printStackTrace();
      return;
    }
  }
  
  public void queueLoad(String[] args) {
    if(args.length!=1) {
      help("queueLoad");
      return;
    }
    
    String name = args[0];
    QueueMBean queue = findQueue(name);

    if(queue!=null) {
      int c = queue.getPendingMessageCount();
      System.out.println("Pending count of \""+name+"\" : "+c);
    } else {
      System.err.println("There is no queue with the name \""+name+"\".");
    }
  }
  
  //TODO recode this when service registration fixed (object name's properties registered)
  private QueueMBean findQueue(String name) {
    Object[] objs = queueTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      QueueMBean q = (QueueMBean) o;
      if(q.getName().equals(name)) {
        return q;
      }
    }
    return null;
  }

  private TopicMBean findTopic(String name) {
    Object[] objs = topicTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      TopicMBean t = (TopicMBean) o;
      if(t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }
  
  public void subscriptionLoad(String[] args) {
    if(args.length != 2) {
      help("subscriptionLoad");
      return;
    }
    //TODO not used yet
    String userName = args[0];
    String subName = args[1];
    ClientSubscriptionMBean sub =
        findClientSubscription(userName, subName);
    if(sub == null) {
      System.err.println("Error: There is no subscription of "+userName+" to "+subName);
    } else {
      System.out.println("Pending count of \""+subName+"\" ("+userName+") : "+sub.getPendingMessageCount());
    }
  }
  
  private ClientSubscriptionMBean findClientSubscription(String userName, String subName) {
    Object[] objs = clientSubTracker.getServices();
    if(objs==null)
      return null;
    for(Object o : objs) {
      ClientSubscriptionMBean c = (ClientSubscriptionMBean)o;
      if(c.getName().equals(subName))
        return c;
    }
    return null;
  }
  
  public void info(String[] args) {
    if(args.length!=2) {
      help("info");
      return;
    }
    String category = args[0];
    String destName = args[1];
    if(category.equals("topic")) {
      infoTopic(destName);
    } else if(category.equals("queue")) {
      infoQueue(destName);
    } else {
      System.err.println("Error: Unknown category.");
      help("info");
      return;     
    }
  }

  private void infoTopic(String name) {
    TopicMBean dest = findTopic(name);
    if(dest==null) {
      System.err.println("Error: Topic \""+name+"\" not found.");
      return;
    }
    System.out.println("Topic name       : "+dest.getName());
    System.out.println("Destination ID   : "+dest.getDestinationId());
    System.out.println("Creation date    : "+dest.getCreationDate());
    System.out.println("Free reading     : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing     : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Message sent     : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Message received : "+dest.getNbMsgsReceiveSinceCreation());
    System.out.println("Nb of subscribers: "+dest.getNumberOfSubscribers());
}

  private void infoQueue(String name) {
    QueueMBean dest = findQueue(name);
    if(dest==null) {
      System.err.println("Error: Queue \""+name+"\" not found.");
      return;
    }
    System.out.println("Topic name        : "+dest.getName());
    System.out.println("Destination ID    : "+dest.getDestinationId());
    System.out.println("Creation date     : "+dest.getCreationDate());
    System.out.println("Free reading      : "+(dest.isFreeReading()?"Yes":"No"));
    System.out.println("Free writing      : "+(dest.isFreeWriting()?"Yes":"No"));
    System.out.println("Pending messages  : "+dest.getPendingMessageCount());
    System.out.println("Messages sent     : "+dest.getNbMsgsDeliverSinceCreation());
    System.out.println("Messages received : "+dest.getNbMsgsReceiveSinceCreation());
  }

  public void lsMsg(String[] args) {
    if(args.length < 1 || args.length > 2) {
      help("lsMsg");
      return;
    }
    QueueMBean queue = findQueue(args[0]);
    if(queue == null) {
      System.err.println("Error: Could not find a queue with the name \""+args[0]+"\"");
      return;
    }
    List<MessageView> msgs = queue.getMessagesView();
    if(args.length==2) {
      if(args[1].matches("\\d*:\\d*")) {
        String[] parts = args[1].split(":",2);
        int start = parts[0].length()!=0?Integer.parseInt(parts[0]):0;
        int end = parts[1].length()!=0?Integer.parseInt(parts[1]):msgs.size()-1;
        msgs=getMessageRange(msgs, start, end);
      } else {
        System.err.println("Error: Incorrect range format. Must be [start]:[end].");
        return;
      }
    }
    String[][] table = new String[msgs.size()+1][];
    table[0] =
        new String[]{"Msg ID","Type","Creation date","Text","Expiration Date",
        "Priority"};
    //TODO: The order attribute (in joram.mom.messages.Message, visible via JMXMessageWrapper) unretrievable
    int i = 1;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss dd-MMM-yyyy");
    for(MessageView msg : msgs) {
      String type = "UNKNOWN";
      switch (msg.getType()) {
        case org.objectweb.joram.shared.messages.Message.SIMPLE:
          type="SIMPLE"; break;
        case org.objectweb.joram.shared.messages.Message.TEXT:
          type="TEXT"; break;
        case org.objectweb.joram.shared.messages.Message.OBJECT:
          type="OBJECT"; break;
        case org.objectweb.joram.shared.messages.Message.MAP:
          type="MAP"; break;
        case org.objectweb.joram.shared.messages.Message.STREAM:
          type="STREAM"; break;
        case org.objectweb.joram.shared.messages.Message.BYTES:
          type="BYTES"; break;
        case org.objectweb.joram.shared.messages.Message.ADMIN:
          type="ADMIN"; break;
        default:
          break;
      }
      String date = msg.getExpiration()!=0?
          sdf.format(new Date(msg.getExpiration())):
          "-";
      table[i++] = new String[] {
          msg.getId(),
          type,
          sdf.format(new Date(msg.getTimestamp())),
          type=="TEXT"?msg.getText():"N/A",
          date,
          Integer.toString(msg.getPriority())
      };
    }
    ShellDisplay.displayTable(table, true);
  }
  
  private List<MessageView> getMessageRange(List<MessageView> msgs, int start, int end) {
    if(start<0 || end > msgs.size() || start > end)
      return msgs;
    List<MessageView> res = new ArrayList<MessageView>();
    for(int i = start; i<=end; i++)
      res.add(msgs.get(i));
    return res;
  }
  
  public static void main(String[] args) {
    help("list");
    help("create");
  }
}