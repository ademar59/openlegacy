/*******************************************************************************
 * Copyright (c) 2014 OpenLegacy Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     OpenLegacy Inc. - initial API and implementation
 *******************************************************************************/
package org.openlegacy.designtime.analyzer.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.designtime.analyzer.TextTranslator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Map;

public class CharReplaceTranslator implements TextTranslator, InitializingBean {

	private Map<String, String> charsToReplace;

	private final static Log logger = LogFactory.getLog(CharReplaceTranslator.class);

	@Override
	public String translate(String text) {
		StringBuilder sb = new StringBuilder(text);
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			String newChar = charsToReplace.get(String.valueOf(chars[i]));
			if (newChar != null) {
				sb.setCharAt(i, newChar.charAt(0));
			}
		}
		String result = sb.toString();
		if (logger.isDebugEnabled() && !text.equals(result)) {
			logger.debug(MessageFormat.format("Translated \"{0}\" to \"{1}\"", text, result));
		}
		return result;
	}

	public void setCharsToReplace(Map<String, String> charsToReplace) {
		this.charsToReplace = charsToReplace;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(charsToReplace);
	}
}
