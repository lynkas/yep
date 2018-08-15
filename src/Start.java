import java.util.Scanner;

public class Start {
    public static void main(String[] args) {
        Server server = new Server();
        Thread t = new Thread(server);
        t.start();

        Live live = new Live();
        Thread l = new Thread(live);
        l.start();

        Scanner scanner = new Scanner(System.in);
        while (true){
            String com =scanner.nextLine();
            if (com.toLowerCase().equals("restart server")){
                if (t.isAlive())t.stop();
                t = new Thread(server);
                t.start();
            }
            else if (com.toLowerCase().equals("next live")){
                l.stop();
                TaM.getManager().playFinished();
                l = new Thread(live);
                l.start();
            }else {
                System.out.println("try other");
            }




        }
    }
}
