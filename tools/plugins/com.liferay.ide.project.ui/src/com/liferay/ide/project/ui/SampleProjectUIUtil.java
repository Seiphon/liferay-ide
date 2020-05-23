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

import java.util.Date;

import org.eclipse.mylyn.commons.notifications.core.AbstractNotification;
import org.eclipse.mylyn.commons.notifications.ui.AbstractUiNotification;
import org.eclipse.swt.graphics.Image;

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
				return "liferay-blade-samples-" + liferayVersion +
					".jar is downloading, please wait a minute and try again. Here is the download link: " +
						"https://github.com/liferay/liferay-blade-samples/archive/" + liferayVersion + ".zip";
			}

			@Override
			public String getLabel() {
				return "Download Blade Sample Archive";
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
			}

		};
	}

}