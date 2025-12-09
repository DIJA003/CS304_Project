package game;

public enum Difficulty {
    Easy("src//assets//SkeletonBoss"),
    Medium("src//assets//Knight2"),
    Hard("src//assets//SamuraiBoss");
    public final String path;
    Difficulty(String path){
        this.path = path;
    }
}
