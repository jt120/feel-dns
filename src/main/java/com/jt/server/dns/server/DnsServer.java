package com.jt.server.dns.server;

import com.jt.server.dns.service.LocalNameService;
import com.jt.server.dns.worker.DnsQueryWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;

/**
 * since 2016/6/24.
 */
@Service
public class DnsServer implements Runnable, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DnsServer.class);

    public static transient boolean start = true;
    public static final int dns_port = 53;
    public static final int dns_pack_size = 512;

    @Resource
    private LocalNameService localNameService;
    @Resource
    private ExecutorService executorService;

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.submit(this);
    }

    @Override
    public void destroy() throws Exception {
        this.start = false;
    }

    public void start() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(dns_port);
            while (start) {
                log.warn("start dns proxy...");
                try {
                    byte[] in = new byte[dns_pack_size];
                    DatagramPacket clientP = new DatagramPacket(in, in.length);
                    socket.receive(clientP);
                    DnsQueryWorker task = new DnsQueryWorker(clientP, socket);
                    task.setLocalNameService(localNameService);
                    executorService.submit(task);
                } catch (Exception e) {
                    log.warn("query dns fail", e);
                }
            }

        } catch (Exception e) {
            log.warn("start dns server fail", e);
        } finally {
            if (socket != null) {
                socket.close();
            }
            executorService.shutdown();
        }
    }

    @Override
    public void run() {
        start();
    }
}
