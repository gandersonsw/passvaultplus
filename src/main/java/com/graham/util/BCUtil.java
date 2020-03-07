/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.jdom2.Element;
import org.jdom2.Text;

/**
 * @author graham
 *
 */
public class BCUtil {

	protected static Font  bodyFont;

	public static void setFrameSizeAndCenter(java.awt.Component c, int width, int height) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0002");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = dim.getWidth();//1024;
		double screenHeight = dim.getWidth();//768;
		
		c.setSize(width, height);
		c.setLocation((int)((screenWidth - width) / 2), (int)((screenHeight - height) / 3));
	}
	
	public static void center(java.awt.Component c) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = dim.getWidth();
		double screenHeight = dim.getHeight();
		Dimension d = c.getSize();

		if (d.getWidth() > screenWidth - 80 || d.getHeight() > screenHeight - 120) {
			d.setSize(d.getWidth() > screenWidth - 80 ? screenWidth - 80 : d.getWidth(), d.getHeight() > screenHeight - 120 ? screenHeight - 120 : d.getHeight());
			c.setSize(d);
		}
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0003");
		c.setLocation((int)((screenWidth - d.getWidth()) / 2), (int)((screenHeight - d.getHeight()) / 3));
	}

	public static void center(java.awt.Component c, JFrame owner) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0004");
			if (owner == null) {
					center(c);
			} else {
					Point loc = owner.getLocationOnScreen();
					Dimension osize = owner.getSize();
					Dimension size = c.getSize();
					c.setLocation(Math.max(loc.x + (int)((osize.width - size.width) / 2), 1), Math.max(loc.y + (int)((osize.height - size.height) / 3), 1));
			}
	}

	public static Font getBodyFont() {
		if (bodyFont == null) {
			bodyFont = new Font("Lucida Grande", Font.PLAIN, 10);
			if (bodyFont == null) {
				bodyFont = new Font("SansSerif", Font.PLAIN, 10);
			}
		}
		return bodyFont;
	}
	
	public static void makeButtonSmall(final JButton b) {
		b.putClientProperty("JComponent.sizeVariant", "small");
	}

	public static String getExceptionTrace(Exception e) {
		if (e == null)
			return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		e.printStackTrace(pw);
		//pw.close();
		return sw.toString();
	}

	public static String replaceAll(String s, String searchString, String replaceWithString) {
		
		if (s.indexOf(searchString) == -1)
			return s;
		
		StringBuffer ret = new StringBuffer();
		
		int i = 0;
		int previ = 0;
		while ((i = s.indexOf(searchString, i)) != -1) {
			ret.append(s.substring(previ,i));
			ret.append(replaceWithString);
			i += searchString.length();
			previ = i;
		}
	
		ret.append(s.substring(previ));
		return ret.toString();
	}
	
	public static String getPluralAppendix(final int count) {
		if (count == 1) {
			return "";
		}
		return "s";
	}
	
	public static String makeXMLSafe(final String txt) {
		if (txt == null) {
			return "";
		}
		String ret = txt;
		ret = replaceAll(ret,"&","&amp;");
		ret = replaceAll(ret,"<","&lt;");
		ret = replaceAll(ret,">","&gt;");
		ret = replaceAll(ret,"\"","&quot;");
		return ret;
	}
	
	public static String unmakeXMLSafe(final String txt) {
		if (txt == null) {
			return "";
		}
		String ret = txt;
		ret = replaceAll(ret,"&lt;","<");
		ret = replaceAll(ret,"&gt;",">");
		ret = replaceAll(ret,"&quot;","\"");
		ret = replaceAll(ret,"&amp;","&");
		return ret;
	}
	
	/**
	 * Convert "first-name" to "First Name"
	 */
	public static String unmakeXMLName(final String txt) {
		String ret = txt.trim();
		int dashLoc;
		while ((dashLoc = ret.indexOf("-")) != -1) {
		//	String aa = ret.substring(0, dashLoc);
		//	String bb = ret.substring(dashLoc + 1, dashLoc + 2);
		//	String cc = ret.substring(dashLoc + 2);
			
			if (dashLoc + 2 > ret.length()) {
				ret = ret.substring(0, dashLoc) + " ";
			} else {
				ret = ret.substring(0, dashLoc) + " " + ret.substring(dashLoc + 1, dashLoc + 2).toUpperCase() + ret.substring(dashLoc + 2);
			}
		}
		ret = ret.substring(0,1).toUpperCase() + ret.substring(1);
		return ret;
	}
	
	/**
	 * Convert "First Name" to "first-name"
	 */
	public static String makeXMLName(final String txt) {
		String ret = txt;
		ret = replaceAll(ret," ","-");
		ret = ret.toLowerCase();
		return ret;
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
    
    public static void addOrSetContent(Element parent, String elementName, String text) {
        Element e = parent.getChild(elementName);
        Text t = new Text(text);
        if (e == null) {
            e = new Element(elementName);
            e.setContent(t);
            parent.addContent(e);
        } else {
            e.setContent(t);
        }
    }

	public static String dumpInputStreamToString(InputStream s) throws IOException {
		byte b[] = new byte[1024];
		int bytes;
		StringBuilder sb = new StringBuilder();
		while ((bytes = s.read(b)) > 0) {
			String str = new String(b, 0, bytes);
			sb.append(str);
		}
		return sb.toString();
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
	
	public static int dataInString(String s) {
		byte[] sbytes = s.getBytes();
		
		int[] counts = new int[256];
		for (byte b : sbytes) {
			int bi = (int)b + 128;
			counts[bi]++;
		}
		int numberOfValues = 0;
		for (int count : counts) {
	    	if (count > 0) {
	    		numberOfValues++;
	    	}
		}
		return (int)((Math.log(numberOfValues + 1) / Math.log(2)) * s.length());
	}
}
