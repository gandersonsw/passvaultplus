/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.graham.passvaultplus.model.core.PvpFileInterface;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class PvpGoogleDrive {
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
    
    PvpContext context;
    boolean ready = false;
    
    public PvpGoogleDrive(PvpContext contextParam) {
    	context = contextParam;
    }
    
    private boolean getReady() {
    	if (!context.getUseGoogleDrive()) {
    		return false;
    	}
    	if (ready) {
    		return true;
    	}
    	try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            ready = true;
        } catch (Exception e) {
        	context.notifyBadException(e, true, PvpException.GeneralErrCode.GoogleDrive);
        }
    	return ready;
    }
    
    public void readFrom() {
    	if (!getReady()) {
    		return;
    	}
    }
    
    public boolean writeTo() {
    	if (!getReady()) {
    		return true;
    	}
    	// TODO check if anything changed
    	System.out.println("at writeTo 1" );
		try {
			 // Build a new authorized API client service.
	        Drive driveService = getDriveService();
	        System.out.println("at writeTo 2" );
			
			String localFile = context.getDataFile().getName();
			String fileName = PvpFileInterface.formatFileName(
					"PassVaultPlusDataGDAPI", 
					PvpFileInterface.isCompressed(localFile), 
					PvpFileInterface.isEncrypted(localFile));
			
			File fileMetadata = new File();
			fileMetadata.setName(fileName);
		
			FileContent mediaContent = new FileContent(null, context.getDataFile()); // TODO not sure null is correct
			File file = driveService.files().create(fileMetadata, mediaContent)
			    .setFields("id")
			    .execute();
			System.out.println("File ID: " + file.getId());
			
		} catch (Exception e) {
			 System.out.println("at writeTo E: " + e.getMessage() );
			context.notifyBadException(e, true, PvpException.GeneralErrCode.GoogleDrive);
			return false;
		}
		return true;
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
        		TestGoogleDoc.class.getResourceAsStream("/client_id.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    private Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}