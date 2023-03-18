package ghs.config;

/*
  This file contains all the details regarding the node
  and its neighbors
 */

import ghs.message.Edge;
import ghs.message.EdgeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private final Logger log = Logger.getLogger(String.valueOf(Config.class));
    int numberOfNodes;
    HashMap<Integer, String> uidToHost;
    HashMap<String, Integer> hostToUID;
    HashMap<String, Integer> hostToPort;
    HashMap<Integer, List<Edge>> graph;

    public Config(){
        numberOfNodes = 0;
        uidToHost = new HashMap<>();
        hostToUID = new HashMap<>();
        graph = new HashMap<>();
        hostToPort = new HashMap<>();
    }

    public void readFile() {
        try {
            File fp = new File("ghs/launch/config.txt");
            Scanner sc = new Scanner(fp);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.charAt(0) == '#') continue;

                if (numberOfNodes == 0) {
                    numberOfNodes = Integer.parseInt(line);
                } else {
                    String[] values = line.split(" ");
                    if (values.length == 3) {
                        uidToHost.put(Integer.parseInt(values[0]), values[1]);
                        hostToUID.put(values[1], Integer.parseInt(values[0]));
                        hostToPort.put(values[1], Integer.parseInt(values[2]));
                    }else if (values.length == 2) {
                        StringBuilder sb = new StringBuilder(values[0]);
                        String[] vertices = sb.substring(1, sb.length()-1).split(",");
                        int u = Integer.parseInt(vertices[0]);
                        int v = Integer.parseInt(vertices[1]);
                        int wt = Integer.parseInt(values[1]);
                        graph.computeIfAbsent(u, k->new ArrayList<>()).add(new Edge(u, v, wt, EdgeType.NORMAL_EDGE));
                        graph.computeIfAbsent(v, k->new ArrayList<>()).add(new Edge(v, u, wt, EdgeType.NORMAL_EDGE));
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
        if (!hostToPort.containsKey(host))
            throw new NoSuchElementException();

        return hostToPort.get(host);
    }
    public int getNumberOfNodes() {
        return numberOfNodes;
    }
    public int getMyUID(String host) {
        if (!hostToUID.containsKey(host))
            throw new NoSuchElementException();

        return hostToUID.get(host);
    }

    public List<Edge> getNeighbors(int uid) {
        return graph.get(uid);
    }

    public String getHostName(Integer UID) {  return uidToHost.get(UID); }

    // this is for testing
    public static void main(String[] args) {
        Config c = new Config();
        c.readFile();
        System.out.println(c.graph);
    }
}
