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

import java.util.Iterator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.ICredentials;
import org.knime.semanticweb.services.RDFService;

/**
 * @author Lara Gorini
 */
public class SPARQLEndpointService implements RDFService {

	private final String m_endpoint;
	private final String m_credential;
	private final String m_username;
	private final char[] m_password;

	/**
	 * @param endpoint
	 *            URI of an endpoint.
	 * @param username
	 *            Name of user. Can be null.
	 * @param password
	 *            Password. Can be null.
	 * @param credential
	 */
	public SPARQLEndpointService(final String endpoint, final String username, final char[] password,
			final String credential) {
		m_endpoint = endpoint;
		m_username = username;
		m_password = password;
		m_credential = credential;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SPARQLEndpointService)) {
			return false;
		}
		final SPARQLEndpointService con = (SPARQLEndpointService) obj;
		final EqualsBuilder eb = new EqualsBuilder();
		eb.append(m_endpoint, con.m_endpoint);
		eb.append(m_username, con.m_username);
		eb.append(m_password, con.m_password);
		eb.append(m_credential, con.m_credential);
		return eb.isEquals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(m_endpoint);
		hcb.append(m_username);
		hcb.append(m_password);
		hcb.append(m_credential);
		return hcb.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Endpoint:\n" + m_endpoint + "\n\n");
		sb.append("Username:\n" + m_username + "\n\n");
		sb.append("Credential:\n" + m_credential + "\n\n");
		return sb.toString();
	}

	@Override
	public QueryExecution createQueryExecution(final Query query, final CredentialsProvider cp)
			throws InvalidSettingsException {
		final SimpleAuthenticator simpleAuthenticator = getSimpleAuthenticator(cp);
		if (simpleAuthenticator == null) {
			return QueryExecutionFactory.sparqlService(m_endpoint, query);
		} else {
			return QueryExecutionFactory.sparqlService(m_endpoint, query, simpleAuthenticator);
		}
	}

	@Override
	public String getFactoryID() {
		return SPARQLEndpointServiceFactory.ID;
	}

	@Override
	public String getSummary() {
		return this.toString();
	}

	@Override
	public void updateQuery(final String update, final CredentialsProvider cp) throws InvalidSettingsException {
		final UpdateRequest updateRequest = UpdateFactory.create(update);
		final UpdateProcessRemoteBase ue;

		final SimpleAuthenticator simpleAuthenticator = getSimpleAuthenticator(cp);
		if (simpleAuthenticator == null) {
			ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updateRequest, m_endpoint,
					new Context());
		} else {
			ue = (UpdateProcessRemoteBase) UpdateExecutionFactory.createRemote(updateRequest, m_endpoint, new Context(),
					simpleAuthenticator);
		}

		ue.execute();
	}

	@Override
	public void createGraph(final String graphName, final CredentialsProvider cp) throws InvalidSettingsException {
		if (graphName != null) {
			updateQuery("CREATE GRAPH <" + graphName + "> ", cp);
		}
	}

	@Override
	public boolean containsGraphName(final String graphName, final CredentialsProvider cp)
			throws InvalidSettingsException {
		if (graphName != null) {
			return createQueryExecution(QueryFactory.create("ASK WHERE {  GRAPH <" + graphName + "> { ?s ?p ?o . } }"),
					cp).execAsk();
		}
		return true; // default graph always exists
	}

	@Override
	public String getDefaultGraphName() throws InvalidSettingsException {
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
	public void dropGraph(final String graphName, final CredentialsProvider cp) throws InvalidSettingsException {
		if (graphName != null) {
			updateQuery("DROP SILENT GRAPH <" + graphName + ">", cp);
		}
	}

	@Override
	public Model writeToFile(final String graphName, final CredentialsProvider cp) throws InvalidSettingsException {
		final String query;
		if (graphName != null && !graphName.trim().isEmpty()) {
			if (!containsGraphName(graphName, cp)) {
				throw new IllegalArgumentException("Graph doesn't exist.");
			}
			query = "SELECT * FROM NAMED <" + graphName + "> WHERE { GRAPH ?graph { ?sub ?pre ?obj } }";
		} else {
			query = "SELECT * WHERE {  ?sub ?pre ?obj }";
		}
		final QueryExecution queryExecution = createQueryExecution(QueryFactory.create(query), cp);
		final ResultSet resultSet = queryExecution.execSelect();
		final Model model = ModelFactory.createDefaultModel();
		while (resultSet.hasNext()) {
			final QuerySolution querySolution = resultSet.next();
			final Iterator<String> varNames = querySolution.varNames();
			String sub = null;
			String pre = null;
			String obj = null;
			while (varNames.hasNext()) {
				final String varName = varNames.next();
				final RDFNode rdfNode = querySolution.get(varName);
				if ("sub".equals(varName)) {
					sub = rdfNode.toString();
				} else if ("pre".equals(varName)) {
					pre = rdfNode.toString();
				} else if ("obj".equals(varName)) {
					obj = rdfNode.toString();
				}
			}
			model.createResource(sub).addProperty(new PropertyImpl(pre), obj);
		}
		return model;

	}

	String getEndoint() {
		return m_endpoint;
	}

	String getUsername() {
		return m_username;
	}

	char[] getPassword() {
		return m_password;
	}

	String getCredential() {
		return m_credential;
	}

	SimpleAuthenticator getSimpleAuthenticator(final CredentialsProvider cp) throws InvalidSettingsException {
		String user = null;
		char[] pwd = null;
		if (m_credential != null) {
			try {
				final ICredentials iCredentials = cp.get(m_credential);
				if (iCredentials != null) {
					user = iCredentials.getLogin();
					if (iCredentials.getPassword() != null) {
						pwd = iCredentials.getPassword().toCharArray();
					} else {
						throw new InvalidSettingsException("Password of credential \"" + user + "\" is not set.");
					}
				}
			} catch (final IllegalArgumentException e) {
				throw new InvalidSettingsException(e.getMessage());
			}
		} else if (m_username != null || m_password != null) {
			user = m_username;
			pwd = m_password;
		}

		if (user != null || pwd != null) {
			return new SimpleAuthenticator(user, pwd);
		} else {
			return null;
		}
	}

}
