package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.Country;
import be.ugent.equatic.domain.CountryRepository;
import be.ugent.equatic.exception.CountryNotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Transactional(readOnly = true)
    public List<Country> findAll() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Country findByCode(String code) throws CountryNotFoundException {
        Country country = countryRepository.findByCode(code);

        if (country == null) {
            throw new CountryNotFoundException(code);
        }

        return country;
    }

    @Transactional
    public void save(Country... countries) {
        countryRepository.save(Arrays.asList(countries));
    }

    @Transactional
    public void deleteAll() {
        countryRepository.deleteAll();
    }
}
