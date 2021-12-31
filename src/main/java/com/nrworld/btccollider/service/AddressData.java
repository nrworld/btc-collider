package com.nrworld.btccollider.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.openhft.chronicle.set.ChronicleSet;
import net.openhft.chronicle.set.ChronicleSetBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        val size = IOUtils.readLines(new FileReader(path))
                .stream().skip(1).map(ColliderService::toAddressMap)
                .filter(Objects::nonNull)
                .filter(addressMap -> addressMap.getBalance() > 7777777 && addressMap.getAddress().length() <= 35)
                .count();

        addressSet = ChronicleSetBuilder.of(String.class).entries(size)
                .averageKeySize(35)
                .createPersistedTo(chronicleFile);
        IOUtils.readLines(new FileReader(path))
                .stream().skip(1).map(ColliderService::toAddressMap)
                .filter(Objects::nonNull)
                .filter(addressMap -> addressMap.getBalance() > 7777777 && addressMap.getAddress().length() <= 35)
                .map(AddressMap::getAddress)
                .forEach(addressSet::add);

        log.info("Updated new Addresses, size {}", addressSet.size());
    }

    public Boolean contains(String key) {
        return addressSet.contains(key);
    }

    public void closeSet() {
        addressSet.close();
    }
}
