package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataSheetUploadRepository extends JpaRepository<DataSheetUpload, Long> {

    DataSheetUpload findById(Long uploadId);

    List<DataSheetUpload> findByDataSheetCode(DataSheetCode dataSheetCode);

    List<DataSheetUpload> findByInstitutionAndDataSheetCodeIn(Institution institution, DataSheetCode[] dataSheetCodes);

    List<DataSheetUpload> findByDataSheetCodeIn(DataSheetCode[] dataSheetCodes);
}
