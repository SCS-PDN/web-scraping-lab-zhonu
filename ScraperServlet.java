import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;

public class ScrapeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get request parameters
        String url = request.getParameter("url");
        boolean scrapeTitle = "true".equals(request.getParameter("scrapeTitle"));
        boolean scrapeLinks = "true".equals(request.getParameter("scrapeLinks"));
        boolean scrapeImages = "true".equals(request.getParameter("scrapeImages"));
        String format = request.getParameter("format");

        // Validate URL
        if (url == null || url.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL is required");
            return;
        }

        // Collect scraped data
        List<String[]> data = new ArrayList<>();
        if (scrapeTitle) {
            try {
                String title = WebScraper.getTitle(url);
                data.add(new String[]{"Title", title});
            } catch (IOException e) {
                data.add(new String[]{"Error", "Failed to scrape title: " + e.getMessage()});
            }
        }
        if (scrapeLinks) {
            try {
                List<String> links = WebScraper.getLinks(url);
                for (String link : links) {
                    data.add(new String[]{"Link", link});
                }
            } catch (IOException e) {
                data.add(new String[]{"Error", "Failed to scrape links: " + e.getMessage()});
            }
        }
        if (scrapeImages) {
            try {
                List<String> images = WebScraper.getImages(url);
                for (String image : images) {
                    data.add(new String[]{"Image", image});
                }
            } catch (IOException e) {
                data.add(new String[]{"Error", "Failed to scrape images: " + e.getMessage()});
            }
        }

        // Session tracking
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        visitCount++;
        session.setAttribute("visitCount", visitCount);

        // Handle response based on format
        if ("csv".equals(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"scraped_data.csv\"");
            PrintWriter out = response.getWriter();
            out.println("Type,Value");
            for (String[] row : data) {
                String type = row[0].replace("\"", "\"\"");
                String value = row[1].replace("\"", "\"\"");
                out.println("\"" + type + "\",\"" + value + "\"");
            }
        } else if ("json".equals(format)) {
            response.setContentType("application/json");
            Gson gson = new Gson();
            String json = gson.toJson(data);
            response.getWriter().write(json);
        } else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html><html><head><title>Scrape Results</title></head><body>");

            // Display visit count
            out.println("<p>You have visited this page " + visitCount + " times.</p>");

            // Results table
            out.println("<table border='1'><tr><th>Type</th><th>Value</th></tr>");
            for (String[] row : data) {
                out.println("<tr><td>" + row[0] + "</td><td>" + row[1] + "</td></tr>");
            }
            out.println("</table>");

            // Download links
            String queryString = request.getQueryString() != null ? request.getQueryString().replaceAll("&format=(csv|json)", "") : "";
            out.println("<br><button onclick=\"window.location.href='scrape?" + queryString + "&format=csv'\">Download Results as CSV</button>");
            out.println("<br><a href='scrape?" + queryString + "&format=json'>View as JSON</a>");

            out.println("</body></html>");
        }
    }
}