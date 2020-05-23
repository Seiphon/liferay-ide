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

package com.liferay.ide.project.ui;

import com.liferay.ide.project.core.util.SampleProjectUtil;
import com.liferay.ide.project.ui.samples.NewSampleWizard;
import com.liferay.ide.ui.util.UIUtil;

import java.util.Collections;
import java.util.Date;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.core.INotificationService;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Seiphon Wang
 */
@SuppressWarnings("restriction")
public class SampleProjectUIUtil {

	public static AbstractNotification createDownloadRequiredNotification(String liferayVersion) {
		Date date = new Date();

		return new AbstractUiNotification("com.liferay.ide.project.ui.notifications.downloadrequired") {

			@SuppressWarnings({"rawtypes", "unchecked"})
			public Object getAdapter(Class adapter) {
				return null;
			}

			@Override
			public Date getDate() {
				return date;
			}

			@Override
			public String getDescription() {
				return "Liferay blade samples archive " + liferayVersion + " download completes.";
			}

			@Override
			public String getLabel() {
				return "New Sample Project";
			}

			@Override
			public Image getNotificationImage() {
				return null;
			}

			@Override
			public Image getNotificationKindImage() {
				return null;
			}

			@Override
			public void open() {
				Shell activeShell = UIUtil.getActiveShell();

				WizardDialog dialog = new WizardDialog(activeShell, new NewSampleWizard());

				dialog.open();
			}

		};
	}

	public static void listenIfDownloadCompletes() {
		IJobManager jobManager = Job.getJobManager();

		Job[] jobs = jobManager.find(null);

		for (Job job : jobs) {
			if (job.getProperty(SampleProjectUtil.LIFERAY_PROJECT_DOWNLOAD_JOB) != null) {
				Object liferayVersion = job.getProperty(SampleProjectUtil.LIFERAY_BLADE_ARCHIVE_VERSION);

				job.addJobChangeListener(
					new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							INotificationService notificationService = NotificationsUi.getService();

							notificationService.notify(
								Collections.singletonList(
									SampleProjectUIUtil.createDownloadRequiredNotification(liferayVersion.toString())));
						}

					});
			}
		}
	}

}