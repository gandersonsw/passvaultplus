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
		context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream");
		String id = context.getGoogleDriveDocId();
		
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
				context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream :: HttpResponseException : " + e.getStatusCode() );
				if (e.getStatusCode() == 404) {
					// process below
				} else {
					throw e;
				}
			}
		}
		
		context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream :: looking for google doc");
		
		FileList result = driveService.files().list().execute();
		List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
        	context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream :: No files found.");
            return null;
        } else {
        	final String localFileName = getFileName(false);
        	context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream :: Files:");
            for (File file : files) {
            	context.notifyInfo("PvpBackingStoreGoogleDocs.openInputStream :: " + file.getName() + " :: " + file.getId());
                if (localFileName.equals(file.getName())) {
                	id = file.getId();
                	context.setGoogleDriveDocId(id);
                	return driveService.files().get(id).executeMediaAsInputStream();
                }
            }
        }
		
		return null;
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
	public void doFileUpload() throws IOException {
		context.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: START" );

        Drive driveService;
		try {
			driveService = getDriveService();
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
		
		final FileContent mediaContent = new FileContent(null, context.getDataFile()); // TODO not sure null is correct
		File returnedFileMetaData = null;
		final String id = context.getGoogleDriveDocId();
		final File newFileMetadata = new File();
		newFileMetadata.setName(getFileName(false));
		
		if (id == null || id.length() == 0) {
			context.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: creating new file ");
			returnedFileMetaData = driveService.files().create(newFileMetadata, mediaContent).setFields("id").execute();
		} else {
			context.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: updating file file " + id);
			try {
				final File ignored = driveService.files().update(id, newFileMetadata, mediaContent).execute();
			} catch (HttpResponseException e) {
				context.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: HttpResponseException : " + e.getStatusCode() );
				if (e.getStatusCode() == 404) {
					returnedFileMetaData = driveService.files().create(newFileMetadata, mediaContent).setFields("id").execute();
				} else {
					throw e;
				}
			}
		}
		
		if  (returnedFileMetaData != null) {
			context.setGoogleDriveDocId(returnedFileMetaData.getId());
			context.notifyInfo("PvpBackingStoreGoogleDocs.doFileUpload :: File ID: " + returnedFileMetaData.getId());
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
		context.notifyInfo("PvpBackingStoreGoogleDocs.authorize :: Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
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
	
	@Override
	public String getDisplayableResourceLocation() {
		return "Google™ Doc: " + getFileName(true);
	}
	
	@Override
	public String getShortName() {
		return "Google™";
	}
	
	@Override
	String getErrorMessageForDisplay() {
		Throwable t = exception.getCause();
		if (t instanceof HttpResponseException) {
			int httpCode = ((HttpResponseException)t).getStatusCode();
			if (httpCode == 404) {
				return "File not found on Google Doc Server. A new file will be created when saving.";
			}
		}
		
		return exception.getMessage();
	}

}
