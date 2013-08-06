package org.easycluster.easycluster.serialization.protocol.meta;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(PackageUtil.class);

	private static final List<String> EMPTY_LIST = new ArrayList<String>();

	public static String[] findClassesInPackage(String packageName,
			List<String> included, List<String> excluded) throws IOException {
		String packageOnly = packageName;
		boolean recursive = false;
		if (packageName.endsWith(".*")) {
			packageOnly = packageName.substring(0,
					packageName.lastIndexOf(".*"));
			recursive = true;
		}

		List<String> vResult = new ArrayList<String>();
		String packageDirName = packageOnly.replace('.', '/');
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (logger.isDebugEnabled()) {
			logger.debug("using classloader: " + cl);
		}
		Enumeration<URL> dirs = cl.getResources(packageDirName);
		if (logger.isDebugEnabled()) {
			logger.debug("PackageUtil: getResources: " + dirs
					+ ", hasMoreElements:" + dirs.hasMoreElements());
		}
		while (dirs.hasMoreElements()) {
			URL url = dirs.nextElement();
			String protocol = url.getProtocol();
			// if(!matchTestClasspath(url, packageDirName, recursive)) {
			// continue;
			// }
			if (logger.isDebugEnabled()) {
				logger.debug("PackageUtil: url: " + url);
			}

			if ("file".equals(protocol)) {
				findClassesInDirPackage(packageOnly, included, excluded,
						URLDecoder.decode(url.getFile(), "UTF-8"), recursive,
						vResult);
			} else if ("jar".equals(protocol)) {
				JarFile jar = ((JarURLConnection) url.openConnection())
						.getJarFile();
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.charAt(0) == '/') {
						name = name.substring(1);
					}
					if (name.startsWith(packageDirName)) {
						int idx = name.lastIndexOf('/');
						if (idx != -1) {
							packageName = name.substring(0, idx).replace('/',
									'.');
						}

						if (logger.isDebugEnabled()) {
							logger.debug("PackageUtil: Package name is "
									+ packageName);
						}

						// Utils.log("PackageUtil", 4, "Package name is " +
						// packageName);
						if ((idx != -1) || recursive) {
							// it's not inside a deeper dir
							if (name.endsWith(".class") && !entry.isDirectory()) {
								String className = name.substring(
										packageName.length() + 1,
										name.length() - 6);

								if (logger.isDebugEnabled()) {
									logger.debug("PackageUtil: Found class "
											+ className
											+ ", seeing it if it's included or excluded");
								}
								includeOrExcludeClass(packageName, className,
										included, excluded, vResult);
							}
						}
					}
				}
			}
		}

		String[] result = vResult.toArray(new String[vResult.size()]);
		return result;
	}

	private static void findClassesInDirPackage(String packageName,
			List<String> included, List<String> excluded, String packagePath,
			final boolean recursive, List<String> classes) {
		File dir = new File(packagePath);

		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}

		File[] dirfiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});

		if (logger.isDebugEnabled()) {
			logger.debug("PackageUtil: Looking for test classes in the directory: "
					+ dir);
		}
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findClassesInDirPackage(packageName + "." + file.getName(),
						included, excluded, file.getAbsolutePath(), recursive,
						classes);
			} else {
				String className = file.getName().substring(0,
						file.getName().length() - 6);

				if (logger.isDebugEnabled()) {
					logger.debug("PackageUtil: Found class " + className
							+ ", seeing it if it's included or excluded");
				}
				includeOrExcludeClass(packageName, className, included,
						excluded, classes);
			}
		}
	}

	private static void includeOrExcludeClass(String packageName,
			String className, List<String> included, List<String> excluded,
			List<String> classes) {
		if (isIncluded(className, included, excluded)) {
			if (logger.isDebugEnabled()) {
				logger.debug("PackageUtil: ... Including class " + className);
			}
			classes.add(packageName + '.' + className);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("PackageUtil: ... Excluding class " + className);
			}
		}
	}

	private static boolean isIncluded(String name, List<String> included,
			List<String> excluded) {
		boolean result = false;

		//
		// If no includes nor excludes were specified, return true.
		//
		if (null == included) {
			included = EMPTY_LIST;
		}

		if (null == excluded) {
			excluded = EMPTY_LIST;
		}

		if (included.size() == 0 && excluded.size() == 0) {
			result = true;
		} else {
			boolean isIncluded = PackageUtil.find(name, included);
			boolean isExcluded = PackageUtil.find(name, excluded);
			if (isIncluded && !isExcluded) {
				result = true;
			} else if (isExcluded) {
				result = false;
			} else {
				result = included.size() == 0;
			}
		}
		return result;
	}

	private static boolean find(String name, List<String> list) {
		
		for (String regexpStr : list) {
			if (Pattern.matches(regexpStr, name))
				return true;
		}
		return false;
	}
}
