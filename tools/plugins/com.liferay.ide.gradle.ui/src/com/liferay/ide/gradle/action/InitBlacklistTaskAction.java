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

package com.liferay.ide.gradle.action;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.core.workspace.LiferayWorkspaceUtil;
import com.liferay.ide.ui.action.AbstractObjectAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Seiphon Wang
 */
public class InitBlacklistTaskAction extends AbstractObjectAction {

	@Override
	public void run(IAction arg0) {
		IPath buildDirPath = _buildDir.getRawLocation();

		buildDirPath = buildDirPath.makeAbsolute();

		File buildDir = buildDirPath.toFile();

		IPath targetWarPath = _targetWarFile.getRawLocation();

		targetWarPath = targetWarPath.makeAbsolute();

		try {
			@SuppressWarnings("resource")
			ZipFile warFile = new ZipFile(targetWarPath.toString());

			Enumeration<? extends ZipEntry> warEntries = warFile.entries();

			HashSet<String> blackSet = new HashSet<>();

			while (warEntries.hasMoreElements()) {
				ZipEntry entry = warEntries.nextElement();

				String entryName = entry.getName();

				if (entryName.endsWith(".jar") && entryName.startsWith("WEB-INF/lib")) {
					try (InputStream in = warFile.getInputStream(entry)) {
						File tempFile = new File(buildDir, entryName.substring("WEB-INF/lib".length()));

						FileUtil.writeFileFromStream(tempFile, in);

						try (ZipFile tempZipFile = new ZipFile(tempFile)) {
							Enumeration<? extends ZipEntry> tempEntries = tempZipFile.entries();

							while (tempEntries.hasMoreElements()) {
								ZipEntry tempEntry = tempEntries.nextElement();

								if (tempEntry.isDirectory()) {
									String tempEntryName = tempEntry.getName();

									tempEntryName = tempEntryName.substring(0, tempEntryName.length() - 1);

									if (tempEntryName.contains("/")) {
										blackSet.add(tempEntry.toString() + ",\\");
									}
								}
							}
						}

						tempFile.delete();
					}
				}
			}

			ArrayList<String> blacklist = new ArrayList<>();

			blacklist.addAll(blackSet);

			Collections.sort(blacklist);

			String lastItem = blacklist.get(blacklist.size() - 1);

			lastItem = lastItem.replace(",\\", "");

			blacklist.set(blacklist.size() - 1, lastItem);

			IWorkspaceProject workspace = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

			IProject workspaceProject = workspace.getProject();

			IPath portalextPath = workspaceProject.getLocation();

			portalextPath = portalextPath.append(workspace.getLiferayHome());

			portalextPath = portalextPath.append("portal-ext.properties");

			File portalext = portalextPath.toFile();

			try {
				if (FileUtil.notExists(portalext)) {
					portalext.createNewFile();
				}

				if (FileUtil.exists(portalext) && portalext.canRead()) {
					try (BufferedWriter out = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(portalext, true)))) {

						out.write("\r\n");

						out.write("module.framework.web.servlet.annotation.scanning.blacklist=");

						for (String item : blacklist) {
							out.write(item + "\r\n");
						}
					}
				}
			}
			finally {
			}
		}
		catch (Exception e) {
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selection instanceof IStructuredSelection) {
			Object[] elements = ((IStructuredSelection)selection).toArray();

			if (ListUtil.isEmpty(elements)) {
				action.setEnabled(false);

				return;
			}

			Object element = elements[0];

			if (!(element instanceof IProject)) {
				action.setEnabled(false);

				return;
			}

			IWorkspaceProject workspace = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

			IProject workspaceProject = workspace.getProject();

			IPath liferayHomePath = workspaceProject.getLocation();

			liferayHomePath = liferayHomePath.append(workspace.getLiferayHome());

			File liferayHome = liferayHomePath.toFile();

			if (!liferayHome.exists()) {
				action.setEnabled(false);

				return;
			}

			IProject project = (IProject)element;

			IFolder buildDir = project.getFolder("build");

			if (!buildDir.exists()) {
				action.setEnabled(false);

				return;
			}

			_buildDir = buildDir;

			IFolder libsDir = buildDir.getFolder("libs");

			if (!libsDir.exists()) {
				action.setEnabled(false);

				return;
			}

			IFile targetFile = libsDir.getFile(project.getName() + ".war");

			if (targetFile.exists()) {
				action.setEnabled(true);

				_targetWarFile = targetFile;
			}
		}
	}

	private IFolder _buildDir;
	private IFile _targetWarFile;

}