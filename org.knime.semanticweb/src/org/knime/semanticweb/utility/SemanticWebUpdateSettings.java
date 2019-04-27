package org.knime.semanticweb.utility;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * @author Lara Gorini
 *
 */
public class SemanticWebUpdateSettings extends SemanticWebSettings {

	private DialogComponentColumnNameSelection m_newValComp;

	private final SettingsModelString m_newVal = createNewValModel();

	private SettingsModelString createNewValModel() {
		return new SettingsModelString("new_value", null);
	}

	@SuppressWarnings("unchecked")
	private DialogComponentColumnNameSelection getNewValComp() {
		m_newValComp = new DialogComponentColumnNameSelection(createNewValModel(), "Update value ", 1, true, true,
				new Class[] { StringValue.class });
		return m_newValComp;
	}

	@Override
	public JPanel getDialogPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;

		final JPanel subPanel = getSubjectComp().getComponentPanel();
		subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(subPanel, gbc);
		gbc.gridy++;

		final JPanel predPanel = getPredicateComp().getComponentPanel();
		predPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(predPanel, gbc);
		gbc.gridy++;

		final JPanel objPanel = getObjectComp().getComponentPanel();
		objPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(objPanel, gbc);
		gbc.gridy++;

		final JPanel newValPanel = getNewValComp().getComponentPanel();
		newValPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(newValPanel, gbc);
		gbc.gridy++;

		final JPanel graphNamePanel = getGraphNameComp().getComponentPanel();
		graphNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(graphNamePanel, gbc);
		gbc.gridy++;

		final JPanel uriPanel = getURIComp().getComponentPanel();
		uriPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(uriPanel, gbc);
		gbc.gridy++;

		final JPanel globalGraphNamePanel = getGlobalGraphNameComp().getComponentPanel();
		globalGraphNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(globalGraphNamePanel, gbc);

		return panel;

	}

	@Override
	public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_newVal.validateSettings(settings);
	}

	@Override
	public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		super.loadSettingsFrom(settings, specs);
		m_newValComp.loadSettingsFrom(settings, specs);
	}

	@Override
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_newVal.loadSettingsFrom(settings);
	}

	@Override
	public void saveModelSettingsTo(final NodeSettingsWO settings) {
		super.saveModelSettingsTo(settings);
		m_newVal.saveSettingsTo(settings);
	}

	@Override
	public void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		super.saveSettingsTo(settings);
		m_newValComp.saveSettingsTo(settings);
	}

	/**
	 * @return the name of column holding update values.
	 */
	public String getNewVal() {
		return m_newVal.getStringValue();
	}
}

