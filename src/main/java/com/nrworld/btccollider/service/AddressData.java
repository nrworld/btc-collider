package com.nrworld.btccollider.service;

import com.google.common.collect.Streams;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.openhft.chronicle.set.ChronicleSet;
import net.openhft.chronicle.set.ChronicleSetBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class AddressData {

    private final ChronicleSet<String> addressSet;

    public AddressData() throws IOException {
        val chronicleFile = new File(System.getProperty("user.home") +
                File.separator + "chronicle.set");
        if (chronicleFile.exists()) {
            addressSet = ChronicleSetBuilder.of(String.class)
                    .averageKeySize(35)
                    .recoverPersistedTo(chronicleFile, false);
            log.info("Addresses size {}", addressSet.size());
            return;
        }
        val path = new File(System.getProperty("user.home") +
                File.separator + "blockchair_bitcoin_addresses_latest.tsv");
        log.info("Loading addresses from {}", path);
        val size = path.length() / (getAvgSize(path) - 5);
        log.info("Entry size is {}", size);
        addressSet = ChronicleSetBuilder.of(String.class).entries(size)
                .averageKeySize(35)
                .createPersistedTo(chronicleFile);
        val iterator = FileUtils.lineIterator(path, "UTF-8");
        Streams.stream(iterator).skip(1).map(ColliderService::toAddressMap)
                .filter(Objects::nonNull)
                .map(AddressMap::getAddress)
                .filter(address -> address.length() <= 35)
                .forEach(addressSet::add);

        log.info("Updated new Addresses, size {}", addressSet.size());
    }

    private long getAvgSize(File path) throws IOException {
        int count = 0;
        List<Integer> lineSize = new ArrayList<>();
        val iterator = FileUtils.lineIterator(path, "UTF-8");
        iterator.nextLine(); // ignore 1st line
        while (iterator.hasNext() && count <= 10) {
            lineSize.add(iterator.nextLine().length());
            count++;
        }
        return lineSize.stream().mapToInt(i -> i).sum() / lineSize.size();
    }

    public Boolean contains(String key) {
        return addressSet.contains(key);
    }

    public void closeSet() {
        addressSet.close();
    }
}
