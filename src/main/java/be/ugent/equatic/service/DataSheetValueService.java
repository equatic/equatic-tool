package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.ugent.equatic.domain.DataSheetColumn;
import be.ugent.equatic.domain.DataSheetValue;
import be.ugent.equatic.domain.DataSheetValueCode;
import be.ugent.equatic.domain.DataSheetValueRepository;
import be.ugent.equatic.exception.DataSheetValueNotFoundException;

import java.util.List;

@Service
public class DataSheetValueService {

    @Autowired
    private DataSheetValueRepository dataSheetValueRepository;

    @Transactional(readOnly = true)
    public DataSheetValue findByColumnAndValueIgnoreCase(DataSheetColumn column, String value)
            throws DataSheetValueNotFoundException {
        DataSheetValue dataSheetValue = dataSheetValueRepository.findByColumnTypeAndValueIgnoreCase(column.getType(),
                value);

        if (dataSheetValue == null) {
            throw new DataSheetValueNotFoundException(column, value);
        }

        return dataSheetValue;
    }

    @Transactional(readOnly = true)
    public DataSheetValue findByCode(DataSheetValueCode code) throws DataSheetValueNotFoundException {
        List<DataSheetValue> dataSheetValues = dataSheetValueRepository.findByCode(code);

        if (dataSheetValues.isEmpty()) {
            throw new DataSheetValueNotFoundException(code);
        }

        return dataSheetValues.get(0);
    }

    @Transactional(readOnly = true)
    public List<DataSheetValue> findByColumn(DataSheetColumn column) {
        return dataSheetValueRepository.findByColumnType(column.getType());
    }
}
