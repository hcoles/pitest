package org.pitest.mutationtest.tdghistory;
import java.io.Serializable;
import java.net.URL;
public class Tdgsha implements Serializable{
    private String crc32Hash;
    private URL fileUrl;

    public Tdgsha(URL fileUrl, String crc32Hash) {
        this.fileUrl = fileUrl;
        this.crc32Hash = crc32Hash;
    }

    @Override
    public String toString() {
        return this.fileUrl + ":" + this.crc32Hash;
    }

    public URL getFileUrl() {
        return this.fileUrl;
    }

    public String getCRC32Hash() {
        return this.crc32Hash;
    }
}
