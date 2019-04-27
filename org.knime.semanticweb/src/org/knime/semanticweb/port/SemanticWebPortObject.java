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
 *   Mar 19, 2014 ("Patrick Winter"): created
 */
package org.knime.semanticweb.port;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.util.ViewUtils;
import org.knime.semanticweb.services.RDFService;
import org.knime.semanticweb.services.RDFServiceFactory;
import org.knime.semanticweb.services.RDFServiceRegistry;

/**
 * @author Lara Gorini
 *
 */
public final class SemanticWebPortObject implements PortObject {

	private static final String CFG = "CONFIG";
	private static final String CFG_FACTORY_ID = "FACTORY_ID";
	private static final String CFG_SPEC = "SPEC";
	private final RDFService m_service;

	private final SemanticWebPortObjectSpec m_spec;

	/**
	 * @author Lara Gorini
	 * @noreference This class is not intended to be referenced by clients.
	 */
	public static final class Serializer extends PortObjectSerializer<SemanticWebPortObject> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void savePortObject(final SemanticWebPortObject portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			save(portObject, out, exec);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public SemanticWebPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			return load(in, exec);
		}

		private void save(final SemanticWebPortObject portObject, final ZipOutputStream out,
				final ExecutionMonitor exec) throws IOException {
			final String factoryId = portObject.m_service.getFactoryID();
			final ModelContent model = new ModelContent(CFG);
			model.addString(CFG_FACTORY_ID, factoryId);
			out.putNextEntry(new ZipEntry(CFG));
			final ModelContentWO specModel = model.addModelContent(CFG_SPEC);
			portObject.getSpec().save(specModel);
			model.saveToXML(new NonClosableOutputStream.Zip(out));
			@SuppressWarnings("unchecked")
			final RDFServiceFactory<RDFService> factory = (RDFServiceFactory<RDFService>) RDFServiceRegistry
					.getFactory(factoryId);
			factory.save(exec, out, portObject.m_service);
		}

		private SemanticWebPortObject load(final ZipInputStream in, final ExecutionMonitor exec)
				throws IOException {
			try {
				final ZipEntry ze = in.getNextEntry();
				if (!ze.getName().equals(CFG)) {
					throw new IOException("Key \"" + ze.getName() + "\" does not " + " match expected zip entry name \""
							+ CFG + "\".");
				}
				final ModelContentRO model = ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
				final String factoryID = model.getString(CFG_FACTORY_ID);
				final ModelContentRO specModel = model.getModelContent(CFG_SPEC);
				final SemanticWebPortObjectSpec spec = new SemanticWebPortObjectSpec(specModel);
				final RDFServiceFactory<?> factory = RDFServiceRegistry.getFactory(factoryID);
				final RDFService service = factory.create(exec, in);
				return new SemanticWebPortObject(spec, service);
			} catch (final InvalidSettingsException ise) {
				throw new IOException(ise);
			}
		}
	}


	/**
	 * The type of this port.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(SemanticWebPortObject.class);
	/**
	 * @param spec
	 *            The specification of this port object.
	 * @param service
	 */
	public SemanticWebPortObject(final SemanticWebPortObjectSpec spec, final RDFService service) {
		m_spec = spec;
		m_service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSummary() {
		return m_service.getSummary();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SemanticWebPortObjectSpec getSpec() {
		return m_spec;
	}

	/**
	 * @return the {@link RDFService}
	 */
	public RDFService getService() {
		return m_service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent[] getViews() {
		String text;
		if (m_service != null) {
			text = "<html>" + m_service.getSummary().replace("\n", "<br>") + "</html>";
		} else {
			text = "No connection available";
		}
		final JPanel f = ViewUtils.getInFlowLayout(new JLabel(text));
		f.setName("Connection");
		final JScrollPane scrollPane = new JScrollPane(f);
		scrollPane.setName("Connection");
		return new JComponent[] { scrollPane };
	}

}
