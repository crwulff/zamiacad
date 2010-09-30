/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 10, 2009
 */
package org.zamia.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

/**
 * Most of this is inspired by JNA
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class Native {

	public enum OSType {
		UNKNOWN, MAC, LINUX, WINDOWS, SOLARIS, FREEBSD, OPENBSD, AIX, HPUX
	};

	private static final OSType osType;

	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Linux")) {
			osType = OSType.LINUX;
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
			osType = OSType.MAC;
		} else if (osName.startsWith("AIX")) {
			osType = OSType.AIX;
		} else if (osName.startsWith("Windows")) {
			osType = OSType.WINDOWS;
		} else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
			osType = OSType.SOLARIS;
		} else if (osName.startsWith("FreeBSD")) {
			osType = OSType.FREEBSD;
		} else if (osName.startsWith("OpenBSD")) {
			osType = OSType.OPENBSD;
		} else if (osName.startsWith("HP-UX")) {
			osType = OSType.HPUX;
		} else {
			osType = OSType.UNKNOWN;
		}
	}

	private Native() {
	}

	public static final OSType getOSType() {
		return osType;
	}

	public static final boolean isMac() {
		return osType == OSType.MAC;
	}

	public static final boolean isLinux() {
		return osType == OSType.LINUX;
	}

	public static final boolean isWindows() {
		return osType == OSType.WINDOWS;
	}

	public static final boolean isSolaris() {
		return osType == OSType.SOLARIS;
	}

	public static final boolean isFreeBSD() {
		return osType == OSType.FREEBSD;
	}

	public static final boolean isOpenBSD() {
		return osType == OSType.OPENBSD;
	}

	public static final boolean isAIX() {
		return osType == OSType.AIX;
	}

	public static final boolean isHPUX() {
		return osType == OSType.HPUX;
	}

	public static final boolean isX11() {
		return !Native.isWindows() && !Native.isMac();
	}

	public static final boolean deleteNativeLibraryAfterVMExit() {
		return osType == OSType.WINDOWS;
	}

	public static final boolean is64Bit() {
		String model = System.getProperty("sun.arch.data.model");
		if (model != null)
			return "64".equals(model);
		String arch = System.getProperty("os.arch").toLowerCase();
		if ("x86_64".equals(arch) || "ppc64".equals(arch) || "sparcv9".equals(arch) || "amd64".equals(arch)) {
			return true;
		}
		return false;
	}

	private static String nativeLibraryPath = null;

	private static boolean unpacked;

	//	private static final Object finalizer = new Object() {
	//		protected void finalize() {
	//			deleteNativeLibrary();
	//		}
	//	};

	/** Remove any unpacked native library.  Forcing the class loader to
	    unload it first is required on Windows, since the temporary native
	    library can't be deleted until the native library is unloaded.  Any
	    deferred execution we might install at this point would prevent the
	    Native class and its class loader from being GC'd, so we instead force
	    the native library unload just a little bit prematurely.
	 */
	@SuppressWarnings("unchecked")
	private static boolean deleteNativeLibrary() {
		String path = nativeLibraryPath;
		if (path == null || !unpacked)
			return true;
		File flib = new File(path);
		if (flib.delete()) {
			nativeLibraryPath = null;
			unpacked = false;
			return true;
		}
		// Reach into the bowels of ClassLoader to force the native
		// library to unload
		try {
			ClassLoader cl = Native.class.getClassLoader();
			Field f = ClassLoader.class.getDeclaredField("nativeLibraries");
			f.setAccessible(true);
			List libs = (List) f.get(cl);
			for (Iterator i = libs.iterator(); i.hasNext();) {
				Object lib = i.next();
				f = lib.getClass().getDeclaredField("name");
				f.setAccessible(true);
				String name = (String) f.get(lib);
				if (name.equals(path) || name.indexOf(path) != -1) {
					Method m = lib.getClass().getDeclaredMethod("finalize", new Class[0]);
					m.setAccessible(true);
					m.invoke(lib, new Object[0]);
					nativeLibraryPath = null;
					if (unpacked) {
						if (flib.exists()) {
							if (flib.delete()) {
								unpacked = false;
								return true;
							}
							return false;
						}
					}
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/** For internal use only. */
	public static class DeleteNativeLibrary extends Thread {
		private final File file;

		public DeleteNativeLibrary(File file) {
			this.file = file;
		}

		public void run() {
			// If we can't force an unload/delete, spawn a new process
			// to do so
			if (!deleteNativeLibrary()) {
				try {
					Runtime.getRuntime().exec(
							new String[] { System.getProperty("java.home") + "/bin/java", "-cp", System.getProperty("java.class.path"), getClass().getName(),
									file.getAbsolutePath(), });
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static String getNativeLibraryResourcePath(OSType osType, String arch, String name) {
		String osPrefix;
		arch = arch.toLowerCase();
		switch (osType) {
		case WINDOWS:
			if ("i386".equals(arch))
				arch = "x86";
			osPrefix = "win32-" + arch;
			break;
		case MAC:
			osPrefix = "darwin";
			break;
		case LINUX:
			if ("i386".equals(arch) || "i686".equals(arch)) {
				arch = "x86";
			} else if ("amd64".equals(arch)) {
				arch = "x86_64";
			}
			osPrefix = "linux-" + arch;
			break;
		case AIX:
			if ("ppc64".equals(arch)) {
				arch = "ppc64";
			} else {
				arch = "power";
			}
			osPrefix = "aix-" + arch;
			break;
		case HPUX:
			if ("PA-RISC".equals(arch)) {
				arch = "parisc";
			} else {
				arch = "itanium";
			}
			osPrefix = "hpux-" + arch;
			break;
		case SOLARIS:
			osPrefix = "sunos-" + arch;
			break;
		default:
			osPrefix = name.toLowerCase();
			if ("i386".equals(arch) || "i686".equals(arch)) {
				arch = "x86";
			}
			if ("x86_64".equals(arch)) {
				arch = "x86_64";
			}
			if ("powerpc".equals(arch)) {
				arch = "ppc";
			}
			int space = osPrefix.indexOf(" ");
			if (space != -1) {
				osPrefix = osPrefix.substring(0, space);
			}
			osPrefix += "-" + arch;
			break;
		}
		//return "/native/" + osPrefix;
		return "/" + osPrefix;
	}

	@SuppressWarnings("deprecation")
	public static void loadNativeLibrary(String aLibraryName) {
		String libname = System.mapLibraryName(aLibraryName);
		String arch = System.getProperty("os.arch");
		String name = System.getProperty("os.name");
		String resourceName = getNativeLibraryResourcePath(Native.getOSType(), arch, name) + "/" + libname;
		URL url = Native.class.getResource(resourceName);

		// Add an ugly hack for OpenJDK (soylatte) - JNI libs use the usual .dylib extension
		if (url == null && Native.isMac() && resourceName.endsWith(".dylib")) {
			resourceName = resourceName.substring(0, resourceName.lastIndexOf(".dylib")) + ".jnilib";
			url = Native.class.getResource(resourceName);
		}
		if (url == null) {
			throw new UnsatisfiedLinkError("Native (" + resourceName + ") not found in resource path");
		}

		File lib = null;
		if (url.getProtocol().toLowerCase().equals("file")) {
			// NOTE: use older API for 1.3 compatibility
			lib = new File(URLDecoder.decode(url.getPath()));
		} else {
			InputStream is = Native.class.getResourceAsStream(resourceName);
			if (is == null) {
				throw new Error("Can't obtain jnidispatch InputStream");
			}

			FileOutputStream fos = null;
			try {
				// Suffix is required on windows, or library fails to load
				// Let Java pick the suffix, except on windows, to avoid
				// problems with Web Start.
				lib = File.createTempFile("zamiaJNI", Native.isWindows() ? ".dll" : null);
				lib.deleteOnExit();
				ClassLoader cl = Native.class.getClassLoader();
				if (Native.deleteNativeLibraryAfterVMExit() && (cl == null || cl.equals(ClassLoader.getSystemClassLoader()))) {
					Runtime.getRuntime().addShutdownHook(new DeleteNativeLibrary(lib));
				}
				fos = new FileOutputStream(lib);
				int count;
				byte[] buf = new byte[1024];
				while ((count = is.read(buf, 0, buf.length)) > 0) {
					fos.write(buf, 0, count);
				}
			} catch (IOException e) {
				throw new Error("Failed to create temporary file for jnidispatch library: " + e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
			unpacked = true;
		}
		System.load(lib.getAbsolutePath());
		nativeLibraryPath = lib.getAbsolutePath();
	}

}
