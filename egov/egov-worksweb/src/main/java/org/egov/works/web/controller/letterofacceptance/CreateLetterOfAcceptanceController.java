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
package org.egov.works.web.controller.letterofacceptance;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.DepartmentService;
import org.egov.infra.exception.ApplicationException;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.StringUtils;
import org.egov.infra.utils.autonumber.AutonumberServiceBeanResolver;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.abstractestimate.service.EstimateService;
import org.egov.works.autonumber.LetterOfAcceptanceNumberGenerator;
import org.egov.works.letterofacceptance.entity.SearchRequestContractor;
import org.egov.works.letterofacceptance.service.LetterOfAcceptanceService;
import org.egov.works.master.service.ContractorGradeService;
import org.egov.works.master.service.ContractorService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.workorder.entity.WorkOrder;
import org.egov.works.workorder.entity.WorkOrderEstimate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = "/letterofacceptance")
public class CreateLetterOfAcceptanceController {

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private LetterOfAcceptanceService letterOfAcceptanceService;

    @Autowired
    private AutonumberServiceBeanResolver beanResolver;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ContractorGradeService contractorGradeService;

    @Autowired
    @Qualifier("contractorService")
    private ContractorService contractorService;

    @Autowired
    private EstimateService estimateService;

    @RequestMapping(value = "/newform", method = RequestMethod.GET)
    public String showNewForm(@ModelAttribute("workOrder") final WorkOrder workOrder, final Model model,
            final HttpServletRequest request) {
        final String estimateNumber = request.getParameter("estimateNumber");
        final AbstractEstimate abstractEstimate = estimateService
                .getAbstractEstimateByEstimateNumberAndStatus(estimateNumber);
        setDropDownValues(model, abstractEstimate);
        workOrder.setWorkOrderDate(new Date());

        final List<AppConfigValues> values = appConfigValuesService.getConfigValuesByModuleAndKey(
                WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_PERCENTAGE_ON_ESTIMATERATE_OR_WORKVALUE);
        final AppConfigValues value = values.get(0);
        model.addAttribute("percentage_on_estimaterate_or_workvalue", value.getValue());
        model.addAttribute("lineEstimateDetails", abstractEstimate);
        model.addAttribute("documentDetails", abstractEstimate.getDocumentDetails());
        model.addAttribute("abstractEstimate", estimateService.getAbstractEstimateByEstimateNumber(estimateNumber));
        model.addAttribute("workOrder", workOrder);
        model.addAttribute("loggedInUser", securityUtils.getCurrentUser().getName());
        return "createLetterOfAcceptance-form";
    }

    private void setDropDownValues(final Model model, final AbstractEstimate abstractEstimate) {
        model.addAttribute("engineerInchargeList",
                letterOfAcceptanceService.getEngineerInchargeList(abstractEstimate.getExecutingDepartment().getId(),
                        letterOfAcceptanceService.getEngineerInchargeDesignationId()));
    }

    @RequestMapping(value = "/loa-save", method = RequestMethod.POST)
    public String create(@ModelAttribute("workOrder") WorkOrder workOrder, final Model model,
            final BindingResult resultBinder, final HttpServletRequest request,
            @RequestParam("file") final MultipartFile[] files) throws IOException {
        final AbstractEstimate abstractEstimate = estimateService
                .getAbstractEstimateByEstimateNumber(workOrder.getEstimateNumber());
        final WorkOrder existingWorkOrder = letterOfAcceptanceService
                .getWorkOrderByEstimateNumber(workOrder.getEstimateNumber());

        if (existingWorkOrder != null)
            resultBinder.reject("error.loa.exists.for.estimate",
                    new String[] { existingWorkOrder.getWorkOrderNumber(), workOrder.getEstimateNumber() },
                    "error.loa.exists.for.estimate");

        validateInput(workOrder, resultBinder);

        if (abstractEstimate != null && abstractEstimate.getLineEstimateDetails().getLineEstimate().isSpillOverFlag()
                && abstractEstimate.getLineEstimateDetails().getLineEstimate().isWorkOrderCreated())
            validateSpillOverInput(workOrder, resultBinder, abstractEstimate);

        if (resultBinder.hasErrors()) {
            setDropDownValues(model, abstractEstimate);
            model.addAttribute("abstractEstimate", abstractEstimate);
            model.addAttribute("documentDetails", abstractEstimate.getDocumentDetails());
            model.addAttribute("loggedInUser", securityUtils.getCurrentUser().getName());
            model.addAttribute("contractorSearch", request.getParameter("contractorSearch"));
            model.addAttribute("contractorCode", request.getParameter("contractorCode"));
            model.addAttribute("engineerIncharge", request.getParameter("engineerIncharge"));
            return "createLetterOfAcceptance-form";
        } else {
            workOrder.setContractor(contractorService.findById(workOrder.getContractor().getId(), false));
            final WorkOrderEstimate workOrderEstimate = letterOfAcceptanceService.createWorkOrderEstimate(workOrder);

            if (abstractEstimate != null
                    && abstractEstimate.getLineEstimateDetails().getLineEstimate().isSpillOverFlag()
                    && !abstractEstimate.getLineEstimateDetails().getLineEstimate().isWorkOrderCreated()
                    || !abstractEstimate.getLineEstimateDetails().getLineEstimate().isSpillOverFlag()) {
                LetterOfAcceptanceNumberGenerator l = beanResolver
                        .getAutoNumberServiceFor(LetterOfAcceptanceNumberGenerator.class);
                final String workOrderNumber = l.getNextNumber(workOrderEstimate);
                workOrder.setWorkOrderNumber(workOrderNumber);
            }
            final WorkOrder savedWorkOrder = letterOfAcceptanceService.create(workOrder, files);
            return "redirect:/letterofacceptance/loa-success?loaNumber=" + savedWorkOrder.getWorkOrderNumber();
        }
    }

    @RequestMapping(value = "/loa-success", method = RequestMethod.GET)
    public String showLetterOfAcceptanceSuccessPage(@RequestParam("loaNumber") final String loaNumber,
            final Model model, @RequestParam(value = "isModify", required = false) final boolean isModify) {
        final WorkOrder workOrder = letterOfAcceptanceService.getWorkOrderByWorkOrderNumber(loaNumber);
        model.addAttribute("workOrder", workOrder);
        if (isModify)
            model.addAttribute("mode", "modify");
        return "letterofacceptance-success";
    }

    private void validateInput(final WorkOrder workOrder, final BindingResult resultBinder) {
        if (StringUtils.isBlank(workOrder.getFileNumber()))
            resultBinder.rejectValue("fileNumber", "error.fileno.required");
        if (workOrder.getFileDate() == null)
            resultBinder.rejectValue("fileDate", "error.filedate.required");
        if (workOrder.getWorkOrderAmount() <= 0)
            resultBinder.rejectValue("workOrderAmount", "error.workorderamount.required");
        if (workOrder.getContractor() == null || workOrder.getContractor().getId() == null)
            resultBinder.rejectValue("contractor", "error.contractor.required");
        if (workOrder.getContractPeriod() == null || workOrder.getContractPeriod() <= 0)
            resultBinder.rejectValue("contractPeriod", "error.contractorperiod.required");
        if (workOrder.getDefectLiabilityPeriod() <= 0)
            resultBinder.rejectValue("defectLiabilityPeriod", "error.defectliabilityperiod.required");
        if (workOrder.getEngineerIncharge() == null || workOrder.getEngineerIncharge().getId() == null)
            resultBinder.rejectValue("engineerIncharge", "error.engineerincharge.required");

    }

    private void validateSpillOverInput(final WorkOrder workOrder, final BindingResult resultBinder,
            final AbstractEstimate abstractEstimate) {
        if (StringUtils.isBlank(workOrder.getWorkOrderNumber()))
            resultBinder.rejectValue("workOrderNumber", "error.workordernumber.required");
        final WorkOrder wo = letterOfAcceptanceService.getWorkOrderByWorkOrderNumber(workOrder.getWorkOrderNumber());
        if (wo != null)
            resultBinder.rejectValue("workOrderNumber", "error.workordernumber.unique");
        if (workOrder.getFileDate().before(abstractEstimate.getEstimateDate()))
            resultBinder.rejectValue("fileDate", "error.loa.filedate");
        if (workOrder.getWorkOrderDate().before(workOrder.getFileDate()))
            resultBinder.rejectValue("fileDate", "error.loa.workorderdate");
    }

    @RequestMapping(value = "/contractorsearchform", method = RequestMethod.GET)
    public String showSearchContractorForm(@ModelAttribute final SearchRequestContractor searchRequestContractor,
            final Model model) throws ApplicationException {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("contractorClasses", contractorGradeService.getAllContractorGrades());
        model.addAttribute("searchRequestContractor", searchRequestContractor);
        return "contractor-search";
    }

}
