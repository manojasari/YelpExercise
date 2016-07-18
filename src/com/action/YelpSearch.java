package com.action;

import static org.junit.Assert.*;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class YelpSearch {

	private WebDriver driver;
    private static final String baseURL = "https://www.yelp.com";

    private static final String RATING = "//div[@class='rating-large']/i";
	private static final String SEARCH_RESULTS = "span[class='pagination-results-window']";
	private static final String REVIEW_CONTENT = "//div[@class='review-content']";
	private static final String WEBSITE = "//span[@class='biz-website']/a";
	private static final String TELEPHONE = "//span[@itemprop='telephone']";
	private static final String ADDRESS_POSTAL_CODE = "//span[@itemprop='postalCode']";
	private static final String ADDRESS_REGION = "//span[@itemprop='addressRegion']";
	private static final String ADDRESS_LOCALITY = "//span[@itemprop='addressLocality']";
	private static final String ADDRESS_STREET = "//span[@itemprop='streetAddress']";
	private static final String SEARCH_SUBMIT = "header-search-submit";
	private static final String SEARCH_BOX = "find_desc";
	private static final String FIRST_SEARCH_RESULT = "//div[@class='search-results-content']/ul[2]/li[1]/div/div[1]/div[1]/div/div[1]/div/a";
	private static final String FILTER_BUTTON = "span[class='filter-label all-filters-toggle show-tooltip']";
	
	private static final String PRICE_FILTER = "filter-set price-filters";
	private static final String DISTANCE_FILTER = "filter-set distance-filters";
    
    private static final Map<String, Integer> distanceFilter = new HashMap<>();
    private static final Map<String, Integer> priceFilter = new HashMap<>();

    private static final String SEARCH_OPTION = "restaurants";
    private static final String SEARCH_VALUE = "Pizza";
	

    @BeforeClass
    public static void beforeClass() {
    	File driverFile = new File("resources/chromedriver");
    	driverFile.setExecutable(true);
    	System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());
        
        distanceFilter.put("Bird's-eye View", 1);
        distanceFilter.put("Driving",2);
        distanceFilter.put("Biking", 3);
        distanceFilter.put("Walking", 4);
        distanceFilter.put("Within 4 blocks", 5);
        
        priceFilter.put("$", 1);
        priceFilter.put("$$", 2);
        priceFilter.put("$$$", 3);
        priceFilter.put("$$$$", 4);
    }
    
    @Before
    public void setup() {
    	driver = new ChromeDriver();
    	driver.get(baseURL);
    	driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }
    
    @After
    public void teardown() {
    	driver.quit();
    }
    
    @Test
    public void testSearch() {
    	search(SEARCH_OPTION, SEARCH_VALUE);
    	logSearchResultInformation("");
    }
    
    @Test
    public void testFilteredSearch() {
    	filteredSearch("Driving", "$$");  // Iteration 1
    	filteredSearch("Biking", "$");    // Iteration 2
    	filteredSearch("Walking", "$$$"); // Iteration 3
    }
    
    @Test
    public void testFirstSearchResult() {
		search(SEARCH_OPTION, SEARCH_VALUE);
    	
    	driver.findElement(By.cssSelector(FILTER_BUTTON)).click();
    	sleep(3000);
    	
    	applyFilter(DISTANCE_FILTER, "Driving");
    	applyFilter(PRICE_FILTER, "$");
    	
    	driver.findElement(By.xpath(FIRST_SEARCH_RESULT)).click();
    	
    	String address = "";
    	address = driver.findElement(By.xpath(ADDRESS_STREET)).getText();
    	address += " " + driver.findElement(By.xpath(ADDRESS_LOCALITY)).getText();
    	address += " " + driver.findElement(By.xpath(ADDRESS_REGION)).getText();
    	address += " " + driver.findElement(By.xpath(ADDRESS_POSTAL_CODE)).getText();
    	logToConsole("Address: " + address);
    	logToConsole("Phone: " + driver.findElement(By.xpath(TELEPHONE)).getText());
    	logToConsole("Website: " + driver.findElement(By.xpath(WEBSITE)).getText());
    	
    	List<WebElement> reviewList = driver.findElements(By.xpath(REVIEW_CONTENT));
    	for(int r=0; r<3; r++) {
    		logToConsole("Review-" + (r+1) + ": " + reviewList.get(r).findElement(By.tagName("p")).getText());
    	}
    }
    
    
    private void search(String option, String value) {
    	driver.findElement(By.id(SEARCH_BOX)).clear();
    	driver.findElement(By.id(SEARCH_BOX)).sendKeys("");
    	List<WebElement> webElements = driver.findElements(By.cssSelector("strong[class='suggestion-detail suggestion-name']"));
    	
    	String searchText = "";
    	for(WebElement element : webElements) {
    		if(element.getText().equalsIgnoreCase(option)) {
    			searchText = element.getText() + " " + value;
    			break;
    		}
    	}
    	
    	driver.findElement(By.id(SEARCH_BOX)).sendKeys(searchText);
    	driver.findElement(By.id(SEARCH_SUBMIT)).click();
    }
    
    private void filteredSearch(String distanceFilter, String priceFilter) {
    	search(SEARCH_OPTION, SEARCH_VALUE);
    	
    	try {
	    	if (driver.findElement(By.cssSelector(FILTER_BUTTON)) != null) {
	    		driver.findElement(By.cssSelector(FILTER_BUTTON)).click();
	    		sleep(3000);
	    	}
    	}
    	catch(Exception e) {
    		// filter window is already expanded
    	}
    	
    	applyFilter(DISTANCE_FILTER, distanceFilter);
    	applyFilter(PRICE_FILTER, priceFilter);
    	
    	logSearchResultInformation("Filter ");
    	
    	int i=1;
    	List<WebElement> starRatings = driver.findElements(By.xpath(RATING));
    	for(WebElement starRating : starRatings) {
    		logToConsole("Search Result " + i + ": " + starRating.getAttribute("title"));
    		i++;
    	}
    }
    
	private void logSearchResultInformation(String prefixText) {
		WebElement searchResultInfo = driver.findElement(By.cssSelector(SEARCH_RESULTS));
		
    	String searchResultCountStr = searchResultInfo.getText();
    	
    	int totalSearchResultCount = new Integer(searchResultCountStr.substring(searchResultCountStr.indexOf("of ")+3));
    	int searchResultCount = new Integer(searchResultCountStr.substring(searchResultCountStr.indexOf("-")+1, searchResultCountStr.indexOf(" of")));
    	
    	logToConsole(prefixText + "Search Result Count: "+searchResultCount);
    	logToConsole(prefixText + "Total Search Result Count: "+totalSearchResultCount);
	}
	
	private void applyFilter(String filter, String value) {
		int filterValue = 0;
		if(filter.equals(DISTANCE_FILTER)) {
			filterValue = distanceFilter.get(value);
		}
		else {
			filterValue = priceFilter.get(value);
		}
		
		driver.findElement(By.xpath("//div[@class='"+filter+"']/ul/li["+filterValue+"]/label/span")).click();
    	sleep(3000);
	}
	
	private void logToConsole(String text) {
		System.out.println("[" + new Date().toString() + "] " + text);
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
