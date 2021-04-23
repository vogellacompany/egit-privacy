package de.empri.devops.gitprivacy.preferences;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class GitPrivacySettings
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private List<BooleanFieldEditor> repoBooleans;

	public GitPrivacySettings() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, FrameworkUtil.getBundle(this.getClass()).getSymbolicName())); //$NON-NLS-1$
		setDescription("A demonstration of a preference page implementation");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
				"org.eclipse.egit.core"); //$NON-NLS-1$
		String string = scopedPreferenceStore.getString("GitRepositoriesView.GitDirectories");
		System.out.println(string);
		String[] split = string.split(":");
		repoBooleans = new ArrayList<>(split.length);
		for (String s : split) {
			BooleanFieldEditor editor = new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN, s,
					getFieldEditorParent());
			addField(editor);
			repoBooleans.add(editor);
		}

		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	
	@Override
	public boolean performOk() {
		super.performOk();
		
		repoBooleans.stream().filter(BooleanFieldEditor::getBooleanValue).forEach(editor -> {
			Bundle bundle = FrameworkUtil.getBundle(this.getClass());
			Path path = new Path("");
			URL fileURL = FileLocator.find(bundle, path, null);
			System.out.println("### fileURL: " + fileURL);
			try {
				URL pluginURL = FileLocator.resolve(bundle.getEntry("/"));
				System.out.println("### pluginURL: " + pluginURL);
				String trimmedPath = pluginURL.getPath().trim();
				System.out.println("### trimmedPath: " + trimmedPath);
				URI uri = pluginURL.toURI();
				System.out.println("### uri: " + uri);
				String externalForm = pluginURL.toExternalForm();
				System.out.println("### externalForm: " + externalForm);
//			File output = new File("/home/vogella/git/hooktesting/.git/hooks/post-commit2");
			File output = new File(editor.getLabelText() + "/hooks/post-commit");
				// TODO(FAP): this works, but now we also need to change the jar file name from
				// settings to the hook plug-in
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						new FileInputStream(new File(new URI(trimmedPath.substring(0, trimmedPath.length() - 2)))));
				bufferedInputStream.transferTo(new FileOutputStream(output));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		});

		return true;
	}

}