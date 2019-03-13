package proxy;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class StatsCounter {

    private ArrayList<DomainStats> domainStatsList;
    private String filename;

    public StatsCounter(String filename){
        this.domainStatsList = new ArrayList<>();
        this.filename = filename;
        loadFromCsv();
    }

    public void registerDomain(String host){

        if(!isRegistered(host))
            domainStatsList.add(new DomainStats(host, 0, 0, 0));
        exportToCsv();
    }

    private boolean isRegistered(String host){

        for(DomainStats domainStats: domainStatsList){
            if(domainStats.getHost().equals(host)){
                return true;
            }
        }
        return false;

    }

    private DomainStats findDomain(URL url){

        DomainStats result = null;
        String host = url.getHost();

        for(DomainStats domainStats: domainStatsList){
            if(domainStats.getHost().equals(host)){
                result = domainStats;
                break;
            }
        }
        return result;
    }

    public void increaseQueriesCount(URL url){
        DomainStats domainStats = findDomain(url);
        if(domainStats != null)
            domainStats.setNumberOfQueries(domainStats.getNumberOfQueries() + 1);
    }

    public void increaseReceivedBytes(URL url, int numBytes){
        DomainStats domainStats = findDomain(url);
        if(domainStats != null)
            domainStats.setTotalBytesReceived(domainStats.getTotalBytesReceived() + numBytes);
    }

    public void increaseSendBytes(URL url, int numBytes){
        DomainStats domainStats = findDomain(url);
        if(domainStats != null)
            domainStats.setTotalBytesSend(domainStats.getTotalBytesSend() + numBytes);
    }

    private void loadFromCsv(){

        if(Files.exists(Paths.get(filename))){
            try {
                ArrayList<String> lines = (ArrayList<String>)Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
                for(int i=1; i<lines.size(); i++){
                    String line = lines.get(i);
                    String[] fields = line.split(",");

                    DomainStats domainStats = new DomainStats();
                    domainStats.setHost(fields[0]);
                    domainStats.setNumberOfQueries(Long.valueOf(fields[1].trim()));
                    domainStats.setTotalBytesSend(Long.valueOf(fields[2].trim()));
                    domainStats.setTotalBytesReceived(Long.valueOf(fields[3].trim()));

                    domainStatsList.add(domainStats);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void exportToCsv(){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))){
            bufferedWriter.write("url, count, bytesSend, bytesReceived \n");

            for(DomainStats domainStats: domainStatsList){
                bufferedWriter.write(domainStats.toString() + "\n");
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateCsv(DomainStats domainStats){
        try {
            ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
            String hostToFind = domainStats.getHost();

            for(int i=0; i<lines.size(); i++){
                String line = lines.get(i);
                String host = line.split(",")[0];

                if(host.equals(hostToFind)){
                    lines.set(i, domainStats.toString());
                    break;
                }

            }
            Files.write(Paths.get(filename), lines, StandardCharsets.UTF_8);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void save(URL url){
        DomainStats domainStats = findDomain(url);
        if(domainStats != null)
            updateCsv(domainStats);

    }


}
