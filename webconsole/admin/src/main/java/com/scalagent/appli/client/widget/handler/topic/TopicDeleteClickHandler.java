/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.widget.handler.topic;

import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

/**
 * @author Yohann CINTRE
 */
public class TopicDeleteClickHandler implements ClickHandler {

  private TopicListPresenter presenter;
  private TopicListRecord record;

  public TopicDeleteClickHandler(TopicListPresenter presenter, TopicListRecord record) {
    super();
    this.presenter = presenter;
    this.record = record;
  }

  public void onClick(ClickEvent event) {
    SC.confirm(Application.messages.topicWidget_confirmDelete(), new BooleanCallback() {

      @Override
      public void execute(Boolean value) {
        if (value)
          presenter.deleteTopic(record.getTopic());
      }
    });
  }

}
