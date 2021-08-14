/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

import com.graham.framework.PasswordGenerator;
import com.graham.passvaultplus.PvpContext;

public class RightClickPopup extends MouseAdapter {
  private PvpContext context;

  RightClickPopup(PvpContext c) {
    context = c;
  }

  void addListener(JTextComponent tc) {
    tc.addMouseListener(this);
  }

  @Override
  public void mousePressed(MouseEvent e) {
      showPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
      showPopup(e);
  }

  private void showPopup(MouseEvent e) {
    com.graham.passvaultplus.PvpContextUI.checkEvtThread("0099");
      if (e.isPopupTrigger()) {
      //  TODO JTextComponent tc = (JTextComponent)e.getComponent();
      //  getPopupMenu(tc).show(tc, e.getX(), e.getY());
      }
  }

  JPopupMenu getPopupMenu(RecordEditFieldText ref) {
    com.graham.passvaultplus.PvpContextUI.checkEvtThread("0097");
		JPopupMenu popupMenu = new JPopupMenu("Edit");
  	popupMenu.add(new JMenuItem(new ClearFieldAction(ref)));
  	popupMenu.add(new JMenuItem(new ClearAndPasteAction(ref)));

		JMenu passwordMenu = new JMenu("Generate Password");
    addPasswordGen(passwordMenu, ref);
    //passwordMenu.addSeparator();
  	//passwordMenu.add(new JMenuItem("Create New Password Generator"));

		popupMenu.add(passwordMenu);

		return popupMenu;
	}

  static public void addPasswordGen(JMenu passwordMenu, RecordEditFieldText ref) {
    com.graham.passvaultplus.PvpContextUI.checkEvtThread("0095");
    //List<PvpRecord> pwGenList = context.data.getDataInterface().getRecordsOfType(PvpDataInterface.TYPE_PASSWORD_GEN);
    //if (pwGenList.size() == 0) {
    PasswordGenerator.PwGenParams params = new PasswordGenerator.PwGenParams();
    passwordMenu.add(new GenPasswordAction("Default Password Generator", 1, ref));
    passwordMenu.add(new GenPasswordAction("8 Basic", 2, ref));
    passwordMenu.add(new GenPasswordAction("12 Strong", 3, ref));
    passwordMenu.add(new GenPasswordAction("20 Strong", 4, ref));
    passwordMenu.add(new GenPasswordAction("10 Upper+Lower+Digit+Special", 5, ref));
    //} else {
    //}
  }
  
  static public void addPasswordGen(JPopupMenu passwordMenu, RecordEditFieldText ref) {
    com.graham.passvaultplus.PvpContextUI.checkEvtThread("0095");
    //List<PvpRecord> pwGenList = context.data.getDataInterface().getRecordsOfType(PvpDataInterface.TYPE_PASSWORD_GEN);
    //if (pwGenList.size() == 0) {
    PasswordGenerator.PwGenParams params = new PasswordGenerator.PwGenParams();
    passwordMenu.add(new GenPasswordAction("Default Password Generator", 1, ref));
    passwordMenu.add(new GenPasswordAction("8 Basic", 2, ref));
    passwordMenu.add(new GenPasswordAction("12 Strong", 3, ref));
    passwordMenu.add(new GenPasswordAction("20 Strong", 4, ref));
    passwordMenu.add(new GenPasswordAction("10 Upper+Lower+Digit+Special", 5, ref));
    //} else {
    //}
  }

	class ClearFieldAction extends AbstractAction {
    RecordEditFieldText ref;
		public ClearFieldAction(RecordEditFieldText refParam) {
			super("Clear Field");
      ref = refParam;
		}
		public void actionPerformed(ActionEvent e) {
      ref.setFieldText("");
		}
	}

  class ClearAndPasteAction extends AbstractAction {
    RecordEditFieldText ref;
    public ClearAndPasteAction(RecordEditFieldText refParam) {
      super("Paste (Replace)");
      ref = refParam;
    }
    public void actionPerformed(ActionEvent e) {
      
      try {
        String data = (String) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
        if (com.graham.util.StringUtil.stringNotEmpty(data)) {
          ref.setFieldText(data);
        }
      } catch (Exception exc) {
        
      }
      
      /* tc.setText("");
      TODO TODO 
      Action a = tc.getActionMap().get("paste");
      if (a != null) {
        ActionEvent ae = new ActionEvent(tc, ActionEvent.ACTION_PERFORMED, "");
        a.actionPerformed(ae);
      }
      */
    }
  }

}
