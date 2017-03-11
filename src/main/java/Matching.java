import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.abs;

public class Matching {
    public static final int SIZE_OF_CONSECUTIVE_MATCHES_ARRAY = 1;
    public static int[] totalHashes=new int[24];
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
        int[] maxNoOfHits;
        for (String key : dbHashesMap.keySet()) {
            songIDList.add(key);
            listOfDbMaps.add(dbHashesMap.get(key));
        }

        System.out.println(Arrays.toString(songIDList.toArray()));
        int count;
        Set keys = songToBeMatched.keySet();

        for (int dbSongs = 0; dbSongs < listOfDbMaps.size(); dbSongs++) {
            Iterator<Double> iterator = keys.iterator();

            while (iterator.hasNext()) {
                double sampleTime = iterator.next();
                long value = songToBeMatched.get(sampleTime);

                matchMapReturnValue = matchMapNew(listOfDbMaps.get(dbSongs), value);

                for (double dbTime : matchMapReturnValue) {
                    //System.out.println(dbTime);
                    double offset = abs(sampleTime - dbTime);//System.out.println(round(offset,2));
                    //System.out.println("=================== "+offset);
                    offsetList.add(offset);
                }
                matchMapReturnValue.clear();
                //System.out.println("jjjjjjjjj");
            }

            if (!offsetList.isEmpty()) {
                //Find max count of equal offset for a particular song
                count = findMode(offsetList);
                offsetList.clear();
                //System.out.println("Offset max count :" + count);
                //Add that count as the count for the particular song into the maxCount list
                maxCount.add(count);
                //Then get the max no of hits out of all the songs in the database by taking the max from the maxCount list
            } else {
                maxCount.add(0);
                System.out.println("not matched");
            }

            totalHashes[dbSongs]= listOfDbMaps.get(dbSongs).size();

        }
//??????
        int[] target = new int[maxCount.size()];
        for (int i = 0; i < target.length; i++) {
            //if (maxCount.get(i) >= 5) {
                target[i] = maxCount.get(i);
            //}
            System.out.print(1000*target[i]/totalHashes[i]+"  ");
        }
        System.out.println("\n");
        System.out.println(Arrays.toString(target));
        System.out.println(Arrays.toString(totalHashes));


        for(int i=0;i<target.length;i++){
            System.out.println("--Max No Of Hits---" + target[i]);
       // maxNoOfHits = Collections.max(maxCount);
        //System.out.println("-----" + maxNoOfHits);
        //Logic- if maxNoOfHits is in more than one place in the max offset count, return unidentified. It is safe. Do that check here.
        if (1000*target[i]/totalHashes[i] <= 26) {
            matchedSongID = "Unidentified";
        } else {
            matchedSongID = songIDList.get(maxCount.indexOf(target[i]));

            //System.out.println(currTime);

        }
            System.out.println("Matched Commercial ID = " + matchedSongID);
            System.out.println("----------------------------------");
        }
        //System.out.println(listOfDbMaps.size());

        //Set an iterator on the keys(Actually the time)

        //System.out.println(count1111);
        //System.out.println("Matched CommercialID = " + matchedSongID);
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
                   // System.out.println(dbTime);
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