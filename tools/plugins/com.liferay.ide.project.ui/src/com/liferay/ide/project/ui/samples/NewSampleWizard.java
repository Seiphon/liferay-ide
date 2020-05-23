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

import com.liferay.ide.core.workspace.WorkspaceConstants;
import com.liferay.ide.project.core.samples.NewSampleOp;
import com.liferay.ide.project.core.util.SampleProjectUtil;
import com.liferay.ide.project.ui.BaseProjectWizard;
import com.liferay.ide.project.ui.SampleProjectUIUtil;

import java.io.IOException;

import java.util.Collections;

import org.eclipse.mylyn.commons.notifications.core.INotificationService;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.sapphire.ui.def.DefinitionLoader;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class NewSampleWizard extends BaseProjectWizard<NewSampleOp> {

	public NewSampleWizard() {
		super(NewSampleOp.TYPE.instantiate(), DefinitionLoader.sdef(NewSampleWizard.class).wizard());

		for (String liferayVersion : WorkspaceConstants.liferayTargetPlatformVersions.keySet()) {
			try {
				if (!SampleProjectUtil.isBladeRepoArchiveExist(liferayVersion)) {
					INotificationService notificationService = NotificationsUi.getService();

					notificationService.notify(
						Collections.singletonList(
							SampleProjectUIUtil.createDownloadRequiredNotification(liferayVersion)));
				}
			}
			catch (IOException ioe) {
				System.out.print(ioe);
			}
		}
	}

}