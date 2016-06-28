package com.jt.server.dns.worker;

import com.google.common.base.Stopwatch;
import com.jt.server.dns.server.DnsServer;
import com.jt.server.dns.data.ZoneData;
import com.jt.server.dns.service.LocalNameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * since 2016/6/25.
 */
public class DnsQueryWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DnsQueryWorker.class);

    private DatagramPacket in;
    private DatagramSocket socket;
    private LocalNameService localNameService;
    private static long ttl = 64;

    public DnsQueryWorker(DatagramPacket in, DatagramSocket socket) {
        this.in = in;
        this.socket = socket;
    }

    public LocalNameService getLocalNameService() {
        return localNameService;
    }

    public void setLocalNameService(LocalNameService localNameService) {
        this.localNameService = localNameService;
    }

    @Override
    public void run() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();

            SocketAddress clientSocketAddress = in.getSocketAddress();
            Message query = new Message(in.getData());
            Record question = query.getQuestion();
            //clients1.google.com.	0	IN	A
            log.info("dns question {}", question);
            String name = question.getName().toString();
            log.info("name: {}", name);//p3p.sogou.com.
            ZoneData.show();
            String cip = localNameService.getIp(name.substring(0, name.length()-1));
            Message resp = null;
            if (cip != null) {
                //拦截成功了
                Record record = buildRecord(question.getType(), question.getName(), question.getDClass(), cip);
                log.info("match local config host, record {}", record);
                query.addRecord(record, Section.ANSWER);
                resp = query;
            } else {
                log.info("forward query");
                forward(in);
                resp = new Message(in.getData());
            }

            log.info("dns answer {} {} {}", resp.getHeader(), resp.getQuestion(), resp.getSectionArray(Section.ANSWER));
            byte[] b = resp.toWire();

            in.setData(b);
            in.setSocketAddress(clientSocketAddress);
            socket.send(in);
            log.info("cost {}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Record buildRecord(int type, Name name, int dclass, String answer) throws UnknownHostException, TextParseException {
		switch (type) {
		case Type.A:
			return new ARecord(name, dclass, ttl, Address.getByAddress(answer));
		case Type.MX:
			return new MXRecord(name, dclass, ttl, 10, new Name(answer));
		case Type.PTR:
			return new PTRRecord(name, dclass, ttl, new Name(answer));
		case Type.CNAME:
			return new CNAMERecord(name, dclass, ttl, new Name(answer));
		default:
			throw new IllegalStateException("type " + Type.string(type)
					+ " is not supported ");
		}
	}

      private void forward(DatagramPacket clientP) throws IOException {
        String ip = "192.168.101.13";
        clientP.setAddress(InetAddress.getByName(ip));
        clientP.setPort(DnsServer.dns_port);
        DatagramSocket send = new DatagramSocket();
        send.send(clientP);
        send.receive(clientP);
    }

}
