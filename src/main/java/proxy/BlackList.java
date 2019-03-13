package proxy;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class BlackList {

    private ArrayList<URL> blocked;
    private String filename;

    public BlackList(String filename){
        this.blocked = new ArrayList<>();
        this.filename = filename;
        readUrls();
    }

    private void readUrls(){

        try(BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))){
            String line;
            while((line = reader.readLine()) != null){
                URL url = null;
                try{
                    url = new URL(line);
                    blocked.add(url);
                }catch(MalformedURLException e){

                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void writeUrl(URL url){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, true))){
            bufferedWriter.write(url.toString());

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void refresh(){
        blocked.clear();
        readUrls();
    }

    public void addToList(URL url){
        blocked.add(url);
        writeUrl(url);
    }

    public boolean isOnList(URL url){

        for(URL blckedUrl: blocked){
            String blockedHost = blckedUrl.getHost();
            if(blockedHost.equals(url.getHost())){
                return true;
            }
        }
        return false;
    }


}
