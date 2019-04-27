package org.knime.semanticweb.nodes.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.semanticweb.port.SemanticWebPortObject;
import org.knime.semanticweb.services.RDFService;

/**
 *
 * @author Lara Gorini
 *
 */
public class SPARQLQueryNodeModel extends NodeModel {

	private final SettingsModelString m_query = createQueryModel();

	private final SettingsModelLongBounded m_timeout = createTimeoutModel();

	static SettingsModelString createQueryModel() {
		return new SettingsModelString("query", "SELECT * WHERE { ?sub ?pred ?obj }");
	}

	static SettingsModelLongBounded createTimeoutModel() {
		return new SettingsModelLongBounded("timeout", 60, 0, Long.MAX_VALUE);
	}

	/**
	 * The constructor is this class.
	 */
	public SPARQLQueryNodeModel() {
		super(new PortType[] { SemanticWebPortObject.TYPE }, new PortType[] { BufferedDataTable.TYPE });
	}

	/** {@inheritDoc} */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.NONDISTRIBUTED_NONSTREAMABLE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.NONDISTRIBUTED };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_query.saveSettingsTo(settings);
		m_timeout.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_query.validateSettings(settings);
		m_timeout.validateSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_query.loadSettingsFrom(settings);
		m_timeout.loadSettingsFrom(settings);
	}

	private void run(final ExecutionContext exec, final RDFService service, final RowOutput rowOutput)
			throws InterruptedException, InvalidSettingsException {
		final long timeout = m_timeout.getLongValue() * 1000;
		final Query query = QueryFactory.create(m_query.getStringValue());
		final ResultSet resultSet = executeQuery(service.createQueryExecution(query, getCredentialsProvider()),
				timeout);
		writeToTable(rowOutput, resultSet);
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
				final RowOutput rowOutput = (RowOutput) outputs[0];
				run(exec, service, rowOutput);
			}

		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		final RDFService service = ((SemanticWebPortObject) inObjects[0]).getService();
		final BufferedDataTableRowOutput rowOutput = new BufferedDataTableRowOutput(exec.createDataContainer(
				createSpec(service.createQueryExecution(QueryFactory.create(m_query.getStringValue()), getCredentialsProvider()),
						m_timeout.getLongValue() * 1000)));
		run(exec, service, rowOutput);
		return new PortObject[] { rowOutput.getDataTable() };
	}

	/**
	 * Writes values of a {@link ResultSet} into a {@link BufferedDataTable}.
	 *
	 * @param rowOutput
	 *            The table output.
	 * @param resultSet
	 *            The result set.
	 * @throws InterruptedException
	 */
	public static void writeToTable(final RowOutput rowOutput, final ResultSet resultSet) throws InterruptedException {
		final List<String> resultVars = resultSet.getResultVars();

		final DataCell[] dataCells = new DataCell[resultVars.size()];
		long index = 0;
		while (resultSet.hasNext()) {
			final QuerySolution querySolution = resultSet.next();
			final Iterator<String> varNames = querySolution.varNames();
			while (varNames.hasNext()) {
				final String varName = varNames.next();
				final int indexOf = resultVars.indexOf(varName);
				final RDFNode rdfNode = querySolution.get(varName);
				if (rdfNode != null) {
					final String string = rdfNode.toString();
					dataCells[indexOf] = new StringCell(string);
				} else {
					dataCells[indexOf] = DataType.getMissingCell();
				}
			}
			rowOutput.push(new DefaultRow(RowKey.createRowKey(index++), dataCells));
		}
		rowOutput.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return null;
	}

	/**
	 * Executes the {@link QueryExecution}.
	 *
	 * @param queryExecution
	 *            The {@link QueryExecution}.
	 * @param timeout
	 *            Execution interrupts after timeout.
	 * @return the result Set.
	 */
	public static ResultSet executeQuery(final QueryExecution queryExecution, final long timeout) {
		queryExecution.setTimeout(timeout);
		final ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}

	/**
	 * Creates a {@link DataTableSpec} of output {@link BufferedDataTable}.
	 *
	 * @param queryExecution
	 *            The {@link QueryExecution}.
	 * @param timeout
	 *            Execution interrupts after timeout.
	 * @return the spec of output.
	 */
	public static DataTableSpec createSpec(final QueryExecution queryExecution, final long timeout) {
		final ResultSet resultSet = executeQuery(queryExecution, timeout);
		final List<String> resultVars = resultSet.getResultVars();
		final List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
		for (final String name : resultVars) {
			colSpecs.add(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
		}
		return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()]));
	}
}
