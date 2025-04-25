import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    // Scrape the title of the webpage
    public static String getTitle(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.title();
    }

    // Scrape all headings (h1 to h6) from the webpage
    public static List<String> getHeadings(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
        List<String> headingTexts = new ArrayList<>();
        for (Element heading : headings) {
            headingTexts.add(heading.text());
        }
        return headingTexts;
    }

    // Scrape all links (<a> tags) from the webpage
    public static List<String> getLinks(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        List<String> linkUrls = new ArrayList<>();
        for (Element link : links) {
            linkUrls.add(link.attr("href"));
        }
        return linkUrls;
    }

    // **New Method: Scrape news headlines, publication dates, and author names**
    public static List<NewsArticle> getNewsArticles(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements promos = doc.select(".gs-c-promo");
        List<NewsArticle> articles = new ArrayList<>();
        for (Element promo : promos) {
            String headline = promo.select(".gs-c-promo-heading h3").text();
            String date = promo.select(".gs-c-promo-meta time").text();
            String summary = promo.select(".gs-c-promo-summary").text();
            String author = "Unknown";
            if (summary.startsWith("By ")) {
                int commaIndex = summary.indexOf(',');
                if (commaIndex != -1) {
                    author = summary.substring(3, commaIndex);
                } else {
                    author = summary.substring(3);
                }
            }
            if (!headline.isEmpty()) { // Only add if headline is present
                articles.add(new NewsArticle(headline, date, author));
            }
        }
        return articles;
    }

    // **New Inner Class: NewsArticle to store scraped data**
    public static class NewsArticle {
        public String headline;
        public String date;
        public String author;

        public NewsArticle(String headline, String date, String author) {
            this.headline = headline;
            this.date = date;
            this.author = author;
        }
    }

    // Main method to test scraping (updated to test news articles)
    public static void main(String[] args) {
        try {
            String url = "https://www.bbc.com";
            // Test title scraping
            String title = getTitle(url);
            System.out.println("Title: " + title);
            // Test news articles scraping
            List<NewsArticle> articles = getNewsArticles(url);
            System.out.println("\nNews Articles:");
            for (NewsArticle article : articles) {
                System.out.println("Headline: " + article.headline);
                System.out.println("Date: " + article.date);
                System.out.println("" +
                        "Author: " + article.author);
                System.out.println("---");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}