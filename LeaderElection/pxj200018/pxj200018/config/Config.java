package pxj200018.config;

/*
  This file contains all the details regarding the node
  and its neighbors
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private final Logger log = Logger.getLogger(String.valueOf(Config.class));
    int numberOfNodes;
    HashMap<Integer, Integer> valToUID;
    HashMap<String, Integer> hostToUID;
    HashMap<String, Integer> hostToPort;
    HashMap<Integer, List<Integer>> graph;
    HashMap<Integer, String> uidToHost;

    public Config(){
        numberOfNodes = 0;
        valToUID = new HashMap<>();
        hostToUID = new HashMap<>();
        graph = new HashMap<>();
        hostToPort = new HashMap<>();
        uidToHost = new HashMap<>();
    }
    public void readFile() {
        int i = 1, j = 1;
        try {
            File fp = new File("pxj200018/config/config.txt");
            Scanner sc = new Scanner(fp);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.charAt(0) == '#') continue;
                
                if (line.length() == 1 && numberOfNodes == 0) {
                    numberOfNodes = Integer.parseInt(line);
                } else {
                    String[] values = line.split(" ");

                    if (line.contains("dc")) {
                        valToUID.put(i++, Integer.parseInt(values[0]));
                        hostToUID.put(values[1], Integer.parseInt(values[0]));
                        hostToPort.put(values[1], Integer.parseInt(values[2]));
                        uidToHost.put(Integer.valueOf(values[0]), values[1]);
                    } else {
                        int nodeVal = valToUID.get(j);
                        for (String neighbors : values) {
                            if (!graph.containsKey(nodeVal)) graph.put(nodeVal, new ArrayList<>());
                            graph.get(nodeVal).add(Integer.parseInt(neighbors));
                        }
                        j++;
                    }
                }
            }

        }catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "Error in opening file\n");
            e.printStackTrace();
        }
    }

    public Integer getMyPortNumber(String host) {
        //if the map does not contain a key then throw error.
        if (!hostToPort.containsKey(host)) {
            return 8000;
            //throw new NoSuchElementException();
        }
        return hostToPort.get(host);
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getMyUID(String host) {
        if (!hostToUID.containsKey(host)) {
            throw new NoSuchElementException();
        }
        return hostToUID.get(host);
    }

    public List<Integer> getNeighbors(int uid) {
        return graph.get(uid);
    }

    public String getHostName(Integer UID) {  return uidToHost.get(UID); }
}
