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
package org.openlegacy.plugins;

import org.openlegacy.exceptions.OpenLegacyException;

import java.util.List;

public interface PluginsRegistry {

	public static final String PLUGINS_REGISTRY_BEAN_ID = "pluginsRegistry";

	public Plugin getPlugin(String pluginName);

	public void addPlugin(Plugin plugin) throws OpenLegacyException;

	public void putPlugin(Plugin plugin);

	public void clear();

	public List<Plugin> getPlugins();

	public List<String> getSpringWebContextResources();

	public List<String> getSpringContextResources();

	public boolean isEmpty();

	public List<String> getViewDeclarations();

	public void extractViews(String rootPath) throws OpenLegacyException;
}
