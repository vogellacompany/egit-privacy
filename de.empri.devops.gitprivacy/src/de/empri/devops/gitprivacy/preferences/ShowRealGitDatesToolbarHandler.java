package de.empri.devops.gitprivacy.preferences;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.internal.IRepositoryCommit;
import org.eclipse.egit.ui.internal.history.HistoryPageInput;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
/** <b>Warning</b> : 
As explained in <a href="http://wiki.eclipse.org/Eclipse4/RCP/FAQ#Why_aren.27t_my_handler_fields_being_re-injected.3F">this wiki page</a>, it is not recommended to define @Inject fields in a handler. <br/><br/>
<b>Inject the values in the @Execute methods</b>
*/
public class ShowRealGitDatesToolbarHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object o, MPart part) {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~");
//		MessageDialog.openInformation(s, "E4 Information Dialog", "Hello world from a pure Eclipse 4 plug-in");
		System.out.println(o);
		if (o instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) o;
			if (sel.getFirstElement() instanceof IRepositoryCommit) {
				IRepositoryCommit commit = (IRepositoryCommit) sel.getFirstElement();
				Repository repository = commit.getRepository();
				System.out.println("from selection:");
				System.out.println(repository);
			}
		}
		Object widget = part.getWidget();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart activePart = activePage.getActivePart();
		if (activePart instanceof IHistoryView) {
			IHistoryView genericHistoryView = (IHistoryView) activePart;
			IHistoryPage gitHistoryPage = genericHistoryView.getHistoryPage();
			if (gitHistoryPage != null && gitHistoryPage.getInput() instanceof HistoryPageInput) {
					HistoryPageInput historyPageInput = (HistoryPageInput) gitHistoryPage.getInput();
					Repository repository = historyPageInput.getRepository();
					System.out.println("from page:");
					System.out.println(repository);
					System.out.println(repository.getDirectory());
			}
		}
	}


}
