package be.ugent.equatic.domain;

import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;

import java.util.List;

/**
 * Repository interface for the <code>Institution</code> domain object.
 * <p>
 * Copied from Thibault Duchateau.
 */
public interface InstitutionDatatablesRepository {

    /**
     * <p>
     * Query used to populate the DataTables that display the list of persons.
     *
     * @param criterias The DataTables criterias used to filter the persons.
     *                  (maxResult, filtering, paging, ...)
     * @return a filtered list of persons.
     */
    List<Institution> findInstitutionsNotVirtualWithDatatablesCriterias(DatatablesCriterias criterias);

    /**
     * <p>
     * Query used to return the number of filtered institutions.
     *
     * @param criterias The DataTables criterias used to filter the persons.
     *                  (maxResult, filtering, paging, ...)
     * @return the number of filtered persons.
     */
    Long getFilteredNotVirtualCount(DatatablesCriterias criterias);

    /**
     * @return the total count of persons.
     */
    Long getTotalNotVirtualCount();
}
