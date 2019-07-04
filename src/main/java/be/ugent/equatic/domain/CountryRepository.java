package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {

    List<Country> findAllByOrderByNameAsc();

    @Query("select c from Country c where c.code = ?1 or (c.alternativeCode is not null and c.alternativeCode = ?1)")
    Country findByCode(String code);
}
