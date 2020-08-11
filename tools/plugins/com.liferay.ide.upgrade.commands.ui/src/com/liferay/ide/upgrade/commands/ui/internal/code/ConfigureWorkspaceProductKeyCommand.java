/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.upgrade.commands.ui.internal.code;

import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.workspace.WorkspaceConstants;
import com.liferay.ide.project.core.workspace.ConfigureWorkspaceProductOp;
import com.liferay.ide.ui.util.UIUtil;
import com.liferay.ide.upgrade.commands.core.code.ConfigureWorkspaceProductKeyCommandKeys;
import com.liferay.ide.upgrade.commands.ui.internal.UpgradeCommandsUIPlugin;
import com.liferay.ide.upgrade.plan.core.ResourceSelection;
import com.liferay.ide.upgrade.plan.core.UpgradeCommand;
import com.liferay.ide.upgrade.plan.core.UpgradeCompare;
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradePreview;

import java.io.File;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.PropertiesConfiguration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.swt.SapphireDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Seiphon Wang
 */
@Component(
	property = "id=" + ConfigureWorkspaceProductKeyCommandKeys.ID, scope = ServiceScope.PROTOTYPE,
	service = {UpgradeCommand.class, UpgradePreview.class}
)
public class ConfigureWorkspaceProductKeyCommand implements UpgradeCommand, UpgradePreview {

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		File gradleProperties = _getGradlePropertiesFile();

		if (gradleProperties == null) {
			return Status.CANCEL_STATUS;
		}

		IStatus status = _updateWorkspaceProductKeyValue(gradleProperties, false);

		return status;
	}

	@Override
	public void preview(IProgressMonitor progressMonitor) {
		File gradeProperties = _getGradlePropertiesFile();

		if (gradeProperties == null) {
			return;
		}

		File tempDir = getTempDir();

		FileUtil.copyFileToDir(gradeProperties, "gradle.properties-preview", tempDir);

		File tempFile = new File(tempDir, "gradle.properties-preview");

		_updateWorkspaceProductKeyValue(tempFile, true);

		UIUtil.async(
			() -> {
				_upgradeCompare.openCompareEditor(gradeProperties, tempFile);
			});
	}

	private File _getGradlePropertiesFile() {
		List<IProject> projects = _resourceSelection.selectProjects(
			"Select Liferay Workspace Project", false, ResourceSelection.WORKSPACE_PROJECTS);

		if (projects.isEmpty()) {
			return null;
		}

		IProject project = projects.get(0);

		IFile gradeProperties = project.getFile("gradle.properties");

		return FileUtil.getFile(gradeProperties);
	}

	private IStatus _updateWorkspaceProductKeyValue(File gradeProperties, boolean preview) {
		UpgradePlan upgradePlan = _upgradePlanner.getCurrentUpgradePlan();

		String targetVersion = upgradePlan.getTargetVersion();

		final AtomicReference<String> productKey = new AtomicReference<>();

		final AtomicInteger dialogStatus = new AtomicInteger();

		try {
			UIUtil.sync(
				() -> {
					IWorkbench workbench = PlatformUI.getWorkbench();

					IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

					Shell shell = workbenchWindow.getShell();

					String dialogId = new String(
						"com.liferay.ide.project.ui.workspace.ConfigureWorkspaceProductDialog");

					DefinitionLoader loader = DefinitionLoader.context(getClass());

					DefinitionLoader definitionLoader = loader.sdef(dialogId);

					ConfigureWorkspaceProductOp op = ConfigureWorkspaceProductOp.TYPE.instantiate();

					op.setFilterString(targetVersion);

					op.setSkipExecute(preview);

					DefinitionLoader.Reference<DialogDef> dialogRef = definitionLoader.dialog(
						"ConfigureWorkspaceProduct");

					SapphireDialog dialog = new SapphireDialog(shell, op, dialogRef);

					dialogStatus.set(dialog.open());

					productKey.set(_getter.get(op.getProductVersion()));
				});

			if (dialogStatus.get() != Status.OK) {
				return Status.CANCEL_STATUS;
			}

			if (preview) {
				PropertiesConfiguration config = new PropertiesConfiguration(gradeProperties);

				config.setProperty(WorkspaceConstants.WORKSPACE_PRODUCT_PROPERTY, productKey);

				config.save();
			}

			return Status.OK_STATUS;
		}
		catch (Exception e) {
			return UpgradeCommandsUIPlugin.createErrorStatus("Unable to configure target platform", e);
		}
	}

	private static final SapphireContentAccessor _getter = new SapphireContentAccessor() {
	};

	@Reference
	private ResourceSelection _resourceSelection;

	@Reference
	private UpgradeCompare _upgradeCompare;

	@Reference
	private UpgradePlanner _upgradePlanner;

}