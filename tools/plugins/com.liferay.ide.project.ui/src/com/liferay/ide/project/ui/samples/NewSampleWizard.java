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

package com.liferay.ide.project.ui.samples;

import com.liferay.ide.project.core.samples.NewSampleOp;
import com.liferay.ide.project.ui.BaseProjectWizard;
import com.liferay.ide.project.ui.RequireLiferayWorkspaceProject;
import com.liferay.ide.project.ui.SampleProjectUIUtil;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.ui.IWorkbench;

/**
 * @author Terry Jia
 * @author Seiphon Wang
 */
public class NewSampleWizard extends BaseProjectWizard<NewSampleOp> implements RequireLiferayWorkspaceProject {

	public NewSampleWizard() {
		super(NewSampleOp.TYPE.instantiate(), DefinitionLoader.sdef(NewSampleWizard.class).wizard());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);

		promptIfLiferayWorkspaceNotExists("Liferay Sample Project");
	}

	@Override
	public boolean performCancel() {
		SampleProjectUIUtil.listenIfDownloadCompletes();

		return super.performCancel();
	}

}