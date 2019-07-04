package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataSheetValueRepository extends JpaRepository<DataSheetValue, Long> {

    DataSheetValue findByColumnTypeAndValueIgnoreCase(DataSheetColumnType columnType, String value);

    List<DataSheetValue> findByColumnType(DataSheetColumnType type);

    List<DataSheetValue> findByCode(DataSheetValueCode code);
}
