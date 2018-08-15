import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;


public class TaM {
    private static TaM manager = new TaM();
    private ArrayList<Episode> playing = new ArrayList<>();
//    private ArrayList<Episode> playing = new ArrayList<>();

    private FFmpeg fFmpeg;
    private FFprobe fFprobe;
    private Properties p;
    private int ServerPort;
    private String FFmpeg_Path;
    private String FFprobe_Path;
    private String RTMP_Add;
    private String Store_Folder;
    private String Download_Folder;
    private String TG_Bot;
    private boolean noticeBot=false;
    private MegaHandler mh = new MegaHandler("1@1","1");
    private String[] Seedbox_Uname;
    private String[] Seedbox_Pswd;
    private String[] Seedbox_contains;
    private String Default_Video;
    private boolean hasDV;
    private boolean firstChanged=true;

    private ArrayList<Burn> burns = new ArrayList<>();

    private HashMap<String, ArrayList<Episode>> library = new HashMap<>();
    private LinkedHashMap<Integer,Episode> lib = new LinkedHashMap<>();

    public void add2L(Episode episode){
        boolean flag = false;
        int f = -1;
        for (Episode e : lib.values()){
            if (e.equals(episode)){
                flag=true;
                f=e.getID();
            }
        }

        if(flag){
            removeFL(f);
        }

        lib.put(episode.getID(),episode);
        if (!library.containsKey(episode.getShortName()))
            library.put(episode.getShortName(),new ArrayList<>());


        library.get(episode.getShortName()).add(episode);
        update();
    }

    public Episode getById(int i){
        try {
            return lib.get(i);
        }catch (Exception e ){
            return null;
        }
    }


    public void removeFL(int ID){
        if (playing.get(0).getID()==ID)firstChanged=true;
        playing.removeIf((Episode episode) -> episode.getID()==ID);
        library.get(lib.get(ID).getShortName()).removeIf((Episode episode) -> episode.getID()==ID);
        lib.remove(ID);
        update();
    }

    public void newPlayList(int[] nID){
        if (playing.get(0).getID()!=nID[0])firstChanged=true;
        ArrayList<Episode> newP = new ArrayList<>();
        for (int n : nID)if (lib.containsKey(n))newP.add(lib.get(n));
        playing=newP;
        update();
    }

//    public ArrayList<Episode> getPlaying() {
//        return playing;
//    }

    public boolean hasShow(){

              boolean a=  playing.size()!=0;
              return a;
    }

    public Episode getFirst(){
        return playing.get(0);
    }

    public boolean isFirstChanged() {
        return firstChanged;
    }

    public void changeSE(int id,String s,String e){
        if (lib.containsKey(id)) {
            lib.get(id).setSeason(s);
            lib.get(id).setEpisode(e);
            lib.get(id).getPlayLocation().renameTo(new File(TaM.getManager().getStore_Folder()+lib.get(id).getShortName()+".S"+lib.get(id).getSeason()+"E"+lib.get(id).getEpisode()+".mp4"));
            lib.get(id).genLocation();
            update();
        }
    }

    public void playFinished(){
        if (firstChanged){
            firstChanged=false;
            return;
        }
        if (playing.size()>0)
            Collections.rotate(playing,-1);
        update();
    }

    synchronized void update(){
        updateSchedule();
        updateLibrary();
    }

    synchronized void updateSchedule(){
        ArrayList<EpInSchedule> schedule = new ArrayList<>();
        long let = Live.lastStart;
        if (hasShow()) {
            for (Episode episode : playing) {
                schedule.add(new EpInSchedule(episode, let));
                let += episode.getDuration() * 1000;
            }
        }
        Gson gson = new Gson();
        try {
            FileUtils.writeStringToFile(new File("./timetable.json"),gson.toJson(schedule),"UTF-8");
        }catch (IOException e){
            System.out.println(" save timetable.json failed");
        }

    }
    private synchronized void updateLibrary(){
        Gson gson = new Gson();
        try {
            FileUtils.writeStringToFile(new File("./option/library.json"),gson.toJson(library),"UTF-8");
            FileUtils.writeStringToFile(new File("./option/lib.json"),gson.toJson(lib),"UTF-8");
            FileUtils.writeStringToFile(new File("./playing.json"),gson.toJson(playing),"UTF-8");

        }catch (IOException e){
            System.out.println(" save library/playing.json failed");
        }

    }


    public HashMap<String, ArrayList<Episode>> getLibrary() {
        return library;
    }

    public ArrayList<Episode> getPlaying() {
        return playing;
    }
//    public static void setPlaying(ArrayList<Episode> playing) {
//        TaM.playing = playing;
//    }

    public  ArrayList<Burn> getBurns() {
        return burns;
    }

    public int getServerPort() {
        return ServerPort;
    }

    public String getDefault_Video() {
        return Default_Video;
    }

    public boolean isHasDV() {
        return hasDV;
    }

    public String getFFmpeg_Path() {
        return FFmpeg_Path;
    }

    public String getFFprobe_Path() {
        return FFprobe_Path;
    }

    public String getRTMP_Add() {
        return RTMP_Add;
    }

    public String getTG_Bot() {
        return TG_Bot;
    }

    public boolean isNoticeBot() {
        return noticeBot;
    }

    public String[] getSeedbox_Uname() {
        return Seedbox_Uname;
    }

    public String[] getSeedbox_Pswd() {
        return Seedbox_Pswd;
    }

    public String[] getSeedbox_contains() {
        return Seedbox_contains;
    }

    private TaM(){
        p = new Properties();
        try {
            p.load(new FileInputStream("config"));
        }catch (IOException e){
            System.out.println("use default setting");
        }
        configChecker(p);
        try {
            fFmpeg = new FFmpeg(getFFmpeg_Path());
            fFprobe= new FFprobe(getFFprobe_Path());
        }catch (IOException e){
            System.out.println("ffmpeg zha");
        }


        System.out.println(p.get("q"));

        File data = new File("./option/library.json");
        if (data.exists()){
            try{
                String d = FileUtils.readFileToString(data,"UTF-8");
                Gson gson = new Gson();
                Type managerType = new TypeToken<HashMap<String,ArrayList<Episode>>>(){}.getType();
                library=gson.fromJson(d,managerType);

                for (String n : library.keySet()){
                    for (Episode e:library.get(n)){
                        if (Episode.getNewID()<=e.getID()){
                            Episode.setNewID(e.getID()+1);
                        }
                    }
                }

            }catch (IOException e){
                System.out.println("no valid library.json");
            }
        }else {
            System.out.println("no stored library.json");
        }
        File play = new File("./playing.json");

        if (play.exists()){
            try{
                String d = FileUtils.readFileToString(play,"UTF-8");
                Gson gson = new Gson();
                Type managerType = new TypeToken<ArrayList<Episode>>(){}.getType();
                this.playing=gson.fromJson(d,managerType);
            }catch (IOException e){
                System.out.println("no valid playing.json");
            }
        }else {
            System.out.println("no stored playing.json");
        }

        File libb = new File("./option/lib.json");

        if (libb.exists()){
            try{
                String d = FileUtils.readFileToString(libb,"UTF-8");
                Gson gson = new Gson();
                Type managerType = new TypeToken<LinkedHashMap<Integer,Episode>>(){}.getType();
                this.lib=gson.fromJson(d,managerType);
            }catch (IOException e){
                System.out.println("no valid lib.json");
            }
        }else {
            System.out.println("no stored lib.json");
        }

        updateSchedule();
    }



    public MegaHandler getMh() {
        return mh;
    }

    public static TaM getManager() {
        return manager;
    }

    public FFmpeg getfFmpeg() {
        return fFmpeg;
    }

    public FFprobe getfFprobe() {
        return fFprobe;
    }

    public String getStore_Folder() {
        return Store_Folder;
    }

    public void setFirstChanged(){
        firstChanged=true;
    }

    public String getDownload_Folder() {
        return Download_Folder;
    }

    void configChecker(Properties p){
        try{
            if (p.getProperty("Server_Port")==null)throw new Exception();
            ServerPort=Integer.parseInt(p.getProperty("Server_Port"));
        }catch (Exception e){
            ServerPort=8000;
        }

        try{
            if (p.getProperty("FFmpeg_Path")==null)throw new Exception();
            FFmpeg_Path=p.getProperty("FFmpeg_Path");
        }catch (Exception e){
            FFmpeg_Path="/usr/bin/ffmpeg";
        }

        try{
            if (p.getProperty("FFprobe_Path")==null)throw new Exception();
            FFprobe_Path=p.getProperty("FFprobe_Path");
        }catch (Exception e){
            FFmpeg_Path="/usr/bin/ffprobe";
        }

        try{
            if (p.getProperty("RTMP_Add")==null)throw new Exception();
            RTMP_Add=p.getProperty("RTMP_Add");
        }catch (Exception e){
            System.out.println("RTMP Address needed");
            System.exit(1);
        }

        try{
            if (p.getProperty("Store_Folder")==null)throw new Exception();
            Store_Folder=p.getProperty("Store_Folder");
            File file= new File(Store_Folder);
            if (!file.exists())file.mkdir();
        }catch (Exception e){
            Store_Folder="./video/";
            File file= new File(Store_Folder);
            if (!file.exists())file.mkdir();
        }

        try{
            if (p.getProperty("Download_Folder")==null)throw new Exception();
            Download_Folder=p.getProperty("Download_Folder");
            File file= new File(Download_Folder);
            if (!file.exists())file.mkdir();
        }catch (Exception e){
            Download_Folder="./download/";
            File file= new File(Download_Folder);
            if (!file.exists())file.mkdir();
        }

        if (p.getProperty("TG_Bot")!=null&&!p.getProperty("TG_Bot").equals("")){
            TG_Bot=p.getProperty("TG_Bot");
            noticeBot=true;
        }

        if (p.getProperty("Default_Video")!=null&&!p.getProperty("Default_Video").equals("")){
            Default_Video=p.getProperty("Default_Video");
            hasDV=true;
        }

        if (p.getProperty("Seedbox_Uname")!=null&&!p.getProperty("Seedbox_Uname").equals("")){
            Seedbox_Uname=p.getProperty("Seedbox_Uname").split(",");
        }

        if (p.getProperty("Seedbox_Pswd")!=null&&!p.getProperty("Seedbox_Pswd").equals("")){
            Seedbox_Pswd=p.getProperty("Seedbox_Pswd").split(",");
        }

        if (p.getProperty("Seedbox_contains")!=null&&!p.getProperty("Seedbox_contains").equals("")){
            Seedbox_contains=p.getProperty("Seedbox_contains").split(",");
        }


    }


}

class EpInSchedule {
    private String displayName;
    private String season;
    private String e;
    private long startTime;

    EpInSchedule(Episode episode, long led){
        this.displayName=episode.getFullName();
        this.season=episode.getSeason();
        this.e=episode.getEpisode();
        this.startTime=led;

    }
}