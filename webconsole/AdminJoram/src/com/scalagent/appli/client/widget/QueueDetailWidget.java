/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.widget.handler.message.MessageDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.events.DrawHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;



public class QueueDetailWidget extends BaseWidget<QueueDetailPresenter> {

	boolean redrawChart = false;

	int chartWidth;

	boolean showRecieved = true;
	boolean showDelivered = true;
	boolean showSentDMQ = true;
	boolean showPending = true;

	SectionStack queueDetailStack;

	SectionStackSection buttonSection;
	VLayout vl;
	HLayout hl;
	IButton refreshButton;
	HLayout hl2;
	DetailViewer queueDetailLeft = new DetailViewer();
	DetailViewer queueDetailRight = new DetailViewer();;

	SectionStackSection listStackSection;
	ListGrid messageList;

	SectionStackSection viewSection;
	HLayout queueView;
	DetailViewer messageDetailLeft;
	DetailViewer messageDetailRight;
	VLayout queueChart;
	
	AnnotatedTimeLine chart;
	DynamicForm columnForm;
	CheckboxItem showRecievedBox;
	CheckboxItem showDeliveredBox;
	CheckboxItem showSentDMQBox;
	CheckboxItem showPendingBox;

	HashMap<String, String> etat=new HashMap<String, String>();


	public QueueDetailWidget(QueueDetailPresenter queueDetailsPresenter) {
		super(queueDetailsPresenter);
		etat.put("true", Application.baseMessages.main_true());
		etat.put("false", Application.baseMessages.main_false());
	}

	public IButton getRefreshButton() {
		return refreshButton;
	}

	@Override
	public Widget asWidget() {


		queueDetailStack = new SectionStack();
		queueDetailStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		queueDetailStack.setWidth100();
		queueDetailStack.setHeight100();


		refreshButton = new IButton();  
		refreshButton.setAutoFit(true);
		refreshButton.setIcon("refresh.gif");  
		refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
		refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		refreshButton.addClickHandler(new RefreshAllClickHandler(presenter)); 

		hl = new HLayout();
		hl.setHeight(20);
		hl.setPadding(5);
		hl.addMember(refreshButton);


		DetailViewerField nameFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NAME, Application.messages.queueWidget_nameFieldD_title());
		DetailViewerField creationDateFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_CREATIONDATE, Application.messages.queueWidget_creationDateFieldD_title());
		DetailViewerField DMQIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DMQID, Application.messages.queueWidget_DMQIdFieldD_title());
		DetailViewerField destinationIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DESTINATIONID, Application.messages.queueWidget_destinationIdFieldD_title());
		DetailViewerField nbMsgsDeliverSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, Application.messages.queueWidget_nbMsgsDeliverSinceCreationFieldD_title());
		DetailViewerField nbMsgsReceiveSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, Application.messages.queueWidget_nbMsgsRecieveSinceCreationFieldD_title());
		DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.queueWidget_nbMsgsSentToDMQSinceCreationFieldD_title());
		DetailViewerField periodFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_PERIOD, Application.messages.queueWidget_periodFieldD_title());
		DetailViewerField rightsFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_RIGHTS, Application.messages.queueWidget_RightsFieldD_title());
		DetailViewerField freeReadingFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEREADING, Application.messages.queueWidget_freeReadingFieldD_title());
		DetailViewerField freeWritingFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEWRITING, Application.messages.queueWidget_freeWritingFieldD_title());
		DetailViewerField thresholdFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_THRESHOLD, Application.messages.queueWidget_thresholdFieldD_title());
		DetailViewerField waitingRequestCountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT, Application.messages.queueWidget_waitingRequestCountFieldD_title());
		DetailViewerField pendingMessageCountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT, Application.messages.queueWidget_pendingMessageCountFieldD_title());
		DetailViewerField deliveredMessagecountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_DELIVEREDMESSAGECOUNT, Application.messages.queueWidget_deliveredMessageCountFieldD_title());
		DetailViewerField nbMaxMessFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMAXMSG, Application.messages.queueWidget_nbMaxMessFieldD_title());
		freeReadingFieldD.setValueMap(etat);
		freeWritingFieldD.setValueMap(etat);


		queueDetailLeft = new DetailViewer();
		queueDetailLeft.setMargin(2);
		queueDetailLeft.setWidth("25%");
		queueDetailLeft.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
		queueDetailLeft.setFields(nameFieldD, creationDateFieldD, DMQIdFieldD, destinationIdFieldD, nbMsgsDeliverSinceCreationFieldD, nbMsgsReceiveSinceCreationFieldD, nbMsgsSentToDMQSinceCreationFieldD, periodFieldD);

		queueDetailRight = new DetailViewer();
		queueDetailRight.setMargin(2);
		queueDetailRight.setWidth("25%");
		queueDetailRight.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
		queueDetailRight.setFields(rightsFieldD, freeReadingFieldD, freeWritingFieldD, thresholdFieldD, waitingRequestCountFieldD, pendingMessageCountFieldD, deliveredMessagecountFieldD, nbMaxMessFieldD);

		queueDetailRight.setData(new Record[] {new QueueListRecord(presenter.getQueue())});
		queueDetailLeft.setData(new Record[] {new QueueListRecord(presenter.getQueue())});


		chartWidth = (com.google.gwt.user.client.Window.getClientWidth()/2)-35;
		chart = new AnnotatedTimeLine(createTable(), createOptions(true), ""+chartWidth, "170");




		columnForm = new DynamicForm();
		columnForm.setNumCols(8);
		
		showRecievedBox = new CheckboxItem();  
		showRecievedBox.setTitle(Application.messages.common_recieved());
		showRecievedBox.setValue(true);
		showRecievedBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showRecieved = showRecievedBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showDeliveredBox = new CheckboxItem();  
		showDeliveredBox.setTitle(Application.messages.common_delivered());
		showDeliveredBox.setValue(true);
		showDeliveredBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {

				showDelivered = showDeliveredBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showSentDMQBox = new CheckboxItem();  
		showSentDMQBox.setTitle(Application.messages.common_sentDMQ());
		showSentDMQBox.setValue(true);
		showSentDMQBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showSentDMQ = showSentDMQBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showPendingBox = new CheckboxItem();  
		showPendingBox.setTitle(Application.messages.common_pending());
		showPendingBox.setValue(true);
		showPendingBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showPending = showPendingBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});


		columnForm.setFields(showRecievedBox, showDeliveredBox, showSentDMQBox, showPendingBox);


		queueChart = new VLayout();
		queueChart.setMargin(2);
		queueChart.setPadding(5);
		queueChart.setWidth("50%");
		queueChart.setHeight(175);
		queueChart.setAlign(Alignment.CENTER);
		queueChart.setAlign(VerticalAlignment.TOP);
		queueChart.setShowEdges(true);
		queueChart.setEdgeSize(1);
		queueChart.addMember(columnForm);
		queueChart.addMember(chart);
		queueChart.addDrawHandler(new DrawHandler() {
			public void onDraw(DrawEvent event) { redrawChart = true; }
		});

		hl2 = new HLayout();
		hl2.setMargin(2);
		hl2.setPadding(2);
		hl2.addMember(queueDetailLeft);
		hl2.addMember(queueDetailRight);
		hl2.addMember(queueChart);


		vl = new VLayout();
		vl.setPadding(0);
		vl.addMember(hl);
		vl.addMember(hl2);


		buttonSection = new SectionStackSection(Application.messages.queueDetailWidget_buttonSection_title());
		buttonSection.setExpanded(true);
		buttonSection.addItem(vl);

		// Liste


		messageList = new ListGrid() {

			@Override  
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

				String fieldName = this.getFieldName(colNum);  

				if (fieldName.equals("deleteField")) {

					IButton buttonDelete = new IButton();  
					buttonDelete.setAutoFit(true);
					buttonDelete.setHeight(20); 
					buttonDelete.setIcon("remove.png");  
					buttonDelete.setTitle(Application.messages.queueDetailWidget_buttonDelete_title());
					buttonDelete.setPrompt(Application.messages.queueDetailWidget_buttonDelete_prompt());
					buttonDelete.addClickHandler(new MessageDeleteClickHandler(presenter, (MessageListRecord) record));


					return buttonDelete;

				} else {  
					return null;                     
				}  	   
			}	
		};


		messageList.setRecordComponentPoolingMode("viewport");
		messageList.setAlternateRecordStyles(true);
		messageList.setShowRecordComponents(true);          
		messageList.setShowRecordComponentsByCell(true);

		ListGridField idSFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_IDS, Application.messages.queueDetailWidget_idFieldL_title());
		ListGridField deliverycountFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_DELIVERYCOUNT, Application.messages.queueDetailWidget_deliveyCountFieldL_title());
		ListGridField priorityFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_PRIORITY, Application.messages.queueDetailWidget_priorityFieldL_title());
		ListGridField typeFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_TYPE, Application.messages.queueDetailWidget_typeFieldL_title());
		ListGridField deleteField = new ListGridField("deleteField", Application.messages.queueDetailWidget_deleteFieldL_title(), 114);
		deleteField.setAlign(Alignment.CENTER);  



		messageList.setFields(idSFieldL, deliverycountFieldL, priorityFieldL, typeFieldL, deleteField);
		messageList.addRecordClickHandler(new RecordClickHandler() {
			public void onRecordClick(RecordClickEvent event) {
				messageDetailLeft.setData(new Record[]{event.getRecord()});
				messageDetailRight.setData(new Record[]{event.getRecord()});
			}
		});

		DetailViewerField idSFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_IDS, Application.messages.queueDetailWidget_idFieldD_title());
		DetailViewerField expirationFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_EXPIRATION, Application.messages.queueDetailWidget_expirationFieldD_title());
		DetailViewerField timestampFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TIMESTAMP, Application.messages.queueDetailWidget_timestampFieldD_title());
		DetailViewerField deliverycountFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_DELIVERYCOUNT, Application.messages.queueDetailWidget_deliveyCountFieldD_title());
		DetailViewerField priorityFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_PRIORITY, Application.messages.queueDetailWidget_priorityFieldD_title());
		DetailViewerField typeFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TYPE, Application.messages.queueDetailWidget_typeFieldD_title());
		DetailViewerField textFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TEXT, Application.messages.queueDetailWidget_textFieldD_title());
		DetailViewerField propertiesFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_PROPERTIES, Application.messages.queueDetailWidget_propertiesFieldD_title());


		messageDetailLeft = new DetailViewer();
		messageDetailLeft.setMargin(2);
		messageDetailLeft.setWidth("50%");
		messageDetailLeft.setEmptyMessage(Application.messages.queueDetailWidget_messageDetail_emptyMessage());
		messageDetailLeft.setFields(idSFieldD, expirationFieldD, timestampFieldD, deliverycountFieldD, priorityFieldD);

		messageDetailRight = new DetailViewer();
		messageDetailRight.setMargin(2);
		messageDetailRight.setWidth("50%");
		messageDetailRight.setEmptyMessage(Application.messages.queueDetailWidget_messageDetail_emptyMessage());
		messageDetailRight.setFields(typeFieldD, textFieldD, propertiesFieldD);


//		queueChart = new VLayout();
//		queueChart.setMargin(2);
//		queueChart.setWidth("33%");
//		queueChart.setShowEdges(true);
//		queueChart.setAlign(Alignment.CENTER);
//		queueChart.setAlign(VerticalAlignment.CENTER);

		queueView = new HLayout();
		queueView.setMargin(0);
		queueView.setPadding(2);
		queueView.addMember(messageDetailLeft);
		queueView.addMember(messageDetailRight);
//		queueView.addMember(queueChart);


		// Section stack of the queue list
		listStackSection = new SectionStackSection(Application.messages.queueDetailWidget_listSection_title());
		listStackSection.setExpanded(true);
		listStackSection.addItem(messageList);


		// Section stack of the view (details & buttons)
		viewSection = new SectionStackSection(Application.messages.queueDetailWidget_detailsSection_title());
		viewSection.setExpanded(true);
		viewSection.addItem(queueView);
		viewSection.setCanReorder(true);

		queueDetailStack.addSection(buttonSection);
		queueDetailStack.addSection(listStackSection);
		queueDetailStack.addSection(viewSection);
		queueDetailStack.setCanResizeSections(true);
		
		presenter.initList();

		return queueDetailStack;

	}

	public void setData(List<MessageWTO> data) {

		MessageListRecord[] messageListRecord = new MessageListRecord[data.size()];
		for (int i=0;i<data.size();i++) {
			messageListRecord[i] = new MessageListRecord(data.get(i));
		}

		messageList.setData(messageListRecord);
	}

	public void updateMessage(MessageWTO message) {
		MessageListRecord messageListRecords = (MessageListRecord)messageList.getRecordList().find(MessageListRecord.ATTRIBUTE_IDS, message.getIdS());
		if(messageListRecords != null)  {

			messageListRecords.setIdS(message.getIdS());
			messageListRecords.setExpiration(message.getExpiration());
			messageListRecords.setTimestamp(message.getTimestamp());
			messageListRecords.setDeliveryCount(message.getDeliveryCount());
			messageListRecords.setPriority(message.getPriority());
			messageListRecords.setText(message.getText());
			messageListRecords.setType(message.getType());

			messageListRecords.setMessage(message);
			messageList.markForRedraw();

		}

		messageDetailLeft.setData(new Record[]{messageList.getSelectedRecord()});
		messageDetailRight.setData(new Record[]{messageList.getSelectedRecord()});

	}

	public void addMessage(MessageListRecord message) {
		messageList.addData(message);
		messageList.markForRedraw();
	}

	public void removeMessage(MessageListRecord message) {
		RecordList list = messageList.getDataAsRecordList();
		MessageListRecord toRemove = (MessageListRecord)list.find(MessageListRecord.ATTRIBUTE_IDS, message.getIdS());
		if(toRemove!=null)
			messageList.removeData(toRemove);
		messageList.markForRedraw();
	}

	public void updateQueue() {
		queueDetailLeft.setData(new Record[] {new QueueListRecord(presenter.getQueue())});
		queueDetailRight.setData(new Record[] {new QueueListRecord(presenter.getQueue())});
	}

	public void redrawChart(boolean reuseChart) {
		if(redrawChart) {
			chart.draw(createTable(), createOptions(reuseChart));
		}
	}

	private Options createOptions(boolean reuseChart) {
		Options options = Options.create();
		options.setDisplayAnnotations(false);
		options.setDisplayAnnotationsFilter(false);
		options.setDisplayZoomButtons(true);
		options.setDisplayRangeSelector(false);
		options.setAllowRedraw(reuseChart);
		options.setDateFormat("dd MMM HH:mm:ss");
		options.setFill(5);
		options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
		options.setWindowMode(WindowMode.TRANSPARENT);

		return options;
	}

	private AbstractDataTable createTable() {
		DataTable data = DataTable.create();


		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showRecieved)	data.addColumn(ColumnType.NUMBER, Application.messages.common_recieved()); 
		if(showDelivered)	data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered()); 
		if(showSentDMQ)	data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ()); 
		if(showPending)	data.addColumn(ColumnType.NUMBER, Application.messages.common_pending()); 

		SortedMap<Date, int[]> history = presenter.getQueueHistory();
		if(history != null) {
			data.addRows(history.size());

			int i=0;
			for(Date d : history.keySet()) {
				if(d!=null) {
					int j=1;
					data.setValue(i, 0, d);
					if(showRecieved) { data.setValue(i, j, history.get(d)[0]); j++; }
					if(showDelivered){ data.setValue(i, j, history.get(d)[1]); j++; }
					if(showSentDMQ) { data.setValue(i, j, history.get(d)[2]); j++; }
					if(showPending) { data.setValue(i, j, history.get(d)[3]); j++; }
					i++;
					j=1;
				}
			}
		}
		
		return data;
	}

	public void stopChart() {
		redrawChart=false;
	}

	private void enableDisableCheckbox() {
		if(!showDelivered && !showSentDMQ && !showPending) {
			showRecievedBox.disable();
		}
		else if(!showRecieved && !showSentDMQ && !showPending) {
			showDeliveredBox.disable();
		}
		else if(!showRecieved && !showDelivered && !showPending){
			showSentDMQBox.disable();
		}
		else if(!showRecieved && !showDelivered && !showSentDMQ) {
			showPendingBox.disable();
		}
		else {
			showRecievedBox.enable();
			showDeliveredBox.enable();
			showSentDMQBox.enable();
			showPendingBox.enable();
		}
	}
}