/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;

public class PvpBackingStoreGoogleDocs extends PvpBackingStoreAbstract {
	
	public static final String DOC_NAME = "PassVaultPlusDataGDAPI";
	
	 /** Application name. */
    private static final String APPLICATION_NAME =
        "PassVaultPlus";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/drive-java-pvp");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
           Arrays.asList(DriveScopes.DRIVE_FILE);
	
	final private PvpContext context;
	private Drive driveService;
	private DateTime lastUpdatedDate;
	private String remoteFileName;
	
	public PvpBackingStoreGoogleDocs(PvpContext contextParam) {
		context = contextParam;
	}

	@Override
	public ChattyLevel getChattyLevel() {
		return PvpBackingStore.ChattyLevel.remoteMedium;
	}

	@Override
	public boolean isEnabled() {
		return context.getUseGoogleDrive();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		System.out.println("google getting in stream");
		final String id = context.getGoogleDriveDocId();
		if (id == null || id.length() == 0) {
			System.out.println("no google yet");
			return null;
		}
		
		final Drive driveService;
		try {
			driveService = getDriveService();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
		return driveService.files().get(id).executeMediaAsInputStream();
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
				loadFileProps();
			}
			return remoteFileName;
		} else {
			final String localFile = context.getDataFile().getName();
			return PvpPersistenceInterface.formatFileName(
					DOC_NAME, 
					PvpPersistenceInterface.isCompressed(localFile), 
					PvpPersistenceInterface.isEncrypted(localFile));
		}
	}
	
	@Override
	public void doFileUpload() {
    	System.out.println("at doFileUpload" );
		try {
			 // Build a new authorized API client service.
	        final Drive driveService = getDriveService();
			final FileContent mediaContent = new FileContent(null, context.getDataFile()); // TODO not sure null is correct
			
			File file;
			final String id = context.getGoogleDriveDocId();
			if (id == null || id.length() == 0) {
				System.out.println("creating new file ");
				File fileMetadata = new File();
				fileMetadata.setName(getFileName(false));
				file = driveService.files().create(fileMetadata, mediaContent)
					    .setFields("id")
					    .execute();
			} else {
				System.out.println("updating file file " + id);
				file = driveService.files().get(id).execute();
				File updatedFile = driveService.files().update(id, file, mediaContent).execute();
			}
			
			context.setGoogleDriveDocId(file.getId());
			
			System.out.println("File ID: " + file.getId());
			
		} catch (Exception e) {
			System.out.println("at writeTo E: " + e.getMessage() );
			context.notifyBadException(e, true, PvpException.GeneralErrCode.GoogleDrive);
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
		// Load client secrets.
		InputStream in = PvpBackingStoreGoogleDocs.class.getResourceAsStream("/client_id.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
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

	private void loadFileProps() {
		try {
			final String id = context.getGoogleDriveDocId();
			if (id != null && id.length() > 0) {
				final Drive driveService = getDriveService();
				final File f = driveService.files().get(id).setFields("modifiedTime,name").execute();
				lastUpdatedDate = f.getModifiedTime();
				remoteFileName = f.getName();
				return;
			}
		} catch (Exception e) {
			context.notifyWarning("Google Doc File Properties Error", e);
		}
		
		lastUpdatedDate = new DateTime(Long.MAX_VALUE);
		remoteFileName = "error642";
	}
	
	@Override
	public long getLastUpdatedDate() {
		if (lastUpdatedDate == null) {
			loadFileProps();
		}
		return lastUpdatedDate.getValue();
	}
	
	@Override
	public void clearTransientData() {
		if (isEnabled()) {
			driveService = null;
			lastUpdatedDate = null;
			remoteFileName = null;
			super.clearTransientData();
		}
	}

}
