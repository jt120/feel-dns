package com.jt.server.dns;

import org.junit.Test;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Date;

/**
 * since 2016/6/24.
 */
public class DnsTest {


    @Test
    public void testUdpClient() throws Exception {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(0);
            InetAddress host = InetAddress.getByName("127.0.0.1");
            DatagramPacket req = new DatagramPacket(new byte[1], 1, host, 8888);
            byte[] data = new byte[1024];
            DatagramPacket resp = new DatagramPacket(data, data.length);
            socket.send(req);
            socket.receive(resp);

            String daytime = new String(resp.getData(), 0, resp.getLength(), "US-ASCII");
            System.out.println(daytime);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Test
    public void testUdpServer() throws Exception {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(8888);
            DatagramPacket req = new DatagramPacket(new byte[1024], 1024);
            socket.receive(req);

            String daytime = new Date().toString();
            byte[] data = daytime.getBytes("US-ASCII");
            DatagramPacket resp = new DatagramPacket(data, data.length, req.getAddress(), req.getPort());
            socket.send(resp);
            System.out.println("send resp " + req.getAddress() + " " + daytime);
        } catch (Exception e) {

        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Test
    public void testProxy() throws Exception {
        DatagramSocket socket = null;
        DatagramSocket proxy = null;
        int bufferSize = 8192;
        byte[] buf = new byte[512];
        String charset = "US-ASCII";
        try {
            socket = new DatagramSocket(53);
            while (true) {
                DatagramPacket query = new DatagramPacket(buf, buf.length);
                socket.receive(query);
                System.out.println("local receive " + new String(query.getData(), charset));


                proxy = new DatagramSocket(0);
                proxy.connect(InetAddress.getByName("8.8.8.8"), 53);
                proxy.send(query);

                DatagramPacket incoming
                        = new DatagramPacket(new byte[bufferSize], bufferSize);

                proxy.receive(incoming);
                System.out.println("8.8.8.8 resp " + new String(incoming.getData(), charset));
                socket.send(incoming);
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    @Test
    public void testProxyWithDns() throws Exception {
        DatagramSocket socket = null;
        //SimpleResolver resolver = new SimpleResolver("192.168.101.13");
        try {
            socket = new DatagramSocket(53);
            socket.setSoTimeout(10000);
            while (true) {
                System.out.println("start dns proxy...");
                try {
                    queryDns(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } finally {
            //if (socket != null) {
            //    socket.close();
            //}
        }
    }

    private void queryDns(DatagramSocket socket) throws IOException {
        byte[] in = new byte[512];
        DatagramPacket clientP = new DatagramPacket(in, in.length);
        socket.receive(clientP);
        SocketAddress clientSocketAddress = clientP.getSocketAddress();

        Message query = new Message(in);
        Record question = query.getQuestion();
        //clients1.google.com.	0	IN	A
        System.out.println("question " + question + " " + clientP.getAddress() + ":" + clientP.getPort());

        //Lookup l = new Lookup(question.getName(), question.getType());

        //l.setResolver(resolver);
        //l.run();

        //Record[] answers = l.getAnswers();
        //byte[] out = new byte[512];
        //DatagramPacket proxyP = new DatagramPacket(out, out.length);
        String ip = "192.168.101.13";
        clientP.setAddress(InetAddress.getByName(ip));
        clientP.setPort(53);
        DatagramSocket send = new DatagramSocket();
        send.send(clientP);


        send.receive(clientP);

        //http://stackoverflow.com/questions/32769258/dns-proxy-server-using-dnsjava
        Message resp = new Message(clientP.getData());
        System.out.println("answer " + resp.getHeader() + "," + resp.getQuestion() + "," +
                "" + resp.getSectionArray(Section.ANSWER));
        //resp.getHeader().setFlag(Flags.QR);
        //if (query.getHeader().getFlag(Flags.RD)) {
        //    resp.getHeader().setFlag(Flags.RD);
        //}
        //Record record = new RecordB
        // System.out.println("answer: " + record);
        //        resp.addRecord(record, Section.ANSWER);
        //for (Record record : answers) {
        //answer: clients-china.l.google.com.	179	IN	A	74.125.23.139


        //}

        byte[] b = resp.toWire();
        DatagramPacket ansP = new DatagramPacket(b, b.length, clientSocketAddress);
        System.out.println("return to client");
        //if (socket == null) {
        //    socket = new DatagramSocket(53);
        //}
        socket.send(ansP);
    }
}
