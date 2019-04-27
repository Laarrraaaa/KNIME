package org.knime.semanticweb.services;

import java.io.FileNotFoundException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.CredentialsProvider;

/**
 * This interface provides several methods for {@link RDFService}s.
 *
 * @author Lara Gorini
 *
 */
public interface RDFService {

	/**
	 * @return the ID of {@link RDFService}.
	 */
	public String getFactoryID();

	/**
	 * @return the summary of {@link RDFService}.
	 */
	public String getSummary();

	/**
	 * Creates a {@link QueryExecution}
	 *
	 * @param query
	 *            The query.
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @return the {@link QueryExecution}.
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public QueryExecution createQueryExecution(Query query, CredentialsProvider cp) throws InvalidSettingsException;

	/**
	 * Executes an update query.
	 *
	 * @param update
	 *            The query.
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public void updateQuery(String update, CredentialsProvider cp) throws InvalidSettingsException;

	/**
	 * Creates a named graph. If graphName is null, it will be interpreted as
	 * default graph which won't be created since it always exists (if service
	 * permits default graph).
	 *
	 * @param name
	 *            The name of the graph
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public void createGraph(String name, CredentialsProvider cp) throws InvalidSettingsException;

	/**
	 * Asks if graph is already contained in endpoint.
	 *
	 * @param graphName
	 *            The name of the graph
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @return TRUE if graphName is already contained; FALSE otherwise. If
	 *         graphName is null, it will be interpreted as default graph which
	 *         always exists (if service permits default graph).
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public boolean containsGraphName(String graphName, CredentialsProvider cp) throws InvalidSettingsException;

	/**
	 * @return null if service provides default graph.
	 * @throws InvalidSettingsException
	 *             if service doesn't permit a default graph.
	 */
	public String getDefaultGraphName() throws InvalidSettingsException;

	/**
	 *
	 * @param statement
	 *            A statement
	 * @param graphName
	 *            The name of the graph. If graphName is null, it won't be
	 *            included in query.
	 * @return a query including statement and graphName
	 */
	public String getQueryStatement(final String statement, final String graphName);

	/**
	 * @param graphName
	 *            The name of graph which will be dropped. If graphName is null,
	 *            it will be interpreted as a default graph (if service permits
	 *            one) which won't be dropped.
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public void dropGraph(String graphName, CredentialsProvider cp) throws InvalidSettingsException;

	/**
	 *
	 * Writes all triples of a graph in a file.
	 *
	 * @param graphName
	 *            The name of graph. When graphName is null, it depends on given
	 *            endpoint, whether triples of default graph or all existing
	 *            triples are returned.
	 * @param cp
	 *            The {@link CredentialsProvider}.
	 * @return a {@link Model} containing all triples in given graph.
	 * @throws FileNotFoundException
	 *             if file doesn't exist.
	 * @throws InvalidSettingsException
	 *             if authentication does not work properly.
	 */
	public Model writeToFile(String graphName, final CredentialsProvider cp)
			throws FileNotFoundException, InvalidSettingsException;

}
