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
package org.egov.works.web.controller.abstractestimate;

import java.util.ArrayList;
import java.util.List;

import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Department;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.DepartmentService;
import org.egov.infra.exception.ApplicationException;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.abstractestimate.entity.AbstractEstimateForLoaSearchRequest;
import org.egov.works.abstractestimate.entity.SearchAbstractEstimate;
import org.egov.works.abstractestimate.service.EstimateService;
import org.egov.works.lineestimate.entity.DocumentDetails;
import org.egov.works.lineestimate.service.LineEstimateService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.utils.WorksUtils;
import org.egov.works.workorder.service.WorkOrderEstimateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/abstractestimate")
public class SearchAbstractEstimateController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EstimateService estimateService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private LineEstimateService lineEstimateService;

    @Autowired
    private WorkOrderEstimateService workOrderEstimateService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private WorksUtils worksUtils;

    @RequestMapping(value = "/searchform", method = RequestMethod.GET)
    public String searchForm(@ModelAttribute final SearchAbstractEstimate searchAbstractEstimate, final Model model)
            throws ApplicationException {
        setDropDownValues(model);
        model.addAttribute("searchAbstractEstimate", searchAbstractEstimate);
        return "abstractestimate-search";
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewAbstractEstimate(@PathVariable final String id, final Model model)
            throws ApplicationException {
        final AbstractEstimate abstractEstimate = estimateService.getAbstractEstimateById(Long.valueOf(id));

        getEstimateDocuments(abstractEstimate);
        final List<AppConfigValues> values = appConfigValuesService.getConfigValuesByModuleAndKey(
                WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_SHOW_SERVICE_FIELDS);
        final AppConfigValues value = values.get(0);
        if (value.getValue().equalsIgnoreCase("Yes"))
            model.addAttribute("isServiceVATRequired", true);
        else
            model.addAttribute("isServiceVATRequired", false);
        model.addAttribute("mode", "view");
        model.addAttribute("abstractEstimate", abstractEstimate);
        model.addAttribute("documentDetails", abstractEstimate.getDocumentDetails());
        model.addAttribute("workOrderEstimate",
                workOrderEstimateService.getWorkOrderEstimateByAbstractEstimateId(Long.valueOf(id)));
        model.addAttribute("paymentreleased",
                estimateService.getPaymentsReleasedForLineEstimate(abstractEstimate.getLineEstimateDetails()));

        return "abstractestimate-view";
    }

    @RequestMapping(value = "/searchabstractestimateforloa-form", method = RequestMethod.GET)
    public String searchAbstractEstimateForLOA(
            @ModelAttribute final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest,
            final Model model) {
        setDropDownValues(model);
        final List<Department> departments = lineEstimateService.getUserDepartments(securityUtils.getCurrentUser());
        final List<Long> departmentIds = new ArrayList<Long>();
        if (departments != null)
            for (final Department department : departments)
                departmentIds.add(department.getId());
        final List<User> abstractEstimateCreatedByUsers = estimateService.getAbstractEstimateCreatedByUsers(departmentIds);
        model.addAttribute("abstractEstimateForLoaSearchRequest", abstractEstimateForLoaSearchRequest);
        model.addAttribute("abstractEstimateCreatedByUsers", abstractEstimateCreatedByUsers);
        model.addAttribute("departments", departments);
        return "searchAbstractEstimateForLoa-search";
    }

    private void setDropDownValues(final Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("createdUsers", estimateService.getAbstractEstimateCreatedByUsers());
        model.addAttribute("abstractEstimateStatus", worksUtils.getStatusByModule(WorksConstants.ABSTRACTESTIMATE));

    }

    private void getEstimateDocuments(final AbstractEstimate abstractEstimate) {
        List<DocumentDetails> documentDetailsList = new ArrayList<DocumentDetails>();
        documentDetailsList = worksUtils.findByObjectIdAndObjectType(abstractEstimate.getId(),
                WorksConstants.ABSTRACTESTIMATE);
        abstractEstimate.setDocumentDetails(documentDetailsList);
    }
}
