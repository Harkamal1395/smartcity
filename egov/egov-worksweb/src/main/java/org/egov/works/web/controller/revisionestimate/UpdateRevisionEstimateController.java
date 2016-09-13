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
package org.egov.works.web.controller.revisionestimate;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.eis.web.controller.workflow.GenericWorkFlowController;
import org.egov.infra.exception.ApplicationException;
import org.egov.works.abstractestimate.entity.AbstractEstimate.EstimateStatus;
import org.egov.works.abstractestimate.entity.Activity;
import org.egov.works.abstractestimate.service.EstimateService;
import org.egov.works.abstractestimate.service.MeasurementSheetService;
import org.egov.works.letterofacceptance.service.WorkOrderActivityService;
import org.egov.works.lineestimate.service.LineEstimateService;
import org.egov.works.mb.service.MBHeaderService;
import org.egov.works.revisionestimate.entity.RevisionAbstractEstimate;
import org.egov.works.revisionestimate.service.RevisionEstimateService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.workorder.entity.WorkOrderActivity;
import org.egov.works.workorder.entity.WorkOrderEstimate;
import org.egov.works.workorder.service.WorkOrderEstimateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/revisionestimate")
public class UpdateRevisionEstimateController extends GenericWorkFlowController {

    final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private RevisionEstimateService revisionEstimateService;

    @Autowired
    private WorkOrderEstimateService workOrderEstimateService;

    @Autowired
    private MeasurementSheetService measurementSheetService;

    @Autowired
    private LineEstimateService lineEstimateService;

    @Autowired
    private EstimateService estimateService;

    @Autowired
    private WorkOrderActivityService workOrderActivityService;

    @Autowired
    private MBHeaderService mbHeaderService;

    @Autowired
    private MessageSource messageSource;

    @ModelAttribute("revisionEstimate")
    public RevisionAbstractEstimate getRevisionEstimate(@PathVariable final String revisionEstimateId) {
        final RevisionAbstractEstimate revisionEstimate = revisionEstimateService
                .getRevisionEstimateById(Long.parseLong(revisionEstimateId));
        return revisionEstimate;
    }

    @RequestMapping(value = "/update/{revisionEstimateId}", method = RequestMethod.GET)
    public String updateForm(final Model model, @PathVariable final Long revisionEstimateId,
            final HttpServletRequest request, @RequestParam(value = "mode", required = false) final String mode) {

        final RevisionAbstractEstimate revisionEstimate = revisionEstimateService.getRevisionEstimateById(revisionEstimateId);
        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService
                .getWorkOrderEstimateByAbstractEstimateId(revisionEstimate.getParent().getId());
        revisionEstimateService.loadViewData(revisionEstimate, workOrderEstimate, model);

        prepareNonTenderedAndLumpSumActivities(revisionEstimate);
        prepareChangeQuantityActivities(revisionEstimate);

        if (revisionEstimate.getCurrentState() != null
                && !WorksConstants.NEW.equals(revisionEstimate.getCurrentState().getValue()))
            model.addAttribute("currentState", revisionEstimate.getCurrentState().getValue());

        final WorkflowContainer workflowContainer = new WorkflowContainer();
        prepareWorkflow(model, revisionEstimate, workflowContainer);
        if (EstimateStatus.NEW.toString().equals(revisionEstimate.getEgwStatus().getCode())) {
            List<String> validActions = Collections.emptyList();

            validActions = customizedWorkFlowService.getNextValidActions(revisionEstimate.getStateType(), workflowContainer
                    .getWorkFlowDepartment(), workflowContainer.getAmountRule(), workflowContainer.getAdditionalRule(),
                    WorksConstants.NEW, workflowContainer.getPendingActions(), revisionEstimate.getCreatedDate());
            model.addAttribute("validActionList", validActions);
        }

        model.addAttribute("workflowHistory",
                lineEstimateService.getHistory(revisionEstimate.getState(), revisionEstimate.getStateHistory()));
        model.addAttribute("approvalDepartmentList", departmentService.getAllDepartments());
        model.addAttribute("approvalDesignation", request.getParameter("approvalDesignation"));
        model.addAttribute("approvalPosition", request.getParameter("approvalPosition"));
        model.addAttribute("measurementsPresent", measurementSheetService.existsByEstimate(revisionEstimate.getId()));

        if (WorksConstants.SAVE_ACTION.equalsIgnoreCase(mode))
            model.addAttribute("message",
                    messageSource.getMessage("msg.revisionestimate.saved", new String[] { revisionEstimate.getEstimateNumber() },
                            null));
        if (EstimateStatus.NEW.toString().equals(revisionEstimate.getEgwStatus().getCode()) ||
                EstimateStatus.REJECTED.toString().equals(revisionEstimate.getEgwStatus().getCode())) {
            model.addAttribute("mode", "edit");
            return "revisionEstimate-form";
        } else {
            model.addAttribute("estimateValue", revisionEstimate.getEstimateValue().setScale(2, BigDecimal.ROUND_HALF_EVEN));
            model.addAttribute("mode", "workflowView");
            return "revisionEstimate-view";
        }

    }

    @RequestMapping(value = "/view/{revisionEstimateId}", method = RequestMethod.GET)
    public String viewForm(final Model model, @PathVariable final Long revisionEstimateId,
            final HttpServletRequest request) {

        final RevisionAbstractEstimate revisionEstimate = revisionEstimateService.getRevisionEstimateById(revisionEstimateId);
        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService
                .getWorkOrderEstimateByAbstractEstimateId(revisionEstimate.getParent().getId());
        revisionEstimateService.loadViewData(revisionEstimate, workOrderEstimate, model);

        prepareNonTenderedAndLumpSumActivities(revisionEstimate);
        prepareChangeQuantityActivities(revisionEstimate);

        model.addAttribute("workflowHistory",
                lineEstimateService.getHistory(revisionEstimate.getState(), revisionEstimate.getStateHistory()));
        model.addAttribute("estimateValue", revisionEstimate.getEstimateValue().setScale(2, BigDecimal.ROUND_HALF_EVEN));
        model.addAttribute("mode", "view");
        return "revisionEstimate-view";
    }

    private void prepareNonTenderedAndLumpSumActivities(final RevisionAbstractEstimate revisionEstimate) {

        for (final Activity activity : revisionEstimate.getActivities())
            if (activity.getSchedule() != null && activity.getParent() == null)
                revisionEstimate.getNonTenderedActivities().add(activity);
            else if (activity.getNonSor() != null && activity.getParent() == null)
                revisionEstimate.getLumpSumActivities().add(activity);
    }

    private void prepareChangeQuantityActivities(final RevisionAbstractEstimate revisionEstimate) {
        for (final Activity activity : revisionEstimate.getActivities())
            if (activity.getParent() != null) {
                final WorkOrderActivity workOrderActivity = workOrderActivityService
                        .getWorkOrderActivityByActivity(activity.getParent().getId());
                if (workOrderActivity != null) {
                    revisionEstimateService.deriveWorkOrderActivityQuantity(workOrderActivity);
                    final Double consumedQuantity = mbHeaderService.getPreviousCumulativeQuantity(-1L, workOrderActivity.getId());
                    activity.setConsumedQuantity(consumedQuantity == null ? 0 : consumedQuantity);
                    activity.setEstimateQuantity(workOrderActivity.getApprovedQuantity());
                }
                revisionEstimate.getChangeQuantityActivities().add(activity);
            }
    }

    @RequestMapping(value = "/update/{revisionEstimateId}", method = RequestMethod.POST)
    public String update(@ModelAttribute("revisionEstimate") final RevisionAbstractEstimate revisionEstimate,
            final BindingResult errors, final RedirectAttributes redirectAttributes,
            final Model model, final HttpServletRequest request,
            @RequestParam final String removedActivityIds)
            throws ApplicationException, IOException {

        String mode = "";
        String workFlowAction = "";
        RevisionAbstractEstimate updatedRevisionEstimate = null;

        if (request.getParameter("mode") != null)
            mode = request.getParameter("mode");

        if (request.getParameter("workFlowAction") != null)
            workFlowAction = request.getParameter("workFlowAction");

        Long approvalPosition = 0l;
        String approvalComment = "";

        if (request.getParameter("approvalComent") != null)
            approvalComment = request.getParameter("approvalComent");

        if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
            approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));

        // For Get Configured ApprovalPosition from workflow history
        if (approvalPosition == null || approvalPosition.equals(Long.valueOf(0)))
            approvalPosition = estimateService.getApprovalPositionByMatrixDesignation(revisionEstimate, approvalPosition, null,
                    mode, workFlowAction);

        if ((approvalPosition == null || approvalPosition.equals(Long.valueOf(0)))
                && request.getParameter("approvalPosition") != null
                && !request.getParameter("approvalPosition").isEmpty())
            approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));

        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService
                .getWorkOrderEstimateByAbstractEstimateId(revisionEstimate.getParent().getId());

        revisionEstimateService.validateChangeQuantityActivities(revisionEstimate, errors);
        if (errors.hasErrors()) {

            revisionEstimateService.loadViewData(revisionEstimate, workOrderEstimate, model);

            final WorkflowContainer workflowContainer = new WorkflowContainer();
            prepareWorkflow(model, revisionEstimate, workflowContainer);
            List<String> validActions = Collections.emptyList();
            validActions = customizedWorkFlowService.getNextValidActions(revisionEstimate.getStateType(),
                    workflowContainer.getWorkFlowDepartment(), workflowContainer.getAmountRule(),
                    workflowContainer.getAdditionalRule(), WorksConstants.NEW, workflowContainer.getPendingActions(),
                    revisionEstimate.getCreatedDate());
            if (revisionEstimate.getState() != null && revisionEstimate.getState().getNextAction() != null)
                model.addAttribute("nextAction", revisionEstimate.getState().getNextAction());
            model.addAttribute("validActionList", validActions);
            model.addAttribute("mode", null);
            model.addAttribute("approvalDesignation", request.getParameter("approvalDesignation"));
            model.addAttribute("approvalPosition", request.getParameter("approvalPosition"));
            model.addAttribute("designation", request.getParameter("designation"));
            model.addAttribute("approvedByValue", request.getParameter("approvedBy"));
            model.addAttribute("removedActivityIds", removedActivityIds);
            return "revisionEstimate-form";
        } else {
            if (null != workFlowAction)
                updatedRevisionEstimate = revisionEstimateService.updateRevisionEstimate(revisionEstimate, approvalPosition,
                        approvalComment, null, workFlowAction, removedActivityIds, workOrderEstimate);

            redirectAttributes.addFlashAttribute("revisionEstimate", updatedRevisionEstimate);

            if (EstimateStatus.NEW.toString().equals(updatedRevisionEstimate.getEgwStatus().getCode())) {
                return "redirect:/revisionestimate/update/" + updatedRevisionEstimate.getId() + "?mode=save";
            } else if (approvalPosition == null) {
                return "redirect:/revisionestimate/revisionestimate-success?revisionEstimate=" + updatedRevisionEstimate.getId()
                        + "&approvalPosition=";
            } else {
                return "redirect:/revisionestimate/revisionestimate-success?revisionEstimate=" + updatedRevisionEstimate.getId()
                        + "&approvalPosition=" + approvalPosition;
            }

        }
    }

}
