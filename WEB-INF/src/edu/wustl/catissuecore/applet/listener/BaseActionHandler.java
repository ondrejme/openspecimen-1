package edu.wustl.catissuecore.applet.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;


/**
 * This is base Handler class for all of the component in tissuecore.
 * 
 * @author Mandar Deshmukh
 * @author Rahul Ner
 *
 */
public class BaseActionHandler implements ActionListener
{
	/**
	 * 
	 */
	protected JTable table;
	
	public BaseActionHandler(JTable table)
	{
		this.table = table;
	}
	
	/**
	 * This method is of the ActionListener interface.
	 * We call the three methods from it. These methods provide the ability to perform some tasks after the actual event handling is done.
	 *  
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event)
	{
		preActionPerformed();		
		handleAction(event);
		postActionPerformed(event);
	}

	/**
	 * This method provides a hook to specific Listener classes that needs to do some functionality before action executed. 
	 */
	protected void preActionPerformed()
	{
	
	}

	/**
	 * This method provides a hook to specific Listener classes that needs to do some functionality after action executed. 
	 */
	protected void postActionPerformed(ActionEvent event)
	{
		//fireEditingStopped();
		table.getModel().setValueAt(getSelectedValue(event),table.getSelectedRow(),table.getSelectedColumn());
		System.out.println(getSelectedValue(event));
	}

	/**
	 * This method returns the value of the source object on which the event occurs.
	 * This method is to be overridden by the subclasses for specific functionality.
	 * @param event Event Objcet.
	 * @return Value of source object on which the event occured. 
	 */
	protected Object getSelectedValue(ActionEvent event) {
		return null;
	}
	
	/**
	 * This method handles specific action. Each subclass can provide custom handling.
	 */
	protected void handleAction(ActionEvent event)
	{
		
	}
}
