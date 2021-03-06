package hwu.elixir.scrape.scraper;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import hwu.elixir.scrape.db.crawl.StatusOfScrape;
import hwu.elixir.scrape.exceptions.CannotWriteException;
import hwu.elixir.scrape.exceptions.FourZeroFourException;
import hwu.elixir.scrape.exceptions.JsonLDInspectionException;
import hwu.elixir.scrape.exceptions.MissingMarkupException;

public class ServiceScraperTest {

	private ServiceScraper scraper;
	private static String outputLoction  = System.getProperty("user.home")+File.separator+"toDelete";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File outputFolder = new File(outputLoction);
		boolean result = outputFolder.mkdir();
		if(!result) {
			throw new Exception("Cannot create output folder for temporary files used during ServiceScraperTest!");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		File outputFolder = new File(outputLoction);
		
		String[] listOfFiles = outputFolder.list();
		for(String fileName : listOfFiles) {
			File currentFile = new File(outputFolder.getPath(), fileName);
			currentFile.delete();
		}		
		
		boolean result = outputFolder.delete();
		if(!result) {
			throw new Exception("Cannot delete output folder for temporary files used during ServiceScraperTest!");
		}	
	}

	@Before
	public void setUp() throws Exception {
		scraper = new ServiceScraper();
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void test_scrape_doSomething() throws FourZeroFourException, JsonLDInspectionException, CannotWriteException, MissingMarkupException {
		assertEquals(true, scraper.scrape("http://www.hw.ac.uk", 100L, outputLoction, StatusOfScrape.FAILED));
		assertEquals(true, scraper.scrape("http://www.hw.ac.uk", 100L, outputLoction, StatusOfScrape.UNTRIED));
	}
	
	@Test
	public void test_scrape_NothingToDo()
			throws FourZeroFourException, JsonLDInspectionException, CannotWriteException, MissingMarkupException {
		assertEquals(false, scraper.scrape("http://www.hw.ac.uk", 100L, outputLoction, StatusOfScrape.SUCCESS));

		assertEquals(false, scraper.scrape("http://www.hw.ac.uk/", 100L, outputLoction, StatusOfScrape.GIVEN_UP));

		assertEquals(false, scraper.scrape("http://www.hw.ac.uk#", 100L, outputLoction, StatusOfScrape.DOES_NOT_EXIST));
		
		assertEquals(false, scraper.scrape("http://www.hw.ac.uk#", 100L, outputLoction, StatusOfScrape.HUMAN_INSPECTION));
	}

}
