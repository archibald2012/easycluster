/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.meta;

import java.io.IOException;
import java.util.Collection;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.meta.PackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMetainfoUtils {

	private static final Logger	logger	= LoggerFactory.getLogger(BeanMetainfoUtils.class);

	public static Int2TypeMetainfo createTypeMetainfo(Collection<String> packages, Transformer<Class<?>, Integer> transformer) {
		DefaultInt2TypeMetainfo typeMetainfo = new DefaultInt2TypeMetainfo();

		if (null != packages) {
			for (String pkgName : packages) {
				try {
					String[] clsNames = PackageUtil.findClassesInPackage(pkgName, null, null);
					for (String clsName : clsNames) {
						try {
							ClassLoader cl = Thread.currentThread().getContextClassLoader();
							if (logger.isDebugEnabled()) {
								logger.debug("using ClassLoader {} to load Class {}", cl, clsName);
							}
							Class<?> cls = cl.loadClass(clsName);
							Integer value = transformer.transform(cls);
							if (null != value) {
								typeMetainfo.add(value, cls);
								logger.info("metainfo: add " + value + ":=>" + cls);
							}
						} catch (ClassNotFoundException e) {
							logger.error("createTypeMetainfo: ", e);
						}
					}
				} catch (IOException e) {
					logger.error("createTypeMetainfo: ", e);
				}
			}
		}

		return typeMetainfo;
	}

	public static Int2TypeMetainfo createTypeMetainfoByClasses(Collection<Class<?>> clazzes, Transformer<Class<?>, Integer> transformer) {
		DefaultInt2TypeMetainfo typeMetainfo = new DefaultInt2TypeMetainfo();

		for (Class<?> cls : clazzes) {
			Integer value = transformer.transform(cls);
			if (null != value) {
				typeMetainfo.add(value, cls);
			}
		}

		return typeMetainfo;
	}
}
