package com.liferay.ide.gradle.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class GradleProjectPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IProject) {
			try {
				return GradleUtil.isGradleProject((IProject)receiver);
			}
			catch (CoreException ce) {
			}
		}

		return false;
	}
}
