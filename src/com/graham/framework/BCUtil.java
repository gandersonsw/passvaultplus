/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.jdom.Element;
import org.jdom.Text;

/**
 * @author graham
 *
 */
public class BCUtil {

	protected static Font titleFont, bodyFont;

	/** replace exactly one occurence, if no occurence found, throw exception */
	public static String replaceOne(String src, String searchFor, String replaceWith) {
		int i = src.indexOf(searchFor);
		if (i == -1) {
			throw new IllegalArgumentException("replaceOne requires search for string to be found in source string");
		}
		
		return src.substring(0,i) + replaceWith + src.substring(i + searchFor.length());
	}
	
	public static void setFrameSizeAndCenter(java.awt.Component c, int width, int height) {
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

		c.setLocation((int)((screenWidth - d.getWidth()) / 2), (int)((screenHeight - d.getHeight()) / 3));
	}
	
	public static Font getTitleFont() {
		if (titleFont == null) {
			titleFont = new Font("Lucida Grande", Font.BOLD, 14);
			if (titleFont == null) {
				titleFont = new Font("SansSerif", Font.BOLD, 14);
			}
		}
		return titleFont;
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
	
	public static void showStopDialog(Frame owner, String message, Exception e) {
		BCUtil u = new BCUtil();
		if (message == null)
			message = "An error has occured";
		u.showStopDialog2(owner, message, e);
	}
	
	private void showStopDialog2(Frame owner, String message, Exception e) {
		JDialog d;
		if (owner == null)
			d = new JDialog();
		else
			d = new JDialog(owner, true);
		
		d.getContentPane().setLayout(new BorderLayout());
		JTextArea txt = new JTextArea(message,4,30);
		txt.setBorder(new EmptyBorder(5,5,5,5));
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		txt.setEditable(false);
		d.getContentPane().add(txt, BorderLayout.NORTH);
		
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		cancelActionClass cancelAction = new cancelActionClass("Cancel", d);
		p.add(new JButton(cancelAction));
		if (e != null) {
			showDetailsActionClass showDetailsAction = new showDetailsActionClass("Show Details", d, e);
			p.add(new JButton(showDetailsAction));
			
			quitActionClass quitAction = new quitActionClass("Quit", d);
			p.add(new JButton(quitAction));
		}
		d.getContentPane().add(p, BorderLayout.SOUTH);
		setFrameSizeAndCenter(d,400,160);
		d.pack();
		d.setVisible(true);
		//d.show();
	}
	
	public String getExceptionTrace(Exception e) {
		if (e == null)
			return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		e.printStackTrace(pw);
		//pw.close();
		return sw.toString();
	}
	
	public class cancelActionClass extends AbstractAction {
		private static final long serialVersionUID = 6058864397827182800L;
		JDialog d;
		public cancelActionClass(String text, JDialog paramd) {
			super(text);
			d = paramd;
		}
		public void actionPerformed(ActionEvent e) {
			d.dispose();
		}
	}
	
	public class quitActionClass extends AbstractAction {
		private static final long serialVersionUID = 2886374665750304675L;
		JDialog d;
		public quitActionClass(String text, JDialog paramd) {
			super(text);
			d = paramd;
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
			//d.dispose();
		}
	}
	
	public class showDetailsActionClass extends AbstractAction {
		private static final long serialVersionUID = 6805643335234968742L;
		JDialog d;
		Exception exp;
		public showDetailsActionClass(String text, JDialog paramd, Exception e) {
			super(text);
			d = paramd;
			exp = e;
		}
		public void actionPerformed(ActionEvent evt) {
			JTextArea txt = new JTextArea(getExceptionTrace(exp));
			txt.setBorder(new EmptyBorder(5,5,5,5));
			txt.setLineWrap(false);
			txt.setEditable(false);
			d.getContentPane().add(txt, BorderLayout.CENTER);
			setEnabled(false);
			d.pack();
		}
	}

    public static void addListItem(StringBuffer sb, String newItem) {
    		if (sb.length() > 0)
    			sb.append(',');
    		sb.append(newItem);
    }
    
    public static List<String> convertListToArray(String s) {
    		List<String> arr = new ArrayList<String>();
    		
    		if (s == null || s.length() == 0)
    			return arr;
    	
    		int i = 0;
    		int j;
    		
    		while ((j = s.indexOf(",",i)) != -1) {
    			arr.add(decodeListItem(s.substring(i,j)));
    			i = j+1;
    		}
    		
    		arr.add(decodeListItem(s.substring(i)));
    		
    		return arr;
    }
    		
    	private static String decodeListItem(String s) {
    		return s;
    	}
    	
    	public static String simpleEncrypt(String s) {
    		int i1 = 1;
    		int i2 = 2;
    		int tmp;
    		StringBuffer ret = new StringBuffer();
    		
    		if (s.length() > 3) {
    			s = "k" + s.charAt(3) + s.charAt(1) + s.charAt(2) + s.charAt(0) + s.substring(4);
    		}
    		
    		for (int j = 0; j < s.length(); j++) {
    			
    			char c = s.charAt(j);
    			char c2;
    			
    			
    			if (Character.isDigit(c)) {
    				c2 = (char)('9' - c + '0');
    				
    			} else if (Character.isLetter(c)) {
    				if (Character.isLowerCase(c)) {
    					c2 = (char)('z' - c + 'a');
    				} else {
    					c2 = (char)('Z' - c + 'A');
    				}
    			} else {
    				c2 = c;
    			}
    			
    			ret.append(c2);
    			
    			if (j == i1) {
    				tmp = i1;
    				i1 = i2+i1;
    				i2 = tmp;
    				char c3 = (char)((67 + tmp) % 26 + 'a');
    				ret.append(c3);
    			}
    		}
    		
    		return "se" + ret.toString();
    	}
    	
    	public static String simpleDecrypt(String s) {
      	int i1 = 1;
    		int i2 = 2;
    		int i1offset = 0; 
    		boolean skipThisSkip = false;
    		int tmp;
    		StringBuffer ret = new StringBuffer();
    		
    		if (!s.startsWith("se"))
    			return "error:must start with se";
    		
    		s = s.substring(2);
    		
    		for (int j = 0; j < s.length(); j++) {
    			
    			char c = s.charAt(j);
    			char c2;
    			
    			if (Character.isDigit(c)) {
    				c2 = (char)('9' - c + '0');
    				
    			} else if (Character.isLetter(c)) {
    				if (Character.isLowerCase(c)) {
    					c2 = (char)('z' - c + 'a');
    				} else {
    					c2 = (char)('Z' - c + 'A');
    				}
    			} else {
    				c2 = c;
    			}
    			
    			if (skipThisSkip) {
    				skipThisSkip = false;
    			} else {
	    			if (j == i1+i1offset) {
	    				skipThisSkip = true;
	    				i1offset++;
	    				tmp = i1;
	    				i1 = i2+i1;
	    				i2 = tmp;
	    			} else {
	    				//ret.append(c2);
	    			}
	    			ret.append(c2);
    			}
    			
    		}
    		
    		s = ret.toString();
    		
    		if (s.length() > 4) {
    			s = "" + s.charAt(4) + s.charAt(2) + s.charAt(3) + s.charAt(1) + s.substring(5);
    		}
    		
    		return s;
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
		String ret = txt;
		ret = replaceAll(ret,"&","&amp;");
		ret = replaceAll(ret,"<","&lt;");
		ret = replaceAll(ret,">","&gt;");
		ret = replaceAll(ret,"\"","&quot;");
		return ret;
	}
	
	public static String unmakeXMLSafe(final String txt) {
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
    
    public static void dumpInputStream(InputStream s) {
		byte b[] = new byte[1024];
		int bytes;
		try {
			while ((bytes = s.read(b)) > 0) {
				System.out.write(b, 0, bytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String dumpInputStreamToString(InputStream s) {
		byte b[] = new byte[1024];
		int bytes;
		StringBuilder sb = new StringBuilder();
		try {
			while ((bytes = s.read(b)) > 0) {
				String str = new String(b, 0, bytes);
				sb.append(str);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static void dumpStringToFile(String s, File f) {
		try {
			PrintWriter pw = new PrintWriter(f);
			pw.print(s);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

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
