/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.actions.ExportXmlFile;

/**
 * All methods to load data into RtDataInterface from the file
 */
public class PvpFileInterface {

	public static class XmlStuff {
		Document jdomDoc;
		String rawXml;

		XmlStuff(Document d) {
			jdomDoc = d;
		}

		XmlStuff(String s) {
			rawXml = s;
		}

		public Document getJdomDoc() {
			return jdomDoc;
		}

		public String getRawXml() {
			return rawXml;
		}
	}

	public static final String EXT_COMPRESS = "zip";
	public static final String EXT_ENCRYPT = "bmn";
	public static final String EXT_XML = "xml";

	PvpContext context;
	int maxID;
	boolean wasXmlError;

	public PvpFileInterface(final PvpContext contextParam) {
		context = contextParam;
	}

	public static boolean isCompressed(final String path) {
		// path is .zip or .zip.bmn
		return path.endsWith("." + EXT_COMPRESS) || path.endsWith("." + EXT_COMPRESS + "." + EXT_ENCRYPT);
	}

	public static boolean isEncrypted(final String path) {
		// path is .bmn or .zip.bmn
		return path.endsWith("." + EXT_ENCRYPT);
	}

	public XmlStuff getJdomDoc(boolean getAsString) throws JDOMException, IOException, UserAskToChangeFileException {

		wasXmlError = false;
		BufferedInputStream bufInStream = null;
		InputStream inStream = null;
		ZipInputStream zipfile = null;
		CipherInputStream cypherStream = null;
		FileInputStream fileStream = null;
		boolean badEncryption = true;
		boolean passwordTried = false;
		try {
			while (badEncryption) {
			String path = context.getDataFilePath();
			File pathFile = new File(path);
			if (pathFile.isDirectory()) {
				pathFile = new File(pathFile, "rem-this-data.xml");
			}
			if (!pathFile.exists()) {
				createDefaultStarterFile(pathFile);
			}
			// 4 possible file extensions: .xml .zip .bmn.zip .bmn
			

		//	System.out.println("path=" + path);

			if (isEncrypted(path)) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(path);
					byte[] encryptionHeaderBytes = new byte[CipherWrapper.getEncryptHeaderSize()];
					int br = fis.read(encryptionHeaderBytes);
					if (br != CipherWrapper.getEncryptHeaderSize()) {
						throw new IOException("encrypt header not read:" + br);
					}
					final String password = context.getPasswordOrAskUser(passwordTried);
					CipherWrapper cw = new CipherWrapper(password, encryptionHeaderBytes);
					
					cypherStream = new CipherInputStream(fis, cw.cipher);
					inStream = cypherStream;
					
					byte[] check = new byte[8];
					if (inStream.read(check, 0, 8) == 8) {
						String s = new String(check);
						if (s.equals("remthis7")) {
							badEncryption = false;
						}
					}
					passwordTried = true;

				} catch (NoSuchAlgorithmException e) {
			//		context.notifyBadException("decrypting", e, false);
					throw new RuntimeException(e);
				} catch (NoSuchPaddingException e) {
			//		context.notifyBadException("decrypting", e, false);
					throw new RuntimeException(e);
				} catch (InvalidKeyException e) {
					context.notifyBadException("Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy", e, true);
					throw new RuntimeException(e);
				} catch (InvalidKeySpecException e) {
					throw new RuntimeException(e);
				} catch (InvalidAlgorithmParameterException e) {
					throw new RuntimeException(e);
				} finally {
					// if cypherStream is null, some kind of error happened, so close the file stream
					if (cypherStream == null && fis != null) {
						try { fis.close(); } catch (Exception e) { }
					}
				}

			} else {
				badEncryption = false;
				fileStream = new FileInputStream(path);
				inStream = fileStream;
			}

			if (!badEncryption) {
			if (isCompressed(path)) {
				zipfile = new ZipInputStream(inStream);

				ZipEntry entry = zipfile.getNextEntry();
				if (entry != null) {
				//	entry.
					inStream = zipfile; // zipfile.getInputStream(entry);
				}

				//Enumeration e = zipfile.entries();
			//	if (e.hasMoreElements()) {
			//		ZipEntry entry = (ZipEntry) e.nextElement();
			//		inStream = zipfile.getInputStream(entry);
			//	}
			}
			}
			}
			bufInStream = new BufferedInputStream(inStream);

		//	BCUtil.dumpInputStream(bufInStream);

			if (getAsString) {
				return new XmlStuff(BCUtil.dumpInputStreamToString(bufInStream));
			} else {
				try {
					SAXBuilder builder = new SAXBuilder();
					return new XmlStuff(builder.build(bufInStream));
				} catch (Exception e) {
					wasXmlError = true;
					throw e;
				}
			}
		} finally {
			if (bufInStream != null) {
				try { bufInStream.close(); } catch (IOException ioe) { }
			}
			if (cypherStream != null) {
				try { cypherStream.close(); } catch (IOException ioe) { }
			}
			if (fileStream != null) {
				try { fileStream.close(); } catch (IOException ioe) { }
			}
			// bufInStream.close() will also close inStream

			if (zipfile != null) {
				try { zipfile.close(); } catch (IOException ioe) { }
			}
		}
	}

	public void load(PvpDataInterface dataInterface) throws UserAskToChangeFileException {
		try {
			List<PvpType> types = null;
			List<PvpRecord> records = null;

			XmlStuff doc = getJdomDoc(false); //builder.build(getXmlFilePath());
			Element root = doc.jdomDoc.getRootElement();
			if (!root.getName().equals("mydb")) {
				context.notifyWarning("unexpected element:" + root.getName());
			}
			System.out.println("data file locale=" + root.getAttribute("locale"));
			PvpFileLoader loader = new PvpFileLoader(context);
			List children = root.getChildren();
			for (int i = 0; i < children.size(); i++) {
				Element e = (Element)children.get(i);
				if (e.getName().equals("types")) {
					types = loader.loadTypes(e);
					//newData.setTypes(loader.loadTypes(e))
				} else if (e.getName().equals("records")) {
					records = loader.loadRecords(e);
				//	newData.setRecords(loader.loadRecords(e));
				} else {
					context.notifyWarning("unexpected element:" + e.getName());
				}
			}

      if (types == null) {
				context.notifyWarning("loading failed, type data not found");
				return;
			}

			if (records == null) {
				context.notifyWarning("loading failed, record data not found");
				return;
			}

			PvpDataInterface tempDataInterface = new PvpDataInterface(context, types, records);

			// do any initialization after all the data is loaded
			for (PvpRecord r : records) {
				r.initalizeAfterLoad(context, tempDataInterface);
			}

			maxID = loader.getMaxID();
			dataInterface.setData(tempDataInterface);

		} catch (UserAskToChangeFileException cfe) {
			throw cfe;
		} catch (Exception e) {
			ExportXmlFile action = null;

			if (wasXmlError) {
				action = new ExportXmlFile(this);
			}
			context.notifyBadException("cannot load data from xml file", e, false, action);
			throw new RuntimeException(e);
		}
	}

	public void save(PvpDataInterface dataInterface) {
		new PvpFileSaver(context).save(dataInterface.getTypes(), dataInterface.getRecords());
	}

	public int getNextMaxID() {
		maxID++;
		return maxID;
	}
	
	private void createDefaultStarterFile(final File destinationFile) throws IOException {
		if (PvpContext.JAR_BUILD) {
			// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
			InputStream sourceStream = PvpContext.class.getResourceAsStream("/datafiles/starter-rem-this-data.xml");
			BCUtil.copyFile(sourceStream, destinationFile);
		} else {
			File sourceFile = new File("datafiles/starter-rem-this-data.xml");
			System.out.println("sourceFile=" + sourceFile.getAbsolutePath());
			BCUtil.copyFile(sourceFile, destinationFile);
		}
	}

}
