
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.abs;

public class Matching {
    public static final int SIZE_OF_CONSECUTIVE_MATCHES_ARRAY = 1;
    String[] matchedSongIDHolder = new String[]{"free", "free"};
    ArrayList<LinkedHashMap> listOfDbMaps = new ArrayList<LinkedHashMap>();
    ArrayList<Long> hashesToBeMatchedNow = new ArrayList<Long>(SIZE_OF_CONSECUTIVE_MATCHES_ARRAY);
    ArrayList<Double> matchMapReturnValue;
    ArrayList<String> songIDList = new ArrayList<String>();
    ArrayList<Integer> maxCount = new ArrayList<Integer>();
    ArrayList<Double> offsetList = new ArrayList<Double>();
    ArrayList<Double> matchingTimes = new ArrayList<Double>();


    public void slidingWindowMatchNew(LinkedHashMap<Double, Long> songToBeMatched, LinkedHashMap<String, LinkedHashMap<Double, Long>> dbHashesMap) throws UnknownHostException {
        String matchedSongID = null;
        int maxNoOfHits;

        //This is the date to note the time of broadcast
        DateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        DateFormat time = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String currDate = date.format(cal.getTime());
        String currTime = time.format(cal.getTime());

        //Add db data to listOfDbMaps list. This list includes map for each song in db
        for (String key : dbHashesMap.keySet()) {
            //For each songID in the databaseMap, add it to the songIDList so that we can later refer to it
            songIDList.add(key);
            //Get hash list (The map as in the database) by songID
            // Add hash lists of each song as elements of the array list
            listOfDbMaps.add(dbHashesMap.get(key));
        }
        //System.out.println(listOfDbMaps.get(0));

        int count;
        //1s 4096 chunk *10
        Set keys = songToBeMatched.keySet();
        //Go through all the songs in the database
        for (int dbSongs = 0; dbSongs < listOfDbMaps.size(); dbSongs++) {
            Iterator<Double> iterator = keys.iterator();
            //iterate through songToBeMatched keyset
            //System.out.println("aaaaa");
            while (iterator.hasNext()) {
                double sampleTime = iterator.next();
                long value = songToBeMatched.get(sampleTime);


                //get db time-hash map , song hash
                matchMapReturnValue = matchMapNew(listOfDbMaps.get(dbSongs), value);

                for (double dbTime : matchMapReturnValue) {
                    //System.out.println(dbTime);
                    double offset = abs(sampleTime - dbTime);//System.out.println(round(offset,2));
                    //System.out.println(offset);
                    offsetList.add(offset);
                }
                matchMapReturnValue.clear();
                //System.out.println("jjjjjjjjj");
            }

            if (!offsetList.isEmpty()) {
                //Find max count of equal offset for a particular song
                count = findMode(offsetList);
                offsetList.clear();
                System.out.println("Offset max count :" + count);
                //Add that count as the count for the particular song into the maxCount list
                maxCount.add(count);
                //Then get the max no of hits out of all the songs in the database by taking the max from the maxCount list
            } else {
                maxCount.add(0);
                System.out.println("not matched");
            }

        }

        maxNoOfHits = Collections.max(maxCount);
        System.out.println("-----" + maxNoOfHits);
        //Logic- if maxNoOfHits is in more than one place in the max offset count, return unidentified. It is safe. Do that check here.
        if (maxNoOfHits <= 3 || moreThanOneMatch(maxNoOfHits, maxCount)) {
            matchedSongID = "Unidentified";
        } else {
            matchedSongID = songIDList.get(maxCount.indexOf(maxNoOfHits));
            System.out.println("Matched CommercialID = " + matchedSongID);
            //System.out.println(currTime);

            if (matchedSongIDHolder[0] == matchedSongID && matchedSongIDHolder[1] == matchedSongID) {
                //COUNT = COUNT+1;
                //get the time
            } else if (matchedSongIDHolder[0] == matchedSongID && matchedSongIDHolder[1] != matchedSongID) {
                matchedSongIDHolder[1] = matchedSongID;
            } else if (matchedSongIDHolder[0] != matchedSongID && matchedSongIDHolder[1] == matchedSongID) {
                matchedSongIDHolder[0] = matchedSongID;
            } else if (matchedSongIDHolder[0] != matchedSongID && matchedSongIDHolder[1] != matchedSongID) {
                if (matchedSongIDHolder[0] == matchedSongIDHolder[1]) {
                    //addToDB(matchedSongIDHolder[0], currTime, currDate);
                    System.out.println("Added Commercial to DB: " + matchedSongID);
                    matchedSongIDHolder[1] = matchedSongID;
                } else {
                    matchedSongIDHolder[0] = matchedSongIDHolder[1];
                    matchedSongIDHolder[1] = matchedSongID;
                }
            }
        }
        //System.out.println(listOfDbMaps.size());

        //Set an iterator on the keys(Actually the time)
        System.out.println("----------------------------------");
        //System.out.println(count1111);
        System.out.println("Matched CommercialID = " + matchedSongID);
        listOfDbMaps.clear();
        maxCount.clear();

    }


    //give dbsong to match with given 1 chunk
    public ArrayList<Double> matchMapNew(LinkedHashMap<Double, Long> dbHashes, long hash) {
        if (dbHashes.containsValue(hash)) {
            Set keys = dbHashes.keySet();
            Iterator<Double> iterator = keys.iterator();
            while (iterator.hasNext()) {
                double dbTime = iterator.next();
                long value = dbHashes.get(dbTime);
                if (hash == value) {
                    System.out.println(dbTime);
                    matchingTimes.add(dbTime);
                }
            }
        }

        return matchingTimes;/*this matchingIndex is the time of the last value which being matched This is the last value of the 4 hashes*/

    } //Matching index returns -1 only when the 4 hashes does not match to any part of the dbsong


    private int findMode(ArrayList<Double> list) {
        ArrayList<Integer> countList = new ArrayList<Integer>();
        double offset;
        for (int i = 0; i < list.size(); i++) {
            offset = list.get(i);
            int count = countNumberEqual(list, offset);
            countList.add(count);
            //System.out.println(count);
        }
        return Collections.max(countList);
    }

    private int countNumberEqual(ArrayList<Double> itemList, double itemToCheck) {
        int count = 0;
        for (double i : itemList) {
            if (i == itemToCheck) {
                count++;
            }
        }
        return count;
    }

    private boolean moreThanOneMatch(int maxNoOfHits, ArrayList<Integer> maxCountList) {

        int count = 0;
        for (int num : maxCountList) {
            if (num == maxNoOfHits) {
                count++;
                if (count == 2) {
                    return true;
                }
            }
        }
        return false;
    }


}