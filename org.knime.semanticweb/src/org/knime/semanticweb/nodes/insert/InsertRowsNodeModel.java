package org.knime.semanticweb.nodes.insert;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.semanticweb.port.SemanticWebPortObject;
import org.knime.semanticweb.services.RDFService;
import org.knime.semanticweb.utility.SemanticWebSettings;
import org.knime.semanticweb.utility.SemanticWebUtility;


/**
 * @author Lara Gorini
 *
 */
public class InsertRowsNodeModel extends NodeModel {

	private final SemanticWebSettings m_settings = new SemanticWebSettings();

	/**
	 * The constructor if this class.
	 */
	public InsertRowsNodeModel() {
		super(new PortType[] { SemanticWebPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { SemanticWebPortObject.TYPE });
	}

	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/** {@inheritDoc} */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.NONDISTRIBUTED_NONSTREAMABLE,
				InputPortRole.NONDISTRIBUTED_STREAMABLE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.NONDISTRIBUTED };
	}

	private void run(final ExecutionContext exec, final RDFService service, final RowInput rowInput)
			throws InterruptedException, InvalidSettingsException {

		final DataTableSpec spec = rowInput.getDataTableSpec();
		final int subject_idx = spec.findColumnIndex(m_settings.getSubject());
		final int predicate_idx = spec.findColumnIndex(m_settings.getPredicate());
		final int object_idx = spec.findColumnIndex(m_settings.getObject());

		final String defaultUri = m_settings.getURI();

		final int graphName_idx = spec.findColumnIndex(m_settings.getGraphName());

		final Set<String> names = new LinkedHashSet<String>();

		DataRow row;

		while ((row = rowInput.poll()) != null) {

			final StringBuilder update = new StringBuilder();

			final String graphName = SemanticWebUtility.getGraphName(row, graphName_idx, defaultUri, service,
					m_settings.getGlobalGraphName());

			// graphName can be null, depends on service

			final String subject = SemanticWebUtility.getResourceName(defaultUri,
					((StringCell) row.getCell(subject_idx)).getStringValue());
			final String predicate = SemanticWebUtility.getResourceName(defaultUri,
					((StringCell) row.getCell(predicate_idx)).getStringValue());
			final String object = ((StringCell) row.getCell(object_idx)).getStringValue();

			final String insert = "<" + subject + "> <" + predicate + "> \"" + object + "\"";

			update.append("INSERT DATA " + service.getQueryStatement(insert, graphName));

			if (names.contains(graphName)) {
				service.updateQuery(update.toString(), getCredentialsProvider());
			} else {
				if (!service.containsGraphName(graphName, getCredentialsProvider())) {
					service.createGraph(graphName, getCredentialsProvider());
					names.add(graphName);
				}
				service.updateQuery(update.toString(), getCredentialsProvider());
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new StreamableOperator() {

			@Override
			public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
					throws Exception {

				final RDFService service = ((SemanticWebPortObject) ((PortObjectInput) inputs[0]).getPortObject())
						.getService();
				final RowInput rowInput = (RowInput) inputs[1];
				run(exec, service, rowInput);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		final RDFService service = ((SemanticWebPortObject) inObjects[0]).getService();
		final DataTableRowInput rowInput = new DataTableRowInput((BufferedDataTable) inObjects[1]);
		run(exec, service, rowInput);
		return new PortObject[] { inObjects[0] };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (m_settings.getSubject() == null || m_settings.getPredicate() == null || m_settings.getObject() == null) {
			throw new InvalidSettingsException("Selecting subject, predicate and object is mandatory");
		}

		return new PortObjectSpec[] { inSpecs[0] };
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_settings.saveModelSettingsTo(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings.loadValidatedSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// not used
	}

}
