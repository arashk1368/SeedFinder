package cloudservices.brokerage.crawler.seedfinder;

import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.BaseDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.SeedDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.Seed;
import cloudservices.brokerage.crawler.seedfinder.search_engines.GoogleCustomSearch;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.cfg.Configuration;

public class App {

    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        String query = "web service repository";


        try {
            String fileName = query + " log";
            LoggerSetup.setup(fileName + ".txt", fileName + ".html");
            Logger logger = Logger.getLogger("");
            logger.setLevel(Level.FINER);
        } catch (IOException e) {
            throw new RuntimeException("Problems with creating the log files");
        }

        GoogleCustomSearch gcs = new GoogleCustomSearch();
        long startTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Searching Start");

        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            BaseDAO.openSession(configuration);

            LOGGER.log(Level.INFO, "Query: {0}", query);
            gcs.search(query);
        } catch (GeneralSecurityException | IOException | DAOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            BaseDAO.closeSession();
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            LOGGER.log(Level.INFO, "Searching End in {0}ms", totalTime);
            LOGGER.log(Level.INFO, "Total Searched: {0}", gcs.getTotalSeached());
            LOGGER.log(Level.INFO, "Seeds Found: {0}", gcs.getSeedsFound());
        }


    }
}
