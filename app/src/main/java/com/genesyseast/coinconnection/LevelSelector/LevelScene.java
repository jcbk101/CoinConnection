package com.genesyseast.coinconnection.LevelSelector;

public class LevelScene
{
    public int   layoutScene;
    public int   sceneIndex;
    public int   baseLevel;
    public int[] currentStars;
    
    public LevelScene( int layoutScene, int sceneIndex, int baseLevel, int[] currentStars )
    {
        this.layoutScene = layoutScene;
        this.sceneIndex = sceneIndex;
        this.baseLevel = baseLevel;
        this.currentStars = currentStars;
    }
}
