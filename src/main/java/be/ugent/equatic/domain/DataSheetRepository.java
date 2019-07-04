package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSheetRepository extends JpaRepository<DataSheet, String> {

    DataSheet findByCode(DataSheetCode dataSheetCode);
}
