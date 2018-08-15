import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Download implements Runnable {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static int newID = 0;
    private String[] links;
    private String storeName;
    private String fullName;
    private String season;
    private String episode;
    private int mode;
    private int[] array;
    private boolean manual = true;
    private ArrayList<Thread> taskArray = new ArrayList<>();

    Download(String links, String storeName, String fullName, int mode, int[] array) {
        this.links = links.split("\\r?\\n");
        this.storeName = storeName;
        this.fullName = fullName;
        this.mode = mode;
        this.array = array;
        this.manual = false;

    }

    Download(String links, String storeName, String fullName, String season, String episode, int mode, int[] array) {
        this.links = links.split("\\r?\\n");
        this.storeName = storeName;
        this.fullName = fullName;
        this.season = season;
        this.episode = episode;
        this.mode = mode;
        this.array = array;
    }

    public static String randomAlphaNumeric(int count) {

        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {

            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());

            builder.append(ALPHA_NUMERIC_STRING.charAt(character));

        }

        return builder.toString();

    }

    @Override
    public void run() {
        for (String link : links) {

            Thread thread = new Thread(() -> dlTask(link));

            try {
                thread.start();
            } catch (Exception e) {
                System.out.println(link + " dl Failed");
            }
            taskArray.add(thread);
        }


        for (Thread t : taskArray) {

            try {
                synchronized (t) {
                    t.wait();
                }

            } catch (InterruptedException e) {
                System.out.println("dl InterruptedException");
            }
        }

        System.out.println("dl finished");
    }

    private void dlTask(String link) {
        String tID = genTaskID();
        ArrayList<Thread> vf = new ArrayList<>();

        File folder = new File(TaM.getManager().getDownload_Folder() + tID + "/");
        try {
            FileUtils.forceMkdir(folder);
            if (link.contains("mega.")) {
                dlMega(link, tID);
            } else {
                dlSb(link, tID);
            }

            System.out.println("download " + link + " done");
            for (File f : folder.listFiles()) {
                if (f.getAbsolutePath().endsWith(".mkv") || f.getAbsolutePath().endsWith(".mp4") || f.getAbsolutePath()
                        .endsWith(".avi") || f.getAbsolutePath().endsWith(".flv") || f.getAbsolutePath().endsWith(".webm")) {
                    if (!manual) {
                        season = dumbJudge(f.getName())[0];
                        episode = dumbJudge(f.getName())[1];
                    }

                    File nf = new File(TaM.getManager().getDownload_Folder() + tID + "/" + f.getName().replaceAll("[^a-zA-Z0-9\\.\\-]", "."));
                    System.out.println();
                    System.out.println(nf.getAbsolutePath());
                    System.out.println();
                    f.renameTo(nf);


                    Burn task = new Burn(tID, nf.getName(), fullName, storeName, season, episode, mode, array);
                    Thread thread = new Thread(task);
                    thread.start();
                    vf.add(thread);
                }
            }

        } catch (Exception e) {

            System.out.println("dl failed");

        }

        for (Thread t : vf) {
            synchronized (t) {
                if (t.isAlive())
                    try {
                        t.wait();
                    } catch (InterruptedException e) {

                    }
            }
        }
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            System.out.println("del failed");
        }

    }

    private void dlMega(String link, String dlID) throws Exception {
        TaM.getManager().getMh().download(link, TaM.getManager().getDownload_Folder() + dlID + "/");

    }

    private void dlSb(String link, String dlID) {

        for (int i = 0; i < TaM.getManager().getSeedbox_contains().length; i++) {

            if (link.contains(TaM.getManager().getSeedbox_contains()[i])) {
                if (!(TaM.getManager().getSeedbox_Uname()[i] + TaM.getManager().getSeedbox_Pswd()[i]).equals(""))
                    Authenticator.setDefault(new CustomAuthenticator(TaM.getManager().getSeedbox_Uname()[i], TaM.getManager().getSeedbox_Pswd()[i]));
                try {
                    FileUtils.copyURLToFile(new URL(link), new File(TaM.getManager().getDownload_Folder() + dlID + "/" + link.split("/")[link.split("/").length - 1]), 10000, 10000);
                    break;
                } catch (Exception e) {
                    System.out.println("boom dl 99");
                }
            }
        }
    }

    String[] dumbJudge(String fileName) {
        Pattern p = Pattern.compile("(.*?)[.\\s][sS](\\d{2})[eE](\\d{2}[abAB]?).*");
        Matcher m = p.matcher(fileName);
        if (m.matches()) return new String[]{m.group(2), m.group(3)};
        return new String[]{randomAlphaNumeric(2), randomAlphaNumeric(2)};
    }

    String genTaskID() {
        newID++;
        return "dl" + newID;
    }

    static class CustomAuthenticator extends Authenticator {
        String uname;
        String passwd;

        CustomAuthenticator(String uname, String passwd) {
            this.uname = uname;
            this.passwd = passwd;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            String username = this.uname;
            String password = this.passwd;
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }

}