package org.openlegacy.ide.eclipse.wizards.project.organized;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openlegacy.designtime.mains.DesignTimeExecuter;
import org.openlegacy.designtime.newproject.organized.NewProjectMetadataRetriever;
import org.openlegacy.designtime.newproject.organized.model.HostType;
import org.openlegacy.ide.eclipse.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Bort
 * 
 */
public class OpenLegacyWizardHostPage extends AbstractOpenLegacyWizardPage {

	private List<HostType> hostTypes = new ArrayList<HostType>();
	private Combo hostTypeCombo;
	private Text hostText;
	private Text hostPortText;
	private Text codePageText;
	private Label hostTypeDescriptionLabel;

	protected OpenLegacyWizardHostPage() {
		super("wizardProviderPage"); //$NON-NLS-1$
		setTitle(Messages.getString("title_ol_project_wizard"));
		setDescription(Messages.getString("info_ol_project_wizard"));
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.getString("label_host_type"));

		hostTypeCombo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.widthHint = 100;
		hostTypeCombo.setLayoutData(gd);
		hostTypeCombo.setItems(new String[] { "Pending..." });
		hostTypeCombo.select(0);
		hostTypeCombo.addSelectionListener(getDefaultSelectionListener());

		// stub first column in grid layout
		label = new Label(container, SWT.NONE);

		hostTypeDescriptionLabel = new Label(container, SWT.NONE);
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		hostTypeDescriptionLabel.setLayoutData(gd);

		label = new Label(container, SWT.NULL);
		label.setText(Messages.getString("label_host_ip"));//$NON-NLS-1$

		hostText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 200;
		hostText.setLayoutData(gd);
		hostText.addModifyListener(getDefaultModifyListener());

		label = new Label(container, SWT.NONE);
		label.setText(Messages.getString("label_host_port"));//$NON-NLS-1$

		hostPortText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 40;
		hostPortText.setLayoutData(gd);
		hostPortText.setText(String.valueOf(DesignTimeExecuter.DEFAULT_PORT));
		hostPortText.addModifyListener(getDefaultModifyListener());

		label = new Label(container, SWT.NULL);
		label.setText(Messages.getString("label_code_page"));

		codePageText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 40;
		codePageText.setLayoutData(gd);
		codePageText.setText(String.valueOf(DesignTimeExecuter.DEFAULT_CODE_PAGE));
		codePageText.addModifyListener(getDefaultModifyListener());

		setControl(container);
		setPageComplete(false);
	}

	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage("wizardThemePage");
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete() && getWizardModel().isProjectSupportTheme();
	}

	@Override
	public void updateControlsData(NewProjectMetadataRetriever retriever) {
		// if dialog was closed before coming here
		if (getControl().isDisposed()) {
			return;
		}
		hostTypes.clear();
		hostTypes.addAll(retriever.getHostTypes());
		checkHostTypes();
	}

	/**
	 * Must be called only from General Page
	 */
	public void updateControlsData(String backendSolution, final String codePage) {
		if (!StringUtils.isEmpty(backendSolution)) {
			checkHostTypes();
			if (!hostTypes.isEmpty()) {
				final List<String> hostNames = new ArrayList<String>();
				for (HostType hostType : hostTypes) {
					if (hostType.getBackendSolution().equals(backendSolution)) {
						hostNames.add(hostType.getDisplayName());
					}
				}
				getControl().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						hostTypeCombo.setItems(hostNames.toArray(new String[] {}));
						hostTypeCombo.select(0);
						hostTypeCombo.notifyListeners(SWT.Selection, new Event());
					}
				});
			}
		}

		if (!StringUtils.isEmpty(codePage)) {
			getControl().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					codePageText.setText(codePage);
				}
			});
		}
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private void checkHostTypes() {
		if (!hostTypes.isEmpty()) {
			getControl().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					updateStatus(Messages.getString("error_new_project_metadata_not_found"));
				}
			});
		}
	}

	private SelectionListener getDefaultSelectionListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				HostType hostType = getSelectedHostType();
				if (hostType != null) {
					hostTypeDescriptionLabel.setText(hostType.getDescription());
				} else {
					hostTypeDescriptionLabel.setText("");
				}
				getWizardModel().update(hostType);
				if (hostTypeCombo.getText().contains("mock-up")) {
					updateStatus(null);
					setControlsEnabled(false);
					return;
				}
				setControlsEnabled(true);
				validateControls();
			}

		};
	}

	private ModifyListener getDefaultModifyListener() {
		return new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (validateControls()) {
					getWizardModel().setHost(hostText.getText());
					getWizardModel().setHostPort(Integer.valueOf(hostPortText.getText()));
					getWizardModel().setCodePage(codePageText.getText());
				} else {
					getWizardModel().setHost("");
					getWizardModel().setHostPort(0);
					getWizardModel().setCodePage("");
				}
			}
		};
	}

	private void setControlsEnabled(boolean enabled) {
		hostText.setEnabled(enabled);
		hostPortText.setEnabled(enabled);
		codePageText.setEnabled(enabled);
	}

	private boolean validateControls() {
		if (hostText.getText().length() == 0) {
			updateStatus(Messages.getString("error_host_name_not_specified"));
			return false;
		}
		if (hostPortText.getText().length() == 0) {
			updateStatus(Messages.getString("errror_host_port_not_specified"));
			return false;
		}
		if (!NumberUtils.isNumber(hostPortText.getText())) {
			updateStatus(Messages.getString("error_port_not_numeric"));
			return false;
		}
		if (codePageText.getText().length() == 0) {
			updateStatus(Messages.getString("error_code_page_not_specified"));
			return false;
		}
		updateStatus(null);
		return true;
	}

	private HostType getSelectedHostType() {
		for (HostType hostType : hostTypes) {
			if (hostType.getBackendSolution().equals(getWizardModel().getBackendSolution())
					&& hostType.getDisplayName().equals(hostTypeCombo.getText())) {
				return hostType;
			}
		}
		return null;
	}
}
