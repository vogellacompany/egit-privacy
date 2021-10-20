package de.empri.devops.gitprivacy.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.framework.FrameworkUtil;

public class RedactEmailDialog extends TitleAreaDialog {

	private TableViewer v;
	private Repository repository;
	private Image image;
	private List<EMail> addressesToRedact;

	protected RedactEmailDialog(Shell parentShell, Repository repository) {
		super(parentShell);
		this.repository = repository;
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		Composite main = WidgetFactory.composite(SWT.NONE).layout(layout).create(parent);
		applyDialogFont(main);
		GridDataFactory.fillDefaults().indent(0, 0)
				.minSize(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), 0)
				.grab(true, true).applyTo(main);
		
		
		v = new TableViewer(main, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		v.setContentProvider(ArrayContentProvider.getInstance());
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, true);
		layoutData.minimumHeight = 300;
		v.getTable().setLayoutData(layoutData);

		TableColumn column = new TableColumn(v.getTable(),SWT.NONE);
		column.setWidth(300);
		column.setText(UIText.RedactEmailDialog_emailToRedactColumnName);
		TableViewerColumn viewerColumn1 = new TableViewerColumn(v, column);
		viewerColumn1.setEditingSupport(new EditEmailColumn(v));
		viewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EMail && element != null) {
					return ((EMail) element).address;
				}
				return super.getText(element);
			}

		});

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(v,
				new FocusCellOwnerDrawHighlighter(v));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(v, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(300);
		column.setText(UIText.RedactEmailDialog_replacementColumnName);
		TableViewerColumn viewerColumn2 = new TableViewerColumn(v, column);
		viewerColumn2.setEditingSupport(new EditReplacementColumn(v));
		viewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EMail && element != null) {
					return ((EMail) element).replacement;
				}
				return super.getText(element);
			}
		});

		List<EMail> model = new ArrayList<>();
		model.add(new EMail());
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		Composite buttonComposite = new Composite(main, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(buttonComposite);
		GridLayoutFactory.fillDefaults().applyTo(buttonComposite);

		Button addRowButton = new Button(buttonComposite, SWT.NONE);
		setButtonLayoutData(addRowButton);
		addRowButton.setText(UIText.RedactEmailDialog_addRowButtonText);

		addRowButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Table table = v.getTable();
				v.add(new EMail());
				table.setTopIndex(table.getItemCount());
				table.select(table.getItemCount() - 1);
			}
		});

		Button deleteRowButton = new Button(buttonComposite, SWT.NONE);
		setButtonLayoutData(deleteRowButton);
		deleteRowButton.setText(UIText.RedactEmailDialog_deleteRowButtonText);

		deleteRowButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = v.getSelection();
				if (selection instanceof IStructuredSelection) {
					Iterator<IStructuredSelection> iterator = ((IStructuredSelection) selection).iterator();
					while (iterator.hasNext()) {
						Object obj = iterator.next();
						v.remove(obj);
					}
				}
			}
		});

		return main;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			if (noEmailToReplaceGiven()) {
				MessageDialog.openError(getParentShell(), UIText.RedactEmailDialog_validationErrorDialog_title,
						UIText.RedactEmailDialog_validationErrorDialog_noAddressGivenMessage);
				return;
			}
			if (rowWithReplacementButNoEmailExists()) {
				MessageDialog.openError(getParentShell(), UIText.RedactEmailDialog_validationErrorDialog_title,
						UIText.RedactEmailDialog_validationErrorDialog_replacmentWithoutAddressMessage);
				return;
			}
			okPressed();
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}


	private boolean noEmailToReplaceGiven() {
		return !Arrays.stream(v.getTable().getItems()).map(item -> (EMail) item.getData())
				.anyMatch(email -> !email.address.isBlank());
	}

	private boolean rowWithReplacementButNoEmailExists() {
		return Arrays.stream(v.getTable().getItems()).map(item -> (EMail) item.getData())
				.anyMatch(email -> email.address.isBlank() && !email.replacement.isBlank());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, UIText.RedactEmailDialog_redactButtonText, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(UIText.RedactEmailDialog_dialogTitle);
	}

	@Override
	public void create() {
		super.create();
		setTitle(NLS.bind(UIText.RedactEmailDialog_titleArea_title,
				RepositoryUtil.INSTANCE.getRepositoryName(repository)));
		image = ImageDescriptor.createFromURL(FrameworkUtil.getBundle(getClass()).getEntry("icons/empri-logo.png")) //$NON-NLS-1$
				.createImage();
		setTitleImage(image);
		setMessage(UIText.RedactEmailDialog_titleArea_message, IMessageProvider.INFORMATION);
	}

	@Override
	public boolean close() {
		if (image != null) {
			image.dispose();
		}
		addressesToRedact = Arrays.stream(v.getTable().getItems()).map(item -> (EMail) item.getData())
				.collect(Collectors.toList());
		return super.close();
	}

	public List<EMail> getAddressesToRedact() {
		return addressesToRedact;
	}

	private class EditEmailColumn extends EditingSupport {

		public EditEmailColumn(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((EMail) element).address;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((EMail) element).address = value.toString();
			getViewer().update(element, null);
		}

	}

	private class EditReplacementColumn extends EditingSupport {

		public EditReplacementColumn(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((EMail) element).replacement;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((EMail) element).replacement = value.toString();
			getViewer().update(element, null);
		}

	}

	protected class EMail {

		String address = ""; //$NON-NLS-1$
		String replacement = "noreply@gitprivacy.invalid"; //$NON-NLS-1$

	}
	
}
