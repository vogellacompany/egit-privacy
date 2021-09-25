package de.empri.devops.gitprivacy.preferences;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.TimeZone;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder.DecodedDates;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoderFactory;

public class ShowOriginalCommitDatesDialog extends Dialog {

	protected static final int SHORT_OBJECT_ID_LENGTH = 7;
	// EGit also has org.eclipse.egit.ui.internal.PreferenceBasedDateFormatter.java
	private GitDateFormatter relativeDateFormatter = new GitDateFormatter(GitDateFormatter.Format.RELATIVE);
	private GitDateFormatter isoDateFormatter = new GitDateFormatter(GitDateFormatter.Format.ISO);
	private static final String GIT_ISO_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
	private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(GIT_ISO_TIME_PATTERN);
	private Repository repository;
	private TableViewer tableViewer;
	private TableColumnLayout tableColumnLayout;
	private Optional<OriginalCommitDateEncoder> encoderOptional;

	public ShowOriginalCommitDatesDialog(Shell parent) {
		super(parent);
	}

	public ShowOriginalCommitDatesDialog(Shell parent, Repository repository) {
		super(parent);
		this.repository = repository;
		encoderOptional = OriginalCommitDateEncoderFactory.build(repository);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Original Commit Dates");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		SashForm main = new SashForm(parent, SWT.VERTICAL);
		GridLayoutFactory.swtDefaults().applyTo(main);
		GridDataFactory.fillDefaults().indent(0, 0).minSize(1000, 600).grab(true, true).applyTo(main);
		Composite tableParent = new Composite(main, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(tableParent);
		GridDataFactory.fillDefaults().indent(0, 0).grab(true, true).applyTo(tableParent);
		tableColumnLayout = new TableColumnLayout();
		tableParent.setLayout(tableColumnLayout);
		tableViewer = createTableViewer(tableParent);
		// TODO(FAP): fix layout so textViewer is properly visible
		TextViewer textViewer = new TextViewer(main, SWT.V_SCROLL | SWT.BORDER);
		textViewer.setEditable(false);
		tableViewer.addSelectionChangedListener(sel -> {
			ISelection selection = sel.getSelection();
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				RevCommit firstElement = (RevCommit) structuredSelection.getFirstElement();
				textViewer.setDocument(new Document(firstElement.getFullMessage()));
			}
		});
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.minimumHeight = 200;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		textViewer.getControl().setLayoutData(gridData);

		main.setWeights(40000, 10000);
		return main;
	}

	private TableViewer createTableViewer(Composite main) {
		TableViewer viewer = new TableViewer(main,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		createColumns(viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		HashSet<RevCommit> revCommits = new LinkedHashSet<>();
		try (RevWalk revWalk = new RevWalk(repository)) {
			try {
				ObjectId headCommit = repository.resolve(Constants.HEAD);
				if (headCommit != null) {
					revWalk.markStart(revWalk.parseCommit(headCommit));
				}
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
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		return viewer;
	}

	private void createColumns(TableViewer viewer) {
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		String[] titles = { "Id", "Message", "Author", "Authored Date", "Real Date", "Commiter", "Committed Date",
				"Real Date" };

		// TODO(FAP): see CommitLabelProvider#getColumnText, CommitGraphTable#createColumns
		// TODO(FAP): tooltips come from org.eclipse.egit.ui.internal.history.CommitGraphTableHoverManager

		TableViewerColumn col = createTableViewerColumn(viewer, titles[0]);
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

		col = createTableViewerColumn(viewer, titles[1]);
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

		col = createTableViewerColumn(viewer, titles[2]);
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

		col = createTableViewerColumn(viewer, titles[3]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				return relativeAuthorDate(r);
			}

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				return isoDateFormatter.formatDate(r.getAuthorIdent());
			}
		});

		col = createTableViewerColumn(viewer, titles[4]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				if (encoderOptional.isEmpty()) {
					return "";
				}
				Optional<DecodedDates> decoded = encoderOptional.get().decode(r.getFullMessage());
				if (decoded.isEmpty()) {
					return "";
				}
				ZonedDateTime authoredDateTime = decoded.get().getAuthoredDateTime();
				Date date = Date.from(authoredDateTime.toInstant());
				return relativeDateFormatter
						.formatDate(new PersonIdent("", "", date, TimeZone.getTimeZone(authoredDateTime.getZone())));
			}


			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				if (encoderOptional.isEmpty()) {
					return null;
				}
				Optional<DecodedDates> decoded = encoderOptional.get().decode(r.getFullMessage());
				if (decoded.isEmpty()) {
					return null;
				}
				ZonedDateTime authoredDateTime = decoded.get().getAuthoredDateTime();
				Date date = Date.from(authoredDateTime.toInstant());
				return isoDateFormatter
						.formatDate(new PersonIdent("", "", date, TimeZone.getTimeZone(authoredDateTime.getZone())));
            }
        });

		col = createTableViewerColumn(viewer, titles[5]);
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

		col = createTableViewerColumn(viewer, titles[6]);
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

		col = createTableViewerColumn(viewer, titles[7]);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(5, 120));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				RevCommit r = (RevCommit) element;
				if (encoderOptional.isEmpty()) {
					return "";
				}
				Optional<DecodedDates> decoded = encoderOptional.get().decode(r.getFullMessage());
				if (decoded.isEmpty()) {
					return "";
				}
				ZonedDateTime committedDateTime = decoded.get().getCommittedDateTime();
				Date date = Date.from(committedDateTime.toInstant());
				return relativeDateFormatter
						.formatDate(new PersonIdent("", "", date, TimeZone.getTimeZone(committedDateTime.getZone())));
			}

			@Override
			public String getToolTipText(Object element) {
				RevCommit r = (RevCommit) element;
				if (encoderOptional.isEmpty()) {
					return null;
				}
				Optional<DecodedDates> decoded = encoderOptional.get().decode(r.getFullMessage());
				if (decoded.isEmpty()) {
					return null;
				}
				ZonedDateTime committedDateTime = decoded.get().getCommittedDateTime();
				Date date = Date.from(committedDateTime.toInstant());
				return isoDateFormatter
						.formatDate(new PersonIdent("", "", date, TimeZone.getTimeZone(committedDateTime.getZone())));
			}
		});
	}

	private String relativeAuthorDate(RevCommit r) {
		return relativeDateFormatter.formatDate(r.getAuthorIdent());
	}

	private TableViewerColumn createTableViewerColumn(TableViewer tableViewer, String title) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

}
