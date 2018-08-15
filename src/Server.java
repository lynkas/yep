import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;
import org.apache.commons.io.FileUtils;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;

public class Server implements Runnable{

    @Override
    public void run(){
        WebServer webServer = WebServers.createWebServer(TaM.getManager().getServerPort())
                .add(new CtrlHandler())
                .add(new GetHandler())
                .add(new LibHandler())
                .add(new PlayHandler())
                .add(new TextHandler())
                .add(new StaticFileHandler("./option"));
        try {
            webServer.start().get();
        }catch (Exception e){
            System.out.println("server boom");;
        }
        System.out.println("Listening on " + webServer.getUri());
    }
}


class CtrlHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest httpRequest, org.webbitserver.HttpResponse httpResponse, HttpControl httpControl) {
        try {
            if (httpRequest.uri().startsWith("/ctrl")) {
                System.out.println(httpRequest.uri());
                System.out.println("ok, is ctrl");

                httpResponse.header("Content-type", "text/html")
                        .content(httpRequest.body())
                        .end();
                System.out.println(httpRequest.body());


            } else {
                httpControl.nextHandler();
            }
        } catch (Exception e) {
            System.out.println("Err 1");
        }
    }
}

class TextHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest httpRequest, org.webbitserver.HttpResponse httpResponse, HttpControl httpControl) {
        try {
            String para ;
            if (httpRequest.uri().equals("/text")) {
                System.out.println();
                System.out.println("ok, is text");
                para=URLDecoder.decode(httpRequest.body(),"UTF-8");
                System.out.println(para);

                HashMap<String, String> p = paramToMap(para);
                if (p.containsKey("fun")) {
                    if (p.get("fun").equals("1")) {
                        System.out.println(1);
                        if (p.containsKey("auto")) {
                            int[] array;
                            try {
                                array = Arrays.stream(p.get("array").split(","))
                                        .mapToInt(Integer::parseInt)
                                        .toArray();

                            } catch (Exception e) {
                                array = new int[0];
                            }
                            try {
                                Download download = new Download(p.get("link"), p.get("short"), p.get("series"), Integer.parseInt(p.get("pe")), array);
                                Thread t = new Thread(download);
                                t.start();
                                try {
                                    httpResponse.header("Content-type", "text/html")
                                            .content(FileUtils.readFileToString(new File("./option/index.html"), "UTF-8"))
                                            .end();
                                } catch (IOException e) {
                                    httpResponse.header("Content-type", "text/html")
                                            .content("read file err")
                                            .end();
                                }
                                return;
                            } catch (Exception e) {
                                System.out.println(p.get("link") + " failed");
                                httpResponse.header("Content-type", "text/html")
                                        .content(p.get("link") + " failed")
                                        .end();
                            }

                        } else {
                            int[] array;
                            try {
                                array = Arrays.stream(p.get("array").split(","))
                                        .mapToInt(Integer::parseInt)
                                        .toArray();
                            } catch (Exception e) {
                                array = new int[0];
                            }
                            try {
                                Download download = new Download(p.get("link"), p.get("short"), p.get("series"), p.get("season"), p.get("episode"), Integer.parseInt(p.get("pe")), array);
                                Thread t = new Thread(download);
                                t.start();

                                try {
                                    httpResponse.header("Content-type", "text/html")
                                            .content(FileUtils.readFileToString(new File("./option/index.html"), "UTF-8"))
                                            .end();
                                } catch (IOException e) {
                                    httpResponse.header("Content-type", "text/html")
                                            .content("read file err")
                                            .end();
                                }
                                return;

                            } catch (Exception e) {
                                System.out.println("download " + p.get("link") + " failed");
                                httpResponse.header("Content-type", "text/html")
                                        .content(p.get("link") + " failed")
                                        .end();
                            }
                        }

                    }

                    if (p.get("fun").equals("2")) {
                        System.out.println(2);
                        int[] array;
                        try {
                            array = Arrays.stream(p.get("array").split(","))
                                    .mapToInt(Integer::parseInt)
                                    .toArray();
                        } catch (Exception e) {
                            array = new int[0];
                        }

                        for (int i : array) {
                            try {
                                try {
                                    if (p.containsKey("df")) TaM.getManager().getById(i).rmFile();
                                } catch (Exception e) {

                                }
                                TaM.getManager().removeFL(i);
                                httpResponse.header("Content-type", "text/html")
                                        .content("rmed " + i)
                                        .end();
                            } catch (Exception e) {
                                httpResponse.header("Content-type", "text/html")
                                        .content("rm" + i + " failed")
                                        .end();
                            }

                        }
                        return;
                    }

                    if (p.get("fun").equals("3")) {
                        System.out.println(3);
                        try {
                            Episode episode = new Episode(new Series(p.get("short"), p.get("series")), p.get("season"), p.get("episode"));
                            if (!episode.getPlayLocation().exists())throw new FileNotFoundException();
                            TaM.getManager().add2L(episode);
                            httpResponse.header("Content-type", "text/html")
                                    .content("add" + episode)
                                    .end();
                            System.out.println(true);
                        } catch (Exception e) {
                            System.out.println(false);
                            httpResponse.header("Content-type", "text/html")
                                    .content("rec failed")
                                    .end();
                        }
                        return;
                    }
                    if (p.get("fun").equals("4")) {
                        try {

                            TaM.getManager().changeSE(Integer.parseInt(p.get("array").replaceAll(",", "")),p.get("season").equals("")?TaM.getManager().getById(Integer.parseInt(p.get("array").replaceAll(",", ""))).getSeason():p.get("season"),p.get("episode").equals("")?TaM.getManager().getById(Integer.parseInt(p.get("array").replaceAll(",", ""))).getEpisode():p.get("episode"));

                            try {

                                httpResponse.header("Content-type", "text/html")
                                        .content(FileUtils.readFileToString(new File("./option/index.html"), "UTF-8"))
                                        .end();
                            } catch (IOException e) {
                                httpResponse.header("Content-type", "text/html")
                                        .content("read file err")
                                        .end();
                            }
                        } catch (Exception e) {
                            httpResponse.header("Content-type", "text/html")
                                    .content("change failed")
                                    .end();
                        }
                        return;
                    }


                    if (p.get("fun").equals("5")) {
                        int[] array;
                        try {
                            array = Arrays.stream(p.get("array").split(","))
                                    .mapToInt(Integer::parseInt)
                                    .toArray();
                        } catch (Exception e) {
                            array = new int[0];
                        }

                        for (int i : array) {
                            try {
                                TaM.getManager().getPlaying().add(TaM.getManager().getById(i));
                            } catch (Exception e) {
                                System.out.println("add " + i + " to lib failed");
                            }

                        }
                        try {
                            httpResponse.header("Content-type", "text/html")
                                    .content(FileUtils.readFileToString(new File("./option/index.html"), "UTF-8"))
                                    .end();
                        } catch (IOException e) {
                            httpResponse.header("Content-type", "text/html")
                                    .content("read file err")
                                    .end();
                        }
                        return;
                    }

                }
                httpResponse.header("Content-type", "text/html")
                        .content("write function")
                        .end();
                System.out.println(httpRequest.body());
//                dealPara(paramToMap(httpRequest.body()));


            } else {
                httpControl.nextHandler();
            }
        } catch (Exception e) {
            System.out.println("Err 1");
        }
    }

    public static HashMap<String, String> paramToMap(String paramStr) {
        String[] params = paramStr.split("&");
        for (int i=0;i<params.length;i++){
            try {
                params[i]=URLDecoder.decode(params[i],"UTF-8");
            }catch (UnsupportedEncodingException e){
                System.out.println("dk utf8");
            }

        }
        HashMap<String, String> resMap = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            String[] param = params[i].split("=");
            if (param.length >= 2) {
                String key = param[0];
                String value = param[1];
                for (int j = 2; j < param.length; j++) {
                    value += "=" + param[j];
                }
                resMap.put(key, value);
            }
        }
        return resMap;
    }

}


class PlayHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest httpRequest, org.webbitserver.HttpResponse httpResponse, HttpControl httpControl) {
        try {
            if (httpRequest.uri().equals("/play")) {
                System.out.println("ok, is play");
                try {
                    int[] newPlay = Arrays.stream(httpRequest.body().split(","))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    System.out.println(Arrays.toString(newPlay));
                    TaM.getManager().newPlayList(newPlay);

                    httpResponse.header("Content-type", "text/html")
                            .content("to " + httpRequest.body())
                            .end();

                } catch (NumberFormatException e) {
                    httpResponse.header("Content-type", "text/html")
                            .content("format error")
                            .end();
                }

            } else {
                httpControl.nextHandler();
            }
        } catch (Exception e) {
            System.out.println("Err 1");
        }
    }
}

class LibHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest httpRequest, org.webbitserver.HttpResponse httpResponse, HttpControl httpControl) {
        try {
            if (httpRequest.uri().equals("/lib")) {
                System.out.println("ok, is lib");
                StringBuilder sb = new StringBuilder();
                for (String s : TaM.getManager().getLibrary().keySet()){
                    for (Episode i : TaM.getManager().getLibrary().get(s)){
                        sb.append(i.genDiv());
                        System.out.println(i.genDiv());
                    }
                }
                httpResponse.header("Content-type", "text/html")
                        .content(sb.toString())
                        .end();

            } else {
                httpControl.nextHandler();
            }
        } catch (Exception e) {
            System.out.println("Err 1");
        }
    }
}


    class GetHandler implements HttpHandler {
        @Override
        public void handleHttpRequest(HttpRequest httpRequest, org.webbitserver.HttpResponse httpResponse, HttpControl httpControl) {
            try {
                if (httpRequest.uri().equals("/get")) {
                    System.out.println("ok, is get");
                    StringBuilder sb = new StringBuilder();
                    for (Episode e :TaM.getManager().getPlaying()){
                        sb.append(e.genDiv());
                        System.out.println(e);
                    }

                    String all = sb.toString();

                    httpResponse.header("Content-type", "text/html")
                            .content(all)
                            .end();

                } else {
                    httpControl.nextHandler();
                }
            } catch (Exception e) {
                System.out.println("Err 1");
            }
        }




}
