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
import com.scalagent.appli.client.presenter.UserListPresenter;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.handler.user.UserDetailsClickHandler;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.client.widget.record.UserListRecord;
import com.scalagent.appli.shared.UserWTO;
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


public class UserListWidget extends BaseWidget<UserListPresenter> {

	int chartWidth;
	boolean redrawChart = false;

	boolean showSentDMQ = true;
	boolean showSubCount = true;


	SectionStack userStack;

	SectionStackSection buttonSection;
	HLayout hl;
	IButton refreshButton;

	SectionStackSection listStackSection;
	ListGrid userList;

	SectionStackSection viewSection;
	HLayout	userView;
	DetailViewer userDetail;
	VLayout userChart;
	AnnotatedTimeLine chart;

	DynamicForm columnForm;
	CheckboxItem showSentDMQBox;
	CheckboxItem showSubCountBox;

	HashMap<String, String> etat=new HashMap<String, String>();


	public UserListWidget(UserListPresenter userPresenter) {
		super(userPresenter);

		etat.put("true", Application.baseMessages.main_true());
		etat.put("false", Application.baseMessages.main_false());

	}

	public IButton getRefreshButton() {
		return refreshButton;
	}

	public ListGrid getUserList() {
		return userList;
	}

	@Override
	public Widget asWidget() {

		userStack = new SectionStack();
		userStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		userStack.setWidth100();
		userStack.setHeight100();

		refreshButton = new IButton();  
		refreshButton.setAutoFit(true);
		refreshButton.setIcon("refresh.gif");  
		refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
		refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		refreshButton.addClickHandler(new RefreshAllClickHandler(presenter)); 

		hl = new HLayout();
		hl.setHeight(22);
		hl.setPadding(5);
		hl.addMember(refreshButton);

		buttonSection = new SectionStackSection(Application.messages.userWidget_actionsSection_title());
		buttonSection.setExpanded(true);
		buttonSection.addItem(hl);

		// Liste
		userList = new ListGrid() {

			@Override  
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

				String fieldName = this.getFieldName(colNum);  
				if (fieldName.equals("browse")) { 

					IButton buttonBrowse = new IButton();  
					buttonBrowse.setAutoFit(true);
					buttonBrowse.setHeight(20);
					buttonBrowse.setIconSize(13);
					buttonBrowse.setIcon("view_right_p.png");  
					buttonBrowse.setTitle(Application.messages.queueWidget_buttonBrowse_title());
					buttonBrowse.setPrompt(Application.messages.queueWidget_buttonBrowse_prompt());
					buttonBrowse.addClickHandler(new UserDetailsClickHandler(presenter, (UserListRecord) record));

					return buttonBrowse;

				}  else {  
					return null;                     
				}  	   
			}	
		};

		userList.setRecordComponentPoolingMode("viewport");
		userList.setAlternateRecordStyles(true);
		userList.setShowRecordComponents(true);          
		userList.setShowRecordComponentsByCell(true);

		ListGridField nameFieldL = new ListGridField(UserListRecord.ATTRIBUTE_NAME, Application.messages.userWidget_nameFieldL_title());		
		ListGridField periodFieldL = new ListGridField(UserListRecord.ATTRIBUTE_PERIOD, Application.messages.userWidget_periodFieldL_title());
		ListGridField nbMsgsSentToDMQSinceCreationFieldL = new ListGridField(UserListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.userWidget_msgsSentFieldL_title());
		ListGridField subscriptionNamesFieldL = new ListGridField(UserListRecord.ATTRIBUTE_SUBSCRIPTIONNAMES, Application.messages.userWidget_subscriptionFieldL_title());		
		ListGridField browseFieldL = new ListGridField("browse", "Browse");

		userList.setFields(
				nameFieldL, 
				periodFieldL, 
				nbMsgsSentToDMQSinceCreationFieldL, 
				subscriptionNamesFieldL,
				browseFieldL);

		userList.addRecordClickHandler(new RecordClickHandler() {

			@Override
			public void onRecordClick(RecordClickEvent event) {
				userDetail.setData(new Record[]{event.getRecord()});
				redrawChart(true);
			}
		});



		DetailViewerField nameFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_NAME, Application.messages.userWidget_nameFieldL_title());		
		DetailViewerField periodFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_PERIOD, Application.messages.userWidget_periodFieldL_title());
		DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.userWidget_msgsSentFieldL_title());
		DetailViewerField subscriptionNamesFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_SUBSCRIPTIONNAMES, Application.messages.userWidget_subscriptionFieldL_title());		

		userDetail = new DetailViewer();
		userDetail.setMargin(2);
		userDetail.setWidth("50%");
		userDetail.setEmptyMessage(Application.messages.userWidget_userDetail_emptyMessage());
		userDetail.setFields(nameFieldD, periodFieldD, nbMsgsSentToDMQSinceCreationFieldD, subscriptionNamesFieldD);

		chartWidth = (com.google.gwt.user.client.Window.getClientWidth()/2)-45;
		chart = new AnnotatedTimeLine(createTable(), createOptions(true), ""+chartWidth, "200");
		
		columnForm = new DynamicForm();
		columnForm.setNumCols(4);

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
		
		showSubCountBox = new CheckboxItem();  
		showSubCountBox.setTitle(Application.messages.common_subscription());
		showSubCountBox.setValue(true);
		showSubCountBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showSubCount = showSubCountBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});


		columnForm.setFields(showSentDMQBox, showSubCountBox);

		
		userChart = new VLayout();
		userChart.setMargin(2);
		userChart.setPadding(5);
		userChart.setWidth("50%");
		userChart.setHeight(220);
		userChart.setAlign(Alignment.CENTER);
		userChart.setAlign(VerticalAlignment.TOP);
		userChart.setShowEdges(true);
		userChart.setEdgeSize(1);
		userChart.addMember(columnForm);
		userChart.addMember(chart);
		userChart.addDrawHandler(new DrawHandler() {
			@Override
			public void onDraw(DrawEvent event) {
				redrawChart = true;
			}
		});
		
		userView = new HLayout();
		userView.setMargin(5);
		userView.setPadding(5);
		userView.addMember(userDetail);
		userView.addMember(userChart);


		//		 Section stack of the queue list
		listStackSection = new SectionStackSection(Application.messages.userWidget_usersSection_title());
		listStackSection.setExpanded(true);
		listStackSection.addItem(userList);


		// Section stack of the view (details & buttons)
		viewSection = new SectionStackSection(Application.messages.userWidget_userDetailsSection_title());
		viewSection.setExpanded(true);
		viewSection.addItem(userView);
		viewSection.setCanReorder(true);

		userStack.addSection(buttonSection);
		userStack.addSection(listStackSection);
		userStack.addSection(viewSection);
		userStack.setCanResizeSections(true);

		return userStack;
	}

	public void setData(List<UserWTO> data) {

		UserListRecord[] userListRecord = new UserListRecord[data.size()];
		for (int i=0;i<data.size();i++) {
			userListRecord[i] = new UserListRecord(data.get(i));
		}

		userList.setData(userListRecord);
	}

	public void updateUser(UserWTO user) {
		UserListRecord userListRecords = (UserListRecord)userList.getRecordList().find(UserListRecord.ATTRIBUTE_NAME, user.getName());
		if(userListRecords != null)  {


			userListRecords.setAttribute(UserListRecord.ATTRIBUTE_NAME, user.getName()); 
			userListRecords.setAttribute(UserListRecord.ATTRIBUTE_PERIOD, user.getPeriod()); 
			userListRecords.setAttribute(UserListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, user.getNbMsgsSentToDMQSinceCreation()); 
			userListRecords.setAttribute(UserListRecord.ATTRIBUTE_SUBSCRIPTIONNAMES, user.getSubscriptionNames()); 


			userListRecords.setUser(user);
			userList.markForRedraw();

		}

		userDetail.setData(new Record[]{userList.getSelectedRecord()});

	}

	public void addUser(UserListRecord user) {
		userList.addData(user);
		userList.markForRedraw();
	}

	public void removeUser(UserListRecord user) {
		RecordList list = userList.getDataAsRecordList();
		UserListRecord toRemove = (UserListRecord)list.find(UserListRecord.ATTRIBUTE_NAME, user.getName());
		userList.removeData(toRemove);
		userList.markForRedraw();
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
		if(showSentDMQ)	data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ()); 
		if(showSubCount) data.addColumn(ColumnType.NUMBER, Application.messages.common_subscription()); 


		Record selectedRecord = userList.getSelectedRecord();
		if(selectedRecord != null) {
			SortedMap<Date, int[]> history = presenter.getUserHistory(selectedRecord.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));
			if(history != null) {
				data.addRows(history.size());

				int i=0;
				for(Date d : history.keySet()) {
					if(d!=null) {
						int j=1;
						data.setValue(i, 0, d);
						if(showSentDMQ) { data.setValue(i, j, history.get(d)[0]); j++; }
						if(showSubCount) { data.setValue(i, j, history.get(d)[1]); j++; }
						i++;
						j=1;
					}
				}
			}
		}

		return data;
	}
	

	public void redrawChart(boolean reuseChart) {
		if(redrawChart) {
			chart.draw(createTable(), createOptions(reuseChart));
		}
	}

	private void enableDisableCheckbox() {
		if(!showSubCount) {
			showSentDMQBox.disable();
		}
		else if(!showSentDMQ) {
			showSubCountBox.disable();
		}
		else {
			showSentDMQBox.enable();
			showSubCountBox.enable();
		}
	}

}