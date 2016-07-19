/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
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
 */
package org.egov.works.web.actions.estimate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.egov.common.entity.UOM;
import org.egov.commons.EgwTypeOfWork;
import org.egov.commons.service.UOMService;
import org.egov.eis.service.AssignmentService;
import org.egov.infra.web.struts.actions.SearchFormAction;
import org.egov.infstr.search.SearchQuery;
import org.egov.infstr.search.SearchQueryHQL;
import org.egov.works.master.service.EstimateTemplateService;
import org.egov.works.models.masters.EstimateTemplate;
import org.egov.works.models.masters.EstimateTemplateActivity;
import org.egov.works.models.masters.ScheduleOfRate;
import org.egov.works.services.AbstractEstimateService;
import org.egov.works.services.WorksService;
import org.egov.works.utils.WorksConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
        @Result(name = EstimateTemplateAction.NEW, location = "estimateTemplate-new.jsp"),
        @Result(name = EstimateTemplateAction.SEARCH, location = "estimateTemplate-search.jsp"),
        @Result(name = EstimateTemplateAction.SUCCESS, location = "estimateTemplate-success.jsp"),
        @Result(name = EstimateTemplateAction.EDIT, location = "estimateTemplate-edit.jsp")
})
public class EstimateTemplateAction extends SearchFormAction {

    private static final long serialVersionUID = 3610026596221473556L;
    private EstimateTemplate estimateTemplate = new EstimateTemplate();
    private List<EstimateTemplateActivity> sorActivities = new LinkedList<EstimateTemplateActivity>();
    private List<EstimateTemplateActivity> nonSorActivities = new LinkedList<EstimateTemplateActivity>();
    @Autowired
    private AssignmentService assignmentService;
    private WorksService worksService;
    // @Autowired
    /* private PersonalInformationService personalInformationService; */
    @Autowired
    private EstimateTemplateService estimateTemplateService;
    private String mode = null;
    private Long id;
    private String sourcePage = null;
    private Long typeOfWork;
    private String estimateTemplateCode;
    private Long subTypeOfWork;
    public static final String SEARCH = "search";
    public static final String EDIT = "edit";
    public static final String SUCCESS = "success";
    private AbstractEstimateService abstractEstimateService;

    @Autowired
    private UOMService uomService;
    
    public EstimateTemplateAction() {
        addRelatedEntity("workType", EgwTypeOfWork.class);
        addRelatedEntity("subType", EgwTypeOfWork.class);
    }

    @Override
    public Object getModel() {
        // TODO Auto-generated method stub
        return estimateTemplate;
    }

    protected void setModel(final EstimateTemplate estimateTemplate) {
        this.estimateTemplate = estimateTemplate;
    }

    @Action(value = "/estimate/estimateTemplate-edit")
    public String edit() {
        return EDIT;
    }

    @Action(value = "/estimate/estimateTemplate-view")
    public String view() {
        return SUCCESS;
    }

    @Override
    public void prepare() {
        if (id != null)
            estimateTemplate = estimateTemplateService.getEstimateTemplateById(id);
        final AjaxEstimateAction ajaxEstimateAction = new AjaxEstimateAction();
        ajaxEstimateAction.setPersistenceService(getPersistenceService());
        ajaxEstimateAction.setAssignmentService(assignmentService);
        // TODO: Need to uncomment
        // ajaxEstimateAction.setPersonalInformationService(personalInformationService);
        super.prepare();
        setupDropdownDataExcluding("workType", "subType");
        addDropdownData("parentCategoryList",
                getPersistenceService().findAllBy("from EgwTypeOfWork etw1 where etw1.parentid is null"));
        addDropdownData("uomList", uomService.findAll());
        addDropdownData("scheduleCategoryList",
                getPersistenceService().findAllBy("from ScheduleCategory order by upper(code)"));
        populateCategoryList(ajaxEstimateAction, estimateTemplate.getWorkType() != null);

    }

    @Action(value = "/estimate/estimateTemplate-newform")
    public String newform() {
        return NEW;
    }

    @Override
    @SkipValidation
    @Action(value = "/estimate/estimateTemplate-search")
    public String search() {
        return SEARCH;
    }

    @Action(value = "/estimate/estimateTemplate-save")
    public String save() {
        estimateTemplate.getEstimateTemplateActivities().clear();
        populateSorActivities();
        populateNonSorActivities();
        populateActivities();
        if (estimateTemplate.getId() == null)
            estimateTemplate.setStatus(1);
        else
            setMode("edit");
        if (estimateTemplate != null) {
            final EstimateTemplate template = estimateTemplateService.getEstimateTemplateByCode(estimateTemplate.getCode());
            if (template != null && estimateTemplate.getId() != template.getId()) {
                addActionMessage(getText("estimateTemplate.code.isunique"));
                return NEW;
            }
        }
        estimateTemplateService.create(estimateTemplate);
        if (StringUtils.isBlank(mode))
            addActionMessage(getText("estimate.template.success.save",
                    new String[] { estimateTemplate.getCode(), estimateTemplate.getName() }));
        else
            addActionMessage(getText("estimate.template.success.modify"));
        return SUCCESS;
    }

    protected void populateSorActivities() {
        for (final EstimateTemplateActivity activity : sorActivities)
            if (validSorActivity(activity)) {
                activity.setSchedule((ScheduleOfRate) getPersistenceService().find("from ScheduleOfRate where id = ?",
                        activity.getSchedule().getId()));
                activity.setUom(activity.getSchedule().getUom());
                estimateTemplate.addActivity(activity);
            }
    }

    protected boolean validSorActivity(final EstimateTemplateActivity activity) {
        if (activity != null && activity.getSchedule() != null && activity.getSchedule().getId() != null)
            return true;

        return false;
    }

    protected void populateNonSorActivities() {
        for (final EstimateTemplateActivity activity : nonSorActivities)
            if (activity != null) {
                activity.setUom(uomService.findOne(activity.getNonSor().getUom().getId()));
                activity.getNonSor().setUom(activity.getUom());
                activity.getNonSor().setCreatedBy(worksService.getCurrentLoggedInUser());
                activity.getNonSor().setCreatedDate(new Date());
                activity.getNonSor().setLastModifiedBy(worksService.getCurrentLoggedInUser());
                activity.getNonSor().setLastModifiedDate(new Date());
                estimateTemplate.addActivity(activity);
            }

    }

    private void populateActivities() {
        for (final EstimateTemplateActivity activity : estimateTemplate.getEstimateTemplateActivities()) {
            activity.setEstimateTemplate(estimateTemplate);

            // TODO:Fixme - Setting auditable properties by time being since HibernateEventListener is not getting
            // triggered on update of estimate for child objects
            activity.setCreatedBy(worksService.getCurrentLoggedInUser());
            activity.setCreatedDate(new Date());
            activity.setModifiedBy(worksService.getCurrentLoggedInUser());
            activity.setModifiedDate(new Date());

        }

        persistenceService.applyAuditing(estimateTemplate);
    }

    protected void populateCategoryList(final AjaxEstimateAction ajaxEstimateAction, final boolean categoryPopulated) {
        if (categoryPopulated) {
            ajaxEstimateAction.setCategory(estimateTemplate.getWorkType().getId());
            ajaxEstimateAction.subcategories();
            addDropdownData("categoryList", ajaxEstimateAction.getSubCategories());
        } else
            addDropdownData("categoryList", Collections.emptyList());
    }

    public boolean validCode() {
        boolean status = false;
        if (estimateTemplate != null && estimateTemplate.getCode() != null) {
            final AjaxEstimateTemplateAction ajaxEstimateTemplateAction = new AjaxEstimateTemplateAction();
            ajaxEstimateTemplateAction.setCode(estimateTemplate.getCode());
            ajaxEstimateTemplateAction.setPersistenceService(persistenceService);
            if (ajaxEstimateTemplateAction.getCodeCheck())
                status = true;
        }
        return status;
    }

    @Action(value = "/estimate/estimateTemplate-searchDetails")
    public String searchDetails() {
        setPageSize(WorksConstants.PAGE_SIZE);
        super.search();
        return SEARCH;
    }

    public List<EstimateTemplateActivity> getSorActivities() {
        return sorActivities;
    }

    public void setSorActivities(final List<EstimateTemplateActivity> sorActivities) {
        this.sorActivities = sorActivities;
    }

    public List<EstimateTemplateActivity> getNonSorActivities() {
        return nonSorActivities;
    }

    public void setNonSorActivities(final List<EstimateTemplateActivity> nonSorActivities) {
        this.nonSorActivities = nonSorActivities;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public SearchQuery prepareQuery(final String sortField, final String sortOrder) {
        String dynQuery = " from EstimateTemplate et where et.id is not null ";
        final List<Object> paramList = new ArrayList<Object>();
        dynQuery = dynQuery + " and et.status = ?";
        paramList.add(estimateTemplate.getStatus());
        if (estimateTemplate.getWorkType() != null && estimateTemplate.getWorkType().getId() != -1) {
            dynQuery = dynQuery + " and et.workType.id = ? ";
            paramList.add(estimateTemplate.getWorkType().getId());
        }
        if (estimateTemplate.getSubType() != null && estimateTemplate.getSubType().getId() != -1) {
            dynQuery = dynQuery + " and et.subType.id = ? ";
            paramList.add(estimateTemplate.getSubType().getId());
        }
        if (StringUtils.isNotBlank(estimateTemplate.getCode().trim()))
            dynQuery = dynQuery + " and UPPER(et.code) like '%" + estimateTemplate.getCode().trim().toUpperCase()
                    + "%'";
        if (StringUtils.isNotBlank(estimateTemplate.getName().trim()))
            dynQuery = dynQuery + " and UPPER(et.name) like '%" + estimateTemplate.getName().trim().toUpperCase()
                    + "%'";
        final String countQuery = "select distinct count(et) " + dynQuery;
        return new SearchQueryHQL(dynQuery, countQuery, paramList);
    }

    public String getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(final String sourcePage) {
        this.sourcePage = sourcePage;
    }

    public Long getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(final Long typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public String getEstimateTemplateCode() {
        return estimateTemplateCode;
    }

    public void setEstimateTemplateCode(final String estimateTemplateCode) {
        this.estimateTemplateCode = estimateTemplateCode;
    }

    public Long getSubTypeOfWork() {
        return subTypeOfWork;
    }

    public void setSubTypeOfWork(final Long subTypeOfWork) {
        this.subTypeOfWork = subTypeOfWork;
    }

    public AbstractEstimateService getAbstractEstimateService() {
        return abstractEstimateService;
    }

    public void setAbstractEstimateService(final AbstractEstimateService abstractEstimateService) {
        this.abstractEstimateService = abstractEstimateService;
    }

    public void setWorksService(final WorksService worksService) {
        this.worksService = worksService;
    }

}