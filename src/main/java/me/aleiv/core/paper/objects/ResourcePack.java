package me.aleiv.core.paper.objects;

import lombok.Data;

@Data
public class ResourcePack {

    String name;
    boolean enabled = false;
    String resoucePackURL;
    byte[] resourcePackHash;

    public ResourcePack(String name, boolean enabled, String resoucePackURL, byte[] resourcePackHash){
        this.name = name;
        this.enabled = enabled;
        this.resoucePackURL = resoucePackURL;
        this.resourcePackHash = resourcePackHash;
    }

    public ResourcePack(String name, String resoucePackURL, byte[] resourcePackHash){
        this.name = name;
        this.resoucePackURL = resoucePackURL;
        this.resourcePackHash = resourcePackHash;
    }

    /*public String getResourcePackHash() {
        return this.encodeUsingBigIntegerToString(resourcePackHash);
    }

    public String encodeUsingBigIntegerToString(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return bigInteger.toString(16);
    }*/
    
}
