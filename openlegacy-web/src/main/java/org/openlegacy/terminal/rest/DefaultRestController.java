package org.openlegacy.terminal.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.openlegacy.exceptions.EntityNotFoundException;
import org.openlegacy.exceptions.RegistryException;
import org.openlegacy.modules.login.Login;
import org.openlegacy.modules.login.LoginException;
import org.openlegacy.modules.menu.Menu;
import org.openlegacy.modules.menu.MenuItem;
import org.openlegacy.modules.navigation.Navigation;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.TerminalSession;
import org.openlegacy.terminal.definitions.ScreenEntityDefinition;
import org.openlegacy.terminal.modules.trail.TrailUtil;
import org.openlegacy.terminal.render.TerminalSnapshotImageRenderer;
import org.openlegacy.terminal.services.ScreenEntitiesRegistry;
import org.openlegacy.terminal.support.SimpleScreenEntityWrapper;
import org.openlegacy.terminal.utils.ScreenEntityUtils;
import org.openlegacy.utils.ProxyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * OpenLegacy default REST API controller. Handles GET/POST requests in the format of JSON or XML. Also handles login /logoff of
 * the host session
 * 
 * @author Roi Mor
 * 
 */
@Controller
public class DefaultRestController {

	private static final String JSON = "application/json";
	private static final String XML = "application/xml";
	private static final String SCREEN_MODEL = "screenModel";
	private static final String ACTION = "action";

	private final static Log logger = LogFactory.getLog(DefaultRestController.class);
	private static final String USER = "user";
	private static final String PASSWORD = "password";

	@Inject
	private TerminalSnapshotImageRenderer imageRenderer;
	
	/**
	 * Whether to perform login on session start. Can be overridden from /application.properties
	 * defaultRestController.requiresLogin=true
	 */
	private boolean requiresLogin = false;

	@Inject
	private TerminalSession terminalSession;

	@Inject
	private ScreenEntitiesRegistry screenEntitiesRegistry;

	@Inject
	private ScreenEntityUtils screenEntityUtils;

	@Inject
	private TrailUtil trailUtil;

	@RequestMapping(value = "/login", consumes = { JSON, XML })
	public void login(@RequestParam(USER) String user, @RequestParam(PASSWORD) String password, HttpServletResponse response)
			throws IOException {
		try {
			terminalSession.getModule(Login.class).login(user, password);
		} catch (RegistryException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		} catch (LoginException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		}
		response.setStatus(HttpServletResponse.SC_OK);

	}

	@RequestMapping(value = "/logoff", method = RequestMethod.GET, consumes = { JSON, XML })
	public void logoff(HttpServletResponse response) {
		trailUtil.saveTrail(terminalSession);
		terminalSession.getModule(Login.class).logoff();

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@RequestMapping(value = "/{screen}", method = RequestMethod.GET, consumes = { JSON, XML })
	public ModelAndView getScreenEntity(@PathVariable("screen") String screenEntityName, HttpServletResponse response)
			throws IOException {
		if (!authenticate(response)) {
			return null;
		}
		try {
			ScreenEntity screenEntity = (ScreenEntity)terminalSession.getEntity(screenEntityName);
			return getEntityInner(screenEntity);
		} catch (EntityNotFoundException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return null;
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.GET, consumes = { JSON, XML })
	public ModelAndView getScreenEntity(HttpServletResponse response) throws IOException {
		if (!authenticate(response)) {
			return null;
		}

		ScreenEntity screenEntity = terminalSession.getEntity();
		return getEntityInner(screenEntity);

	}

	private ModelAndView getEntityInner(ScreenEntity screenEntity) {
		screenEntity = ProxyUtil.getTargetObject(screenEntity);
		ScreenEntityDefinition entityDefinitions = screenEntitiesRegistry.get(screenEntity.getClass());
		SimpleScreenEntityWrapper wrapper = new SimpleScreenEntityWrapper(screenEntity, terminalSession.getModule(
				Navigation.class).getPaths(), entityDefinitions.getActions());
		return new ModelAndView(SCREEN_MODEL, SCREEN_MODEL, wrapper);
	}

	@RequestMapping(value = "/menu", method = RequestMethod.GET, consumes = { JSON, XML })
	public Object getMenu(ModelMap model) {
		MenuItem menus = terminalSession.getModule(Menu.class).getMenuTree();
		return menus;
	}

	/**
	 * Accepts a post request in JSON format, de-serialize it to a screen entity, and send it to the host
	 * 
	 * @param screenEntityName
	 * @param action
	 * @param json
	 * @param response
	 *            Return HTTP OK (200) is success
	 * @throws IOException
	 */
	@RequestMapping(value = "/{screen}", method = RequestMethod.POST, consumes = JSON)
	public void postScreenEntityJson(@PathVariable("screen") String screenEntityName, @RequestParam(ACTION) String action,
			@RequestBody String json, HttpServletResponse response) throws IOException {
		if (!authenticate(response)) {
			return;
		}

		Class<?> entityClass = findAndHandleNotFound(screenEntityName, response);
		if (entityClass == null) {
			return;
		}

		ScreenEntity screenEntity = null;
		try {
			screenEntity = (ScreenEntity)ScreenEntitySerializationUtils.deserialize(json, entityClass);
		} catch (Exception e) {
			handleDeserializationException(screenEntityName, response, e);
			return;
		}

		screenEntityUtils.sendScreenEntity(terminalSession, screenEntity, action);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@RequestMapping(value = "/{screen}", method = RequestMethod.POST, consumes = XML)
	public void postScreenEntityXml(@PathVariable("screen") String screenEntityName, @RequestParam(ACTION) String action,
			@RequestBody String xml, HttpServletResponse response) throws IOException {
		if (!authenticate(response)) {
			return;
		}

		Class<?> entityClass = findAndHandleNotFound(screenEntityName, response);
		if (entityClass == null) {
			return;
		}
		ScreenEntity screenEntity = null;
		try {
			InputSource inputSource = new InputSource(new ByteArrayInputStream(xml.getBytes()));
			screenEntity = (ScreenEntity)Unmarshaller.unmarshal(entityClass, inputSource);
		} catch (MarshalException e) {
			handleDeserializationException(screenEntityName, response, e);
			return;
		} catch (ValidationException e) {
			handleDeserializationException(screenEntityName, response, e);
			return;
		}

		screenEntityUtils.sendScreenEntity(terminalSession, screenEntity, action);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private boolean authenticate(HttpServletResponse response) throws IOException {
		if (!requiresLogin) {
			return true;
		}
		if (!terminalSession.getModule(Login.class).isLoggedIn()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return true;

	}

	private static void handleDeserializationException(String screenEntityName, HttpServletResponse response, Exception e)
			throws IOException {
		String message = MessageFormat.format("Unable to desirialize entity {0}", screenEntityName);
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		logger.fatal(message, e);
		return;
	}

	/**
	 * Look for the given screen entity in the registry, and return HTTP 400 (BAD REQUEST) in case it's not found
	 * 
	 * @param screenEntityName
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private Class<?> findAndHandleNotFound(String screenEntityName, HttpServletResponse response) throws IOException {
		Class<?> entityClass = screenEntitiesRegistry.getEntityClass(screenEntityName);
		if (entityClass == null) {
			String message = MessageFormat.format("Screen entity {0} not found", screenEntityName);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			logger.error(message);
		}
		return entityClass;
	}

	public void setRequiresLogin(boolean requiresLogin) {
		this.requiresLogin = requiresLogin;
	}

	@RequestMapping(value = "/restImage", method = RequestMethod.GET)
	public void image(HttpServletResponse response) throws IOException {
		response.setContentType("image/jpeg");
		imageRenderer.render(terminalSession.getSnapshot(), response.getOutputStream());
	}
}
