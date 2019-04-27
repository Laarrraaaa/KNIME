package org.knime.semanticweb.utility;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * @author Lara Gorini
 *
 */
public class SemanticWebSettings {

	private final SettingsModelString m_subject = createSubjectModel();
	private final SettingsModelString m_predicate = createPredicateModel();
	private final SettingsModelString m_object = createObjectModel();
	private final SettingsModelString m_graphName = createGraphNameModel();
	private final SettingsModelString m_globalUri = createGlobalUriModel();
	private final SettingsModelString m_globalGraphName = createGlobalGraphGraphModel();
	private DialogComponentColumnNameSelection m_subjectComp;
	private DialogComponentColumnNameSelection m_predicateComp;
	private DialogComponentColumnNameSelection m_objectComp;
	private DialogComponentColumnNameSelection m_graphNameComp;
	private DialogComponentString m_globalUriComp;
	private DialogComponentString m_globalGraphComp;

	private SettingsModelString createSubjectModel() {
		return new SettingsModelString("subject", null);
	}

	private SettingsModelString createGlobalGraphGraphModel() {
		return new SettingsModelString("global_graph_name", "http://localhost/graph");
	}

	private SettingsModelString createPredicateModel() {
		return new SettingsModelString("predicate", null);
	}

	private SettingsModelString createObjectModel() {
		return new SettingsModelString("object", null);
	}

	private SettingsModelString createGraphNameModel() {
		return new SettingsModelString("graph_name", null);
	}

	private SettingsModelString createGlobalUriModel() {
		return new SettingsModelString("global_uri", "http://localhost/");
	}

	@SuppressWarnings("unchecked")
	DialogComponentColumnNameSelection getSubjectComp() {
		m_subjectComp = new DialogComponentColumnNameSelection(createSubjectModel(), "Subject          ", 1, true, true,
				new Class[] { StringValue.class });
		return m_subjectComp;
	}

	@SuppressWarnings("unchecked")
	DialogComponentColumnNameSelection getPredicateComp() {
		m_predicateComp = new DialogComponentColumnNameSelection(createPredicateModel(), "Predicate       ", 1, true,
				true, new Class[] { StringValue.class });
		return m_predicateComp;
	}

	@SuppressWarnings("unchecked")
	DialogComponentColumnNameSelection getObjectComp() {
		m_objectComp = new DialogComponentColumnNameSelection(createObjectModel(), "Object           ", 1, true, true,
				new Class[] { StringValue.class });
		return m_objectComp;
	}

	@SuppressWarnings("unchecked")
	DialogComponentColumnNameSelection getGraphNameComp() {
		m_graphNameComp = new DialogComponentColumnNameSelection(createGraphNameModel(), "Graph name   ", 1, true, true,
				new Class[] { StringValue.class });
		return m_graphNameComp;
	}

	DialogComponentString getURIComp() {
		m_globalUriComp = new DialogComponentString(createGlobalUriModel(), "Global URI             ", true, 30);
		return m_globalUriComp;
	}

	DialogComponent getGlobalGraphNameComp() {
		m_globalGraphComp = new DialogComponentString(createGlobalGraphGraphModel(), "Global graph name", false, 30);
		return m_globalGraphComp;
	}

	/**
	 * @return the panel for {@link NodeDialogPane}.
	 */
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

	/**
	 *
	 * This method have to be called in {@link NodeDialogPane}.
	 *
	 * @param settings
	 *            The node settings.
	 * @throws InvalidSettingsException
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		m_subjectComp.saveSettingsTo(settings);
		m_predicateComp.saveSettingsTo(settings);
		m_objectComp.saveSettingsTo(settings);
		m_graphNameComp.saveSettingsTo(settings);
		m_globalUriComp.saveSettingsTo(settings);
		m_globalGraphComp.saveSettingsTo(settings);
	}

	/**
	 *
	 * This method have to be called in {@link NodeModel}.
	 *
	 * @param settings
	 *            The node settings.
	 */
	public void saveModelSettingsTo(final NodeSettingsWO settings) {
		m_subject.saveSettingsTo(settings);
		m_predicate.saveSettingsTo(settings);
		m_object.saveSettingsTo(settings);
		m_graphName.saveSettingsTo(settings);
		m_globalUri.saveSettingsTo(settings);
		m_globalGraphName.saveSettingsTo(settings);

	}

	/**
	 *
	 * This method have to be called in {@link NodeDialogPane}.
	 *
	 * @param settings
	 *            The node settings.
	 * @param specs
	 *            The specs of inputs.
	 * @throws NotConfigurableException
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		m_subjectComp.loadSettingsFrom(settings, specs);
		m_predicateComp.loadSettingsFrom(settings, specs);
		m_objectComp.loadSettingsFrom(settings, specs);
		m_graphNameComp.loadSettingsFrom(settings, specs);
		m_globalUriComp.loadSettingsFrom(settings, specs);
		m_globalGraphComp.loadSettingsFrom(settings, specs);
	}

	/**
	 * This method have to be called in {@link NodeModel}.
	 *
	 * @param settings
	 *            The node settings.
	 * @throws InvalidSettingsException
	 */
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_subject.loadSettingsFrom(settings);
		m_predicate.loadSettingsFrom(settings);
		m_object.loadSettingsFrom(settings);
		m_graphName.loadSettingsFrom(settings);
		m_globalUri.loadSettingsFrom(settings);
		m_globalGraphName.loadSettingsFrom(settings);
	}

	/**
	 *
	 * This method have to be called in {@link NodeModel}.
	 *
	 * @param settings
	 *            The node settings.
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_subject.validateSettings(settings);
		m_predicate.validateSettings(settings);
		m_object.validateSettings(settings);
		m_graphName.validateSettings(settings);
		m_globalUri.validateSettings(settings);
		m_globalGraphName.validateSettings(settings);
	}

	/**
	 * @return the name of column holding subjects.
	 */
	public String getSubject() {
		return m_subject.getStringValue();
	}

	/**
	 * @return the name of column holding predicates.
	 */
	public String getPredicate() {
		return m_predicate.getStringValue();
	}

	/**
	 * @return the name of column holding objects.
	 */
	public String getObject() {
		return m_object.getStringValue();
	}

	/**
	 * @return the name of column holding names of graphs.
	 */
	public String getGraphName() {
		return m_graphName.getStringValue();
	}

	/**
	 * @return the name of column holding global URI.
	 */
	public String getURI() {
		return m_globalUri.getStringValue();
	}

	/**
	 * @return the name of column holding global name of graph.
	 */
	public String getGlobalGraphName() {
		return m_globalGraphName.getStringValue();
	}
}
