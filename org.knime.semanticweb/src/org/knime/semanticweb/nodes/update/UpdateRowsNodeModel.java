/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.semanticweb.nodes.update;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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
import org.knime.semanticweb.utility.SemanticWebUpdateSettings;
import org.knime.semanticweb.utility.SemanticWebUtility;

/**
 * @author Lara Gorini
 *
 */
public class UpdateRowsNodeModel extends NodeModel {

	private final SemanticWebUpdateSettings m_settings = new SemanticWebUpdateSettings();

	/**
	 * The constructor of this class.
	 */
	public UpdateRowsNodeModel() {
		super(new PortType[] { SemanticWebPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { SemanticWebPortObject.TYPE });
	}

	/** {@inheritDoc} */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_settings.saveModelSettingsTo(settings);
	}

	/** {@inheritDoc} */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings.loadValidatedSettingsFrom(settings);

	}

	/** {@inheritDoc} */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_settings.validateSettings(settings);

	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/** {@inheritDoc} */
	@Override
	protected void reset() {
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

		final DataTableSpec inSpec = rowInput.getDataTableSpec();

		final int subIdx = inSpec.findColumnIndex(m_settings.getSubject());
		final int preIdx = inSpec.findColumnIndex(m_settings.getPredicate());
		final int objIdx = inSpec.findColumnIndex(m_settings.getObject());
		final int newIdx = inSpec.findColumnIndex(m_settings.getNewVal());
		final String defaultUri = m_settings.getURI();

		final boolean ignoreMissing = subIdx >= 0 && preIdx >= 0 && objIdx >= 0;
		final int graphName_idx = inSpec.findColumnIndex(m_settings.getGraphName());

		DataRow row;

		final StringBuilder update = new StringBuilder();
		final StringBuilder filter = new StringBuilder();
		final StringBuilder insert = new StringBuilder();

		while ((row = rowInput.poll()) != null) {

			filter.setLength(0);
			insert.setLength(0);

			final String graphName = SemanticWebUtility.getGraphName(row, graphName_idx, defaultUri, service,
					m_settings.getGlobalGraphName());

			final DataCell sub = SemanticWebUtility.getCheckCell(subIdx, row, ignoreMissing);
			final DataCell pre = SemanticWebUtility.getCheckCell(preIdx, row, ignoreMissing);
			final DataCell obj = SemanticWebUtility.getCheckCell(objIdx, row, ignoreMissing);
			final DataCell nnew = SemanticWebUtility.getCheckCell(newIdx, row, ignoreMissing);

			if (!sub.isMissing() && !pre.isMissing() && !obj.isMissing() && !nnew.isMissing()) {
				insert.append("<" + SemanticWebUtility.getResourceName(defaultUri, sub.toString()) + "> <"
						+ SemanticWebUtility.getResourceName(defaultUri, pre.toString()) + "> \"" + nnew.toString() + "\"");
				filter.append("( ?sub = <" + SemanticWebUtility.getResourceName(defaultUri, sub.toString()) + "> && ?pre = <"
						+ SemanticWebUtility.getResourceName(defaultUri, pre.toString()) + "> && ?obj = \"" + obj.toString()
						+ "\" )");
			} else if (!sub.isMissing() && !pre.isMissing() && obj.isMissing() && !nnew.isMissing()) {
				insert.append("<" + SemanticWebUtility.getResourceName(defaultUri, sub.toString()) + "> <"
						+ SemanticWebUtility.getResourceName(defaultUri, nnew.toString()) + "> ?obj");
				filter.append("( ?sub = <" + SemanticWebUtility.getResourceName(defaultUri, sub.toString()) + "> && ?pre = <"
						+ SemanticWebUtility.getResourceName(defaultUri, pre.toString()) + ">)");
			} else if (!sub.isMissing() && pre.isMissing() && obj.isMissing() && !nnew.isMissing()) {
				insert.append("<" + SemanticWebUtility.getResourceName(defaultUri, nnew.toString()) + "> ?pre ?obj");
				filter.append("( ?sub = <" + SemanticWebUtility.getResourceName(defaultUri, sub.toString()) + "> )");
			} else if (sub.isMissing() && !pre.isMissing() && obj.isMissing() && !nnew.isMissing()) {
				insert.append("?sub <" + SemanticWebUtility.getResourceName(defaultUri, nnew.toString()) + "> ?obj");
				filter.append("( ?pre = <" + SemanticWebUtility.getResourceName(defaultUri, pre.toString()) + "> )");
			} else if (sub.isMissing() && pre.isMissing() && !obj.isMissing() && !nnew.isMissing()) {
				insert.append("?sub ?pre \"" + nnew.toString() + "\"");
				filter.append("( ?obj = \"" + obj.toString() + "\" )");
			} else if (!sub.isMissing() && pre.isMissing() && !obj.isMissing() && !nnew.isMissing()) {
				throw new IllegalArgumentException("Subject/object combination is not possible to update.");
			} else if (sub.isMissing() && !pre.isMissing() && !obj.isMissing() && !nnew.isMissing()) {
				throw new IllegalArgumentException("Predicate/object combination is not possible to update.");
			}

			if (filter.length() <= 0) {
				continue;
			}

			update.setLength(0);

			update.append("DELETE " + service.getQueryStatement("?sub ?pre ?obj", graphName));
			update.append(" INSERT " + service.getQueryStatement(insert.toString(), graphName));
			update.append(" WHERE " + service.getQueryStatement("?sub ?pre ?obj . FILTER" + filter, graphName));

			service.updateQuery(update.toString(), getCredentialsProvider());

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

		if (m_settings.getSubject() == null && m_settings.getPredicate() == null && m_settings.getObject() == null) {
			throw new InvalidSettingsException(
					"Selecting at least one column for subject, predicate or object is mandatory.");
		}

		if (m_settings.getNewVal() == null) {
			throw new InvalidSettingsException("Selecting a column for update value is mandatory");
		}
		return new PortObjectSpec[] { inSpecs[0] };
	}

}
