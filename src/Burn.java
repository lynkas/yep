import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Burn implements Runnable{
    private String fullName;
    private String shortName;
    private String season;
    private String episode;
    private File oriFile;
    private File oriSub;
    private File newFile;
    private int duration;
    private int mode;
    private int[] array;


    Burn(String dlID, String fileName, String fullName, String shortName, String season, String episode,int mode ,int[] array){
        this.shortName=shortName;
        this.fullName=fullName;
        this.season=season;
        this.episode=episode;
        this.mode=mode;
        this.array=array;
        this.oriFile=new File(TaM.getManager().getDownload_Folder()+dlID+"/"+fileName);
        this.oriSub=new File(TaM.getManager().getDownload_Folder()+dlID+"/"+fileName+".ass");
        this.newFile=new File(TaM.getManager().getStore_Folder()+shortName+".S"+season+"E"+episode+".mp4");
        this.duration=calDuration();


    }

    @Override
    public void run() {
        synchronized (Burn.class) {
            genSubtitle();
            burn();
            Episode episode1 = new Episode(this.shortName, this.fullName, this.season, this.episode, duration);
            TaM.getManager().add2L(episode1);
            if (mode==1){
                for (int i: array) {
                    try {
                        TaM.getManager().getPlaying().add(i,episode1);
                        if (i==0)TaM.getManager().setFirstChanged();
                    }catch (Exception e){
                        System.out.println("add p "+episode+i+"failed");
                    }
                }
            return;
            }

            if (mode==2){
                for (int i :array){
                    try {
                        TaM.getManager().getPlaying().add(TaM.getManager().getPlaying().indexOf(TaM.getManager().getById(i))+1,episode1);
                    }catch (Exception e){
                        System.out.println("add after "+episode+i+"failed");
                    }
                }
            }

            TaM.getManager().update();

        }
    }



    private int calDuration() {
        try {
            FFmpegProbeResult probeResult = TaM.getManager().getfFprobe().probe(oriFile.getAbsolutePath());
            FFmpegFormat format = probeResult.getFormat();
            return (int)format.duration;
        }catch (Exception e){
            return 0;
        }
    }

    private void  burn(){
        FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                .setInput(oriFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(newFile.getAbsolutePath())
                .setFormat("mp4")
                .setConstantRateFactor(25)
                .setVideoFilter("ass="+oriSub.getAbsolutePath())
                .setAudioChannels(2)
                .setAudioCodec("aac")
                .setAudioSampleRate(44100)
                .setVideoCodec("libx264")
                .setAudioBitRate(78000)
                .addExtraArgs("-tune", "animation")
                .done();
        try {
            System.out.println("processing to "+newFile.getAbsolutePath());
            FFmpegExecutor executor = new FFmpegExecutor(TaM.getManager().getfFmpeg());
            executor.createJob(fFmpegBuilder).run();
            System.out.println(newFile.getName()+" done");
        }catch (IOException e){
            System.out.println(episode+ "FAILED");
        }
    }

    private void genSubtitle(){
        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(oriFile.getAbsolutePath())
                    .addOutput(oriSub.getAbsolutePath())
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor();
            executor.createJob(builder).run();
            if (FileUtils.readFileToString(oriSub,"UTF-8").trim().equals(""))throw new IOException();
        }catch (Exception e){
            try{
                System.out.println(episode +"does not have subtitle");
                oriSub.createNewFile();
                String head = "[Script Info]\n" +
                        "ScriptType: v4.00+\n" +
                        "PlayResX: 384\n" +
                        "PlayResY: 288\n" +
                        "\n" +
                        "[V4+ Styles]\n" +
                        "Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n" +
                        "Style: Default,Arial,16,&H00FFFFFF,&H00FFFFFF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,1,0,2,10,10,10,0\n" +
                        "\n" +
                        "[Events]\n" +
                        "Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n";
                FileUtils.writeStringToFile(oriSub,head,"UTF-8",false);
                System.out.println("manually head");
            }catch (IOException ee){
                System.out.println("FAILED creating file "+episode+ "ass");
            }
        }
        System.out.println("gentime");
        genTime();

    }

    private void genTime(){

        try{
            for (int second = 0 ;second<duration;second++){
                int i=second*1024/duration+1;
                if (second%40==0){
                    String notice ="Dialogue: 0,"+t2HMS(second)+","+t2HMS(second+20)+",Default,,0,0,0,,{\\fad(300,300)\\fs15\\a3\\pos(382,288)\\c&HFFFFFF&\\1a&HAA&\\i0\\fnNoto Sans CJK SC DemiLight}"+"S"+season+"E"+episode+" "+duration/60+"min\n";
                    FileUtils.writeStringToFile(oriSub,notice,"UTF-8",true);
                }else if (second%40==20){
                    String notice ="Dialogue: 0,"+t2HMS(second)+","+t2HMS(second+20)+",Default,,0,0,0,,{\\fad(300,300)\\fs15\\a3\\pos(382,288)\\c&HFFFFFF&\\1a&HAA&\\i0\\fnNoto Sans CJK SC DemiLight}"+fullName+"\n";
                    FileUtils.writeStringToFile(oriSub,notice,"UTF-8",true);
                }

                String t = "Dialogue: 0,"+t2HMS(second)+","+t2HMS(second+1)+",Default,,0,0,0,,{\\bord1\\p1\\pos(0,2)\\3c&H000000&\\c&HFFFFFF&\\1a&H80&}m 0 0 l "+i+" 0 l "+i+" 2 l 0 2 \n";
                FileUtils.writeStringToFile(oriSub,t,"UTF-8",true);
            }

        }catch (IOException e){
            System.out.println("gen time failed");
        }
    }

    public static String t2HMS(int i){
        return i/3600+":"+ms((i%3600)/60)+":"+ms(i%60)+".00";
    }

    public static String ms(int i){
        if (i<10)return "0"+i;
        return ""+i;
    }

}
