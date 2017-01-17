/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpFileInterface;
import com.graham.passvaultplus.view.DataFileDir;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;

public class PvpContext {
	private final MyUndoManager undoManager = new MyUndoManager(this);
	private final TabManager tabManager = new TabManager(this);

	private PvpFileInterface rtFileInterface;
	private PvpDataInterface rtDataInterface;
	private String dataFilePath;
	private String password; // used for encryption of data file
	private String passwordFromUserForThisRuntime;
	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Component prefsComponent;
	private MainFrame mainFrame;
	private JTextArea badExceptionMessage;
	private int badExceptionMessageCount;

	/**
	 * Action A: Select data file: new DataFileDir(...)
	 *    needPassword        -> show Password Dialog
	 *    fileLoaded          -> new MainFrame(this)
	 *    error               -> ???
	 *    Quit button clicked -> System.exit(0)
	 * Action B: Show Password Dialog: getPasswordOrAskUser()
	 *    Password entered and no errors -> new MainFrame(this)
	 *    Password entered and errors    -> ???
	 *    user clicked cancel            -> Select data file   (TODO change button to "Change Data File"
	 *    (TODO add button "Quit")
	 */
	static public void startApp() {
		PvpContext context = new PvpContext();
		boolean loadFailed = true;

		context.rtFileInterface = new PvpFileInterface(context);
		context.rtDataInterface = new PvpDataInterface(context);
		
		if (context.isDataFilePresent()) {
			try {
				context.dataFileSelectedForStartup();
				loadFailed = false;
			} catch (UserAskToChangeFileException cfe) {
				System.out.println("changing file");
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
				loadFailed = false;
			}
		}
			
		if (loadFailed) {
			// We might need a password for that data file
			String userHome = System.getProperty("user.home");
			String fileSep = System.getProperty("file.separator");
			new DataFileDir(context, userHome + fileSep + "remthisdata" + fileSep + "rem-this-data.xml", true);
		}
	}

	private boolean isDataFilePresent() {
		if (getDataFilePath() == null) {
			return false;
		}
		File f = new File(getDataFilePath());
		return f.isFile();
	}

	public Component getPrefsComponent() {
		return prefsComponent;
	}

	public void setPrefsComponent(final Component c) {
		prefsComponent = c;
	}

	public void dataFileSelectedForStartup() throws UserAskToChangeFileException {
		getFileInterface().load(getDataInterface());
		mainFrame = new MainFrame(this);
	}

	public PvpFileInterface getFileInterface() {
		return this.rtFileInterface;
	}

	public PvpDataInterface getDataInterface() {
		return this.rtDataInterface;
	}

	public String getDataFilePath() {
		if (dataFilePath == null) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			dataFilePath = userPrefs.get("data_file", null);
		}
		return dataFilePath;
	}

	public void setDataFilePath(String path) {
		dataFilePath = path;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.put("data_file", path);
	}

	/**
	 * Return the password if it was saved, otherwise, ask user for password.
	 * Will only ask once when the application is started.  Will be saved until quit.
	 * @return
	 */
	public String getPasswordOrAskUser(boolean passwordWasBad) throws UserAskToChangeFileException {
		if (isPasswordSaved() && !passwordWasBad) {
			return getPassword();
		}

		if (passwordWasBad || passwordFromUserForThisRuntime == null) {

			passwordFromUserForThisRuntime = (String)JOptionPane.showInputDialog(
                    null,
                    (passwordWasBad ? "That password is not correct. Please try again.\n\n" : "") + "Password:\n" + dataFilePath,
                    "Pass Vault Plus: Password",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");

			if (passwordFromUserForThisRuntime == null || passwordFromUserForThisRuntime.trim().length() == 0) {
				throw new UserAskToChangeFileException();
				//System.exit(0);
			}
		}

		return passwordFromUserForThisRuntime;
	}

	public boolean isPasswordSaved() {
		final String pw = getPassword();
		if (pw == null || pw.trim().length() == 0) {
			return false;
		}
		return true;
	}

	public String getPassword() {
		if (password == null) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			password = userPrefs.get("cipher_pw", null);
		}
		return password;
	}

	public void setPassword(String passwordParam, boolean makePersistant) {
		password = passwordParam;
		if (makePersistant) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			userPrefs.put("cipher_pw", password);
		}
	}

	public void setPasswordFromUserForThisRuntime(String pw) {
		passwordFromUserForThisRuntime = pw;
	}

	public void notifyBadException(final String reason, final Exception e, final boolean canContinue) {
		notifyBadException(reason, e, canContinue, null);
	}
	/**
	 * To be used when a bad exception happens somewhere in the application.
	 * The application will quit after this message is shown to the user.
	 * @canContinue If false, force the application to quit
	 * @optionalAction Can be null. Otherwise another button to do some other action. Maybe export more debugging data.
	 */
	public void notifyBadException(final String reason, final Exception e, final boolean canContinue, final AbstractAction optionalAction) {
		
		if (badExceptionMessage != null) {
			if (badExceptionMessageCount < 10) {
				badExceptionMessage.append("\n\n" + e.getMessage() + "\n\n" + AppUtil.getExceptionStackTrace(e));
				badExceptionMessageCount++;
			}
		}

		AbstractAction quitAction = new AbstractAction("Quit") {
			private static final long serialVersionUID = 1;
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};

		final JFrame errorFrame = new JFrame("Error");
		errorFrame.getContentPane().setLayout(new BorderLayout());

		badExceptionMessage = new JTextArea();
		badExceptionMessage.setText(e.getMessage() + "\n\n" + AppUtil.getExceptionStackTrace(e));

		JScrollPane messageScroll = new JScrollPane(badExceptionMessage);

		errorFrame.getContentPane().add(messageScroll, BorderLayout.CENTER);

		if (reason != null) {
			JLabel jlr = new JLabel(reason);
			errorFrame.getContentPane().add(jlr, BorderLayout.NORTH);
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		errorFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton quitBut = new JButton(quitAction);
		buttonPanel.add(quitBut);

		if (canContinue) {
			AbstractAction continueAction = new AbstractAction("Continue") {
				private static final long serialVersionUID = 1;
				public void actionPerformed(ActionEvent e) {
					errorFrame.setVisible(false);
					badExceptionMessage = null;
					badExceptionMessageCount = 0;
				}
			};
			JButton continueBut = new JButton(continueAction);
			buttonPanel.add(continueBut);
		} else {
			WindowAdapter wAdaptor = new WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent event) {
					System.exit(0);
				}
			};
			errorFrame.addWindowListener(wAdaptor);
		}

		if (optionalAction != null) {
			JButton optionalButton = new JButton(optionalAction);
			buttonPanel.add(optionalButton);
		}

		errorFrame.pack();
	//	errorFrame.setResizable(false);
		BCUtil.center(errorFrame);
		errorFrame.setVisible(true);
	}

	public void notifyWarning(String s) {
		// TODO
		System.out.println(s);
	}

	public PvpViewListContext getViewListContext() {
		return viewListContext;
	}


	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public MyUndoManager getUndoManager() {
		return undoManager;
	}
	
	public TabManager getTabManager() {
		return tabManager;
	}

	public static ImageIcon getIcon(final String imageName) {
		try {
			//System.out.println("getIcon:" + new File("datafiles/images/" + imageName + ".png").getAbsolutePath());
			final BufferedImage img = ImageIO.read(new File("datafiles/images/" + imageName + ".png"));
			return new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
