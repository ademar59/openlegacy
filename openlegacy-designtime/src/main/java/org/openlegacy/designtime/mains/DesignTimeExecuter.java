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
package org.openlegacy.designtime.mains;

import org.openlegacy.EntityDefinition;
import org.openlegacy.designtime.enums.BackendSolution;
import org.openlegacy.designtime.rpc.GenerateRpcModelRequest;
import org.openlegacy.designtime.rpc.ImportSourceRequest;
import org.openlegacy.designtime.terminal.GenerateScreenModelRequest;
import org.openlegacy.exceptions.GenerationException;
import org.openlegacy.exceptions.OpenLegacyException;
import org.openlegacy.rpc.definitions.RpcEntityDefinition;
import org.openlegacy.terminal.definitions.ScreenEntityDefinition;

import java.io.File;
import java.io.IOException;

/**
 * A interface for OpenLegacy design-time common UI actions which should is invoked for any IDE (currently eclipse only)
 * 
 */
public interface DesignTimeExecuter {

	public static final String MOCK_PROVIDER = "openlegacy-impl";
	public static final String CUSTOM_DESIGNTIME_CONTEXT_FILE_NAME = "openlegacy-designtime-context.xml";
	public static final String CUSTOM_DESIGNTIME_CONTEXT_RELATIVE_PATH = "src/main/resources/META-INF/spring/"
			+ CUSTOM_DESIGNTIME_CONTEXT_FILE_NAME;
	public static final String DEFAULT_DESIGNTIME_CONTEXT_FILE_NAME = "/openlegacy-default-designtime-context.xml";
	public static final String ASPECT_SUFFIX = "_Aspect.aj";
	public static final String RESOURCES_FOLDER_SUFFIX = "-resources";

	public static final int DEFAULT_PORT = 23;
	public static final String DEFAULT_CODE_PAGE = "037";
	public static final String DEFAULT_RTL_CODE_PAGE = "424";

	public void createProject(ProjectCreationRequest projectCreationRequest) throws IOException;

	void generateScreenModel(GenerateScreenModelRequest generateScreenRequest) throws GenerationException;

	boolean generateScreenEntityDefinition(GenerateScreenModelRequest generateModelRequest,
			ScreenEntityDefinition screenEntityDefinition);

	boolean generateRpcEntityDefinition(GenerateRpcModelRequest generateModelRequest, RpcEntityDefinition screenEntityDefinition);

	boolean generateAspect(File javaFile);

	void initialize(File projectPath);

	void generateView(GenerateViewRequest generateViewRequest) throws GenerationException;

	void generateController(GenerateControllerRequest generateControllerRequest) throws GenerationException;

	void copyCodeGenerationTemplates(File projectPath);

	String getPreferences(File projectPath, String key);

	void reloadPreferences(File projectPath);

	void savePreference(File projectPath, String key, String value);

	public void copyDesigntimeContext(File projectPath);

	public void generateRpcModel(GenerateRpcModelRequest generateRpcRequest);

	public void importSourceFile(ImportSourceRequest importSourceRequest) throws OpenLegacyException;

	public boolean isSupportControllerGeneration(File entityFile);

	public boolean isSupportServiceGeneration(File projectPath);

	public void generateService(GenerateServiceRequest generateServiceRequest);

	public void addServiceOutputAnnotation(File javaEntityFile);

	public EntityDefinition<?> initEntityDefinition(File sourceFile);

	public void generateScreenEntityResources(String entityName, GenerateScreenModelRequest generateScreenModelRequest);

	public void renameViews(String fileNoExtension, String newName, File javaFile, String fileExtension);

	public String translate(String text, File projectPath);

	public BackendSolution getServiceType(File projectPath);

	public void obfuscateTrail(File osLocation);

	public boolean isSupportRestControllerGeneration(File file);

	public void generateRestController(GenerateControllerRequest generateControllerRequest);

}
