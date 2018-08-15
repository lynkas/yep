import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;


public class Live implements Runnable{
    private int boomCount=0;

    static long lastStart =new Date().getTime();
    public String RTMP = TaM.getManager().getRTMP_Add();
    @Override
    public void run() {
        while(true){
            if (TaM.getManager().hasShow()) {
                l();
            }else {
                if (TaM.getManager().isHasDV()) {
                    l(TaM.getManager().getStore_Folder() + TaM.getManager().getDefault_Video());
                }else {
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e){

                    }
                }
            }
        }
    }


    private void l(){
        FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                .readAtNativeFrameRate()
                .setInput(TaM.getManager().getFirst().getPlayLocation().getAbsolutePath())
                .addOutput(RTMP)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .setFormat("flv")
                .done();
        try {
            FFmpegExecutor executor = new FFmpegExecutor(TaM.getManager().getfFmpeg());
            System.out.println("live "+ TaM.getManager().getFirst().getPlayLocation().getName()+" started");
            lastStart= new Date().getTime();
            TaM.getManager().update();
            executor.createJob(fFmpegBuilder).run();


            boomCount=0;
            TaM.getManager().playFinished();

        }catch (Exception e){
            System.out.println("live boom 1");
            boomCount++;
            try {
                Thread.sleep(3000);
            }catch (InterruptedException e1){

            }
            if (boomCount>=5)tellAlebrije("live boom 2");
        }
    }

    private void l(String location){
        FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                .readAtNativeFrameRate()
                .setInput(location)
                .addOutput(RTMP)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .setFormat("flv")
                .done();
        try {
            FFmpegExecutor executor = new FFmpegExecutor(TaM.getManager().getfFmpeg());
            System.out.println(location+" started");
            executor.createJob(fFmpegBuilder).run();
        }catch (Exception e){
            try {
                Thread.sleep(10000);
            }catch (InterruptedException e1){

            }
            System.out.println("live boom 3");
        }

    }


    void tellAlebrije(String s) {
        //toDO
        try {
            if (TaM.getManager().isNoticeBot()){
                URL myURL = new URL(TaM.getManager().getTG_Bot()+s);
                URLConnection myURLConnection = myURL.openConnection();
                myURLConnection.connect();
                myURLConnection.getInputStream();
                boomCount=-500;
                System.out.println("told");
            }
        }catch (Exception e){
            System.out.println("tell alebrije failed");
        }
    }

}