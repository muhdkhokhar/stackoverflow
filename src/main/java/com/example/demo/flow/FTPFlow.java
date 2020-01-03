package com.example.demo.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by muhdk on 03/01/2020.
 */
@Component
@Slf4j
public class FTPFlow {

    @Autowired
    private IntegrationFlowContext integrationFlowContext;

    @EventListener(ApplicationReadyEvent.class)
    public void setup(){


        integrationFlowContext.registration(flow()).register();

    }
    public IntegrationFlow flow() {

        DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
        defaultFtpSessionFactory.setHost("localhost");
        defaultFtpSessionFactory.setPort(252);
        defaultFtpSessionFactory.setUsername("user");
        defaultFtpSessionFactory.setPassword("password");
        return IntegrationFlows.from(Ftp.inboundAdapter(defaultFtpSessionFactory).preserveTimestamp(true)
                        .localDirectory(new File("D:/tools/input"))
                        .regexFilter("yo.txt")
                        .remoteDirectory("/testing")
                        .deleteRemoteFiles(true),
                e -> e.poller(Pollers.fixedDelay(10, TimeUnit.SECONDS)))
                .transform((GenericTransformer<File, File>) file -> {

                    log.info("Dummy transformer. ");
                    return file;
                })
                .handle(o -> {

                    log.info("history {}", o.getHeaders());
                })
                .get();
    }
}
