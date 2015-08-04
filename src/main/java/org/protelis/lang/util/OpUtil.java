/*******************************************************************************
 * Copyright (C) 2010, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package org.protelis.lang.util;

/**
 * @author Danilo Pianini
 *
 */
public final class OpUtil {

	private static final int INITIAL_SIZE = 128;
	
	private OpUtil() {
	}

	public static <T> T unsupported(final String op, final Object... a) {
		final StringBuilder msg = new StringBuilder(INITIAL_SIZE);
		msg.append("Nobody told me how to run ");
		msg.append(op);
		if (a.length > 0) {
			msg.append(" with parameters of class: ");
			for (final Object o : a) {
				msg.append(o == null ? "null" : o.getClass().getSimpleName());
				msg.append(", ");
			}
			msg.delete(msg.length() - 2, msg.length());
			msg.append('.');
		}
		throw new UnsupportedOperationException(msg.toString());
	}	
}
