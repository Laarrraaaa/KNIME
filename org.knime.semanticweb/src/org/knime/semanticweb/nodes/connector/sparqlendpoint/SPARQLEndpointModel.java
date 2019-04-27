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
package org.knime.semanticweb.nodes.connector.sparqlendpoint;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication.Type;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.semanticweb.port.SemanticWebPortObject;
import org.knime.semanticweb.port.SemanticWebPortObjectSpec;
import org.knime.semanticweb.services.impl.SPARQLEndpointService;

/**
 * @author Lara Gorini
 *
 */
public class SPARQLEndpointModel extends NodeModel {

	protected final SettingsModelString m_endpoint = createEndpointModel();
	protected final SettingsModelAuthentication m_auth = createAuthenticationModel();

	/**
	 * The constructor of this class.
	 */
	public SPARQLEndpointModel() {
		super(new PortType[0], new PortType[] { SemanticWebPortObject.TYPE });
	}

	protected static SettingsModelAuthentication createAuthenticationModel() {
		return new SettingsModelAuthentication("auth", null, null, null);
	}

	protected static SettingsModelString createEndpointModel() {
		return new SettingsModelString("endpoint", null);
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
		m_endpoint.saveSettingsTo(settings);
		m_auth.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_endpoint.validateSettings(settings);
		final String endpoint = ((SettingsModelString) m_endpoint.createCloneWithValidatedValue(settings))
				.getStringValue();
		if (endpoint == null) {
			throw new InvalidSettingsException("Setting an endpoint is mandatory.");
		}
		m_auth.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_endpoint.loadSettingsFrom(settings);
		m_auth.loadSettingsFrom(settings);
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
		final SPARQLEndpointService service;
		String username = null;
		char[] password = null;
		String credential = null;
		if (m_auth.getSelectedType() == Type.CREDENTIALS) {
			credential = m_auth.getCredential();
		} else {
			username = m_auth.getUsername() == null || m_auth.getUsername().trim().isEmpty() ? null
					: m_auth.getUsername();
			password = m_auth.getPassword() == null || m_auth.getPassword().trim().isEmpty() ? null
					: m_auth.getPassword().toCharArray();
		}
		service = createService(m_endpoint.getStringValue(), username, password, credential);
		return new PortObject[] { new SemanticWebPortObject(createSpec(), service) };
	}

	protected SPARQLEndpointService createService(final String endPoint, final String username, final char[] password,
			final String credential) {
		return new SPARQLEndpointService(endPoint, username, password, credential);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (m_endpoint.getStringValue() == null) {
			throw new InvalidSettingsException("Setting an endpoint is mandatory.");
		}
		return new PortObjectSpec[] { createSpec() };
	}

	protected SemanticWebPortObjectSpec createSpec() throws InvalidSettingsException {
		return new SemanticWebPortObjectSpec("sparql");
	}

}
