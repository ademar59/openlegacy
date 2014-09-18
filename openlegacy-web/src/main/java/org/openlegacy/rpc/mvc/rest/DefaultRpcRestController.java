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
package org.openlegacy.rpc.mvc.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.EntitiesRegistry;
import org.openlegacy.Session;
import org.openlegacy.definitions.ActionDefinition;
import org.openlegacy.modules.trail.TrailUtil;
import org.openlegacy.mvc.AbstractRestController;
import org.openlegacy.rpc.RpcEntity;
import org.openlegacy.rpc.RpcSession;
import org.openlegacy.rpc.services.RpcEntitiesRegistry;
import org.openlegacy.rpc.utils.RpcEntityUtils;
import org.openlegacy.utils.ReflectionUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenLegacy default REST API RPC controller. Handles GET/POST requests in the format of JSON or XML. Also handles login /logoff
 * of the host session
 * 
 * @author Roi Mor
 * 
 */
@Controller
public class DefaultRpcRestController extends AbstractRestController {

	private static final String USER = "user";
	private static final String PASSWORD = "password";

	private final static Log logger = LogFactory.getLog(DefaultRpcRestController.class);

	@Inject
	private RpcSession rpcSession;

	@Inject
	private RpcEntitiesRegistry rpcEntitiesRegistry;

	@Inject
	private RpcEntityUtils rpcEntityUtils;

	@Inject
	private TrailUtil trailUtil;

	@Override
	protected Session getSession() {
		return rpcSession;
	}

	@Override
	protected EntitiesRegistry<?, ?, ?> getEntitiesRegistry() {
		return rpcEntitiesRegistry;
	}

	@RequestMapping(value = "/{entity}", method = RequestMethod.GET, consumes = { JSON, XML })
	public ModelAndView getEntity(@PathVariable("entity") String entityName, HttpServletResponse response) throws IOException {
		return super.getEntity(entityName, false, response);
	}

	@RequestMapping(value = "/{entity}/{key:[[\\w\\p{L}]+[-_ ]*[\\w\\p{L}]+]+}", method = RequestMethod.GET, consumes = { JSON,
			XML })
	public ModelAndView getEntityWithKey(@PathVariable("entity") String entityName, @PathVariable("key") String key,
			HttpServletResponse response) throws IOException {
		return super.getEntityWithKey(entityName, key, false, response);
	}

	@Override
	@RequestMapping(value = "/{entity}/definitions", method = RequestMethod.GET, consumes = { JSON, XML })
	public ModelAndView getEntityDefinitions(@PathVariable("entity") String entityName, HttpServletResponse response)
			throws IOException {
		return super.getEntityDefinitions(entityName, response);
	}

	@Override
	@RequestMapping(value = "/menu", method = RequestMethod.GET, consumes = { JSON, XML })
	public Object getMenu() {
		return super.getMenu();
	}

	@RequestMapping(value = "/{entity}", method = RequestMethod.POST, consumes = JSON)
	public ModelAndView postEntityJson(@PathVariable("entity") String entityName,
			@RequestParam(value = ACTION, required = false) String action, @RequestBody String json, HttpServletResponse response)
			throws IOException {
		return super.postEntityJson(entityName, action, false, json, response);
	}

	@RequestMapping(value = "/{entity}/{key:[[\\w\\p{L}]+[-_ ]*[\\w\\p{L}]+]+}", method = RequestMethod.POST, consumes = JSON)
	public ModelAndView postEntityJsonWithKey(@PathVariable("entity") String entityName, @PathVariable("key") String key,
			@RequestParam(value = ACTION, required = false) String action, @RequestBody String json, HttpServletResponse response)
			throws IOException {
		return super.postEntityJsonWithKey(entityName, key, action, false, json, response);
	}

	@Override
	@RequestMapping(value = "/{entity}/{key:[[\\w\\p{L}]+[-_ ]*[\\w\\p{L}]+]+}", method = RequestMethod.POST, consumes = XML)
	public ModelAndView postEntityXmlWithKey(@PathVariable("entity") String entityName, @PathVariable("key") String key,
			@RequestParam(value = ACTION, required = false) String action, @RequestBody String xml, HttpServletResponse response)
			throws IOException {
		return super.postEntityXmlWithKey(entityName, key, action, xml, response);
	}

	@Override
	@RequestMapping(value = "/{entity}", method = RequestMethod.POST, consumes = XML)
	public ModelAndView postEntityXml(@PathVariable("entity") String entityName,
			@RequestParam(value = ACTION, required = false) String action, @RequestBody String xml, HttpServletResponse response)
			throws IOException {
		return super.postEntityXml(entityName, action, xml, response);
	}

	@Override
	protected Object sendEntity(Object entity, String action) {
		return rpcEntityUtils.sendEntity(rpcSession, (RpcEntity)entity, action);
	}

	@Override
	@RequestMapping(value = "/login", consumes = { JSON, XML })
	public Object login(@RequestParam(USER) String user, @RequestParam(PASSWORD) String password, HttpServletResponse response)
			throws IOException {
		return super.login(user, password, response);
	}

	@Override
	@RequestMapping(value = "/login", consumes = { JSON }, method = RequestMethod.POST)
	public Object loginPostJson(@RequestBody String json, HttpServletResponse response) throws IOException {
		return super.loginPostJson(json, response);
	}

	@Override
	@RequestMapping(value = "/login", consumes = { XML }, method = RequestMethod.POST)
	public Object loginPostXml(@RequestBody String xml, HttpServletResponse response) throws IOException {
		return super.loginPostXml(xml, response);
	}

	@RequestMapping(value = "/logoff", consumes = { JSON, XML })
	public void logoff(HttpServletResponse response) throws IOException {
		try {
			trailUtil.saveTrail(getSession());
		} catch (Exception e) {
			logger.warn("Failed to save trail - " + e.getMessage(), e);
		} finally {
			getSession().disconnect();
		}

	}

	@Override
	protected Object postApiEntity(String entityName, Class<?> entityClass, String key) {
		return ReflectionUtil.newInstance(entityClass);
	}

	@Override
	protected List<ActionDefinition> getActions(Object entity) {
		return getEntitiesRegistry().get(entity.getClass()).getActions();
	}

}