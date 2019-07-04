package be.ugent.equatic.domain;

import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import org.springframework.stereotype.Repository;
import be.ugent.equatic.util.RepositoryUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * JPA implementation of the {@link InstitutionDatatablesRepository}.
 * <p>
 * Copied from Thibault Duchateau.
 */
@Repository
public class InstitutionDatatablesJpaRepository implements InstitutionDatatablesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Institution> findInstitutionsNotVirtualWithDatatablesCriterias(DatatablesCriterias criterias) {
        StringBuilder queryBuilder = new StringBuilder("SELECT e FROM Institution e");

        /*
         * Step 1: global and individual column filtering
         */
        queryBuilder.append(RepositoryUtil.getFilterQuery(criterias, Collections.singletonList(" e.virtual = false")));

        /*
         * Step 2: sorting
         */
        if (criterias.hasOneSortedColumn()) {
            List<String> orderParams = new ArrayList<>();
            queryBuilder.append(" ORDER BY ");
            for (ColumnDef columnDef : criterias.getSortedColumnDefs()) {
                orderParams.add("e." + columnDef.getName() + " " + columnDef.getSortDirection());
            }

            Iterator<String> itr2 = orderParams.iterator();
            while (itr2.hasNext()) {
                queryBuilder.append(itr2.next());
                if (itr2.hasNext()) {
                    queryBuilder.append(" , ");
                }
            }
        }

        TypedQuery<Institution> query = entityManager.createQuery(queryBuilder.toString(), Institution.class);

        /*
         * Step 3: paging
         */
        query.setFirstResult(criterias.getStart());
        query.setMaxResults(criterias.getLength());

        return query.getResultList();
    }

    @Override
    public Long getFilteredNotVirtualCount(DatatablesCriterias criterias) {
        @SuppressWarnings("JpaQlInspection") Query query = entityManager.createQuery(
                "SELECT e FROM Institution e" + RepositoryUtil.getFilterQuery(criterias,
                        Collections.singletonList(" e.virtual = false")));
        return Long.parseLong(String.valueOf(query.getResultList().size()));
    }

    @Override
    public Long getTotalNotVirtualCount() {
        Query query = entityManager.createQuery("SELECT COUNT(i) FROM Institution i WHERE i.virtual = false");
        return (Long) query.getSingleResult();
    }
}
