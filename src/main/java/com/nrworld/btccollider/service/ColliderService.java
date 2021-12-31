package com.nrworld.btccollider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@EnableScheduling
public class ColliderService {

    private final AddressData addressData = new AddressData();
    private final ObjectMapper objectMapper = new JsonMapper();
    private final Integer threads = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(threads);
    private final BlockingQueue<ECKey> blockingQueue = new ArrayBlockingQueue<>(500_000);
    private final AtomicInteger counter = new AtomicInteger(0);

    public ColliderService() throws IOException {
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 4000)
    void logRate() {
        log.info("Processed per second {}, qsize {}", counter.getAndSet(0), blockingQueue.size());
    }

    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 2000)
    void run() {
        for (int i = 0; i < threads - 1; i++) {
            executorService.submit(createProducer());
        }
        executorService.submit(createConsumer());
    }

    Runnable createProducer() {
        return () -> {
            while (true) {
                blockingQueue.offer(new ECKey());
            }
        };
    }

    Runnable createConsumer() {
        return () -> {
            while (true) {
                try {
                    runTest(blockingQueue.poll(10, TimeUnit.SECONDS));
                    counter.incrementAndGet();
                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                }
            }
        };
    }

    private int runTest(ECKey thisKey) throws IOException {
        // Create an address object based on the address format
        val pubAddress = Address.fromKey(MainNetParams.get(), thisKey, Script.ScriptType.P2PKH);

        if (addressData.contains(pubAddress.toString())) {
            log.info("YAAY - matched key {}", pubAddress.toString());
            BtcKey btcKey = BtcKey.builder()
                    .fromKey(pubAddress.toString())
                    .privateKeyAsHex(thisKey.getPrivateKeyAsHex())
                    .publicKeyAsHex(thisKey.getPublicKeyAsHex())
                    .privateKeyAsWiF(thisKey.getPrivateKeyAsWiF(MainNetParams.get()))
                    .build();
            final String key = objectMapper.writeValueAsString(btcKey);

            FileWriter thisFile = new FileWriter(System.getProperty("user.home") +
                    File.separator + "bitcoin_Collider_Match_" + System.currentTimeMillis() + ".txt");
            BufferedWriter writer = new BufferedWriter(thisFile);
            writer.write(key);
            writer.close();
        }
        return 1;
    }

    @PreDestroy
    void closeChronicleMap() {
        log.info("Closing chronicle set");
        addressData.closeSet();
    }

    public static AddressMap toAddressMap(String line) {
        final String[] split = line.split("\t");
        if (split.length < 2) return null;
        return AddressMap.builder()
                .address(split[0])
                .balance(Double.parseDouble(split[1]))
                .build();
    }
}
