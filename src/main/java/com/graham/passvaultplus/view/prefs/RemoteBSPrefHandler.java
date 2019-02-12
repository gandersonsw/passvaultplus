/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.model.gdocs.ChecksForNewFile;
import com.graham.passvaultplus.model.gdocs.PvpBackingStoreGoogleDocs;
import com.graham.passvaultplus.model.gdocs.ToLocalCopier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Remote Backing Store Preference Handler
 * For now, the only one is Google. If more are added int he future, maybe need to add an enum.
 */
public class RemoteBSPrefHandler {
	public enum FileAction {
		Overwrite,
		Merge,
		Cancel
	}

	final private PreferencesContext prefsContext;
	final boolean useGoogleDriveFlag; // this is not updated, original value only
	private JCheckBox useGoogleDrive;

	private JDialog d;
	private FileAction actionHit;

	public RemoteBSPrefHandler() {
			// for testing only
			prefsContext = null;
			useGoogleDriveFlag = false;
	}

	public RemoteBSPrefHandler(PreferencesContext pcontext) {
		prefsContext = pcontext;
		useGoogleDriveFlag = prefsContext.conn.getContextPrefs().getUseGoogleDrive();
	}

	public JPanel buildPrefsUI() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0021");
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		useGoogleDrive = new JCheckBox("Use Google™ Drive");
		useGoogleDrive.setSelected(useGoogleDriveFlag);
		p.add(useGoogleDrive);
		return p;
	}

	public void deleteCredentials() {
		// TODO this doesnt really work. If we delete the file, it still connects. Need to clear out something in the Google API maybe ?
		//	if (!useGoogleDrive.isSelected() && useGoogleDriveFlag) {
		prefsContext.conn.getPvpContextOriginal().ui.notifyInfo("deleteing Google Credientials if present");
		PvpBackingStoreGoogleDocs.deleteLocalCredentials();
		//	}
	}

	/** after prefs are changed and work is done, handle any cleanup */
	public void cleanup() {
			PvpContextUI.getActiveUI().notifyInfo("RemoteBSPrefHandler.cleanup :: " + useGoogleDrive.isSelected() + " :: " + useGoogleDriveFlag);
		if (!useGoogleDrive.isSelected() && useGoogleDriveFlag) {
			prefsContext.conn.getPvpContextOriginal().ui.notifyInfo("deleteing Google Credientials if present");
			PvpBackingStoreGoogleDocs.deleteLocalCredentials();
		}
		if (useGoogleDrive.isSelected() != useGoogleDriveFlag) {
			// TODO uiMain this might bomb on first start
			prefsContext.conn.getPvpContextOriginal().ui.notifyInfo("RemoteBSPrefHandler.cleanup:" + (prefsContext.conn.getPvpContextOriginal().uiMain != null));
			if (prefsContext.conn.getPvpContextOriginal().uiMain != null && prefsContext.conn.getPvpContextOriginal().uiMain.getMainFrame() != null) {
				SwingUtilities.invokeLater(() ->
					prefsContext.conn.getPvpContextOriginal().uiMain.getMainFrame().reinitStatusPanel(prefsContext.conn.getPvpContextOriginal()));
			}
		}
	}

	public boolean shouldSaveOnChange() {
		return useGoogleDrive.isSelected() && !useGoogleDriveFlag;
	}

	/** return true if continue */
	public boolean presave(boolean isNewDB) {
		if (useGoogleDrive.isSelected() && !useGoogleDriveFlag) {
			PvpContext tempContext = new PvpContext(prefsContext.conn.getPvpContextOriginal(), prefsContext.conn.getContextPrefs()); // TODO marker901
			// TODO verify mainUI is not used by this context
			//PvpBackingStoreGoogleDocs.NewChecks nc = PvpBackingStoreGoogleDocs.doChecksForNewFile(tempContext);
			PvpBackingStoreGoogleDocs.NewChecks nc = ChecksForNewFile.doIt(tempContext);
			if (nc.wasCanceled) {
					return false;
			}
			if (nc.excep != null) {
				try {
					this.prefsContext.conn.context.ui.enableQuitFromError(false);
					this.prefsContext.conn.context.ui.notifyBadException(nc.excep,true, PvpException.GeneralErrCode.GoogleDrive);
					this.prefsContext.conn.context.ui.enableQuitFromError(true);
				} catch (Exception e) {}
				return false;
			}
			if (nc.sameFormatExists) {
				askAboutExistingFile(prefsContext.conn.getSuperFrame(), nc.passwordWorks, isNewDB);
				if (actionHit == FileAction.Cancel) {
					return false;
				}
				if (actionHit == FileAction.Overwrite) {
					PvpBackingStoreGoogleDocs.deleteOfType(tempContext);
				}
				if (actionHit == FileAction.Merge) {
				}
				return true;
			} else {
				String message;
				if (nc.fileExists) {
					message = "Files were found on Google Drive, but file type does not match local settings. \n(" + nc.existingFileFormats + "). \n\nClick OK to create a new file on Google Drive. \n\nClick Cancel and then change local settings to match Google Drive file \nformats to use them. (Compressed and Encrypted must match)";
				} else {
					message = "No existing file was found on Google drive, your local database will be copied there.";
				}
				return prefsContext.conn.context.ui.showConfirmDialog("New Remote File", message);
			}
		}
		return true;
	}

	public void askAboutExistingFile(JFrame parent, boolean passwordWorks, boolean isNewDB) {
		d = prefsContext.conn.context.ui.createDialog(null);
		d.getContentPane().setLayout(new BorderLayout());

		{
			ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
			JLabel icnLab = new JLabel(icn);
			icnLab.setBorder(new EmptyBorder(16, 25, 16, 24));
			JPanel p = new JPanel(new BorderLayout());
			p.add(icnLab, BorderLayout.NORTH);
			d.getContentPane().add(p, BorderLayout.WEST);
		}

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		d.getContentPane().add(centerPanel, BorderLayout.CENTER);

		{
			final JPanel sp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp1.add(new JLabel(" "));
			centerPanel.add(sp1);

			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("There is an existing file on Google drive."));
			centerPanel.add(p);
		}

		if (!passwordWorks) {
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("The password does not work for the Google file so " + (isNewDB ? "it cannot be used." : "a merge cannot be done.")));
			centerPanel.add(p);
		}

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel(" "));
			centerPanel.add(p);
		}

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JButton(new OverwriteAction()));
			JButton mb = new JButton(new MergeAction(isNewDB));
			if (!passwordWorks) {
				mb.setEnabled(false);
			}
			p.add(mb);
			p.add(new JButton(new CancelAction()));
			p.setBorder(new EmptyBorder(4,20,10,16));
			centerPanel.add(p);
		}

		actionHit = FileAction.Cancel;
		d.setResizable(false);
		prefsContext.conn.context.ui.showDialog(d); // this is the line that causes the dialog to Block
	}

	class OverwriteAction extends AbstractAction {
		OverwriteAction() {
			super("Overwrite");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = FileAction.Overwrite;
			prefsContext.conn.context.ui.hideDialog(d);
		}
	}
	class MergeAction extends AbstractAction {
		MergeAction(boolean isNewDB) {
			super(isNewDB ? "Use" : "Merge");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = FileAction.Merge;
			prefsContext.conn.context.ui.hideDialog(d);
		}
	}
	class CancelAction extends AbstractAction {
		CancelAction() {
			super("Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = FileAction.Cancel;
			prefsContext.conn.context.ui.hideDialog(d);
		}
	}

	public boolean createFiles() {
		if (useGoogleDrive.isSelected()) {
			PvpContext tempContext = new PvpContext(prefsContext.conn.getPvpContextOriginal(), prefsContext.conn.getContextPrefs()); // TODO marker901
				// TODO verify mainUI is not used by this context
			PvpBackingStoreGoogleDocs.NewChecks nc = ToLocalCopier.doIt(tempContext);
				com.graham.passvaultplus.PvpContextUI.checkEvtThread("0041");
			if (nc.excep != null) {
				this.prefsContext.conn.context.ui.enableQuitFromError(false);
				this.prefsContext.conn.context.ui.notifyBadException(nc.excep,true, PvpException.GeneralErrCode.GoogleDrive);
				this.prefsContext.conn.context.ui.enableQuitFromError(true);
			} else if (nc.sameFormatExists) {
				return true;
			}
		}
		return false;
	}

	public void save() {
		prefsContext.conn.getContextPrefs().setUseGoogleDrive(useGoogleDrive.isSelected());
		if (!useGoogleDrive.isSelected()) {
			prefsContext.conn.getContextPrefs().setGoogleDriveDocId(null);
			prefsContext.conn.getContextPrefs().setGoogleDriveDocUpdateDate(0);
		}
	}

}
