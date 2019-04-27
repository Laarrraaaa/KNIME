package org.knime.semanticweb.services;

import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.knime.core.node.ExecutionMonitor;

/**
 * @author Lara Gorini
 *
 * @param <R>
 *            The {@link RDFService}.
 */
public interface RDFServiceFactory<R extends RDFService> {

	/**
	 * @return the ID of {@link RDFService}.
	 */
	public String getID();

	/**
	 * @param exec
	 *            The {@link ExecutionMonitor}.
	 * @param in
	 *            The {@link ZipInputStream}.
	 * @return a {@link RDFService}.
	 * @throws IOException
	 */
	public R create(ExecutionMonitor exec, ZipInputStream in) throws IOException;

	/**
	 * @param exec
	 *            The {@link ExecutionMonitor}.
	 * @param out
	 *            The {@link ZipOutputStream}.
	 * @param service
	 *            The {@link RDFService}.
	 * @throws IOException
	 */
	public void save(ExecutionMonitor exec, ZipOutputStream out, R service) throws IOException;

}
