package com.liferay.ide.gradle.core;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.internal.core.refactoring.resource.DeleteResourcesProcessor;

@SuppressWarnings("restriction")
public class LiferayGradleModuleProjectDeleteParticipant extends DeleteParticipant {

		public LiferayGradleModuleProjectDeleteParticipant() {
		}

		@Override
		public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {

			return new RefactoringStatus();
		}

		@Override
		public String getName() {
			return null;
		}

		public class RmoveModulePreChange extends RemoveModuleChange{
		
			public RmoveModulePreChange(IProject gradleProject) {
				super(gradleProject);
			}

			@Override
			public Change perform(IProgressMonitor pm) throws CoreException {
				
				IProject workspaceProject = LiferayWorkspaceUtil.getWorkspaceProject();
				
				IWorkspaceProject liferayWorkspaceProject = LiferayCore.create(IWorkspaceProject.class, workspaceProject);
				
				Set<IProject> watchingProject = liferayWorkspaceProject.watching();

				
				System.out.print(watchingProject.size());

				//find watch job and stop job
				
				return null;
			}
		}
		
		public class RmoveModulePostChange extends RemoveModuleChange{
			
			public RmoveModulePostChange(IProject gradleProject) {
				super(gradleProject);
			}

			@Override
			public Change perform(IProgressMonitor pm) throws CoreException {
				
				//get liferay workspace project
				
				//get watching project list
				
				//remove delete project
				
				//check watch condition
				
				//restart watch job
				
				return null;
			}
		}		
		
		public abstract class RemoveModuleChange extends Change {

			public RemoveModuleChange(IProject gradleProject) {
				_gradleProject = gradleProject;
			}

			@Override
			public Object getModifiedElement() {
				return _gradleProject;
			}

			@Override
			public String getName() {
				return "Remove module from workspace project watch list '" + _gradleProject.getName() + "'";
			}

			@Override
			public void initializeValidationData(IProgressMonitor pm) {
			}

			@Override
			public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
				return new RefactoringStatus();
			}

			public abstract Change perform(IProgressMonitor pm) throws CoreException; 

			private IProject _gradleProject;

		}

		@Override
		public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RmoveModulePreChange(_moduleProject);
		}
		
		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RmoveModulePostChange(_moduleProject);
		}		
		
		@Override
		protected boolean initialize(Object element) {
			if (!(element instanceof IProject)) {
				return false;
			}

			RefactoringProcessor processor = getProcessor();

			if (processor instanceof DeleteResourcesProcessor) {
				DeleteResourcesProcessor deleteProcessor = (DeleteResourcesProcessor)processor;

				if (!deleteProcessor.isDeleteContents()) {
					return false;
				}
			}

			_moduleProject = (IProject)element;

			IProject workspaceProject = LiferayWorkspaceUtil.getWorkspaceProject();
			
			IWorkspaceProject liferayWorkspaceProject = LiferayCore.create(IWorkspaceProject.class, workspaceProject);
			
			Set<IProject> watchingProject = liferayWorkspaceProject.watching();

			return watchingProject.contains(_moduleProject);
		}

		private IProject _moduleProject;


}