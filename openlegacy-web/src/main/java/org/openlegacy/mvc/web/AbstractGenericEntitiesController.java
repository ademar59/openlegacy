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
package org.openlegacy.mvc.web;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.EntitiesRegistry;
import org.openlegacy.EntityDefinition;
import org.openlegacy.Session;
import org.openlegacy.definitions.ActionDefinition;
import org.openlegacy.definitions.FieldDefinition;
import org.openlegacy.layout.PageBuilder;
import org.openlegacy.modules.menu.Menu.MenuEntity;
import org.openlegacy.mvc.MvcUtils;
import org.openlegacy.mvc.OpenLegacyWebProperties;
import org.openlegacy.utils.ProxyUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceUtils;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenLegacy generic web controller for a session. Handles GET/POST requests of a web application. Works closely with
 * generic.jspx / composite.jspx. Saves the need for a dedicated controller and page for each screen API, if such doesn't exists.
 * 
 * @author Roi Mor
 * 
 */
public abstract class AbstractGenericEntitiesController<S extends Session> implements InitializingBean {

	protected static final String ACTION = "action";

	private final static Log logger = LogFactory.getLog(AbstractGenericEntitiesController.class);

	private static final String PAGE = "page";

	private static final String MAIN_MENU = "mainMenu";

	@Inject
	private S session;

	protected S getSession() {
		return session;
	}

	@Inject
	private EntitiesRegistry<?, ?, ?> entitiesRegistry;

	@Inject
	private ServletContext servletContext;

	private List<String> mobileViewsPaths = new ArrayList<String>();

	private List<String> webViewsPaths = new ArrayList<String>();

	private String viewsSuffix = ".jspx";

	@Inject
	private OpenLegacyWebProperties openlegacyWebProperties;

	@SuppressWarnings("rawtypes")
	@Inject
	private PageBuilder pageBuilder;

	@RequestMapping(value = "/{entity}", method = RequestMethod.GET)
	public String getEntity(@PathVariable("entity") String entityName,
			@RequestParam(value = "partial", required = false) String partial, Model uiModel, HttpServletRequest request)
			throws IOException {
		if (entityName.equals(MAIN_MENU)) {
			return MvcConstants.ROOTMENU_VIEW;
		}
		return getEntityWithKey(entityName, null, partial, uiModel, request);

	}

	@RequestMapping(value = "/{entity}/{key:[[\\w\\p{L}]+[:-_ ,]*[\\w\\p{L}]+]+}", method = RequestMethod.GET)
	public String getEntityWithKey(@PathVariable("entity") String entityName, @PathVariable("key") String key,
			@RequestParam(value = "partial", required = false) String partial, Model uiModel, HttpServletRequest request)
			throws IOException {

		try {
			// enable sending more then one key, concatenated with "+"
			Object[] keys = new Object[0];
			if (key != null) {
				String[] keysArr = key.split("\\+");
				keys = new Object[keysArr.length];
				List<? extends FieldDefinition> keyFields = entitiesRegistry.get(entityName).getKeys();
				for (int i = 0; i < keyFields.size(); i++) {
					keys[i] = keysArr[i];
					if (keyFields.get(i).getJavaType() == Integer.class) {
						keys[i] = Integer.valueOf(keysArr[i]);
					}
				}
			}
			Object entity = session.getEntity(entityName, keys);
			return prepareView(entity, uiModel, partial != null, request);
		} catch (RuntimeException e) {
			logger.fatal(e, e);
			return handleFallbackUrl(e);
		}
	}

	protected String handleFallbackUrl(RuntimeException e) {
		if (openlegacyWebProperties.isFallbackUrlOnError()) {
			Assert.notNull(openlegacyWebProperties.getFallbackUrl(), "No fallback URL defined");
			logger.error(e.getMessage());
			return MvcConstants.REDIRECT + openlegacyWebProperties.getFallbackUrl();
		} else {
			throw (e);
		}
	}

	protected abstract ActionDefinition findAction(Object entity, String action);

	protected String handleEntity(HttpServletRequest request, Model uiModel, Object resultEntity, String urlPrefix)
			throws MalformedURLException {
		if (resultEntity == null) {
			Assert.notNull(openlegacyWebProperties.getFallbackUrl(), "No fallback URL defined");
			return MvcConstants.REDIRECT + openlegacyWebProperties.getFallbackUrl();
		} else {
			if (StringUtils.isEmpty(urlPrefix)) {
				return prepareView(resultEntity, uiModel, true, request);
			} else {
				EntityDefinition<?> entityDefinition = entitiesRegistry.get(resultEntity.getClass());
				return urlPrefix + entityDefinition.getEntityName();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected String prepareView(Object entity, Model uiModel, boolean partial, HttpServletRequest request)
			throws MalformedURLException {
		String entityName = ProxyUtil.getOriginalClass(entity.getClass()).getSimpleName();
		uiModel.addAttribute(StringUtils.uncapitalize(entityName), entity);
		EntityDefinition<?> entityDefinition = entitiesRegistry.get(entityName);
		uiModel.addAttribute(PAGE, pageBuilder.build(entityDefinition));

		SitePreference sitePreference = SitePreferenceUtils.getCurrentSitePreference(request);

		boolean isComposite = entityDefinition.getChildEntitiesDefinitions().size() > 0 && !partial;
		String suffix = isComposite ? MvcConstants.COMPOSITE_SUFFIX : "";
		String viewName = entityDefinition.getEntityName() + suffix;

		boolean isEmpty = !isComposite && entityDefinition.isEmpty();

		List<String> viewsPaths = sitePreference == SitePreference.MOBILE ? mobileViewsPaths : webViewsPaths;

		// check if custom view exists, if not load generic view by
		// characteristics
		if (!isResourceExists(viewsPaths, viewName, viewsSuffix)) {
			if (isComposite) {
				// generic composite view (multi tabbed page)
				viewName = MvcConstants.COMPOSITE;
			} else if (entityDefinition.getType() == MenuEntity.class) {
				// generic menu view
				viewName = MvcConstants.ROOTMENU_VIEW;
			} else if (partial) {
				// generic inner page view (inner tab)
				viewName = MvcConstants.GENERIC_VIEW;
			} else if (entityDefinition.isWindow()) {
				// generic window pop-pup view
				viewName = MvcConstants.GENERIC_WINDOW;
			} else if (isEmpty) {
				viewName = MvcConstants.REDIRECT + openlegacyWebProperties.getFallbackUrl();
			} else {
				// generic view
				viewName = MvcConstants.GENERIC;
			}
		}

		return viewName;
	}

	private boolean isResourceExists(List<String> viewsPaths, String viewName, String viewsSuffix) throws MalformedURLException {
		for (String viewsPath : viewsPaths) {
			if (servletContext.getResource(MessageFormat.format("{0}/{1}{2}", viewsPath, viewName, viewsSuffix)) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Look for the given entity in the registry, and return HTTP 400 (BAD REQUEST) in case it's not found
	 * 
	 * @param entityName
	 * @param response
	 * @return
	 * @throws IOException
	 */
	protected Class<?> findAndHandleNotFound(String entityName, HttpServletResponse response) throws IOException {
		Class<?> entityClass = entitiesRegistry.getEntityClass(entityName);
		if (entityClass == null) {
			String message = MessageFormat.format("Entity {0} not found", entityName);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			logger.error(message);
		}
		return entityClass;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		MvcUtils.registerEditors(binder, entitiesRegistry);
	}

	public List<String> getWebViewsPaths() {
		return webViewsPaths;
	}

	public void setWebViewsPaths(List<String> webViewsPaths) {
		this.webViewsPaths = webViewsPaths;
	}

	public List<String> getMobileViewsPaths() {
		return mobileViewsPaths;
	}

	public void setMobileViewsPaths(List<String> mobileViewsPaths) {
		this.mobileViewsPaths = mobileViewsPaths;
	}

	public void setViewsSuffix(String viewsSuffix) {
		this.viewsSuffix = viewsSuffix;
	}

	protected EntitiesRegistry<?, ?, ?> getEntitiesRegistry() {
		return entitiesRegistry;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (webViewsPaths.size() == 0) {
			webViewsPaths.add("/WEB-INF/web/views");
		}
		if (mobileViewsPaths.size() == 0) {
			mobileViewsPaths.add("/WEB-INF/mobile/views");
		}
	}

}
