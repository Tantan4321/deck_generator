package com.tantan4321.deck_generator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.*;

import java.io.FileWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.*;

import java.io.IOException;

public class Main {
    private static int totalPeople = 0;

    public static void main(String[] args) throws Exception {

        String siteUrl = "https://en.wikipedia.org";
        String deckName = "actors_actressess";

        // pages to call from
        String[] pages = {
//                "/wiki/Category:20th-century_American_male_actors?from=Ht",
                "/wiki/Category:20th-century_American_actresses",
                "/wiki/Category:20th-century_American_male_actors",
                "/wiki/Category:21st-century_American_actresses",
                "/wiki/Category:21st-century_American_male_actors"
        };

        int maxPages = 100;     // stop if this many sub-pages scraped off main page.
        int maxPeople = 12000;  // stop after this many people in the deck
        JSONObject theDeckJson = new JSONObject();

        int pageCount = pages.length;
        for (int i = 0; i < pageCount; i++) {
            String pageUrl = pages[i];
            totalPeople += scrapePeople(theDeckJson, deckName, siteUrl, pageUrl, maxPages, maxPeople);
            System.out.println("\n Bio's scraped with images: " + totalPeople);
            if (totalPeople >= maxPeople) {
                break;
            }
        }

        // dump the deck to file
        String jsonFile = "./data.whoiswho";
        String zipFile = "./" + deckName + ".zip";
        try (FileWriter file = new FileWriter(jsonFile)) {
            file.write(theDeckJson.toString());
            System.out.println("Successfully Copied JSON Object to File...");
        }
        // now make the zip
        makeZipFile(jsonFile, zipFile);

        if (totalPeople < 600)
            System.out.println(theDeckJson.toString(4));

        System.out.println("\n Bio's scraped with images: " + totalPeople);

        System.out.println(" ==== DONE ====");

    }

    private static int scrapePeople(JSONObject theDeckJson, String deckName, String siteUrl, String pageUrl, int maxPages, int maxPeople) throws IOException {
        int totalScraped = 0;
        while (pageUrl != null && !pageUrl.isBlank()) {
            System.out.println("Send Http GET request: [" + siteUrl + pageUrl + "]");
            String targetId1 = "#mw-pages";

            Document dom = Jsoup.connect(siteUrl + pageUrl).get();

            System.out.print("Web Page Title: ");
            System.out.println(dom.title());
            // try a grab the target list.
            Element divList = dom.select(targetId1).first();
            if (divList != null) {

                //System.out.println(divList.text());
                Elements theList = divList.select("li");
                System.out.println("theList length:" + theList.size());
                //iterate the list elements
                for (Element person : theList) {
                    System.out.print(person.text());
                    System.out.print(" ");
                    // print the raw HTML
                    //System.out.println( person.toString());
                    // get person url
                    String personUrl = person.select("a[href]").attr("href");
                    //System.out.print(personUrl);
                    JSONObject personJson = getPersonDetails(person.text(), siteUrl, personUrl);
                    if (personJson != null) {
                        // add person to the deck
                        theDeckJson.accumulate(deckName, personJson);
                        totalScraped++;
                        System.out.println("");
                        if ((totalPeople + totalScraped) >= maxPeople) {
                            break;
                        }
                    } else {
                        // image not found
                        System.out.println("   No Image found. Skipped.");
                    }
                }
                pageUrl = null;
                Elements links = divList.select("a[href]");
                if (--maxPages == 0 || (totalPeople + totalScraped) >= maxPeople) {
                    break;
                }
                // find the "next page" link
                System.out.println("\n\n Bio's scraped: " + (totalPeople + totalScraped) + " =========================   next page   ========================");
                for (Element link : links) {
                    String check = link.text();
                    if ("next page".equalsIgnoreCase(check)) {
                        // found the next page
                        pageUrl = link.attr("href");
                        break;
                    }
                }
                System.out.println(" Next page: " + pageUrl);
            } else {
                System.out.println("null people list");
                pageUrl = null;
            }
        }
        return totalScraped;
    }


    private static JSONObject getPersonDetails(String person, String siteUrl, String personUrl) throws IOException {
        // return a URL link to the person's image, or null if no image found.
        Document dom = Jsoup.connect(siteUrl + personUrl).get();
        // get the bio card table
        Element bioCard = dom.select("table.vcard").first();
        if (bioCard != null) {
            //System.out.println(bioCard.toString());
            // try for image with alt equal to name.
            Elements imgs = bioCard.select("img[src$=.jpg]");

            if (imgs.size() == 0) {
                // try for png image
                imgs = bioCard.select("img[src$=.png]");
            }
            if (imgs.size() == 0) {
                System.out.print(" " + bioCard.select("img").attr("src"));
                return null;
            }
            // check if height is reasonable
            Element image = imgs.first();
            String size = image.select("img").attr("height");
            if (size.isEmpty() || Integer.parseInt(size) < 120) {
                // no image size or too small
                return null;
            }

            String personImage = image.select("img").attr("src");
            JSONObject personJson = new JSONObject();

            // clean up the name with regex
            person = person.replaceAll("[^\\p{ASCII}]", "-");
            person = person.replaceAll("\"", "");
            person = person.replace("\\", "");
            person = person.replaceAll("\\[\\d+\\]", "");

            personJson.put("name", person);
            personJson.put("img", "https:" + personImage);
            //System.out.print(" Bio: ");
            // get all table rows
            Elements rows = bioCard.select("tr");
            String description = null;
            int rowNum = 0;
            for (Element row : rows) {
                rowNum++;
                if (rowNum > 1) {
                    // skips first row which is repeat of name.
                    String info = row.text();
                    // make only ascii (unicode hyphens are most likely in bio table, so replace non-ascii with -)
                    info = info.replaceAll("[^\\p{ASCII}]", "-");
                    // remove double quotes and backslashes
                    info = info.replaceAll("\"", "");
                    info = info.replace("\\", "");
                    // get rid of foot notes numbers
                    info = info.replaceAll("\\[\\d+\\]", "");
                    info.trim();
                    if (description == null && !info.isEmpty()) {
                        description = info;
                    } else if (!info.isEmpty()) {
                        description += (", " + info);
                    }
                }
            }
            // clean up string
            if (description != null) {
                // strip leading trailing commas and space
                description = description.replaceAll("^,+", "");
                description = description.trim();
                description = description.replaceAll(",+$", "");
                personJson.put("description", description);
            } else {
                personJson.put("description", "Not much is known about this person.");
            }
            return personJson;
        } else {
            //System.out.println("Got NULL for table: " + personUrl);
            return null;
        }
    }

    private static void makeZipFile(String sourceFile, String outputFile) throws IOException {

        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[4096];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
    }
}
