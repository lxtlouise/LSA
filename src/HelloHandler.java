import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HelloHandler extends Thread {
    protected boolean running = true;

    private synchronized boolean isRunning() {
        return this.running;
    }

//    public void run() {
//        while (isRunning()) {
//            while (!Router.helloQueue.isEmpty()) {
//                Packet p = Router.helloQueue.remove();
//                String neighborID = p.srcAddress;
//                int neighborPort = p.srcPort;
//                String routerID = Router.routerID;
//                Router.helloAck.put(neighborID, "11");
//                Packet hello = new Packet();
//                hello.type = 0;
//                hello.srcAddress = routerID;
//                hello.destPort = neighborPort;
//                hello.destAddress = neighborID;
//                hello.lsa = null;
//                hello.cost = (int)System.currentTimeMillis();
//                System.out.println("get hello from " + neighborID);
////                if(Router.lsa.neighbors.containsKey(neighborID)) {
//                    try {
//                        Socket helloSocket = new Socket(InetAddress.getByName(neighborID), UI.routerList.get(neighborID));
//                        ObjectOutputStream out = new ObjectOutputStream(helloSocket.getOutputStream());
//
//                        out.writeObject(hello);
//                        System.out.println("reply hello to " + neighborID);
//                        System.out.println("!!" + Router.helloAck.get(neighborID));
//                        if (Router.helloAck.get(neighborID).contains("10")) {
//                            System.out.println("update hello ack");
//                            Router.helloAck.put(neighborID, "11");
//                        }
////                        else if (Router.helloAck.get(neighborID).equals("00")) {
////                            Router.helloAck.put(neighborID, "10");
////                        }
//                        int sendTime = (int) System.currentTimeMillis();
//                        int count = 0;
////                        while (Router.helloAck.get(neighborID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 30000 && count < 2) {
////                            count++;
////                            Packet resend = new Packet();
////                            resend.type = 0;
////                            resend.srcAddress = routerID;
////                            resend.destPort = neighborPort;
////                            resend.destAddress = neighborID;
////                            resend.lsa = null;
////                            resend.cost = (int) System.currentTimeMillis();
////                            out.writeObject(resend);
////                            System.out.println("no reply resend " + count);
////                        }
//                        helloSocket.close();
////                        if (count == 2 && Router.helloAck.get(neighborID).equals("10")) {
////                            System.out.println(neighborID + " is down!");
////                            Router.helloAck.remove(neighborID);
////                            Router.neighbors.remove(neighborID);
////                            UI.neighbors.get(routerID).remove(neighborID);
////                            Router.lsa.neighbors.remove(neighborID);
////                            Router.LSDB.put(Router.routerID, Router.lsa);
////                            List<String> n = UI.neighbors.get(routerID);
////                            for (int i = 0; i < n.size(); i++) {
////                                String ngID = n.get(i);
////                                int ngPort = UI.routerList.get(ngID);
////                                Packet change = new Packet();
////                                change.type = 1;
////                                change.srcAddress = ngID;
////                                change.destPort = UI.routerList.get(Router.routerID);
////                                change.destAddress = Router.routerID;
////                                change.lsa = Router.lsa;
//////                                Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
////                                change.lsa.sequence++;
////                                Router.lsaSendQueue.add(change);
////                                System.out.println("router down, add change");
////                            }
////
////                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
////                } else {
////                    continue;
////                }
//            }
//
//            try {
//                this.sleep(30000);
//                broadcast();
//            } catch (InterruptedException e) {
//                if(!running){
//                    break;
//                }
//            }
//
//        }
//    }
//
//        public void broadcast() {
//            List<String> n = UI.neighbors.get(Router.routerID);
//            for(int i = 0; i < n.size(); i++) {
//                String neighborID = n.get(i);
//                int neighborPort = UI.routerList.get(neighborID);
//                Packet hello = new Packet();
//                hello.type = 0;
//                hello.destAddress = neighborID;
//                hello.destPort = neighborPort;
//                hello.srcAddress = Router.routerID;
//                hello.lsa = Router.lsa;
//                hello.cost = (int)System.currentTimeMillis();
//                try {
//                    Socket socket = new Socket(InetAddress.getByName(neighborID), neighborPort);
//                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                    out.writeObject(hello);
//                    System.out.println("send hello to " + neighborID);
//                    int count = 0;
//                    int sendTime = (int)System.currentTimeMillis();
//                    Router.helloAck.put(neighborID, "10:" + sendTime);
//                    System.out.println("set hello ack to 10");
////                    while (Router.helloAck.get(neighborID).equals("10") && (int) (System.currentTimeMillis() - sendTime) > 30000 && count < 2) {
////                        count++;
////                        Packet resend = new Packet();
////                        resend.type = 0;
////                        resend.srcAddress = Router.routerID;
////                        resend.destPort = neighborPort;
////                        resend.destAddress = neighborID;
////                        resend.lsa = null;
////                        resend.cost = (int)System.currentTimeMillis();
////                        out.writeObject(resend);
////                        System.out.println("resend");
////                    }
//                    socket.close();
////                    if(count == 2 && Router.helloAck.get(neighborID).equals("10")) {
////                        System.out.println(neighborID + " is down!");
////                        Router.helloAck.remove(neighborID);
////                        Router.neighbors.remove(neighborID);
////                        UI.neighbors.get(Router.routerID).remove(neighborID);
////                        Router.lsa.neighbors.remove(neighborID);
////                        Router.LSDB.put(Router.routerID, Router.lsa);
////                        for(int j = 0; j < n.size(); j++) {
////                            String ngID = n.get(j);
////                            int ngPort = UI.routerList.get(ngID);
////                            Packet change = new Packet();
////                            change.type = 1;
////                            change.srcAddress = Router.routerID;
////                            change.destPort = UI.routerList.get(ngID);
////                            change.destAddress = ngID;
////                            change.lsa = Router.lsa;
//////                            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
////                            change.lsa.sequence++;
////                            Router.lsaSendQueue.add(change);
////                            System.out.println("router down, add change");
////                        }
////
////                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            for(Map.Entry<String, String> entry : Router.helloAck.entrySet()) {
//                System.out.println("check aliveness");
//                int count = 1;
//                String neighborID = entry.getKey();
//                String status = entry.getValue();
//                String[] temp = null;
//                if(status.contains("10")){
//                    temp = status.split(":");
//                }
//                int time = (int) System.currentTimeMillis() - Integer.parseInt(temp[1]);
//                while(status.contains("10") && count < 2 && time > 10000) {
//                    System.out.println("need resend hello");
//                    count++;
//                    Packet resend = new Packet();
//                    resend.type = 0;
//                    resend.srcAddress = Router.routerID;
//                    resend.destPort = UI.routerList.get(neighborID);
//                    resend.destAddress = neighborID;
//                    resend.lsa = null;
//                    resend.cost = (int)System.currentTimeMillis();
//
//                    try {
//                        Socket socket = new Socket(InetAddress.getByName(neighborID), resend.destPort);
//                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                        out.writeObject(resend);
//                        status = "10:"+ resend.cost;
//                        System.out.println("resend hello " + count);
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                if(count == 2 && Router.helloAck.get(neighborID).contains("10")) {
//                        System.out.println(neighborID + " is down!");
//                        Router.helloAck.remove(neighborID);
//                        Router.neighbors.remove(neighborID);
//                        UI.neighbors.get(Router.routerID).remove(neighborID);
//                        Router.lsa.neighbors.remove(neighborID);
//                        Router.LSDB.put(Router.routerID, Router.lsa);
//                        for(int j = 0; j < n.size(); j++) {
//                            String ngID = n.get(j);
//                            int ngPort = UI.routerList.get(ngID);
//                            Packet change = new Packet();
//                            change.type = 1;
//                            change.srcAddress = Router.routerID;
//                            change.destPort = UI.routerList.get(ngID);
//                            change.destAddress = ngID;
//                            change.lsa = Router.lsa;
////                            Router.lsa.sequence = Router.LSDB.get(Router.routerID).sequence++;
//                            change.lsa.sequence++;
//                            Router.lsaSendQueue.add(change);
//                            System.out.println("router down, add change");
//                        }
//
//                    }
//            }
//        }




    public void run() {
        while (isRunning()) {
            while(!Router.helloQueue.isEmpty()) {
                Packet p = Router.helloQueue.remove();
                String neighborID = p.srcAddress;
                int neighborPort = UI.routerList.get(neighborID);
                Packet helloAck = new Packet();
                helloAck.type = 5;
                helloAck.destPort = neighborPort;
                helloAck.destAddress = neighborID;
                helloAck.srcAddress = Router.routerID;
                helloAck.cost = (int)System.currentTimeMillis();
                Router.helloAckQueue.add(helloAck);
                System.out.println("get hello from: " + neighborID);
                System.out.println("add hello ack to the hello ack queue");
            }


            try {
                this.sleep(50000);
                broadcast();
            } catch (InterruptedException e) {
                if(!running){
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void broadcast() throws IOException {
        List<String> ng = UI.neighbors.get(Router.routerID);
        for (int i = 0; i < ng.size(); i++) {
            Packet hello = new Packet();
            String neighborID = ng.get(i);
            hello.type = 0;
            hello.srcAddress = Router.routerID;
            hello.destAddress = neighborID;
            hello.destPort = UI.routerList.get(neighborID);
            hello.cost = (int) System.currentTimeMillis();
            HelloNode hn = Router.helloAck.get(neighborID);
            if (!hn.ack.equals("pending")) {
                hn.sendTime = hello.cost;
                hn.counter++;
                hn.ack = "pending";
                Socket socket = new Socket(InetAddress.getByName(neighborID), hello.destPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(hello);
                System.out.println("broadcast hello to: " + neighborID);
                socket.close();
            }

        }
    }

//    public void checkAlive() throws IOException {
//        System.out.println("check aliveness");
//        int timeout = 30000;
//        for(Map.Entry<String, HelloNode> entry : Router.helloAck.entrySet()){
//            String neighborID = entry.getKey();
//            HelloNode hn = entry.getValue();
//            int time = (int)System.currentTimeMillis() - hn.sendTime;
//            Socket socket = null;
//            while (hn.counter < 3 && hn.ack == false && time > timeout) {
//                Packet resend = new Packet();
//                resend.type = 0;
//                resend.srcAddress = Router.routerID;
//                resend.destAddress = neighborID;
//                resend.destPort = UI.routerList.get(neighborID);
//                resend.cost = (int) System.currentTimeMillis();
//                hn.sendTime = resend.cost;
//                hn.counter++;
//                hn.ack = false;
//                socket = new Socket(InetAddress.getByName(neighborID), resend.destPort);
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                out.writeObject(resend);
//            }
//            socket.close();
//            if (hn.counter == 3 && hn.ack == false) {
//                System.out.println ("neighbor is down " + neighborID);
//                Router.helloAck.remove(neighborID);
//                Router.neighbors.remove(neighborID);
//                UI.neighbors.get(Router.routerID).remove(neighborID);
//                Router.lsa.neighbors.remove(neighborID);
//                Router.LSDB.put(Router.routerID, Router.lsa);
//                for(int j = 0; j < UI.neighbors.get(Router.routerID).size(); j++) {
//                    String ngID = UI.neighbors.get(Router.routerID).get(j);
//                    int ngPort = UI.routerList.get(ngID);
//                    Packet change = new Packet();
//                    change.type = 1;
//                    change.srcAddress = Router.routerID;
//                    change.destPort = UI.routerList.get(ngID);
//                    change.destAddress = ngID;
//                    change.lsa = Router.lsa;
//                    change.lsa.sequence++;
//                    Router.lsaSendQueue.add(change);
//                    System.out.println("router down, broadcast change using lsa");
//                }
//            }
//        }
//    }


    public synchronized void shutdown(){
        this.running = false;
        interrupt();
    }

    public synchronized void restart() {
        this.running = true;
    }

}
