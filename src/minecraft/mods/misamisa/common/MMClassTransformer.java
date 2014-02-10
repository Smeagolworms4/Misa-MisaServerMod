package mods.misamisa.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

public class MMClassTransformer implements IClassTransformer {
	private ZipFile ofZipFile = null;

	public MMClassTransformer() {
		try {
			URLClassLoader ucl = (URLClassLoader) MMClassTransformer.class
					.getClassLoader();

			URL[] urls = ucl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];

				ZipFile zipFile = getOptiFineZipFile(url);
				if (zipFile != null) {
					this.ofZipFile = zipFile;
					dbg("Misa Misa Server ClassTransformer");
					dbg("Misa Misa Server URL: " + url);
					dbg("Misa Misa Server ZIP file: " + zipFile);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.ofZipFile == null) {
			dbg("*** Can not find the Custom Steeve Server JAR in the classpath ***");
			dbg("*** Misa Misa Server will not be loaded! ***");
		}
		
	}
	
	private static ZipFile getOptiFineZipFile(URL url) {
		try {
			URI uri = url.toURI();

			File file = new File(uri);

			ZipFile zipFile = new ZipFile(file);

			if (zipFile.getEntry("mods/misamisa/common/MMClassTransformer.class") == null) {
				zipFile.close();
				return null;
			}

			return zipFile;
		} catch (Exception localException) {
		}

		return null;
	}
  

	public byte[] transform(String name, String transformedName, byte[] bytes) {
		byte[] ofBytes = getCustomClass(name);
		if (ofBytes != null) {
			return ofBytes;
		}

		return bytes;
	}

	private byte[] getCustomClass(String name) {
		
		if (this.ofZipFile == null) {
			return null;
		}
		String fullName = name + ".class";
		ZipEntry ze = this.ofZipFile.getEntry(fullName);
		if (ze == null) {
			return null;
		}
		try {
			InputStream in = this.ofZipFile.getInputStream(ze);
			byte[] bytes = readAll(in);

			if (bytes.length != ze.getSize()) {
				dbg("Invalid size for " + fullName + ": " + bytes.length
						+ ", should be: " + ze.getSize());
				return null;
			}

			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readAll(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (true) {
			int len = is.read(buf);
			if (len < 0)
				break;
			baos.write(buf, 0, len);
		}

		is.close();

		byte[] bytes = baos.toByteArray();

		return bytes;
	}
	
	
	private static void dbg(String str) {
		System.out.println(str);
	}
}