package org.knime.semanticweb.nodes.insert;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.semanticweb.utility.SemanticWebSettings;


/**
 * @author Lara Gorini
 *
 */
public class InsertRowsNodeDialog extends NodeDialogPane {

	private final SemanticWebSettings m_settings = new SemanticWebSettings();

	protected InsertRowsNodeDialog() {

		addTab("Config", m_settings.getDialogPanel());
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		m_settings.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		m_settings.loadSettingsFrom(settings, specs);
	}
}
