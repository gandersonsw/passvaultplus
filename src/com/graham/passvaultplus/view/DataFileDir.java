/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.actions.ChooseDirAction;
import com.graham.passvaultplus.actions.SetJTextFieldAction;

public class DataFileDir extends JFrame {
	private static final long serialVersionUID = 1L;
	private AbstractAction okAction, defaultAction, chooseDirAction, quitAction, cancelAction;
	private JTextField dir;
	private String defaultPath;
	private PvpContext context;
	private boolean appFirstStarting;

	public DataFileDir(PvpContext contextParam, String dirPath, final boolean appFirstStartingParam) {
		super("Pass Vault Plus: Choose data file");

		defaultPath = dirPath;
		context = contextParam;
		appFirstStarting = appFirstStartingParam;

		SymWindow3 aSymWindow = new SymWindow3();
		addWindowListener(aSymWindow);

		getContentPane().setLayout(new BorderLayout());

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout());
		JLabel lab1 = new JLabel("Data File:");
		getContentPane().add(labelPanel, BorderLayout.CENTER);
		labelPanel.add(lab1);

		dir = new JTextField(dirPath,30);
		labelPanel.add(dir);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		if (appFirstStarting) {
			quitAction = new quitActionClass("Quit");
			JButton quitBut = new JButton(quitAction);
			buttonPanel.add(quitBut);
		} else {
			cancelAction = new cancelActionClass("Cancel");
			JButton cancelBut = new JButton(cancelAction);
			buttonPanel.add(cancelBut);
		}

		//defaultAction = new defaultActionClass("Default");
		defaultAction = new SetJTextFieldAction("Default", dir, defaultPath);
		JButton defBut = new JButton(defaultAction);
		buttonPanel.add(defBut);
		//chooseDirAction = new chooseDirActionClass("Choose File...");
		chooseDirAction = new ChooseDirAction(dir, this);
		JButton chooseBut = new JButton(chooseDirAction);
		buttonPanel.add(chooseBut);
		okAction = new okActionClass("OK");
		JButton okBut = new JButton(okAction);
		buttonPanel.add(okBut);

	//	BCUtil.setFrameSizeAndCenter(this,400,120);
		pack();
		BCUtil.center(this);
		setResizable(false);

		setVisible(true);
	}

	class SymWindow3 extends java.awt.event.WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			okAction.actionPerformed(null);
		}
	}

	public class okActionClass extends AbstractAction {
		private static final long serialVersionUID = -6400246208727003539L;
		public okActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			// TODO marker101 dup code
			String path = dir.getText();
			File f = new File(path);
			if (path.equals(defaultPath)) {
				// default path is a file, get the files directory to mkdirs
				System.out.println("mkdirs:" + f.getAbsolutePath());
				new File(f.getParent()).mkdirs();
				//f.mkdirs();
			} else {
				if (!f.isFile()) {
					// TODO not sure this is right
					JOptionPane.showMessageDialog(DataFileDir.this,"That file does not exist on the file system. Please create it or use a different path.");
					return;
				}
			}
			context.setDataFilePath(path);
			setVisible(false);
			if (appFirstStarting) {
				try {
					context.dataFileSelectedForStartup();
				} catch (UserAskToChangeFileException e1) {
					System.out.println("- - - - - DataFileDir OK - - - - - - -");
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public class defaultActionClass extends AbstractAction {
		private static final long serialVersionUID = -8469620475725144784L;
		public defaultActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			dir.setText(defaultPath);
		}
	}
/*
	public class chooseDirActionClass extends AbstractAction {
		private static final long serialVersionUID = -5555646509143714580L;
		public chooseDirActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			// TODO probably allow choose of file or directory
		    JFileChooser chooser = new JFileChooser();
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    int returnVal = chooser.showOpenDialog(DataFileDir.this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    		dir.setText(chooser.getSelectedFile().getPath());
		    }
		}
	}
*/
	public class quitActionClass extends AbstractAction {
		private static final long serialVersionUID = -6806569030187007772L;
		public quitActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	public class cancelActionClass extends AbstractAction {
		private static final long serialVersionUID = -110088486471922603L;
		public cancelActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

}
