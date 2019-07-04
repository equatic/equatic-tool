package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IscedRepository extends JpaRepository<Isced, String> {

    Isced findByCode(String iscedCode);
}
