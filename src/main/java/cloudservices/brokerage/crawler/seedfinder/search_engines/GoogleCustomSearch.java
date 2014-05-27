/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudservices.brokerage.crawler.seedfinder.search_engines;

import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.SeedDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.Seed;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Arash Khodadadi http://www.arashkhodadadi.com/
 */
public class GoogleCustomSearch {

    /**
     * Be sure to specify the name of your application. If the application name
     * is {@code null} or blank, the application will log a warning. Suggested
     * format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "SeedFinder/1.0-SNAPSHOT";
    /**
     * API Key for the registered developer project for your application.
     */
    private static final String API_KEY = "AIzaSyBf4Qo94NfCZBprkkO6jrUs7OCq6QI3SAw";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;
    private static Customsearch client;
    private static final String SEARCH_ENGINE_ID = "017648412356002157062:9vfuqn9qva0";
    private final static Logger LOGGER = Logger.getLogger(GoogleCustomSearch.class.getName());
    private static final Pattern notAccepted = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?"
            + "|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    private SeedDAO seedDAO;
    private int seedsFound;
    private int totalSeached;

    public GoogleCustomSearch() {
        this.seedDAO = new SeedDAO();
        this.seedsFound = 0;
        this.totalSeached = 0;
    }

    public void search(String query) throws IOException, GeneralSecurityException, DAOException {
        // initialize the transport
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // set up global Customsearch instance
        client = new Customsearch.Builder(httpTransport, JSON_FACTORY, null)
                .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(API_KEY))
                .setApplicationName(APPLICATION_NAME).build();

        for (long i = 1; i < 100;) {
            Customsearch.Cse.List lst = client.cse().list(query);
            lst.setCx(SEARCH_ENGINE_ID);
            lst.setStart(i);
            LOGGER.log(Level.FINE, "Starting From: {0}", i);
            Search results = lst.execute();
            List<Result> resultsLst = results.getItems();
            int total = resultsLst.size();
            LOGGER.log(Level.FINE, "Number of Results: {0}", total);
            for (int j = 0; j <= total - 1; j++) {
                Seed seed = new Seed(resultsLst.get(j).getLink(), resultsLst.get(j).getTitle(),
                        resultsLst.get(j).getSnippet());
                LOGGER.log(Level.FINE, "{0}-{1}", new Object[]{i + j, seed.getTitle()});
                LOGGER.log(Level.FINE, "URL: {0}", seed.getUrl());
                LOGGER.log(Level.FINE, "Description: {0}", seed.getDescription());
                this.totalSeached++;

                if (!notAccepted.matcher(seed.getUrl()).matches()) {
                    seedDAO.addSeed(seed);
                    this.seedsFound++;
                    LOGGER.log(Level.FINER, "Successfully Saved to Database");
                } else {
                    LOGGER.log(Level.FINE, "This is not an accepted seed");
                }
            }
            i += total;
        }
    }

    public int getSeedsFound() {
        return seedsFound;
    }

    public void setSeedsFound(int seedsFound) {
        this.seedsFound = seedsFound;
    }

    public int getTotalSeached() {
        return totalSeached;
    }

    public void setTotalSeached(int totalSeached) {
        this.totalSeached = totalSeached;
    }
}
