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
 *
 * History
 *   Mar 18, 2014 ("Patrick Winter"): created
 */
package org.knime.semanticweb.nodes.loadfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetImpl;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.semanticweb.nodes.query.SPARQLQueryNodeModel;

/**
 * @author Lara Gorini
 *
 */
public class AddFileNodeModel extends NodeModel {

	private final SettingsModelString m_file = createFileModel();

	/**
	 * The constructor of this class.
	 */
	public AddFileNodeModel() {
		super(new PortType[0], new PortType[] { BufferedDataTable.TYPE });
	}

	static SettingsModelString createFileModel() {
		return new SettingsModelString("file", null);
	}

	static SettingsModelString createGraphNameModel() {
		return new SettingsModelString("graph_name", "http://localhost/graph");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_file.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_file.validateSettings(settings);
		final String file = ((SettingsModelString) m_file.createCloneWithValidatedValue(settings)).getStringValue();
		if (file == null) {
			throw new InvalidSettingsException("Choosing a file is mandatory.");
		}

		final File cFile = new File(file);
		if (!cFile.exists()) {
			throw new InvalidSettingsException("File  \"" + file + "\" does not exist.");
		} else if (cFile.isDirectory()) {
			throw new InvalidSettingsException("File  \"" + file + "\" is a directory.");
		} else if (RDFLanguages.filenameToLang(file) == null) {
			throw new InvalidSettingsException("File name \"" + file + "\" is not valid.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_file.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// Not used
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		final Dataset dataset = readDataset();
		final long timeout = 1000;
		final String query = "SELECT * WHERE {?subject ?predicate ?object }";
		final DataTableSpec outSpec = SPARQLQueryNodeModel.createSpec(QueryExecutionFactory.create(query, dataset),
				timeout);
		final BufferedDataTableRowOutput rowOutput = new BufferedDataTableRowOutput(exec.createDataContainer(outSpec));
		final ResultSet resultSet = QueryExecutionFactory.create(query, dataset).execSelect();
		SPARQLQueryNodeModel.writeToTable(rowOutput, resultSet);
		return new PortObject[] { rowOutput.getDataTable() };
	}

	private Dataset readDataset() throws FileNotFoundException {
		final Model model = ModelFactory.createDefaultModel();
		final Dataset dataset = new DatasetImpl(model);
		RDFDataMgr.read(dataset, m_file.getStringValue().trim());
		return dataset;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (m_file.getStringValue() == null) {
			throw new InvalidSettingsException("Choosing name of file is mandatory");
		}
		final List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();
		colSpecs.add(new DataColumnSpecCreator("subject", StringCell.TYPE).createSpec());
		colSpecs.add(new DataColumnSpecCreator("predicate", StringCell.TYPE).createSpec());
		colSpecs.add(new DataColumnSpecCreator("object", StringCell.TYPE).createSpec());

		return new PortObjectSpec[] { new DataTableSpec(colSpecs.toArray(new DataColumnSpec[colSpecs.size()])) };

	}

}
