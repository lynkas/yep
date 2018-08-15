import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.security.MessageDigest;
import java.util.Objects;

public class Episode extends Series{

    private String season;
    private String episode;
    private File playLocation;
    private static int newID=0;
    private int ID = newID++;
    private int duration;
    Episode(String shortName,String fullName,String season,String episode,int duration){
        super(shortName,fullName);
        this.season=season;
        this.episode=episode;
        playLocation=new File(TaM.getManager().getStore_Folder()+shortName+".S"+season+"E"+episode+".mp4");
        System.out.println(playLocation.getAbsolutePath());
        this.duration=duration;
    }

    Episode(Series series,String season,String episode){
        super(series.getShortName(),series.getFullName());
        this.season=season;
        this.episode=episode;
        playLocation=new File(TaM.getManager().getStore_Folder()+shortName+".S"+season+"E"+episode+".mp4");
        System.out.println(playLocation.getAbsolutePath());
        this.duration=calDuration();
    }

    public static void setNewID(int newID) {
        Episode.newID = newID;
    }

    public static int getNewID() {
        return newID;
    }

    private int calDuration() {
        try {
            FFmpegProbeResult probeResult = TaM.getManager().getfFprobe().probe(this.playLocation.getAbsolutePath());
            FFmpegFormat format = probeResult.getFormat();
            return (int)format.duration;
        }catch (Exception e){
            return 0;
        }
    }


    public String getSeason() {
        return season;
    }

    public int getDuration() {
        return duration;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void genLocation(){
        this.playLocation=new File(TaM.getManager().getStore_Folder()+shortName+".S"+season+"E"+episode+".mp4");
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public File getPlayLocation() {
        return playLocation;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String getFullName() {
        return super.getFullName();
    }

    @Override
    public String getShortName() {
        return super.getShortName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return Objects.equals(playLocation, episode.playLocation);
    }

    public boolean rmFile(){
        return this.getPlayLocation().delete();
    }


    @Override
    public int hashCode() {

        return Objects.hash(playLocation);
    }

    @Override
    public String toString() {
        return " ID"+ID +" "+fullName+" S"+season+"E"+episode+" ";
    }

    public String genDiv(){
        return "<tr style=\"-moz-user-select:none;background-color:"+toRGB(shortName)+"\"><td class=\"one\">"+ID+"</td><td>"+fullName+"</td><td>S"+season+"E"+episode+"</td></tr>";
    }

    String toRGB(String string){
        try {
            byte[] bytesOfMessage = string.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            int [] a = new int[thedigest.length];
            for (int i =0;i<thedigest.length;i++){
                a[i]=thedigest[i]+128;
                if (i>1){
                    if (a[i-2]+a[i-1]+a[i-0]>108){
                        return (two(Integer.toHexString(a[i-2]))+two(Integer.toHexString(a[i-1]))+two(Integer.toHexString(a[i]))).toUpperCase();
                    }
                }
            }

            return (two(Integer.toHexString(a[0]+36))+two(Integer.toHexString(a[1]+36))+two(Integer.toHexString(a[2]+36))).toUpperCase();

        }catch (Exception e){
            return "";
        }
    }

    static String two(String a){
        return a.length()==1? "0"+a: a;
    }
}
