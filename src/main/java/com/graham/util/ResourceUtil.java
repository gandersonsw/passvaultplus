/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ResourceUtil {
	static final public boolean JAR_BUILD = true; // TODO i think this can be removed

	public static BiConsumer<Exception, String> exceptionHandler;

	public static void setExceptionHandler(BiConsumer<Exception, String> eh) {
		exceptionHandler = eh;
	}

	public static BiConsumer<Exception, String> getExceptionHandler() {
		if (exceptionHandler == null) {
			exceptionHandler = (e, rname) -> {
				System.out.println("Error loading resource: " + rname);
				e.printStackTrace();
			};
		}
		return exceptionHandler;
	}

	@FunctionalInterface
	public interface IOFunction<R> {
		R apply(java.io.BufferedReader t) throws IOException;
	}

	public static <T> T processResourceTextStream(final String rname, IOFunction<T> func) {
		InputStream sourceStream = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		try {
			if (JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = ResourceUtil.class.getResourceAsStream("/" + rname + ".txt");
				isr = new InputStreamReader(sourceStream);
			} else {
				File sourceFile = new File("src/main/resources/" + rname + ".txt");
				isr = new FileReader(sourceFile);
			}

			bufR = new BufferedReader(isr);

			return func.apply(bufR);
		} catch (Exception e) {
			getExceptionHandler().accept(e, rname);
			return null; // "";
		} finally {
			if (bufR != null) {
				try { bufR.close(); } catch (Exception e) { }
			}
			if (isr != null) {
				try { isr.close(); } catch (Exception e) { }
			}
			if (sourceStream != null) {
				try { sourceStream.close(); } catch (Exception e) { }
			}
		}
	}

	public static String getResourceText(final String rname) {
		String ret = processResourceTextStream(rname, bufR -> {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufR.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		});
		if (ret == null) {
			return "";
		}
		return ret;
	}

	private static final Map<String, ImageIcon> cachedIcons = new HashMap<>();

	public static ImageIcon getIcon(final String imageName) {
		return getIcon(imageName, 100);
	}

	public static ImageIcon getIcon(final String imageName, int scalePercent) {
		String cachedName = imageName + scalePercent;
		if (cachedIcons.containsKey(cachedName)) {
			return cachedIcons.get(cachedName);
		}
		try {
			Image img;
			if (JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
				InputStream imageStream = ResourceUtil.class.getResourceAsStream("/images/" + imageName + ".png");
				img = ImageIO.read(imageStream);
			} else {
				img = ImageIO.read(new File("src/main/resources/images/" + imageName + ".png"));
			}

			if (scalePercent != 100) {
				double r = scalePercent / 100.0;
				img = img.getScaledInstance((int)(r * img.getWidth(null)), (int)(r * img.getHeight(null)), Image.SCALE_SMOOTH);
			}
			final ImageIcon i = new ImageIcon(img);
			cachedIcons.put(cachedName, i);
			return i;
		} catch (Exception e) {
			getExceptionHandler().accept(e, "image:" + imageName);
			return null;
		}
	}
}
