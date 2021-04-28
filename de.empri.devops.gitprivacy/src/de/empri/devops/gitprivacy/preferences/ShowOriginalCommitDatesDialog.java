package de.empri.devops.gitprivacy.preferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.GitDateFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ShowOriginalCommitDatesDialog extends Dialog {

	protected static final int SHORT_OBJECT_ID_LENGTH = 7;
	private Repository repository;
	private TableViewer viewer;
	private TableColumnLayout tableColumnLayout;

	public ShowOriginalCommitDatesDialog(Shell parent) {
		super(parent);
	}

	public ShowOriginalCommitDatesDialog(Shell parent, Repository repository) {
		super(parent);
		this.repository = repository;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Original Commit Dates");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(main);
		GridDataFactory.fillDefaults().indent(0, 0).grab(true, true).applyTo(main);
		tableColumnLayout = new TableColumnLayout();
		main.setLayout(tableColumnLayout);
		createViewer(main);

		return main;
	}

	private void createViewer(Composite main) {
		viewer = new TableViewer(main,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(main, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		HashSet<RevCommit> revCommits = new LinkedHashSet<>();
		try (RevWalk revWalk = new RevWalk(repository)) {
			try {
				ObjectId headCommit = repository.resolve(Constants.HEAD);
				revWalk.markStart(revWalk.parseCommit(headCommit));
			} catch (RevisionSyntaxException | IOException e1) {
				System.out.println(e1);
			}
			revWalk.forEach(revCommit -> {
				revCommits.add(revCommit);
			});
		}
		viewer.setInput(revCommits);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
	}

	private void createColumns(Composite main, TableViewer viewer) {
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		String[] titles = { "Id", "Message", "Author", "Authored Date", "Commiter", "Committed Date" };

		// TODO(FAP): see CommitLabelProvider#getColumnText, CommitGraphTable#createColumns
		// TODO(FAP): tooltips come from org.eclipse.egit.ui.internal.history.CommitGraphTableHoverManager

		TableViewerColumn col = createTableViewerColumn(titles[0]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnPixelData(70, false));
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                RevCommit r = (RevCommit) element;
				return r.getId().abbreviate(SHORT_OBJECT_ID_LENGTH).name();
            }

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				return r.getName() + '\n' + r.getShortMessage();
			}
        });

		col = createTableViewerColumn(titles[1]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(20, 300));
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	RevCommit r = (RevCommit) element;
                return r.getShortMessage();
            }

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				return r.getShortMessage();
			}
        });

		col = createTableViewerColumn(titles[2]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				return r.getAuthorIdent().getName();
            }

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				PersonIdent authorIdent = r.getAuthorIdent();
				return authorIdent.getName() + " <" + authorIdent.getEmailAddress() + ">";
			}
        });

		// EGit also has org.eclipse.egit.ui.internal.PreferenceBasedDateFormatter.java
		GitDateFormatter relativeDateFormatter = new GitDateFormatter(GitDateFormatter.Format.RELATIVE);
		GitDateFormatter isoDateFormatter = new GitDateFormatter(GitDateFormatter.Format.ISO);
		col = createTableViewerColumn(titles[3]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				return relativeDateFormatter.formatDate(r.getAuthorIdent());
			}

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				return isoDateFormatter.formatDate(r.getAuthorIdent());
            }
        });

		col = createTableViewerColumn(titles[3]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				return r.getCommitterIdent().getName();
			}

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				PersonIdent comiterIdent = r.getCommitterIdent();
				return comiterIdent.getName() + " <" + comiterIdent.getEmailAddress() + ">";
			}
		});

		col = createTableViewerColumn(titles[4]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				return relativeDateFormatter.formatDate(r.getCommitterIdent());
			}

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				return isoDateFormatter.formatDate(r.getCommitterIdent());
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

}
