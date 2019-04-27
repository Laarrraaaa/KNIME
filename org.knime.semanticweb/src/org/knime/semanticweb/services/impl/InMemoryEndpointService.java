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

package org.knime.semanticweb.services.impl;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.semanticweb.nodes.connector.memory.InMemoryEndpointNodeFactory;
import org.knime.semanticweb.services.RDFService;

/**
 *
 * This class provides {@link RDFService} for
 * {@link InMemoryEndpointNodeFactory}. This Service permits a default graph
 * (which always exists).
 *
 * @author Lara Gorini
 *
 */
public final class InMemoryEndpointService implements RDFService {

	private final static String MODEL_NAME = "MODEL";
	final static String DEFAULT = "DEFAULT";
	private int idx;
	private final Dataset m_dataset;
	private final LinkedHashMap<String, String> m_graphNames;

	/**
	 * @param dataset
	 *            The Semantic Web {@link Dataset}
	 * @param graphNames
	 *            A List containing name of graphs in dataset
	 */
	public InMemoryEndpointService(final Dataset dataset, final LinkedHashMap<String, String> graphNames) {
		m_dataset = dataset;
		m_graphNames = graphNames;
		// Default graph always exists
		m_graphNames.put(DEFAULT, DEFAULT);
		idx = 0;
	}

	@Override
	public QueryExecution createQueryExecution(final Query query, final CredentialsProvider cp) {
		return QueryExecutionFactory.create(query, m_dataset);
	}

	@Override
	public String getFactoryID() {
		return InMemoryEndpointServiceFactory.ID;
	}

	@Override
	public String getSummary() {
		return this.toString();
	}

	@Override
	public void updateQuery(final String update, final CredentialsProvider cp) {
		final UpdateRequest updateRequest = UpdateFactory.create(update);
		UpdateAction.execute(updateRequest.getOperations().get(0), m_dataset);
	}

	@Override
	public void createGraph(final String graphName, final CredentialsProvider cp) {
		if (graphName != null) {
			updateQuery("CREATE GRAPH <" + graphName + "> ", null);
			set(graphName);
		}

	}

	@Override
	public boolean containsGraphName(final String graphName, final CredentialsProvider cp) {
		if (graphName != null) {
			return m_graphNames.containsValue(graphName);
		} else {
			return true;
		}
	}

	@Override
	public String getDefaultGraphName() {
		return null;
	}

	@Override
	public String getQueryStatement(final String statement, final String graphName) {
		if (graphName == null || graphName.trim().isEmpty()) {
			return " {" + statement + "}";
		} else {
			return " { GRAPH <" + graphName + "> {" + statement + "} }";
		}
	}

	@Override
	public void dropGraph(final String graphName, final CredentialsProvider cp) {
		if (graphName != null) {
			updateQuery("DROP SILENT GRAPH <" + graphName + ">", null);
			remove(graphName);
		}

	}

	@Override
	public Model writeToFile(final String graphName, final CredentialsProvider cp) throws FileNotFoundException {
		final Model model;
		if (graphName != null && !graphName.trim().isEmpty()) {
			model = m_dataset.getNamedModel(graphName);
			if (model == null) {
				throw new IllegalArgumentException("Graph doesn't exist.");
			}
		} else {
			model = m_dataset.getDefaultModel();
		}
		return model;
	}

	Dataset getDataset() {
		return m_dataset;
	}

	Set<Entry<String, String>> getNames() {
		return m_graphNames.entrySet();
	}

	private void set(final String graphName) {
		if (!m_graphNames.containsValue(graphName)) {
			m_graphNames.put(MODEL_NAME + idx, graphName);
			idx++;
		}
	}

	private void remove(final String graphName) {
		m_graphNames.remove(graphName);
	}

}
