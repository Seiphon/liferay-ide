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

package com.liferay.ide.upgrade.plan.ui.internal.tasks;

import com.liferay.ide.ui.util.UIUtil;
import com.liferay.ide.upgrade.plan.core.UpgradeEvent;
import com.liferay.ide.upgrade.plan.core.UpgradeListener;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElement;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElementStatusChangedEvent;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeTask;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStep;
import com.liferay.ide.upgrade.plan.core.util.ServicesLookup;
import com.liferay.ide.upgrade.plan.ui.Disposable;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author Christopher Bryan Boyd
 * @author Terry Jia
 * @author Gregory Amerson
 */
public class UpgradeTaskItem implements IExpansionListener, UpgradeItem, UpgradeListener {

	public UpgradeTaskItem(FormToolkit formToolkit, ScrolledForm scrolledForm, UpgradeTask upgradeTask) {
		_formToolkit = formToolkit;
		_scrolledForm = scrolledForm;
		_upgradeTask = upgradeTask;

		Composite parentComposite = _scrolledForm.getBody();

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();

		gridDataFactory.grab(true, true);

		parentComposite.setLayoutData(gridDataFactory.create());

		parentComposite.setLayout(new TableWrapLayout());

		_disposables.add(() -> parentComposite.dispose());

		FormText description = _formToolkit.createFormText(parentComposite, true);

		description.setLayoutData(new TableWrapData(TableWrapData.FILL));

		description.setText(_upgradeTask.getDescription(), true, false);

		_disposables.add(() -> description.dispose());

		_buttonComposite = _formToolkit.createComposite(parentComposite);

		GridLayout buttonGridLayout = new GridLayout(4, false);

		buttonGridLayout.marginHeight = 2;
		buttonGridLayout.marginWidth = 2;
		buttonGridLayout.verticalSpacing = 2;

		_buttonComposite.setLayout(buttonGridLayout);

		_buttonComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		_disposables.add(() -> _buttonComposite.dispose());

		Label fillLabel = _formToolkit.createLabel(_buttonComposite, null);

		GridData gridData = new GridData();

		gridData.widthHint = 16;

		fillLabel.setLayoutData(gridData);

		_disposables.add(() -> fillLabel.dispose());

		Image taskRestartImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_RESTART_IMAGE);

		ImageHyperlink taskRestartImageHyperlink = createImageHyperlink(
			_formToolkit, _buttonComposite, taskRestartImage, this, "Click to restart",
			"Restarting " + _upgradeTask.getTitle() + "...", this::_restartTask);

		_disposables.add(() -> taskRestartImageHyperlink.dispose());

		_restartable = taskRestartImageHyperlink;

		_upgradePlanner = ServicesLookup.getSingleService(UpgradePlanner.class, null);

		_upgradePlanner.addListener(this);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.add(listener);
	}

	public void dispose() {
		_upgradePlanner.removeListener(this);

		for (Disposable disposable : _disposables) {
			try {
				disposable.dispose();
			}
			catch (Throwable t) {
			}
		}
	}

	@Override
	public void expansionStateChanged(ExpansionEvent expansionEvent) {
		ISelection selection = new StructuredSelection(_upgradeTask);

		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);

		_listeners.forEach(
			selectionChangedListener -> {
				selectionChangedListener.selectionChanged(selectionChangedEvent);
			});

		_scrolledForm.reflow(true);
	}

	@Override
	public void expansionStateChanging(ExpansionEvent expansionEvent) {
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void onUpgradeEvent(UpgradeEvent upgradeEvent) {
		if (upgradeEvent instanceof UpgradePlanElementStatusChangedEvent) {
			UpgradePlanElementStatusChangedEvent upgradePlanElementStatusChangedEvent =
				(UpgradePlanElementStatusChangedEvent)upgradeEvent;

			UpgradePlanElement upgradePlanElement = upgradePlanElementStatusChangedEvent.getUpgradePlanElement();

			String elementId = upgradePlanElement.getId();

			for (UpgradeTaskStep step : _upgradeTask.getSteps()) {
				if (elementId.equals(step.getId())) {
					UIUtil.async(() -> _updateRestartablement(_upgradeTask, _restartable));
				}
			}
		}
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	private static void _updateRestartablement(UpgradeTask upgradeTask, Control restartControl) {
		AtomicBoolean restartable = new AtomicBoolean(false);

		if (upgradeTask.restartable()) {
			restartable.set(true);
		}

		if (!restartControl.isDisposed()) {
			restartControl.setEnabled(restartable.get());
		}
	}

	private IStatus _restartTask(IProgressMonitor progressMonitor) {
		UpgradePlanner upgradePlanner = ServicesLookup.getSingleService(UpgradePlanner.class, null);

		upgradePlanner.restartTask(_upgradeTask);

		return Status.OK_STATUS;
	}

	private Composite _buttonComposite;
	private List<Disposable> _disposables = new ArrayList<>();
	private FormToolkit _formToolkit;
	private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();
	private Control _restartable;
	private ScrolledForm _scrolledForm;
	private UpgradePlanner _upgradePlanner;
	private final UpgradeTask _upgradeTask;

}