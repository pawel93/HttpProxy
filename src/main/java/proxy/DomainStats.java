package proxy;

import java.net.URL;

public class DomainStats {

    private String host;
    private long numberOfQueries;
    private long totalBytesReceived;
    private long totalBytesSend;

    public DomainStats(){

    }

    public DomainStats(String host, long numberOfQueries, long totalBytesReceived, long totalBytesSend) {
        this.host = host;
        this.numberOfQueries = numberOfQueries;
        this.totalBytesReceived = totalBytesReceived;
        this.totalBytesSend = totalBytesSend;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getNumberOfQueries() {
        return numberOfQueries;
    }

    public void setNumberOfQueries(long numberOfQueries) {
        this.numberOfQueries = numberOfQueries;
    }

    public long getTotalBytesReceived() {
        return totalBytesReceived;
    }

    public void setTotalBytesReceived(long totalBytesReceived) {
        this.totalBytesReceived = totalBytesReceived;
    }

    public long getTotalBytesSend() {
        return totalBytesSend;
    }

    public void setTotalBytesSend(long totalBytesSend) {
        this.totalBytesSend = totalBytesSend;
    }

    public String toString(){
        return host + ", "
                + numberOfQueries + ", "
                + totalBytesSend + ", "
                + totalBytesReceived;
    }


}
