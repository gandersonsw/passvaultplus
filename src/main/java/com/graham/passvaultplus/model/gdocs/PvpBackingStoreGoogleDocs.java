/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.gdocs;

import java.io.*;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.model.core.*;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.longtask.LTRunnerAsync;

public class PvpBackingStoreGoogleDocs extends PvpBackingStoreAbstract {

	private static String ERRORED_FILE_NAME = "error642";
	private static String CANT_CONNECT_MSG = "Could not connect to Google. Make sure you have an internet connection.";

	public static class NewChecks {
		public boolean fileExists;
		public boolean sameFormatExists;
		public boolean passwordWorks;
		public String existingFileFormats;
		public String error;
		void addFileFormat(String fname) {
			//String newFormat = BCUtil.getFileExtension(fname, true);
			//newFormat = newFormat + ":" + PvpPersistenceInterface.convertFileExtensionToEnglish("." + newFormat);
			String newFormat = PvpPersistenceInterface.convertFileExtensionToEnglish(fname);
			if (existingFileFormats == null) {
				existingFileFormats = newFormat;
			} else {
				existingFileFormats += ", " + newFormat;
			}
		}
	}

	public static final String DOC_NAME = "PassVaultPlusDataGDAPI";

	/** Application name. */
	private static final String APPLICATION_NAME = "PassVaultPlus";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/drive-java-pvp");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials
	 * at ~/.credentials/drive-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE);

	final private PvpContext context;
	private Drive driveService;
	private DateTime lastUpdatedDate;
	private String remoteFileName;
	NewChecks nchecks = new NewChecks();

	public PvpBackingStoreGoogleDocs(PvpContext contextParam) {
		context = contextParam;
	}

	@Override
	public ChattyLevel getChattyLevel() {
		return PvpBackingStore.ChattyLevel.remoteMedium;
	}

	@Override
	public boolean isEnabled() {
		return context.prefs.getUseGoogleDrive();
	}

	@Override
	public boolean shouldBeSaved() {
		return super.shouldBeSaved() || (getException() != null && getException().getCause() instanceof FileNotFoundException);
	}

	@Override
	public InputStream openInputStream() throws IOException {
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream");
		String id = context.prefs.getGoogleDriveDocId();

		final Drive driveService;
		try {
			driveService = getDriveService();
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}

		if (id != null && id.length() > 0) {
			try {
				return driveService.files().get(id).executeMediaAsInputStream();
			} catch (HttpResponseException e) {
				context.ui.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream::HttpResponseException: " + e.getStatusCode() );
				if (e.getStatusCode() == 404) {
					// process below
				} else {
					throw e;
				}
			}
		}

		if (lookForFileInList(driveService)) {
			id = context.prefs.getGoogleDriveDocId();
			return driveService.files().get(id).executeMediaAsInputStream();
		}

		throw new FileNotFoundException();
	}

	private boolean lookForFileInList(final Drive driveService) throws IOException {
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.lookForFileInList :: looking for google doc");

		final FileList result = driveService.files().list().execute();
		final List<File> files = result.getFiles();
		if (files == null || files.size() == 0) {
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.lookForFileInList :: Zero files found.");
			return false;
		} else {
			final String localFileName = getFileName(false);
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.lookForFileInList :: Files:");
			for (File file : files) {
				nchecks.fileExists = true;
				nchecks.addFileFormat(file.getName());
				context.ui.notifyInfo("PvpBackingStoreGoogleDocs.lookForFileInList :: " + file.getName() + " :: " + file.getId());
				if (localFileName.equals(file.getName())) {
					nchecks.sameFormatExists = true;
					context.ui.notifyInfo("PvpBackingStoreGoogleDocs.lookForFileInList :: using this file");
					final String id = file.getId();
					context.prefs.setGoogleDriveDocId(id);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsFileUpload() {
		return true;
	}

	private String getFileName(boolean inFlag) {
		if (inFlag) {
			if (remoteFileName == null) {
				loadFileProps(true);
			}
			return remoteFileName;
		} else {
			final String localFile = context.prefs.getDataFile().getName();
			return PvpPersistenceInterface.formatFileName(
					DOC_NAME,
					PvpPersistenceInterface.isCompressed(localFile),
					PvpPersistenceInterface.isEncrypted(localFile));
		}
	}

	@Override
	public void doFileUpload() throws IOException {
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: START" );

		Drive driveService;
		try {
			driveService = getDriveService();
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}

		final FileContent mediaContent = new FileContent(null, context.prefs.getDataFile()); // null for no file type
		File returnedFileMetaData = null;
		final String id = context.prefs.getGoogleDriveDocId();
		final File newFileMetadata = new File();
		newFileMetadata.setName(getFileName(false));

		if (id == null || id.length() == 0) {
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: creating new file ");
			returnedFileMetaData = driveService.files().create(newFileMetadata, mediaContent).setFields("id").execute();
		} else {
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: updating file " + id);
			try {
				final File ignored = driveService.files().update(id, newFileMetadata, mediaContent).execute();
			} catch (HttpResponseException e) {
				context.ui.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: HttpResponseException : " + e.getStatusCode() );
				if (e.getStatusCode() == 404) {
					returnedFileMetaData = driveService.files().create(newFileMetadata, mediaContent).setFields("id").execute();
				} else {
					throw e;
				}
			}
		}

		if  (returnedFileMetaData != null) {
			context.prefs.setGoogleDriveDocId(returnedFileMetaData.getId());
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: File ID: " + returnedFileMetaData.getId());
		}
	}

	@Override
	public boolean isCompressed(boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(getFileName(inFlag));
	}

	@Override
	public boolean isEncrypted(boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(getFileName(inFlag));
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	private Credential authorize() throws IOException {
			LTManager.nextStep("Google Drive Authorize");
			// Load client secrets.
			InputStream in = PvpBackingStoreGoogleDocs.class.getResourceAsStream("/client_id.json");
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

			// Build flow and trigger user authorization request.
			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
					clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
			Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
			context.ui.notifyInfo("PvpBackingStoreGoogleDocs.authorize :: Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
			LTManager.stepDone("Google Drive Authorize");
			return credential;
	}

	/**
	 * Build and return an authorized Drive client service.
	 *
	 * @return an authorized Drive client service
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private Drive getDriveService() throws IOException, GeneralSecurityException {
		if (driveService == null) {
			if (HTTP_TRANSPORT == null) {
				HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
			}

			Credential credential = authorize();
			driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
		}
		return driveService;
	}

	void loadFileProps(boolean lookInFileList) {
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.loadFileProps");
		LTManager.nextStep("Connecting to Google");
		Drive driveService = null;
		try {
			final String id = context.prefs.getGoogleDriveDocId();
			driveService = getDriveService();
			if (id != null && id.length() > 0) {
				final File f = driveService.files().get(id).setFields("modifiedTime,name").execute();
				lastUpdatedDate = f.getModifiedTime();
				remoteFileName = f.getName();
				nchecks.fileExists = true;
				nchecks.sameFormatExists = true;
				return;
			}
		} catch (Exception e) {
			context.ui.notifyWarning("Google Doc File Properties Error", e);
			if (driveService == null || e instanceof UnknownHostException) {
				lookInFileList = false;
		//		nchecks.error = e instanceof UnknownHostException ? CANT_CONNECT_MSG : e.getMessage();
				this.setException(new PvpException(PvpException.GeneralErrCode.GoogleDrive, e));
			}
		}

		if (lookInFileList) {
			try {
				if (lookForFileInList(driveService)) {
					loadFileProps(false);
					return;
				}
			} catch (Exception e2) {
				context.ui.notifyWarning("Google Doc File Properties Error 2", e2);
			//	nchecks.error = e2 instanceof UnknownHostException ? CANT_CONNECT_MSG : e2.getMessage();
				this.setException(new PvpException(PvpException.GeneralErrCode.GoogleDrive, e2));
			}
		}

		lastUpdatedDate = new DateTime(Long.MAX_VALUE);
		remoteFileName = ERRORED_FILE_NAME;
	}

	@Override
	public long getLastUpdatedDate() {
		if (lastUpdatedDate == null) {
			loadFileProps(true);
		}
		return lastUpdatedDate.getValue();
	}

	@Override
	public void clearTransientData() {
		if (isEnabled()) {
			if (getException() != null) {
				Throwable t = getException().getCause();
				if (t instanceof FileNotFoundException) {
					// In this case, we want to treat it as if it was loaded, because there is no file we need to be careful of overwriting
					setDirty(true);
					this.stateTrans(BsStateTrans.StartLoading);
					this.stateTrans(BsStateTrans.EndLoading);
					//setLoadState(PvpBackingStore.LoadState.loaded);
				}
			}
			driveService = null;
			lastUpdatedDate = null;
			remoteFileName = null;
			super.clearTransientData();
		}
	}

	@Override
	public String getDisplayableResourceLocation() {
		return "Google™ Doc: " + getFileName(true);
	}

	@Override
	public String getShortName() {
		return "Google™";
	}

	@Override
	protected String getErrorMessageForDisplay() {
		Throwable t = getException().getCause();
		if (t instanceof UnknownHostException) {
			return CANT_CONNECT_MSG;
		} else if (t instanceof FileNotFoundException) {
			return "File not found on Google Drive Server. A new file will be created when saving.";
		}
		return getException().getMessage();
	}

	@Override
	public void userAskedToHandleError() {
		Throwable t = getException().getCause();
		if (t instanceof UnknownHostException) {
			boolean tryReconnect = context.ui.showConfirmDialog("Cannot Connect", "Would you like to try to connect to Google again?");
			if (tryReconnect) {
				context.ui.notifyInfo("PvpBackingStoreGoogleDocs.userAskedToHandleError :: reconnecting...");
				//this.setException(null); // TODO not sure this is right. do we want to call super.clearTransientData ?
				loadFileProps(true);
				if (ERRORED_FILE_NAME.equals(remoteFileName)) {
					context.ui.showMessageDialog("Connection Failed", "Connection Failed");
				} else {
					LTManager.run(context.data.getFileInterface().loadLT(context.data.getDataInterface()), new UatheCb(this));
				}
			}

		} else if (t instanceof FileNotFoundException) {
			new ErrUIGoogleDocFileNotFound(context, getFileName(false), this).buildDialog();
		}
	}

	class UatheCb extends PvpBackingStoreLTCB {
		public UatheCb(PvpBackingStore bsParam) {
			super(bsParam);
		}
		@Override
		public void taskComplete(LTRunnerAsync lt) {
			super.taskComplete(lt);
			context.uiMain.getViewListContext().filterUIChanged();
		}
	}

	@Override
	public void allStoresAreUpToDate() {
		if (lastUpdatedDate == null) {
			loadFileProps(true);
		}
		if (getBsState() == BsState.StartState) {
				stateTrans(BsStateTrans.StartLoading);
				stateTrans(BsStateTrans.EndLoading);
		//	setLoadState(LoadState.skipped); // set this in case we did not actually load from this, so that it is treated like it was loaded, so that it saves it
		}
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.allStoresAreUpToDate :: setGoogleDriveDocUpdateDate:" + lastUpdatedDate);
		context.prefs.setGoogleDriveDocUpdateDate(lastUpdatedDate.getValue());
	}

	@Override
	public boolean isUnmodifiedRemote() {
		if (lastUpdatedDate == null) {
			loadFileProps(true);
		}
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.isUnmodifiedRemote :: " + (lastUpdatedDate.getValue() == context.prefs.getGoogleDriveDocUpdateDate()) + ":" + lastUpdatedDate + ":" + new Date(context.prefs.getGoogleDriveDocUpdateDate()));
		return lastUpdatedDate.getValue() == context.prefs.getGoogleDriveDocUpdateDate();
	}

	public static void deleteOfType(PvpContext context) {
		PvpBackingStoreGoogleDocs bs = new PvpBackingStoreGoogleDocs(context);
		try {
			bs.deleteOfType2();
		} catch (IOException e) {
			context.ui.notifyWarning("Trying to remove existing Google drive file", e);
		}
	}

	private void deleteOfType2() throws IOException {
		context.ui.notifyInfo("PvpBackingStoreGoogleDocs.deleteOfType2::START" );

		Drive driveService;
		try {
			driveService = getDriveService();
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}

		if (lookForFileInList(driveService)) {
			final String id = context.prefs.getGoogleDriveDocId();
			driveService.files().delete(id);
			context.ui.notifyInfo("deleted file id::" + id);
		}
	}

	public static void deleteLocalCredentials() {
		if (DATA_STORE_DIR.isDirectory()) {
			String[] entries = DATA_STORE_DIR.list();
			for(String s: entries){
				java.io.File currentFile = new java.io.File(DATA_STORE_DIR.getPath(),s);
				currentFile.delete();
			}
			DATA_STORE_DIR.delete();
		}
	}

}