package org.knime.semanticweb.nodes.writefile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
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
import org.knime.semanticweb.port.SemanticWebPortObject;
import org.knime.semanticweb.services.RDFService;

/**
 * @author Lara Gorini
 *
 */
public class WriteToFileNodeModel extends NodeModel {

	private final SettingsModelString m_file = createFileModel();
	private final SettingsModelString m_graphName = createGraphNameModel();

	/**
	 * The constructor of this class.
	 */
	public WriteToFileNodeModel() {
		super(new PortType[] { SemanticWebPortObject.TYPE }, new PortType[0]);
	}

	static SettingsModelString createFileModel() {
		return new SettingsModelString("file", null);
	}

	static SettingsModelString createGraphNameModel() {
		return new SettingsModelString("graph_name", null);
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
		m_graphName.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_file.validateSettings(settings);
		m_graphName.validateSettings(settings);
		final String file = ((SettingsModelString) m_file.createCloneWithValidatedValue(settings)).getStringValue();
		if (file == null) {
			throw new InvalidSettingsException("Choosing a file is mandatory.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_file.loadSettingsFrom(settings);
		m_graphName.loadSettingsFrom(settings);
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
		final RDFService service = ((SemanticWebPortObject) inObjects[0]).getService();
		final Model model = service.writeToFile(m_graphName.getStringValue(), getCredentialsProvider());
		RDFDataMgr.write(new BufferedOutputStream(new FileOutputStream(m_file.getStringValue()), 1024), model,
				RDFFormat.TURTLE);
		return new PortObject[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		if (m_graphName.getStringValue() == null || m_file.getStringValue() == null) {
			throw new InvalidSettingsException("Choosing name of file and graph is mandatory");
		}

		return new PortObjectSpec[0];
	}

}
