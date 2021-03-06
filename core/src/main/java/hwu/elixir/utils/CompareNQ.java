package hwu.elixir.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares 2 files each of which contains quads to determine if they are identical.
 * 
 * Quads have 4 parts: subject, predicate, object & context. These should all be
 * identical, bar the following exceptions:
 * <ol>
 * <li>When the predicate is 'http://purl.org/pav/retrievedOn' the object will be
 * a date. The value of the date is irrelevant and thus not checked.</li>
 * <li>Bnodes have a random value at the end of the IRI. These random parts are
 * not compared. The rest of the IRI is.</li>
 * </ol>
 * 
 * A list of all IRI parts identified as 'random' are displayed to console.
 * 
 * When quads are not identical the 2 quads are displayed and the first
 * non-matching element type (i.e., subject, predicate, object or context) is
 * displayed.
 * 
 * @author kcm
 *
 */
public class CompareNQ {

	private ArrayList<String> quads1;
	private ArrayList<String> quads2;

	private static Logger logger = LoggerFactory.getLogger(CompareNQ.class.getName());
	
	public boolean compare(File file1, File file2) throws FileNotFoundException, IOException {

		quads1 = getQuadsFromFile(file1);
		quads2 = getQuadsFromFile(file2);

		if (quads1.size() != quads2.size()) {
			logger.error("Quads are a different length! Quads 1 = " + quads1.size() + ". Quads 2 = " + quads2.size());
			return false;
		}

		for (int i = 0; i < quads1.size(); i++) {
			if (compareQuad(quads1.get(i), quads2.get(i)) == false)
				return false;
		}

		return true;
	}

	/**
	 * Confirms if 2 given quads are identical. Quads have 4 parts: subject,
	 * predicate, object & context. These should all be identical, bar the following
	 * exceptions:
	 * <ol>
	 * <li>The the predicate is 'http://purl.org/pav/retrievedOn' the object will be
	 * a date. The value of the date is irrelevant and thus not checked.</li>
	 * <li>Unprocessed Bnodes are of the form _:RANDOM_STUFF. The :_ is filtered out and the RANDOM_STUFF ingnored.</li>
	 * <li>Processed Bnodes have a random value at the end of an IRI (generated from the @id). These random parts are
	 * not compared. The rest of the IRI is.</li>
	 * </ol>
	 * 
	 * If debugging on, a list of all IRI parts identified as 'random' are displayed to console.
	 * 
	 * When quads are not identical the quads are displayed and the first
	 * non-matching element type (i.e., subject, predicate, object or context) is
	 * displayed.
	 * 
	 * @param quad1
	 * @param quad2
	 * @return true if match else false
	 */
	private boolean compareQuad(String quad1, String quad2) {
		StringTokenizer st1 = new StringTokenizer(quad1, "<> ");
		StringTokenizer st2 = new StringTokenizer(quad2, "<> ");

		if (st1.countTokens() != st2.countTokens()) {
			System.out.println("number of tokens doesn't match");
			System.out.println(quad1+"\n"+quad2+"\n\n");
			return false;
		}

		String s1 = st1.nextToken();
		String s2 = st2.nextToken();

		String p1 = st1.nextToken();
		String p2 = st2.nextToken();

		String o1 = st1.nextToken();
		String o2 = st2.nextToken();

		String c1 = st1.nextToken();
		String c2 = st2.nextToken();

		if (s1.equals(s2) && p1.equals(p2) && o1.equals(o2) && c1.equals(c2))
			return true;

		if (!s1.equals(s2)) {
			if (!checkForRandom(s1, s2)) {
				
				return false;

			}
		}

		if (!p1.equals(p2)) {
			logger.error("The following predicates do not match: " + quad1 + "\n" + quad2);
			return false;
		}

		// handle different date created/pulled
		if (p1.equalsIgnoreCase("http://purl.org/pav/retrievedOn") && c1.equals(c2))
			return true;

		if (!o1.equals(o2)) {
			if (!checkForRandom(o1, o2)) {
				logger.error("The following objects do not match: " + quad1 + "\n" + quad2);
				return false;
			}
		}

		if (!c1.equals(c2)) {
			logger.error("The following contexts do not match: " + quad1 + "\n" + quad2);
			return false;
		}

		return true;
	}

	/**
	 * Breaks 2 IRIs down to determine if each component part matches the equivalent
	 * part on the other IRI. The last part is assumed to be random, thus ignored.
	 * These are displayed to screen to allow the user to verify.
	 * 
	 * @param iri1
	 * @param iri2
	 * @return true if IRIs match up to the last part.
	 */
	private boolean checkForRandom(String iri1, String iri2) {
		StringTokenizer st1 = new StringTokenizer(iri1, "<>./: ");
		StringTokenizer st2 = new StringTokenizer(iri2, "<>./: ");

		int numTokens = st1.countTokens();
		if (st2.countTokens() != numTokens) {
			logger.error("Wrong number of tokens in URI!");
			return false;
		}

		for (int i = 0; i < numTokens; i++) {
			String t1 = st1.nextToken();
			String t2 = st2.nextToken();
			if (!t1.equals(t2)) {
				if (i != numTokens - 1) {					
					logger.error("These tokens do not appear to match: " + t1 + "  " + t2);
					return false;
				} else {
					logger.debug("These are random and so don't need to match? If not false, there is a problem! : " + t1 + "  " + t2);
				}
			}
		}

		return true;
	}

	/**
	 * Reads quads from a given file
	 * 
	 * @param file
	 * @return ArrayList where each element is a String, and each String is quad
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private ArrayList<String> getQuadsFromFile(File file) throws FileNotFoundException, IOException {
		ArrayList<String> temp = new ArrayList<String>();

		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = in.readLine()) != null) {
				temp.add(line.trim());
			}
		}

		return temp;
	}

}
