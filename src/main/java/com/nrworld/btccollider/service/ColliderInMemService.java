package com.nrworld.btccollider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
//@Service
//@EnableScheduling
public class ColliderInMemService {

    private final ObjectMapper objectMapper = new JsonMapper();
    private final Set<String> addresses = new HashSet<>();
    private final Integer threads = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(threads);
    private final BlockingQueue<ECKey> blockingQueue = new ArrayBlockingQueue<>(500_000);
    private final AtomicInteger counter = new AtomicInteger(0);

    @PostConstruct
    void init() throws IOException {
        loadAddress(System.getProperty("user.home") + File.separator +
                "blockchair_bitcoin_addresses_latest.tsv");
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

        if (addresses.contains(pubAddress.toString())) {
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

    private void loadAddress(String path) throws IOException {
        log.info("Loading addresses from {}", path);
        val newAddresses = IOUtils.readLines(new FileReader(path))
                .parallelStream().skip(1).map(ColliderInMemService::toAddressMap)
                .filter(Objects::nonNull)
                .filter(addressMap -> addressMap.getBalance() > 7777777 && addressMap.getAddress().length() <= 35)
                .map(AddressMap::getAddress)
                .collect(Collectors.toSet());
        if (newAddresses.size() > addresses.size()) {
            addresses.addAll(newAddresses);
            log.info("Updated new Addresses, size {}", addresses.size());
        }
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
