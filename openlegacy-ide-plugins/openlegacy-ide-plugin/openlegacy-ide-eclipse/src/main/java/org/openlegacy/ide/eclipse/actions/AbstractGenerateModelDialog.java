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
package org.openlegacy.ide.eclipse.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openlegacy.EntityDefinition;
import org.openlegacy.designtime.EntityUserInteraction;
import org.openlegacy.designtime.PreferencesConstants;
import org.openlegacy.ide.eclipse.Messages;
import org.openlegacy.ide.eclipse.util.JavaUtils;

import java.io.File;
import java.text.MessageFormat;

public abstract class AbstractGenerateModelDialog extends AbstractGenerateCodeDialog implements EntityUserInteraction<EntityDefinition<?>> {

	public AbstractGenerateModelDialog(Shell shell, IFile file) {
		super(shell, file);
	}

	private final static Logger logger = Logger.getLogger(AbstractGenerateModelDialog.class);

	@Override
	protected void executeGenerate() {

		Job job = new Job(Messages.getString("job_generating_model")) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				int fileSize = (int)(new File(getFile().getLocation().toOSString()).length() / 1000);
				monitor.beginTask(Messages.getString("job_activating_analyzer"), fileSize);

				monitor.worked(2);
				generate();

				monitor.worked(fileSize - 4);
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						try {
							monitor.worked(1);

							getFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
							monitor.done();
						} catch (CoreException e) {
							logger.fatal(e);
						}
					}
				});

				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	protected abstract void generate();

	@Override
	protected void loadPrefrences() {

		EclipseDesignTimeExecuter designtimeExecuter = EclipseDesignTimeExecuter.instance();
		IProject project = getProject();

		String prefrenceSourceFolderPath = designtimeExecuter.getPreference(project, PreferencesConstants.API_SOURCE_FOLDER);
		getSourceFolderPathText().setText(
				MessageFormat.format("{0}{1}{2}", project.getName(), File.separator, prefrenceSourceFolderPath)); //$NON-NLS-1$

		IJavaProject javaProject = JavaUtils.getJavaProjectFromIProject(project);
		setSourceFolder(javaProject.getPackageFragmentRoot(prefrenceSourceFolderPath));

		String prefrencePackage = designtimeExecuter.getPreference(project, PreferencesConstants.API_PACKAGE);
		if (prefrencePackage != null) {
			getPackageText().setText(prefrencePackage);
		}
		String useAjStr = designtimeExecuter.getPreference(project, PreferencesConstants.USE_AJ);
		if (useAjStr == null || useAjStr.equals("1")) {
			this.setUseAj(true);
		} else {
			this.setUseAj(false);
		}
		String generateTest = designtimeExecuter.getPreference(project, PreferencesConstants.GENERATE_TEST);
		if (generateTest == null || generateTest.equals("1")) {
			this.setGenerateTest(true);
		} else {
			this.setGenerateTest(false);
		}

	}

	@Override
	protected void savePreferences() {
		String sourceFolderOnly = getSourceFolderPathText().getText().substring(
				getSourceFolder().getJavaProject().getProject().getName().length() + 1);

		EclipseDesignTimeExecuter designtimeExecuter = EclipseDesignTimeExecuter.instance();

		designtimeExecuter.savePreference(getProject(), PreferencesConstants.API_SOURCE_FOLDER, sourceFolderOnly);
		designtimeExecuter.savePreference(getProject(), PreferencesConstants.API_PACKAGE, getPackageText().getText());
		designtimeExecuter.savePreference(getProject(), PreferencesConstants.USE_AJ, isUseAj());
		designtimeExecuter.savePreference(getProject(), PreferencesConstants.GENERATE_TEST, isGenerateTest());
	}

}
