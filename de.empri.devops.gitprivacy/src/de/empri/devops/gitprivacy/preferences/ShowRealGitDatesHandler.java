package de.empri.devops.gitprivacy.preferences;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/** <b>Warning</b> : 
As explained in <a href="http://wiki.eclipse.org/Eclipse4/RCP/FAQ#Why_aren.27t_my_handler_fields_being_re-injected.3F">this wiki page</a>, it is not recommended to define @Inject fields in a handler. <br/><br/>
<b>Inject the values in the @Execute methods</b>
*/
public class ShowRealGitDatesHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object o, MPart part) {
		
		MessageDialog.openInformation(s, "E4 Information Dialog", "Hello world from a pure Eclipse 4 plug-in");
		System.out.println(o);
		Object widget = part.getWidget();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

	}


}
