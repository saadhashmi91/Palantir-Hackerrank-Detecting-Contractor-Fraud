package com.company;

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;
import java.util.stream.*;

public class Solution {

    class Contractor {

        String contractor_name = new String();
        public ArrayList<Integer> starts = new ArrayList<>();
        public ArrayList<AbstractMap.SimpleEntry<Integer, ArrayList<Integer>>> events = new ArrayList<>();

        public void addEvents(Integer event_time, String[] events) {
            ArrayList<Integer> eventsAtTime = new ArrayList<>();
            for (String event : events) {
                eventsAtTime.add(Integer.parseInt(event));
            }
            AbstractMap.SimpleEntry<Integer, ArrayList<Integer>> entry = new AbstractMap.SimpleEntry<>(event_time, eventsAtTime);
            this.events.add(entry);
        }

        public boolean hasSubmissionIdGreaterThanListEvents(String[] events) {

            for (String event : events) {
                for (Map.Entry<Integer, ArrayList<Integer>> submissionEvent : this.events) {
                    ArrayList<Integer> submissions = submissionEvent.getValue();
                    for (Integer submissionEntry : submissions) {
                        if (Integer.parseInt(event) < submissionEntry) {
                            return true;
                        }
                    }
                }
            }
            return false;

        }

        public boolean isAStartTimeBeforeAnySubmissionEvents(ArrayList<Integer> start_times) {

            for (Map.Entry<Integer, ArrayList<Integer>> submissionEvent : this.events) {

                for (Integer job_start : start_times) {
                    if (job_start < submissionEvent.getKey()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int numStartTimesAfterSubmissionAtTime(int submissionEventTime, ArrayList<Integer> start_times) {
            int count = 0;
            for (Integer start_time : start_times) {
                if (start_time > submissionEventTime)
                    count++;
            }
            return count;
        }

        public int numGivenSubmissionIdsLessThanSelfSubmissionId(Integer submissionId, String[] events) {

            int count = 0;
            for (String event : events) {
                if (submissionId > Integer.parseInt(event))
                    count++;

            }
            return count;
        }

        public boolean isSuspciousBatch(String[] events, ArrayList<Integer> start_times) {
            int numSubmissions = this.events.size();
            for (Map.Entry<Integer, ArrayList<Integer>> submissionEvent : this.events) {

                int submissionTime = submissionEvent.getKey();
                int numStartTimesAfterThisEvent = numStartTimesAfterSubmissionAtTime(submissionTime, start_times);

                for (Integer submissionEventId : submissionEvent.getValue()) {
                    int numOtherIdsLessThanCurrent = numGivenSubmissionIdsLessThanSelfSubmissionId(submissionEventId, events);
                    if (numStartTimesAfterThisEvent >= numOtherIdsLessThanCurrent)
                        return true;
                }


            }
            return false;
        }

        public void addStartEvent(Integer start_time) {
            this.starts.add(start_time);
        }

        public void setContractor_name(String name) {
            this.contractor_name = name;
        }


        public boolean hasSubmissionEvents() {
            return events.size() > 0;
        }

    }
    class ContractorsModule {

        HashMap<String, Contractor> contractors = new HashMap<>();

        public void addContractor(String contractor_name, Contractor toAdd) {

            contractors.put(contractor_name, toAdd);
        }

        public Contractor getContractor(String contractor_name) {
            if (contractors.containsKey(contractor_name)) {
                return contractors.get(contractor_name);
            } else return null;

        }

    }

    String[] run(String[] datafeed)
    {
        ContractorsModule registry=new ContractorsModule();

        String[] inputReal =datafeed;
                //datafeed;


        int event_counter = 0;
        int line_num = 1;
        ArrayList<String> start_timeline = new ArrayList<>();
        ArrayList<String> fraud_event_timeline = new ArrayList<>();


        for (String input_value : inputReal) {
            String[] name_event = input_value.split(";");
            String contractor_name = name_event[0];
            String event = name_event[1];


            if (event.equals("START")) {
                if (registry.getContractor(contractor_name) != null) {
                    Contractor contractor = registry.getContractor(contractor_name);
                    contractor.addStartEvent(event_counter++);
                    start_timeline.add(contractor_name);
                } else {
                    Contractor contractor = new Contractor();
                    contractor.setContractor_name(contractor_name);
                    contractor.addStartEvent(event_counter++);
                    start_timeline.add(contractor_name);
                    registry.addContractor(contractor_name, contractor);
                }
            } else {
                Contractor currentContractor = registry.getContractor(contractor_name);
                int event_time = event_counter++;
                String[] submissionEvents = event.split(",");
                boolean isFraud = false;
                for (int i = start_timeline.lastIndexOf(contractor_name); i >= 0; i--) {
                    String previousContractorName = start_timeline.get(i);
                    //If previous Contractor is not the same
                    if (!previousContractorName.equals(contractor_name)) {
                        Contractor previousContractor = registry.getContractor(previousContractorName);
                        if (previousContractor != null) {
                            if (previousContractor.hasSubmissionEvents()) {
                                if (previousContractor.hasSubmissionIdGreaterThanListEvents(submissionEvents)) {

                                    if (previousContractor.isAStartTimeBeforeAnySubmissionEvents(currentContractor.starts) && !previousContractor.isSuspciousBatch(submissionEvents, currentContractor.starts)) {
                                        continue;

                                    } else {

                                        isFraud = true;
                                        if (submissionEvents.length == 1)
                                            fraud_event_timeline.add(currentContractor.starts.get(currentContractor.starts.size() - 1) + 1 + ";" + contractor_name + ";SHORTENED_JOB");
                                        else {

                                            fraud_event_timeline.add(line_num + ";" + contractor_name + ";SUSPICIOUS_BATCH");


                                        }
                                    }

                                }
                            }

                        }
                    }

                    if (isFraud)
                        break;
                }
                // if(!isFraud) {
                currentContractor.addEvents(event_time, submissionEvents);
                registry.contractors.put(contractor_name, currentContractor);
                //}


            }
            line_num++;
        }
        return fraud_event_timeline.toArray(new String[fraud_event_timeline.size()]);

    }




    static String[] findViolations(String[] datafeed) {

        Solution sol=new Solution();
        List<String> trimmedStrings =   Arrays.asList(datafeed).stream().map(String::trim).map(elem-> elem.replaceAll("\n","").replaceAll("\r","")).collect(Collectors.toList());
        String[] inputAfter=trimmedStrings.toArray(new String[trimmedStrings.size()]);
        String[] result = sol.run(inputAfter);
        return result;

    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        final String fileName = "OUTPUT_PATH";
        BufferedWriter bw = null;
        if (fileName != null) {
            bw = new BufferedWriter(new FileWriter(fileName));
        }
        else {
            bw = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        String[] res;
        int datafeed_size = 0;
        datafeed_size = Integer.parseInt(in.nextLine().trim());

        String[] datafeed = new String[datafeed_size];
        for(int i = 0; i < datafeed_size; i++) {
            String datafeed_item;
            try {
                datafeed_item = in.nextLine();
            } catch (Exception e) {
                datafeed_item = null;
            }
            datafeed[i] = datafeed_item;
        }

        res = findViolations(datafeed);
        for(int res_i = 0; res_i < res.length; res_i++) {
            bw.write(String.valueOf(res[res_i]));
            bw.newLine();
        }

        bw.close();
    }
}
