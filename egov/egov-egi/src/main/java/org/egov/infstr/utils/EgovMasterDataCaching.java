/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */

package org.egov.infstr.utils;

import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @deprecated no longer supported
 */
@Deprecated
public class EgovMasterDataCaching {
    private static final Logger LOGGER = LoggerFactory.getLogger(EgovMasterDataCaching.class);
    private static final String SQL_QUERY_TYPE = "sql";
    private static final String HQL_QUERY_TYPE = "hql";
    private static final String PATH_DELIM = "/";
    private static final String SQL_TAG_PREFIX = "sql.";
    private static final String CONFIG_FILE_SUFFIX = "_sqlconfig.xml";
    private static final EmbeddedCacheManager CACHE_MANAGER;

    static {
        try {
            CACHE_MANAGER = (EmbeddedCacheManager) ((Context) new InitialContext())
                    .lookup("java:jboss/infinispan/container/master");
        } catch (final NamingException e) {
            throw new ApplicationRuntimeException("Error occurred while getting Cache Manager", e);
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    public static void removeFromCache(final String sqlTagName) {
        try {
            final String[] temp = sqlTagName.split("-");
            final String domainName = ApplicationThreadLocals.getDomainName();
            final String applName = temp[0];
            CACHE_MANAGER.getCache().remove(applName + PATH_DELIM + domainName + PATH_DELIM + sqlTagName);
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching removeFromCache", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching removeFromCache", e);
        }
    }

    /**
     * This method load the data for given sqlTagName and puts it in Cache.
     *
     * @param sqlTagName the sql tag name
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    public List get(final String sqlTagName) {
        final String[] temp = sqlTagName.split("-");
        final String domainName = ApplicationThreadLocals.getDomainName();
        final String applName = temp[0];
        List<Object> dataList = null;
        try {
            HashMap<String, Object> cacheValuesHashMap = (HashMap<String, Object>) CACHE_MANAGER.getCache()
                    .get(applName + PATH_DELIM + domainName + PATH_DELIM + sqlTagName);
            if (cacheValuesHashMap != null)
                dataList = (List<Object>) cacheValuesHashMap.get(sqlTagName);

            if (dataList == null || dataList.isEmpty()) {
                final String type = EGovConfig
                        .getProperty(applName + CONFIG_FILE_SUFFIX, "type", EMPTY, SQL_TAG_PREFIX + sqlTagName).trim();
                if (type.equalsIgnoreCase("java")) {
                    final String className = EGovConfig.getProperty(applName + CONFIG_FILE_SUFFIX, "class", EMPTY,
                            SQL_TAG_PREFIX + sqlTagName);
                    final String methodName = EGovConfig.getProperty(applName + CONFIG_FILE_SUFFIX, "method", EMPTY,
                            SQL_TAG_PREFIX + sqlTagName);
                    final String parametertype = EGovConfig.getProperty(applName + CONFIG_FILE_SUFFIX, "parametertype", EMPTY,
                            SQL_TAG_PREFIX + sqlTagName);
                    final String parametervalue = EGovConfig.getProperty(applName + CONFIG_FILE_SUFFIX, "parametervalue", EMPTY,
                            SQL_TAG_PREFIX + sqlTagName);
                    if (isNotBlank(className) && isNotBlank(methodName))
                        dataList = loadJavaAPIMasterDataList(className, methodName, parametertype.split(","),
                                parametervalue.split(","));
                    else
                        throw new ApplicationRuntimeException("ClassName and MethodName should be mentioned for " + type + " in "
                                + applName + CONFIG_FILE_SUFFIX);
                } else if (type.equalsIgnoreCase(HQL_QUERY_TYPE) || type.equalsIgnoreCase(SQL_QUERY_TYPE)) {
                    final String query = EGovConfig.getProperty(applName + CONFIG_FILE_SUFFIX, "query", EMPTY,
                            SQL_TAG_PREFIX + sqlTagName);
                    if (!query.equalsIgnoreCase(EMPTY))
                        dataList = loadQLMasterData(query, type);
                    else
                        throw new ApplicationRuntimeException(
                                "Query should be mentioned for " + type + " in " + applName + CONFIG_FILE_SUFFIX);
                } else
                    throw new ApplicationRuntimeException("This type (" + type + ") is not supported for " + sqlTagName);
                final HashMap<String, Object> hm = new HashMap<>();
                hm.put(sqlTagName, dataList);
                CACHE_MANAGER.getCache().put(applName + PATH_DELIM + domainName + PATH_DELIM + sqlTagName, hm);
            } else
                LOGGER.info("EgovMasterDataCaching: Got directly from cache, not from db");

        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching", e);
        }
        return dataList;
    }

    /**
     * This method loads the data for Hql and Sql queries.
     *
     * @param query     the query
     * @param queryType the query type
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private List loadQLMasterData(final String query, final String queryType) {
        List list = null;
        try {
            if (queryType.trim().equalsIgnoreCase(HQL_QUERY_TYPE))
                list = queryByHibernate(query);
            else if (queryType.trim().equalsIgnoreCase(SQL_QUERY_TYPE))
                list = queryByJdbc(query);

        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching loadQLMasterData", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching loadQLMasterData", e);
        }
        return list;
    }

    /**
     * This method loads the data for type Java API.
     *
     * @param className      the class name
     * @param methodName     the method name
     * @param parametertype  the parametertype
     * @param parametervalue the parametervalue
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private List loadJavaAPIMasterDataList(final String className, final String methodName, final String[] parametertype,
                                           final String[] parametervalue) {
        List list = null;
        try {
            if (parametertype.length != parametervalue.length)
                throw new ApplicationRuntimeException("Number of parameter types and parameter values doesnt match");
            final Class cls = Class.forName(className);
            final Method method = cls.getMethod(methodName, loadMethodParameter(parametertype));
            list = (List) method.invoke(cls.newInstance(), loadMethodArguments(parametertype, parametervalue));
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching loadJavaAPIMasterDataList", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching loadJavaAPIMasterDataList", e);
        }
        return list;
    }

    /**
     * This method dynamically loads the parameters for a method i.e
     * <parametertype>java.lang.String\,java.lang.Integer\,java.lang.String</parametertype>
     *
     * @param parametertype the parametertype
     * @return Class[]
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private Class[] loadMethodParameter(final String[] parametertype) {
        Class[] className = null;
        try {
            if (!parametertype[0].trim().equalsIgnoreCase(EMPTY)) {
                className = new Class[parametertype.length];
                for (int i = 0; i < parametertype.length; i++)
                    className[i] = Class.forName(parametertype[i]);
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching loadMethodParameter", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching loadMethodParameter", e);
        }
        return className;
    }

    /**
     * This method dynamically loads the arguments for a method i.e <parametervalue>1\,10\,11</parametervalue>
     *
     * @param parametertype  the parametertype
     * @param parametervalue the parametervalue
     * @return Object[]
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private Object[] loadMethodArguments(final String[] parametertype, final String[] parametervalue) {
        Object[] objectName = null;
        try {
            if (!parametertype[0].trim().equalsIgnoreCase(EMPTY)) {
                objectName = new Object[parametervalue.length];
                for (int i = 0; i < parametertype.length; i++)
                    if (parametertype[i].trim().equalsIgnoreCase("java.lang.Integer"))
                        objectName[i] = Integer.valueOf(parametervalue[i]);
                    else if (parametertype[i].trim().equalsIgnoreCase("java.lang.Double"))
                        objectName[i] = Double.valueOf(parametervalue[i]);
                    else if (parametertype[i].trim().equalsIgnoreCase("java.lang.String"))
                        objectName[i] = parametervalue[i];
                    else
                        throw new ApplicationRuntimeException("This " + parametertype[i] + " datatype is not supported");
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching loadMethodArguments", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching loadMethodArguments", e);
        }
        return objectName;
    }

    /**
     * This method executes a hibernate query.
     *
     * @param query the query
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private List queryByHibernate(final String query) {
        List list = null;

        try {
            final Query qry = getCurrentSession().createQuery(query);
            list = qry.list();
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching queryByHibernate", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching queryByHibernate", e);
        }
        return list;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    /**
     * This method executes a sql query.
     *
     * @param query the query
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private List queryByJdbc(final String query) {
        List resultlist = null;
        List returnList = null;
        try {
            resultlist = getCurrentSession().createSQLQuery(query).list();
            if (resultlist != null)
                returnList = resultSetToArrayList(resultlist);
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching queryByJdbc", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching queryByJdbc", e);
        }
        return returnList;
    }

    /**
     * This method returns a list of LabelValueBean using the resultList object.
     *
     * @param resultList the rs
     * @return List
     * @throws ApplicationRuntimeException the eGOV runtime exception
     */

    private List resultSetToArrayList(final List<Object[]> resultList) {

        final List list = new ArrayList();
        LabelValueBean labelValueBean = null;
        BigDecimal id;
        try {
            for (final Object[] objArr : resultList) {
                labelValueBean = new LabelValueBean();
                id = (BigDecimal) objArr[0];
                labelValueBean.setId(id.intValue());
                labelValueBean.setName((String) objArr[1]);
                list.add(labelValueBean);
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred in EgovMasterDataCaching resultSetToArrayList", e);
            throw new ApplicationRuntimeException("Error occurred in EgovMasterDataCaching resultSetToArrayList", e);
        }
        return list;
    }

    @PreDestroy
    public void destroy() {
        if (CACHE_MANAGER != null && CACHE_MANAGER.isDefaultRunning())
            CACHE_MANAGER.stop();
    }
}
