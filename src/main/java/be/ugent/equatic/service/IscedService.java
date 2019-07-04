package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.Isced;
import be.ugent.equatic.domain.IscedRepository;
import be.ugent.equatic.exception.IscedNotFoundException;
import be.ugent.equatic.util.BroadIsced;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IscedService {

    @Autowired
    private IscedRepository iscedRepository;

    @Transactional(readOnly = true)
    public Isced findByCode(String iscedCode) throws IscedNotFoundException {
        if (iscedCode.length() == 3) {
            iscedCode = "0" + iscedCode;
        }

        Isced isced = iscedRepository.findByCode(iscedCode);

        if (isced == null) {
            throw new IscedNotFoundException(iscedCode);
        }

        return isced;
    }

    @Transactional(readOnly = true)
    public List<Isced> findAll() {
        return iscedRepository.findAll();
    }

    public Map<String, BroadIsced> getBroadIscedMap() {
        Map<String, BroadIsced> broadIsceds = new HashMap<>();

        for (Isced isced : findAll()) {
            String broadKey = isced.getBroadKey();

            BroadIsced broadIsced;
            if (broadIsceds.containsKey(broadKey)) {
                broadIsced = broadIsceds.get(broadKey);
            } else {
                broadIsced = new BroadIsced();
                broadIsceds.put(broadKey, broadIsced);
            }

            if (isced.isBroadCode()) {
                broadIsced.setIsced(isced);
            } else {
                broadIsced.addNarrowIsced(isced);
            }
        }

        return broadIsceds;
    }
}
