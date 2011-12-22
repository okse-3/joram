/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common.monitoring;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.lang.Math;
import javax.swing.*;

import com.scalagent.monitoring.Monitoring;
import com.scalagent.monitoring.MonitoringSaxWrapper;

/**
 * The <code>WindowMonitoringTimerTask</code> class allows to periodically watch
 * JMX attributes and send the corresponding values to the outpuStrean.
 */
public class WindowMonitoringTimerTask extends MonitoringTimerTask {

	StringBuffer strbuf = null;
	JTextArea textArea;
	JTextField attField, addMbeanField, delMbeanField;

	/**
	 * Initializes the <code>WindowMonitoringTimerTask</code> component.
	 * 
	 */
	public WindowMonitoringTimerTask() {
		super(MonitoringSaxWrapper.period, MonitoringSaxWrapper.monitoredAttsProps);

		strbuf = new StringBuffer();

		GUI(MonitoringSaxWrapper.initTaskProps.getProperty("name"), 100, 100);

		Timer timer = Monitoring.getTimer();
		start(timer);
	}

	/**
	 * Initialize the record for the current collect time. For the
	 * FileMonitoringTimer, it consists to initialize a StringBuffer to collect
	 * informations about all attributes.
	 * 
	 * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#initializeRecords()
	 */
	protected void initializeRecords() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		strbuf.append(cal.get(Calendar.YEAR)).append('/')
				.append(cal.get(Calendar.MONTH)+1).append('/')
				.append(cal.get(Calendar.DATE)).append(" ")
				.append(cal.get(Calendar.HOUR_OF_DAY)).append(":")
				.append(cal.get(Calendar.MINUTE)).append(':')
				.append(cal.get(Calendar.SECOND)).append(',')
				.append(cal.get(Calendar.MILLISECOND)).append(";\n");
	}

	/**
	 * Records information about the specified attribute.
	 * 
	 * @param mbean
	 *          The name of the related mbean.
	 * @param att
	 *          The name of the related attribute.
	 * @param value
	 *          The value of the related attribute.
	 * 
	 * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#addRecord(javax.management.ObjectName,
	 *      java.lang.String, java.lang.Object)
	 */
	protected void addRecord(String mbean, String att, Object value) {
		strbuf.append(mbean).append(':').append(att).append('=').append(value)
				.append(";\n");
	}

	/**
	 * Finalize the record for the current time.
	 * 
	 * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#finalizeRecords()
	 */
	protected void finalizeRecords() {
		strbuf.append("\n");
		String buff = strbuf.toString();
		textArea.append(buff);
		strbuf.setLength(0);
	}

	/**
	 * Create window for print monitoring results
	 * 
	 */
	protected void GUI(String title, int x, int y) {
		JFrame mainWindow;
		JScrollPane scrollPane;
		JPanel actionPanel;
		JButton addButton, delButton;
		JLabel addAttLabel, addMbeanLabel, delMbeanLabel;
		ActionListener addListener, delListener;
		Random rand = new Random();

		// set monitored values area
		textArea = new JTextArea();
		scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2),
				BorderFactory.createEtchedBorder()));

		/* set action area */
		// set addMonitoredAttributes fields
		addAttLabel = new JLabel("att");
		addMbeanLabel = new JLabel("mbean");
		attField = new JTextField();
		attField.setColumns(10);
		addMbeanField = new JTextField();
		addMbeanField.setColumns(10);

		addListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMonitoredAttributes(addMbeanField.getText(), attField.getText());
			}
		};

		addButton = new JButton("addAttribute");
		addButton.addActionListener(addListener);

		// set delMonitoredAttributes fields
		delMbeanLabel = new JLabel("mbean");
		delMbeanField = new JTextField();
		delMbeanField.setColumns(10);

		delListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delMonitoredAttributes(delMbeanField.getText());
			}
		};

		delButton = new JButton("delAttribute");
		delButton.addActionListener(delListener);

		// set action panel
		actionPanel = new JPanel();
		GroupLayout layout = new GroupLayout(actionPanel);
		actionPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(addButton).addComponent(delButton))
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(addMbeanLabel).addComponent(delMbeanLabel))
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(addMbeanField).addComponent(delMbeanField))
				.addComponent(addAttLabel).addComponent(attField));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(addButton).addComponent(addMbeanLabel)
								.addComponent(addMbeanField).addComponent(addAttLabel)
								.addComponent(attField))
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(delButton).addComponent(delMbeanLabel)
								.addComponent(delMbeanField)));

		layout.linkSize(addButton, delButton);

		// set main window
		mainWindow = new JFrame(title);
		mainWindow.getContentPane().add(scrollPane, BorderLayout.CENTER);
		mainWindow.getContentPane().add(actionPanel, BorderLayout.NORTH);
		mainWindow.setLocation(x, Math.abs(rand.nextInt(y)));
		mainWindow.setSize(500, 200);
		mainWindow.setVisible(true);
	}
}