package org.knime.semanticweb.nodes.writefile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * @author Lara Gorini
 *
 */
public class WriteToFileNodeDialog extends NodeDialogPane {
	private final DialogComponentFileChooser m_fileComp;
	private final DialogComponentString m_graphNameComp;

	protected WriteToFileNodeDialog() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		m_fileComp = new DialogComponentFileChooser(WriteToFileNodeModel.createFileModel(), "1",
				JFileChooser.SAVE_DIALOG, false);
		panel.add(m_fileComp.getComponentPanel(), gbc);
		gbc.gridy++;
		m_graphNameComp = new DialogComponentString(WriteToFileNodeModel.createGraphNameModel(), "Graph name", false,
				30);
		panel.add(m_graphNameComp.getComponentPanel(), gbc);

		addTab("Config", panel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		m_fileComp.loadSettingsFrom(settings, specs);
		m_graphNameComp.loadSettingsFrom(settings, specs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		m_fileComp.saveSettingsTo(settings);
		m_graphNameComp.saveSettingsTo(settings);
	}

}
