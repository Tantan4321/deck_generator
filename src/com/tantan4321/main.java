package com.tantan4321;

import org.json.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class main {

    public static void main(String[] args) {
        String myDirectoryPath = "C:\\Data\\App-dev\\flutter_whoiswho\\assets\\img";

        JSONObject ret = new JSONObject();
        JSONArray store = new JSONArray();
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String this_name = child.getName().replace("_", " ").replace(".jpg", "");
                this_name = toTitleCase(this_name); //TODO: implement better way of generating names
                String path = "/assets/img/" + child.getName();
                JSONObject jo = new JSONObject();
                jo.put("name", this_name);
                jo.put("img", path);
                store.put(jo);
            }
        } else {
            // Handle the case where dir is not really a directory.
        }

        ret.put("famous_people", store); //add array of people objects to container json

        try (FileWriter file = new FileWriter("data.json")) {
            file.write(ret.toString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
