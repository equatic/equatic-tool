package be.ugent.equatic.util;

import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility methods used to build query.
 * <p>
 * Copied from Thibault Duchateau.
 */
public class RepositoryUtil {

    public static StringBuilder getFilterQuery(DatatablesCriterias criterias, List<String> additionalFilteringParams) {
        StringBuilder queryBuilder = new StringBuilder();
        List<String> paramList = new ArrayList<>();

        /*
         * Step 0: additional filtering
         */
        if (!additionalFilteringParams.isEmpty()) {
            queryBuilder.append(" WHERE ");

            for (String additionalFilteringParam : additionalFilteringParams) {
                queryBuilder.append(additionalFilteringParam);
                queryBuilder.append(" AND ");
            }

            queryBuilder.append("( 1 = 1 ");
        }

        /*
         * Step 1.1: global filtering
         */
        if (StringUtils.isNotBlank(criterias.getSearch()) && criterias.hasOneSearchableColumn()) {
            if (additionalFilteringParams.isEmpty()) {
                queryBuilder.append(" WHERE ");
            } else {
                queryBuilder.append(" AND ");
            }

            for (ColumnDef columnDef : criterias.getColumnDefs()) {
                if (columnDef.isSearchable() && StringUtils.isBlank(columnDef.getSearch())) {
                    paramList.add(" LOWER(e." + columnDef.getName()
                            + ") LIKE '%?%'".replace("?", criterias.getSearch().toLowerCase()));
                }
            }

            Iterator<String> itr = paramList.iterator();
            while (itr.hasNext()) {
                queryBuilder.append(itr.next());
                if (itr.hasNext()) {
                    queryBuilder.append(" OR ");
                }
            }
        }

        /*
         * Step 1.2: individual column filtering
         */
        if (criterias.hasOneSearchableColumn() && criterias.hasOneFilteredColumn()) {
            paramList = new ArrayList<>();

            if (!queryBuilder.toString().contains("WHERE")) {
                queryBuilder.append(" WHERE ");
            } else {
                queryBuilder.append(" AND ");
            }

            for (ColumnDef columnDef : criterias.getColumnDefs()) {
                if (columnDef.isSearchable()) {
                    if (StringUtils.isNotBlank(columnDef.getSearchFrom())) {
                        if (columnDef.getName().toUpperCase().endsWith("DATE")) {
                            paramList.add("e." + columnDef.getName() + " >= '" + columnDef.getSearchFrom() + "'");
                        } else {
                            paramList.add("e." + columnDef.getName() + " >= " + columnDef.getSearchFrom());
                        }
                    }

                    if (StringUtils.isNotBlank(columnDef.getSearchTo())) {
                        if (columnDef.getName().toUpperCase().endsWith("DATE")) {
                            paramList.add("e." + columnDef.getName() + " < '" + columnDef.getSearchTo() + "'");
                        } else {
                            paramList.add("e." + columnDef.getName() + " < " + columnDef.getSearchTo());
                        }
                    }

                    if (StringUtils.isNotBlank(columnDef.getSearch())) {
                        paramList.add(" LOWER(e." + columnDef.getName()
                                + ") LIKE '%?%'".replace("?", columnDef.getSearch().toLowerCase()));
                    }
                }
            }

            Iterator<String> itr = paramList.iterator();
            while (itr.hasNext()) {
                queryBuilder.append(itr.next());
                if (itr.hasNext()) {
                    queryBuilder.append(" AND ");
                }
            }
        }

        if (!additionalFilteringParams.isEmpty()) {
            queryBuilder.append(")");
        }

        return queryBuilder;
    }
}
