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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.openlegacy.EntityDefinition;
import org.openlegacy.designtime.UserInteraction;
import org.openlegacy.ide.eclipse.Messages;
import org.openlegacy.ide.eclipse.PluginConstants;
import org.openlegacy.ide.eclipse.util.JavaUtils;
import org.openlegacy.ide.eclipse.util.PathsUtil;
import org.openlegacy.rpc.definitions.RpcEntityDefinition;
import org.openlegacy.terminal.definitions.ScreenEntityDefinition;

import java.io.File;
import java.text.MessageFormat;

public abstract class AbstractGenerateCodeDialog extends Dialog implements UserInteraction {

	private final static Logger logger = Logger.getLogger(AbstractGenerateCodeDialog.class);

	private Text sourceFolderPathText;
	private IPackageFragmentRoot sourceFolder;
	protected Text packageText;
	protected String packageValue;
	private IFile file;
	private boolean useAj = true;
	private boolean generateTest = true;

	protected AbstractGenerateCodeDialog(Shell shell, IFile file) {
		super(shell);
		this.file = file;
	}

	protected void addDialogFotter() {

	}

	@Override
	protected Control createDialogArea(Composite parent) {

		parent = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gd = new GridData();
		gd.widthHint = 400;
		parent.setLayoutData(gd);
		parent.setLayout(gridLayout);

		parent.getShell().setText(PluginConstants.TITLE);

		Label label = new Label(parent, SWT.NULL);
		label.setText(Messages.getString("label_source_folder"));

		setSourceFolderPathText(new Text(parent, SWT.SINGLE | SWT.BORDER));

		getSourceFolderPathText().setEditable(false);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		getSourceFolderPathText().setLayoutData(gd);
		Button sourceFolderButton = new Button(parent, SWT.NONE);
		sourceFolderButton.setText(Messages.getString("label_browse"));
		sourceFolderButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSourceFolderButtonSelected();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Label labelPackage = new Label(parent, SWT.NULL);
		labelPackage.setText(Messages.getString("label_package"));
		packageText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		getPackageText().setLayoutData(gd);

		getPackageText().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent paramModifyEvent) {
				packageValue = getPackageText().getText();
			}
		});

		new Label(parent, SWT.NONE);

		loadPrefrences();

		Button useAjButton = new Button(parent, SWT.CHECK);
		useAjButton.setText(Messages.getString("label_use_aj"));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 400;
		useAjButton.setLayoutData(gd);

		useAjButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				setUseAj(true);

			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setUseAj(!isUseAj());
			}
		});
		useAjButton.setSelection(isUseAj() && isSupportAjGeneration());
		useAjButton.setEnabled(isSupportAjGeneration());

		Button generateTest = new Button(parent, SWT.CHECK);
		generateTest.setSelection(isSupportTestGeneration() && isGenerateTest());
		generateTest.setText(Messages.getString("label_generate_test"));

		generateTest.setEnabled(isSupportTestGeneration());

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 400;
		generateTest.setLayoutData(gd);

		generateTest.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				setGenerateTest(true);
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setGenerateTest(!isGenerateTest());
			}
		});

		createDialogSpecific(parent);
		return parent;
	}

	protected boolean isPackageMandatory() {
		return true;
	}

	protected boolean isSupportTestGeneration() {
		return true;
	}

	protected boolean isSupportAjGeneration() {
		return true;
	}

	protected void createDialogSpecific(Composite parent) {
		// empty to allow override
	}

	protected abstract void loadPrefrences();

	protected void handleSourceFolderButtonSelected() {
		IPackageFragmentRoot newSourceFolder = chooseContainer();

		if (newSourceFolder == null) {
			return;
		}

		setSourceFolder(newSourceFolder);
		String newSourceFolderPath = JavaUtils.convertSourceFolderToString(newSourceFolder);
		getSourceFolderPathText().setText(newSourceFolderPath);
	}

	public void setSourceFolder(IPackageFragmentRoot sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public IPackageFragmentRoot getSourceFolder() {
		return sourceFolder;
	}

	protected IPackageFragmentRoot chooseContainer() {
		IJavaElement initElement = getSourceFolder();
		Class<?>[] acceptedClasses = new Class[] { IPackageFragmentRoot.class, IJavaProject.class };
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false) {

			@Override
			public boolean isSelectedValid(Object element) {
				try {
					if (element instanceof IJavaProject) {
						IJavaProject jproject = (IJavaProject)element;
						IPath path = jproject.getProject().getFullPath();
						return (jproject.findPackageFragmentRoot(path) != null);
					} else if (element instanceof IPackageFragmentRoot) {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					}
					return true;
				} catch (JavaModelException e) {
				}
				return false;
			}
		};

		acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {

			@Override
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (element instanceof IPackageFragmentRoot) {
					try {
						return (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE);
					} catch (JavaModelException e) {
					}
				} else if (element instanceof IJavaProject) {
					IJavaProject javaPr = (IJavaProject)element;
					return javaPr.isOpen();
				}

				return super.select(viewer, parent, element);
			}
		};

		StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
		ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
		dialog.setValidator(validator);
		dialog.setComparator(new JavaElementComparator());
		dialog.setTitle(""); //$NON-NLS-1$
		dialog.setMessage(""); //$NON-NLS-1$
		dialog.addFilter(filter);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		dialog.setInput(JavaCore.create(root));
		dialog.setInitialSelection(initElement);
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			Object element = dialog.getFirstResult();
			if (element instanceof IJavaProject) {
				IJavaProject jproject = (IJavaProject)element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot)element;
			}
			return null;
		}
		return null;
	}

	private boolean validate() {

		if (getPackageText().getText().length() == 0 && isPackageMandatory()) {
			MessageDialog.openError(getShell(), PluginConstants.TITLE, Messages.getString("error_package_cannot_be_empty"));
			return false;
		}
		if (getSourceFolderPathText().getText().length() == 0) {
			MessageDialog.openError(getShell(), PluginConstants.TITLE, Messages.getString("error_source_folder_cannot_be_empty"));
			return false;
		}
		return true;

	}

	@Override
	protected void okPressed() {
		if (!validate()) {
			return;
		}

		savePreferences();
		executeGenerate();

		close();
	}

	protected abstract void savePreferences();

	protected abstract void executeGenerate();

	@Override
	public boolean isOverride(final File file) {
		final Object[] result = new Object[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				result[0] = MessageDialog.openQuestion(getShell(), PluginConstants.TITLE,
						MessageFormat.format(Messages.getString("question_override_file"), file.getName()));
			}
		});

		return (Boolean)result[0];
	}

	public String getPackageValue() {
		return packageValue;
	}

	public Text getSourceFolderPathText() {
		return sourceFolderPathText;
	}

	public void setSourceFolderPathText(Text sourceFolderPathText) {
		this.sourceFolderPathText = sourceFolderPathText;
	}

	public Text getPackageText() {
		return packageText;
	}

	public IFile getFile() {
		return file;
	}

	public IProject getProject() {
		return file.getProject();
	}

	public boolean isUseAj() {
		return useAj;
	}

	public void setUseAj(boolean useAj) {
		this.useAj = useAj;
	}

	protected void setGenerateTest(boolean generateTest) {
		this.generateTest = generateTest;
	}

	public boolean isGenerateTest() {
		return generateTest;
	}

	@Override
	public void open(File file) {
		open(file, null);
	}

	@Override
	public void open(final File file, final EntityDefinition<?> entityDefinition) {

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				final IFolder folder = getProject().getFolder(
						getSourceFolder().getPath() + "/" + PathsUtil.packageToPath(getPackageValue()));
				try {
					if (folder != null) {
						folder.refreshLocal(1, null);
					}
				} catch (CoreException e1) {
					logger.fatal(e1);
				}

				IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
				try {
					IFile javaFile = getProject().getFile(
							getSourceFolder().getPath().toPortableString() + "/" + PathsUtil.packageToPath(getPackageValue())
									+ "/" + file.getName());

					IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(
							javaFile.getName());
					if (entityDefinition != null && entityDefinition instanceof ScreenEntityDefinition) {
						IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(
								"com.openlegacy.enterprise.ide.eclipse.editors.ScreenEntityEditor");
						if (descriptor != null) {
							editorDescriptor = descriptor;
						}
					} else if (entityDefinition != null && entityDefinition instanceof RpcEntityDefinition) {
						IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(
								"com.openlegacy.enterprise.ide.eclipse.editors.RpcEntityEditor");
						if (descriptor != null) {
							editorDescriptor = descriptor;
						}
					}

					page.openEditor(new FileEditorInput(javaFile), editorDescriptor.getId());

				} catch (PartInitException e) {
					logger.fatal(e);
				}
			}
		});

	}
}
