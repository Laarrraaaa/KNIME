package org.knime.semanticweb.nodes.query;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.port.PortObjectSpec;

/**
 * @author Lara Gorini
 */
public class SPARQLQueryNodeDialog extends NodeDialogPane {

	private final DialogComponentMultiLineString m_queryComp;
	private final DialogComponentNumber m_timeoutComp;

	protected SPARQLQueryNodeDialog() {

		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;

		m_timeoutComp = new DialogComponentNumber(SPARQLQueryNodeModel.createTimeoutModel(), "Timeout (in secs)", 1);
		panel.add(m_timeoutComp.getComponentPanel(), gbc);

		gbc.gridy++;

		m_queryComp = new DialogComponentMultiLineString(SPARQLQueryNodeModel.createQueryModel(),
				"SPARQL \"SELECT\" query", true, 100, 20);
		panel.add(m_queryComp.getComponentPanel(), gbc);
		addTab("Config", panel);
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		m_queryComp.saveSettingsTo(settings);
		m_timeoutComp.saveSettingsTo(settings);

	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		m_queryComp.loadSettingsFrom(settings, specs);
		m_timeoutComp.loadSettingsFrom(settings, specs);
	}

}
