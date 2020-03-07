/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class FileUtil {
	/**
	 * Get the file name without a suffix, and the file suffix.
	 * Will always return an array length 2. 1st item is name, 2nd item is file-suffix
	 */
	public static String[] getFileNameParts(final String fileName) {
		int dotLoc = fileName.indexOf(".");
		if (dotLoc == -1) {
			String[] ret = { fileName, ""};
			return ret;
		}

		String fileNameWithNoSuffix = fileName.substring(0, dotLoc);
		String fileSuffix = fileName.substring(dotLoc + 1);
		String[] ret = { fileNameWithNoSuffix, fileSuffix};
		return ret;
	}


	public static String getFileNameNoExt(String fileName, boolean includeAllExtentions) {
		if (fileName.indexOf(".", 1) == -1) {
			return fileName;
		} else if (includeAllExtentions) {
			return fileName.substring(0, fileName.indexOf(".", 1));
		} else {
			return fileName.substring(0, fileName.lastIndexOf(".", 1));
		}
	}

	public static String setFileExt(String fileName, String ext, boolean includeAllExtentions) {
		return getFileNameNoExt(fileName, includeAllExtentions) + "." + ext;
	}



	public static String getFileExtension(String fileName, boolean includeAllExtentions) {
		if (fileName.indexOf(".") == -1) {
			return "";
		} else if (includeAllExtentions) {
			return fileName.substring(fileName.indexOf(".") + 1);
		} else {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		}
	}

	public static void dumpStringToFile(String s, File f) throws FileNotFoundException {
		final PrintWriter pw = new PrintWriter(f);
		pw.print(s);
		pw.close();
	}


	public static void copyFile(final InputStream sourceStream, final File destinationFile) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destinationFile);
			copyFile(sourceStream, fos);
		} finally {
			if (fos != null) {
				try { fos.close(); } catch (Exception e) { }
			}
		}
	}

	public static void copyFile(final InputStream sourceStream, final OutputStream destStream) throws IOException {
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = sourceStream.read(buffer)) > 0) {
			destStream.write(buffer, 0, length);
		}
	}

	public static void copyFile(final File sourceFile, final File destinationFile) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(destinationFile);
			copyFile(fis, fos);
		} finally {
			if (fis != null) {
				try { fis.close(); } catch (Exception e) { }
			}
			if (fos != null) {
				try { fos.close(); } catch (Exception e) { }
			}
		}
	}
}
