package com.genesyseast.coinconnection.Support;
public class JSONAssetList
{
    String fileName;
    int    dataType;
    long   pathID;
    
    public JSONAssetList( String fileName, int dataType, long pathID )
    {
        this.fileName = fileName;
        this.dataType = dataType;
        this.pathID = pathID;
    }
    
    public JSONAssetList()
    {
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    public int getDataType()
    {
        return dataType;
    }
    
    public long getPathID()
    {
        return pathID;
    }
}
