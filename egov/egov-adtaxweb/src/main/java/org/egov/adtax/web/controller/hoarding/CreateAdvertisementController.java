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
package org.egov.adtax.web.controller.hoarding;

import org.egov.adtax.entity.AdvertisementPermitDetail;
import org.egov.adtax.entity.SubCategory;
import org.egov.adtax.entity.enums.AdvertisementStatus;
import org.egov.adtax.utils.constants.AdvertisementTaxConstants;
import org.egov.adtax.web.controller.common.HoardingControllerSupport;
import org.egov.commons.Installment;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/hoarding")
public class CreateAdvertisementController extends HoardingControllerSupport {
	
	@Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @RequestMapping(value = "child-boundaries", method = GET, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody List<Boundary> childBoundaries(@RequestParam final Long parentBoundaryId) {
        return boundaryService.getActiveChildBoundariesByBoundaryId(parentBoundaryId);
    }

    @RequestMapping(value = "subcategories", method = GET, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody List<SubCategory> hoardingSubcategories(@RequestParam final Long categoryId) {
        return subCategoryService.getAllActiveSubCategoryByCategoryId(categoryId);
    }

    @RequestMapping(value = "create", method = GET)
    public String createHoardingForm(@ModelAttribute final AdvertisementPermitDetail advertisementPermitDetail,
            final Model model) {
         WorkflowContainer workFlowContainer= new WorkflowContainer();
         workFlowContainer.setAdditionalRule(AdvertisementTaxConstants.CREATE_ADDITIONAL_RULE);
        prepareWorkflow(model, advertisementPermitDetail,workFlowContainer);
        model.addAttribute("additionalRule", AdvertisementTaxConstants.CREATE_ADDITIONAL_RULE);
       
        model.addAttribute("stateType", advertisementPermitDetail.getClass().getSimpleName());
        model.addAttribute("currentState", "NEW");
        return "hoarding-create";
    }

    @RequestMapping(value = "create", method = POST)
    public String createAdvertisement(@Valid @ModelAttribute final AdvertisementPermitDetail advertisementPermitDetail,
            final BindingResult resultBinder,
            final RedirectAttributes redirAttrib, final HttpServletRequest request, final Model model,
            @RequestParam String workFlowAction) {
       
        validateHoardingDocs(advertisementPermitDetail, resultBinder);
        validateApplicationDate(advertisementPermitDetail, resultBinder);
        validateAdvertisementDetails(advertisementPermitDetail, resultBinder);
        if (advertisementPermitDetail.getState() == null)
            advertisementPermitDetail.setStatus(advertisementPermitDetailService
                    .getStatusByModuleAndCode(AdvertisementTaxConstants.APPLICATION_STATUS_CREATED));
        advertisementPermitDetail.getAdvertisement().setStatus(AdvertisementStatus.WORKFLOW_IN_PROGRESS);
        if (resultBinder.hasErrors()) {
            WorkflowContainer workFlowContainer= new WorkflowContainer();
            workFlowContainer.setAdditionalRule(AdvertisementTaxConstants.CREATE_ADDITIONAL_RULE);
            prepareWorkflow(model, advertisementPermitDetail, workFlowContainer);
            model.addAttribute("additionalRule", AdvertisementTaxConstants.CREATE_ADDITIONAL_RULE);
            model.addAttribute("stateType", advertisementPermitDetail.getClass().getSimpleName());
            return "hoarding-create";
        }
        storeHoardingDocuments(advertisementPermitDetail);

        Long approvalPosition = 0l;
        String approvalComment = "";
        String approverName = "";
        String nextDesignation = "";
        if (request.getParameter("approvalComent") != null)
            approvalComment = request.getParameter("approvalComent");
        if (request.getParameter("workFlowAction") != null)
            workFlowAction = request.getParameter("workFlowAction");
        if (request.getParameter("approverName") != null)
            approverName = request.getParameter("approverName");
        if (request.getParameter("nextDesignation") != null)
            nextDesignation = request.getParameter("nextDesignation");
        if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
            approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));
        advertisementPermitDetail.getAdvertisement().setPenaltyCalculationDate(advertisementPermitDetail.getApplicationDate());
        advertisementPermitDetailService.createAdvertisementPermitDetail(advertisementPermitDetail, approvalPosition,
                approvalComment, "CREATEADVERTISEMENT", workFlowAction);
        redirAttrib.addFlashAttribute("advertisementPermitDetail", advertisementPermitDetail);
        String message = messageSource.getMessage("msg.success.forward",
                new String[] { approverName.concat("~").concat(nextDesignation), advertisementPermitDetail.getApplicationNumber() }, null);
        redirAttrib.addFlashAttribute("message", message);
        return "redirect:/hoarding/success/" + advertisementPermitDetail.getId();
    } 

    private void validateApplicationDate(final AdvertisementPermitDetail advertisementPermitDetail,
            final BindingResult resultBinder) {
        if (advertisementPermitDetail != null && advertisementPermitDetail.getApplicationDate() != null) {
            final Installment installmentObj = advertisementDemandService.getCurrentInstallment();
            if (installmentObj != null && installmentObj.getFromDate() != null)
                if (advertisementPermitDetail.getApplicationDate().after(DateUtils.endOfDay(installmentObj.getToDate())) ||
                        advertisementPermitDetail.getApplicationDate().before(DateUtils.startOfDay(installmentObj.getFromDate())))
                    resultBinder.rejectValue("applicationDate", "invalid.applicationDate");

        }

    }

    @RequestMapping(value = "/success/{id}", method = GET)
    public ModelAndView successView(@PathVariable("id") final String id,
            @ModelAttribute final AdvertisementPermitDetail advertisementPermitDetail) {
        return new ModelAndView("hoarding/hoarding-success", "hoarding",
                advertisementPermitDetailService.findBy(Long.valueOf(id)));

    }

}
