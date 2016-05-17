// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.resourceloader;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;

import org.columba.core.io.DiskIO;

public class ImageLoader {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.core.resourceloader");
	
	static boolean ICON_SET = false;

	private static ResourceBundle bundle;

	private static Properties properties;

	private static String iconset;

	private static Hashtable hashtable = new Hashtable();

	// ******** FOLLOWS STANDARD RESOURCE RETRIEVAL (file or jar protocol)
	// ***************
	public static ImageIcon getUnsafeImageIcon(String name) {
		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			return null;
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	// this is revised and may be used !
	public static ImageIcon getSmallImageIcon(String name) {
		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage_small.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	public static ImageIcon getImageIcon(String name) {
		URL url;

		if (hashtable.containsKey(name) == true) {
			return (ImageIcon) hashtable.get(name);
		}

		url = DiskIO.getResourceURL("org/columba/core/images/" + name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}
	
	public static ImageIcon getImageIconResource(String name) {
		URL url;

		url = DiskIO.getResourceURL(name);

		if (url == null) {
			url = DiskIO
					.getResourceURL("org/columba/core/images/brokenimage.png");
		}

		ImageIcon icon = new ImageIcon(url);

		hashtable.put(name, icon);

		return icon;
	}

	// ******** FOLLOWS SPECIALIZED ZIP-FILE EXTRACTION
	// *************************
	// load image out of jar/zip file
	public static synchronized Image loadImage(File zipFile, String entry) {
		byte[] bytes = loadBytes(zipFile, entry);
		Image image = Toolkit.getDefaultToolkit().createImage(bytes);

		return image;
	}

	// load image out of jar/zip file
	public static synchronized Properties loadProperties(File zipFile,
			String entry) {
		byte[] bytes = loadBytes(zipFile, entry);

		ByteArrayInputStream input = new ByteArrayInputStream(bytes);

		Properties properties = new Properties();

		try {
			properties.load(input);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return properties;
	}

	// load byte-array out of jar/zip file
	protected static synchronized byte[] loadBytes(File zipFile, String entry) {
		byte[] bytes = null;

		try {
			ZipFile zipfile = new ZipFile(zipFile);
			ZipEntry zipentry = zipfile.getEntry(entry);

			if (zipentry != null) {
				long size = zipentry.getSize();

				if (size > 0) {
					bytes = new byte[(int) size];

					InputStream in = new BufferedInputStream(zipfile
							.getInputStream(zipentry));
					in.read(bytes);
					in.close();
				}
			}
		} catch (ZipException e) {
			LOG.severe(e.getMessage());

			return null;
		} catch (IOException e) {
			LOG.severe(e.getMessage());

			return null;
		}

		return bytes;
	}

}