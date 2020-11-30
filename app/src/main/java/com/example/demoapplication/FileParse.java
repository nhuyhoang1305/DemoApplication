package com.example.demoapplication;


import java.io.*;
import java.util.*;

public class FileParse {
    private File inputFile;
    private Scanner input;
    private String[] info;
    private String locationName;
    private String latitude;
    private String longitude;
    private String type;
    private String rating;
    private String detailRating;
    private FileParse fp;
    private File mFile;
    public ArrayList<String[]> infos;
    public FileParse (String file) throws FileNotFoundException {
        inputFile = new File(file);
        input = new Scanner(inputFile);
        infos = new ArrayList<String[]>();

        while(input.hasNext()) {
            info = new String[6];
            String line = input.nextLine();
            String[] aLine = line.split("[|]+");
            //System.out.println(aLine.length);
            for (int i = 0; i < 6; ++i) {
                info[i] = aLine[i].trim();
            }
            infos.add(info);
        }

        //findData();
    }

    public void findData(String line) {
        //boolean hasNotCreatedInfo = true;
        //while(input.hasNextLine() && hasNotCreatedInfo) {
        //String line = input.nextLine();
        Scanner lineScan = new Scanner(line);
        int index = -1;
        while (lineScan.hasNext()) {
            index++;
            String token = lineScan.next();
            boolean findingVerticalLine = true;
            while (lineScan.hasNext() && findingVerticalLine) {
                String token2 = lineScan.next();
                if (token2.startsWith("|")) {
                    findingVerticalLine = false;
                } else {
                    token += " " + token2;
                }
            }
            info[index] = token;
        }
        //hasNotCreatedInfo = false;
        //}
    }

    public String getLocationName() {
        locationName = info[0];
        return locationName;
    }


    public String getLat() {
        latitude = info[1];
        return latitude;
    }

    public String getLong() {
        longitude = info[2];
        return longitude;
    }

    public String getType() {
        type = info[3];
        return type;
    }

    public String getRating() {
        rating = info[4];
        return rating;
    }

    public String getDetailRating(){
        detailRating = info[5];
        return detailRating;
    }

    public void readFile() throws java.lang.Exception {
        inputFile = new File("/home/nhuyhoang/eclipse-workspace/OOP/src/location.txt");

        fp = new FileParse("/home/nhuyhoang/eclipse-workspace/OOP/src/location.txt");
        info[0] = fp.getLocationName();
        info[1] = fp.getLat();
        info[2] = fp.getLong();
        info[3] = fp.getType();
        info[4] = fp.getRating();
        info[5] = fp.getDetailRating();
    }

}