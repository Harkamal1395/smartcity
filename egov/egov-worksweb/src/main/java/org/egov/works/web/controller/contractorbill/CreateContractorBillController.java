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
package org.egov.works.web.controller.contractorbill;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.egov.commons.CChartOfAccounts;
import org.egov.commons.dao.ChartOfAccountsHibernateDAO;
import org.egov.egf.budget.model.BudgetControlType;
import org.egov.egf.budget.service.BudgetControlTypeService;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.eis.web.controller.workflow.GenericWorkFlowController;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.utils.StringUtils;
import org.egov.infra.utils.autonumber.AutonumberServiceBeanResolver;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.model.bills.EgBilldetails;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.abstractestimate.entity.AbstractEstimateDeduction;
import org.egov.works.autonumber.ContractorBillNumberGenerator;
import org.egov.works.contractorbill.entity.ContractorBillRegister;
import org.egov.works.contractorbill.entity.enums.BillTypes;
import org.egov.works.contractorbill.service.ContractorBillRegisterService;
import org.egov.works.letterofacceptance.service.LetterOfAcceptanceService;
import org.egov.works.mb.entity.MBHeader;
import org.egov.works.mb.service.MBHeaderService;
import org.egov.works.models.tender.OfflineStatus;
import org.egov.works.offlinestatus.service.OfflineStatusService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.utils.WorksUtils;
import org.egov.works.workorder.entity.WorkOrder.OfflineStatuses;
import org.egov.works.workorder.entity.WorkOrderEstimate;
import org.egov.works.workorder.service.WorkOrderEstimateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping(value = "/contractorbill")
public class CreateContractorBillController extends GenericWorkFlowController {

    @Autowired
    private LetterOfAcceptanceService letterOfAcceptanceService;

    @Autowired
    private ContractorBillRegisterService contractorBillRegisterService;

    @Autowired
    private AutonumberServiceBeanResolver beanResolver;

    @Autowired
    private WorksUtils worksUtils;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Autowired
    private ChartOfAccountsHibernateDAO chartOfAccountsHibernateDAO;

    @Autowired
    private WorkOrderEstimateService workOrderEstimateService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private OfflineStatusService offlineStatusService;

    @Autowired
    private MBHeaderService mBHeaderService;
  
    @Autowired
    private BudgetControlTypeService budgetControlTypeService;

    @RequestMapping(value = "/newform", method = RequestMethod.GET)
    public String showNewForm(
            @ModelAttribute("contractorBillRegister") final ContractorBillRegister contractorBillRegister,
            final Model model, final HttpServletRequest request) {
        final String woeId = request.getParameter("woeId");
        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService
                .getWorkOrderEstimateById(Long.valueOf(woeId));
        setDropDownValues(model);
        model.addAttribute("assetValues", workOrderEstimate.getAssetValues());
        model.addAttribute("documentDetails", contractorBillRegister.getDocumentDetails());
        model.addAttribute("stateType", contractorBillRegister.getClass().getSimpleName());
        model.addAttribute("woeId", woeId);
        prepareWorkflow(model, contractorBillRegister, new WorkflowContainer());
        contractorBillRegister.setBilldate(new Date());
        model.addAttribute("mode", "new");
        model.addAttribute("mbHeaders",
                mBHeaderService.getMBHeaderBasedOnBillDate(workOrderEstimate.getId(), contractorBillRegister.getBilldate()));
        // TODO: remove this condition to check if spillover
        if (workOrderEstimate.getEstimate().getLineEstimateDetails() != null
                && !workOrderEstimate.getEstimate().getLineEstimateDetails().getLineEstimate().isSpillOverFlag())
            contractorBillRegister.setBilldate(new Date());

        final OfflineStatus offlineStatus = offlineStatusService.getOfflineStatusByObjectIdAndObjectTypeAndStatus(
                workOrderEstimate.getWorkOrder().getId(), WorksConstants.WORKORDER,
                OfflineStatuses.WORK_COMMENCED.toString().toUpperCase());
        model.addAttribute("offlinestatusWorkCommencedDate",
                offlineStatus != null ? offlineStatus.getStatusDate() : "");
        model.addAttribute("workOrderEstimate", workOrderEstimate);
        model.addAttribute("contractorBillRegister", contractorBillRegister);
        if (workOrderEstimate.getEstimate().getLineEstimateDetails() != null
                && workOrderEstimate.getEstimate().getLineEstimateDetails().getLineEstimate().isSpillOverFlag()) {
            model.addAttribute("cutOffDate", worksUtils.getCutOffDate() != null ? worksUtils.getCutOffDate() : "");
            model.addAttribute("currFinYearStartDate", worksUtils.getFinancialYearByDate(new Date()).getStartingDate());
        }
        
		final AbstractEstimate estimate = workOrderEstimate.getEstimate();
		if (!estimate.getAbsrtractEstimateDeductions().isEmpty()) {
			contractorBillRegister.getEgBilldetailes().addAll(prepairBillDetailsMap(estimate, model));
			model.addAttribute("billDetailsMap",
					contractorBillRegisterService.getBillDetailsMap(contractorBillRegister, model));
		}
        
        return "contractorBill-form";
    }

    private void setDropDownValues(final Model model) {
        final List<CChartOfAccounts> contractorPayableAccountList = chartOfAccountsHibernateDAO
                .getAccountCodeByPurposeName(WorksConstants.CONTRACTOR_NETPAYABLE_PURPOSE);
        final List<CChartOfAccounts> contractorRefundAccountList = chartOfAccountsHibernateDAO
                .getAccountCodeByListOfPurposeName(WorksConstants.CONTRACTOR_REFUND_PURPOSE);
        model.addAttribute("netPayableAccounCodes", contractorPayableAccountList);
        model.addAttribute("statutoryDeductionAccounCodes",
                chartOfAccountsHibernateDAO.getAccountCodeByPurposeName(WorksConstants.CONTRACTOR_DEDUCTIONS_PURPOSE));
        model.addAttribute("retentionMoneyDeductionAccounCodes", chartOfAccountsHibernateDAO
                .getAccountCodeByPurposeName(WorksConstants.RETENTION_MONEY_DEDUCTIONS_PURPOSE));

        model.addAttribute("refundAccounCodes", contractorRefundAccountList);
        model.addAttribute("billTypes", BillTypes.values());

        final List<AppConfigValues> retentionMoneyPerForPartBillApp = appConfigValuesService
                .getConfigValuesByModuleAndKey(WorksConstants.WORKS_MODULE_NAME,
                        WorksConstants.APPCONFIG_KEY_RETENTION_MONEY_PER_FOR_PART_BILL);
        final List<AppConfigValues> retentionMoneyPerForFinalBillApp = appConfigValuesService
                .getConfigValuesByModuleAndKey(WorksConstants.WORKS_MODULE_NAME,
                        WorksConstants.APPCONFIG_KEY_RETENTION_MONEY_PER_FOR_FINAL_BILL);
        model.addAttribute("retentionMoneyPerForPartBill", retentionMoneyPerForPartBillApp.get(0).getValue());
        model.addAttribute("retentionMoneyPerForFinalBill", retentionMoneyPerForFinalBillApp.get(0).getValue());
    }

    @RequestMapping(value = "/contractorbill-save", method = RequestMethod.POST)
    public String create(@ModelAttribute("contractorBillRegister") ContractorBillRegister contractorBillRegister,
            final Model model, final BindingResult resultBinder, final HttpServletRequest request,
            @RequestParam String workFlowAction, @RequestParam("file") final MultipartFile[] files) throws IOException {

        final String woeId = request.getParameter("woeId");
        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService
                .getWorkOrderEstimateById(Long.valueOf(woeId));

        final Date workCompletionDate = contractorBillRegister.getWorkOrderEstimate().getWorkCompletionDate();

        contractorBillRegister.setWorkOrderEstimate(workOrderEstimate);
        contractorBillRegister.getWorkOrderEstimate().setWorkCompletionDate(workCompletionDate);

        contractorBillRegisterService.mergeDeductionDetails(contractorBillRegister);

        validateInput(contractorBillRegister, workOrderEstimate, resultBinder, request);
        if (StringUtils.isBlank(workFlowAction))
            validateBillDateToSkipWorkflow(contractorBillRegister, resultBinder);

        contractorBillRegister = addBillDetails(contractorBillRegister, workOrderEstimate, resultBinder, request);

        contractorBillRegisterService.validateRefundAmount(contractorBillRegister, resultBinder);

        if (!contractorBillRegisterService.checkForDuplicateAccountCodes(contractorBillRegister))
            resultBinder.reject("error.contractorbill.duplicate.accountcodes",
                    "error.contractorbill.duplicate.accountcodes");
        
        if(!contractorBillRegisterService.validateDuplicateRefundAccountCodes(contractorBillRegister)) 
            resultBinder.reject("error.contractorbill.duplicate.refund.accountcodes",
                    "error.contractorbill.duplicate.refund.accountcodes");
        
        contractorBillRegisterService.validateTotalDebitAndCreditAmount(contractorBillRegister, resultBinder);

        if (resultBinder.hasErrors()) {
            setDropDownValues(model);
            model.addAttribute("assetValues", workOrderEstimate.getAssetValues());
            model.addAttribute("woeId", woeId);
            model.addAttribute("documentDetails", contractorBillRegister.getDocumentDetails());
            model.addAttribute("netPayableAmount", request.getParameter("netPayableAmount"));
            model.addAttribute("netPayableAccountCode", request.getParameter("netPayableAccountCode"));
            model.addAttribute("stateType", contractorBillRegister.getClass().getSimpleName());
            model.addAttribute("approvalDesignation", request.getParameter("approvalDesignation"));
            model.addAttribute("approvalPosition", request.getParameter("approvalPosition"));
            prepareWorkflow(model, contractorBillRegister, new WorkflowContainer());
            model.addAttribute("mode", "new");
            model.addAttribute("billDetailsMap", getBillDetailsMap(contractorBillRegister, model));
            final OfflineStatus offlineStatus = offlineStatusService.getOfflineStatusByObjectIdAndObjectTypeAndStatus(
                    workOrderEstimate.getWorkOrder().getId(), WorksConstants.WORKORDER,
                    OfflineStatuses.WORK_COMMENCED.toString().toUpperCase());
            model.addAttribute("offlinestatusWorkCommencedDate",
                    offlineStatus != null ? offlineStatus.getStatusDate() : "");
            model.addAttribute("workOrderEstimate", workOrderEstimate);
            model.addAttribute("contractorBillRegister", contractorBillRegister);
            model.addAttribute("mbHeaders",
                    mBHeaderService.getMBHeaderBasedOnBillDate(workOrderEstimate.getId(), contractorBillRegister.getBilldate()));

            model.addAttribute("mode", "edit");

            model.addAttribute("billDetailsMap", contractorBillRegisterService.getBillDetailsMap(contractorBillRegister,model));

            return "contractorBill-form";
        } else {

            Long approvalPosition = 0l;
            String approvalComment = "";
            if (request.getParameter("approvalComment") != null)
                approvalComment = request.getParameter("approvalComent");
            if (request.getParameter("workFlowAction") != null)
                workFlowAction = request.getParameter("workFlowAction");
            if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
                approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));

            Integer partBillCount = contractorBillRegisterService.getMaxSequenceNumberByWorkOrder(workOrderEstimate);
            if (partBillCount == null || partBillCount == 0)
                partBillCount = 1;
            else
                partBillCount++;
            contractorBillRegister.setBillSequenceNumber(partBillCount);
            final ContractorBillNumberGenerator c = beanResolver
                    .getAutoNumberServiceFor(ContractorBillNumberGenerator.class);
            final String contractorBillNumber = c.getNextNumber(contractorBillRegister);
            contractorBillRegister.setBillnumber(contractorBillNumber);
            contractorBillRegister.setPassedamount(contractorBillRegister.getBillamount());
            ContractorBillRegister savedContractorBillRegister = null;
            try {
                savedContractorBillRegister = contractorBillRegisterService.create(contractorBillRegister, files,
                        approvalPosition, approvalComment, null, workFlowAction);

            } catch (final ValidationException e) {
                // TODO: Used ApplicationRuntimeException for time being since
                // there is issue in session after
                // checkBudgetAndGenerateBANumber API call. Needs to replace
                // with errors.reject
                throw new ApplicationRuntimeException("error.contractorbill.budgetcheck.insufficient.amount");
                /*
                 * for (final ValidationError error : e.getErrors()) { if(error.getMessage().contains("Budget Check failed for "))
                 * { errors.reject(messageSource.getMessage( "error.contractorbill.budgetcheck.insufficient.amount",null, null)+
                 * ". " +error.getMessage()); } else errors.reject(error.getMessage()); }
                 */
            }
            final String pathVars = worksUtils.getPathVars(savedContractorBillRegister.getStatus(),
                    savedContractorBillRegister.getState(), savedContractorBillRegister.getId(), approvalPosition);

            return "redirect:/contractorbill/contractorbill-success?pathVars=" + pathVars + "&billNumber="
                    + savedContractorBillRegister.getBillnumber();
        }
    }

    @RequestMapping(value = "/contractorbill-success", method = RequestMethod.GET)
    public String showContractorBillSuccessPage(@RequestParam("billNumber") final String billNumber, final Model model,
            final HttpServletRequest request) {

        final String[] keyNameArray = request.getParameter("pathVars").split(",");
        Long id = 0L;
        String approverName = "";
        String currentUserDesgn = "";
        String nextDesign = "";
        if (keyNameArray.length != 0 && keyNameArray.length > 0)
            if (keyNameArray.length == 1)
                id = Long.parseLong(keyNameArray[0]);
            else if (keyNameArray.length == 3) {
                id = Long.parseLong(keyNameArray[0]);
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
            } else {
                id = Long.parseLong(keyNameArray[0]);
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
                nextDesign = keyNameArray[3];
            }

        if (id != null)
            model.addAttribute("approverName", approverName);
        model.addAttribute("currentUserDesgn", currentUserDesgn);
        model.addAttribute("nextDesign", nextDesign);

        final ContractorBillRegister contractorBillRegister = contractorBillRegisterService
                .getContractorBillByBillNumber(billNumber);

        final String message = getMessageByStatus(contractorBillRegister, approverName, nextDesign);

        model.addAttribute("message", message);

        model.addAttribute("contractorBillRegister", contractorBillRegister);
        return "contractorBill-success";
    }

    private void validateInput(final ContractorBillRegister contractorBillRegister,
            final WorkOrderEstimate workOrderEstimate, final BindingResult resultBinder,
            final HttpServletRequest request) {
        final boolean validateBillInWorkflow = letterOfAcceptanceService.validateContractorBillInWorkflowForWorkorder(
                contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getId());
        if (!validateBillInWorkflow)
            resultBinder.reject("error.contractorbill.in.workflow.for.workorder",
                    new String[] { contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderNumber() },
                    null);

        BigDecimal totalBillAmountIncludingCurrentBill = contractorBillRegister.getBillamount();
        final BigDecimal totalBillAmount = contractorBillRegisterService
                .getTotalBillAmountByWorkOrder(contractorBillRegister.getWorkOrderEstimate());
        if (totalBillAmount != null)
            totalBillAmountIncludingCurrentBill = totalBillAmountIncludingCurrentBill.add(totalBillAmount);
        if (workOrderEstimate.getEstimate().getLineEstimateDetails() != null
                && workOrderEstimate.getEstimate().getLineEstimateDetails().getLineEstimate().isBillsCreated()
                && workOrderEstimate.getEstimate().getLineEstimateDetails().getGrossAmountBilled() != null)
            totalBillAmountIncludingCurrentBill = totalBillAmountIncludingCurrentBill
                    .add(workOrderEstimate.getEstimate().getLineEstimateDetails().getGrossAmountBilled());
        if (totalBillAmountIncludingCurrentBill.doubleValue() > contractorBillRegister.getWorkOrderEstimate()
                .getWorkOrder().getWorkOrderAmount())
            resultBinder.reject("error.contractorbill.totalbillamount.exceeds.workorderamount", new String[] {
                    String.valueOf(totalBillAmountIncludingCurrentBill),
                    String.valueOf(contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderAmount()) },
                    null);

        if (org.apache.commons.lang.StringUtils.isBlank(contractorBillRegister.getBilltype()))
            resultBinder.rejectValue("billtype", "error.billtype.required");
        if (contractorBillRegister.getEgBillregistermis() != null
                && contractorBillRegister.getEgBillregistermis().getPartyBillDate() != null
                && contractorBillRegister.getEgBillregistermis().getPartyBillDate()
                        .before(contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderDate()))
            resultBinder.rejectValue("egBillregistermis.partyBillDate",
                    "error.validate.partybilldate.lessthan.loadate");

        if (contractorBillRegister.getWorkOrderEstimate() != null
                && contractorBillRegister.getWorkOrderEstimate().getWorkOrderActivities().isEmpty()
                && contractorBillRegister.getMbHeader() != null) {
            if (org.apache.commons.lang.StringUtils.isBlank(contractorBillRegister.getMbHeader().getMbRefNo()))
                resultBinder.rejectValue("mbHeader.mbRefNo", "error.mbrefno.required");

            if (contractorBillRegister.getMbHeader().getMbDate() == null)
                resultBinder.rejectValue("mbHeader.mbDate", "error.mbdate.required");

            if (contractorBillRegister.getMbHeader().getFromPageNo() == null)
                resultBinder.rejectValue("mbHeader.fromPageNo", "error.frompageno.required");

            if (contractorBillRegister.getMbHeader().getToPageNo() == null)
                resultBinder.rejectValue("mbHeader.toPageNo", "error.topageno.required");

            if (contractorBillRegister.getMbHeader().getFromPageNo() == 0
                    || contractorBillRegister.getMbHeader().getToPageNo() == 0)
                resultBinder.reject("error.validate.mb.pagenumbers.zero", "error.validate.mb.pagenumbers.zero");

            if (contractorBillRegister.getMbHeader().getFromPageNo() != null
                    && contractorBillRegister.getMbHeader().getToPageNo() != null && contractorBillRegister
                            .getMbHeader().getFromPageNo() > contractorBillRegister.getMbHeader().getToPageNo())
                resultBinder.reject("error.validate.mb.frompagenumber.greaterthan.topagenumber",
                        "error.validate.mb.frompagenumber.greaterthan.topagenumber");

            if (contractorBillRegister.getMbHeader().getMbDate() != null
                    && contractorBillRegister.getMbHeader().getMbDate()
                            .before(contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderDate()))
                resultBinder.rejectValue("mbHeader.mbDate", "error.validate.mbdate.lessthan.loadate");

            if (contractorBillRegister.getMbHeader().getMbDate() != null
                    && contractorBillRegister.getBilldate()
                            .before(contractorBillRegister.getMbHeader().getMbDate()))
                resultBinder.rejectValue("mbHeader.mbDate", "error.billdate.mbdate");

        }

        if (org.apache.commons.lang.StringUtils.isBlank(request.getParameter("netPayableAccountCode")))
            resultBinder.reject("error.netpayable.accountcode.required", "error.netpayable.accountcode.required");
        if (org.apache.commons.lang.StringUtils.isBlank(request.getParameter("netPayableAmount"))
                || Double.valueOf(request.getParameter("netPayableAmount").toString()) < 0)
            resultBinder.reject("error.netpayable.amount.required", "error.netpayable.amount.required");

        // TODO: from this line code should be removed after user data entry is
        // finished.
        if (contractorBillRegister.getEgBillregistermis() != null
                && contractorBillRegister.getEgBillregistermis().getPartyBillDate() != null && contractorBillRegister
                        .getEgBillregistermis().getPartyBillDate().after(contractorBillRegister.getBilldate()))
            resultBinder.rejectValue("egBillregistermis.partyBillDate", "error.partybilldate.billdate");

        final Date workCompletionDate = contractorBillRegister.getWorkOrderEstimate().getWorkCompletionDate();
        if (contractorBillRegister.getBilltype().equals(BillTypes.Final_Bill.toString())
                && workCompletionDate == null)
            resultBinder.rejectValue("workOrderEstimate.workCompletionDate", "error.workcompletiondate.required");

        final Date currentDate = new Date();
        if (workCompletionDate != null) {
            final OfflineStatus offlineStatus = offlineStatusService.getOfflineStatusByObjectIdAndObjectTypeAndStatus(
                    workOrderEstimate.getWorkOrder().getId(), WorksConstants.WORKORDER,
                    OfflineStatuses.WORK_COMMENCED.toString().toUpperCase());
            if (workCompletionDate.after(currentDate))
                resultBinder.rejectValue("workOrderEstimate.workCompletionDate", "error.workcompletiondate.futuredate");
            if (offlineStatus != null) {
                if (workCompletionDate.before(offlineStatus.getStatusDate()))
                    resultBinder.rejectValue("workOrderEstimate.workCompletionDate",
                            "error.workcompletiondate.workcommenceddate");
            } else if (workCompletionDate
                    .before(contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderDate()))
                resultBinder.rejectValue("workOrderEstimate.workCompletionDate",
                        "error.workcompletiondate.workorderdate");
            if (workCompletionDate.after(contractorBillRegister.getBilldate()))
                resultBinder.rejectValue("workOrderEstimate.workCompletionDate", "error.workcompletiondate.billdate");
        }

        if (workOrderEstimate.getEstimate().getLineEstimateDetails() != null
                && workOrderEstimate.getEstimate().getLineEstimateDetails().getLineEstimate().isSpillOverFlag()) {
            final Date currentFinYearStartDate = worksUtils.getFinancialYearByDate(currentDate).getStartingDate();
            if (contractorBillRegister.getBilldate().after(currentDate))
                resultBinder.rejectValue("billdate", "error.billdate.futuredate");
            if (contractorBillRegister.getBilldate()
                    .before(contractorBillRegister.getWorkOrderEstimate().getWorkOrder().getWorkOrderDate()))
                resultBinder.rejectValue("billdate", "error.billdate.workorderdate");
            if (contractorBillRegister.getBilldate().before(currentFinYearStartDate))
                resultBinder.rejectValue("billdate", "error.billdate.finyear");
        }

        final MBHeader mBHeader = mBHeaderService.getLatestMBHeaderToValidateBillDate(
                contractorBillRegister.getWorkOrderEstimate().getId(), contractorBillRegister.getBilldate());
        if (mBHeader != null && contractorBillRegister.getBilldate().before(mBHeader.getMbDate()))
            resultBinder.rejectValue("mbHeader.mbDate", "error.billdate.mbdate");

        if (contractorBillRegister.getWorkOrderEstimate() != null
                && !contractorBillRegister.getWorkOrderEstimate().getWorkOrderActivities().isEmpty()) {
            final List<MBHeader> mbheaders = mBHeaderService.getMBHeaderBasedOnBillDate(
                    contractorBillRegister.getWorkOrderEstimate().getId(), contractorBillRegister.getBilldate());
            if (mbheaders != null && mbheaders.isEmpty())
                resultBinder.reject("error.mbnotexists.tocreatebill", "error.mbnotexists.tocreatebill");
        }

    }

    private String getMessageByStatus(final ContractorBillRegister contractorBillRegister, final String approverName,
            final String nextDesign) {
        String message = "";

        if (contractorBillRegister.getStatus().getCode().equals(ContractorBillRegister.BillStatus.CREATED.toString())) {
            if (StringUtils.isNotBlank(contractorBillRegister.getEgBillregistermis().getBudgetaryAppnumber())
                    && !BudgetControlType.BudgetCheckOption.NONE.toString()
                            .equalsIgnoreCase(budgetControlTypeService.getConfigValue()))
                message = messageSource.getMessage("msg.contractorbill.create.success.with.budgetappropriation",
                        new String[] { contractorBillRegister.getBillnumber(), approverName, nextDesign,
                                contractorBillRegister.getEgBillregistermis().getBudgetaryAppnumber() },
                        null);
            else
                message = messageSource.getMessage("msg.contractorbill.create.success",
                        new String[] { contractorBillRegister.getBillnumber(), approverName, nextDesign }, null);

        } else if (contractorBillRegister.getStatus().getCode()
                .equalsIgnoreCase(ContractorBillRegister.BillStatus.APPROVED.toString()))
            message = messageSource.getMessage("msg.contractorbill.approved.success",
                    new String[] { contractorBillRegister.getBillnumber() }, null);
        else if (contractorBillRegister.getStatus().getCode()
                .equalsIgnoreCase(ContractorBillRegister.BillStatus.RESUBMITTED.toString()))
            message = messageSource.getMessage("msg.contractorbill.resubmit.success",
                    new String[] { contractorBillRegister.getBillnumber(), approverName, nextDesign }, null);
        else if (contractorBillRegister.getState().getValue().equalsIgnoreCase(WorksConstants.WF_STATE_REJECTED))
            message = messageSource.getMessage("msg.contractorbill.reject",
                    new String[] { contractorBillRegister.getBillnumber(), approverName, nextDesign }, null);
        else if (contractorBillRegister.getState().getValue().equalsIgnoreCase(WorksConstants.WF_STATE_CANCELLED))
            message = messageSource.getMessage("msg.contractorbill.cancel",
                    new String[] { contractorBillRegister.getBillnumber() }, null);
        else if (contractorBillRegister.getStatus().getCode().equalsIgnoreCase(WorksConstants.WF_STATE_REJECTED))
            message = messageSource.getMessage("msg.contractorbill.forward.success",
                    new String[] { contractorBillRegister.getBillnumber(), approverName, nextDesign }, null);

        return message;
    }

    private ContractorBillRegister addBillDetails(final ContractorBillRegister contractorBillRegister,
            final WorkOrderEstimate workOrderEstimate, final BindingResult resultBinder,
            final HttpServletRequest request) {

        if (contractorBillRegister.getBillDetailes() == null || contractorBillRegister.getBillDetailes().isEmpty())
            resultBinder.reject("error.contractorbill.accountdetails.required",
                    "error.contractorbill.accountdetails.required");
        for (final EgBilldetails egBilldetails : contractorBillRegister.getBillDetailes())
            if(!contractorBillRegister.getEgBilldetailes().isEmpty() && contractorBillRegister.getEgBilldetailes().size() == 1) {
                for(final EgBilldetails refundBill : contractorBillRegister.getRefundBillDetails()) {
                    if (refundBill.getGlcodeid() != null)
                        contractorBillRegister.addEgBilldetailes(contractorBillRegisterService.getBillDetails(contractorBillRegister, refundBill,
                                workOrderEstimate, resultBinder, request));
                }
                if(egBilldetails.getGlcodeid() != null) {
                contractorBillRegister.addEgBilldetailes(contractorBillRegisterService.getBillDetails(contractorBillRegister, egBilldetails,
                        workOrderEstimate, resultBinder, request));
                }
            } else {
            if (egBilldetails.getGlcodeid() != null)            
                contractorBillRegister.addEgBilldetailes(contractorBillRegisterService.getBillDetails(contractorBillRegister, egBilldetails,
                        workOrderEstimate, resultBinder, request));
            }
        final String netPayableAccountCodeId = request.getParameter("netPayableAccountCode");
        final String netPayableAmount = request.getParameter("netPayableAmount");
        if (org.apache.commons.lang.StringUtils.isNotBlank(netPayableAccountCodeId)
                && org.apache.commons.lang.StringUtils.isNotBlank(netPayableAmount)) {
            final EgBilldetails billdetails = new EgBilldetails();
            billdetails.setGlcodeid(new BigDecimal(netPayableAccountCodeId));
            billdetails.setCreditamount(new BigDecimal(netPayableAmount));
            contractorBillRegister.addEgBilldetailes(
                    contractorBillRegisterService.getBillDetails(contractorBillRegister, billdetails, workOrderEstimate, resultBinder, request));

        }

        return contractorBillRegister;
    }

    public List<Map<String, Object>> getBillDetailsMap(final ContractorBillRegister contractorBillRegister,
            final Model model) {
        final List<Map<String, Object>> billDetailsList = new ArrayList<Map<String, Object>>();
        Map<String, Object> billDetails = new HashMap<String, Object>();

        final List<CChartOfAccounts> contractorNetPayableAccountList = chartOfAccountsHibernateDAO
                .getAccountCodeByPurposeName(WorksConstants.CONTRACTOR_NETPAYABLE_PURPOSE);
        final List<CChartOfAccounts> contractorDeductionAccountList = chartOfAccountsHibernateDAO
                .getAccountCodeByPurposeName(WorksConstants.CONTRACTOR_DEDUCTIONS_PURPOSE);
        final List<CChartOfAccounts> retentionMoneyDeductionAccountList = chartOfAccountsHibernateDAO
                .getAccountCodeByPurposeName(WorksConstants.RETENTION_MONEY_DEDUCTIONS_PURPOSE);
        for (final EgBilldetails egBilldetails : contractorBillRegister.getEgBilldetailes()) {
            if (egBilldetails.getDebitamount() != null) {
                billDetails = new HashMap<String, Object>();
                final CChartOfAccounts coa = chartOfAccountsHibernateDAO
                        .findById(egBilldetails.getGlcodeid().longValue(), false);
                billDetails.put("glcodeId", coa.getId());
                billDetails.put("glcode", coa.getGlcode());
                billDetails.put("accountHead", coa.getName());
                billDetails.put("amount", egBilldetails.getDebitamount());
                billDetails.put("isDebit", true);
                billDetails.put("isNetPayable", false);
            } else if (egBilldetails.getCreditamount() != null) {
                billDetails = new HashMap<String, Object>();
                final CChartOfAccounts coa = chartOfAccountsHibernateDAO
                        .findById(egBilldetails.getGlcodeid().longValue(), false);
                billDetails.put("glcodeId", coa.getId());
                billDetails.put("glcode", coa.getGlcode());
                billDetails.put("accountHead", coa.getName());
                billDetails.put("amount", egBilldetails.getCreditamount());
                billDetails.put("isDebit", false);
                if (contractorNetPayableAccountList != null && !contractorNetPayableAccountList.isEmpty()
                        && contractorNetPayableAccountList.contains(coa)) {
                    billDetails.put("isNetPayable", true);
                    model.addAttribute("netPayableAccountId", egBilldetails.getId());
                    model.addAttribute("netPayableAccountCode", coa.getId());
                    model.addAttribute("netPayableAmount", egBilldetails.getCreditamount());
                } else {
                    billDetails.put("isNetPayable", false);
                    if (contractorDeductionAccountList != null && !contractorDeductionAccountList.isEmpty()
                            && contractorDeductionAccountList.contains(coa))
                        billDetails.put("isStatutoryDeduction", true);
                    else
                        billDetails.put("isStatutoryDeduction", false);
                    if (retentionMoneyDeductionAccountList != null && !retentionMoneyDeductionAccountList.isEmpty()
                            && retentionMoneyDeductionAccountList.contains(coa))
                        billDetails.put("isRetentionMoneyDeduction", true);
                    else
                        billDetails.put("isRetentionMoneyDeduction", false);
                }
            }
            billDetailsList.add(billDetails);
        }
        return billDetailsList;
    }

    private void validateBillDateToSkipWorkflow(final ContractorBillRegister contractorBillRegister,
            final BindingResult resultBinder) {
        final Date cutOffDate = worksUtils.getCutOffDate();
        final SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
        final Date currFinYearStartDate = worksUtils.getFinancialYearByDate(new Date()).getStartingDate();
        if (cutOffDate != null && (contractorBillRegister.getBilldate().before(currFinYearStartDate)
                || contractorBillRegister.getBilldate().after(cutOffDate)))
            resultBinder.reject("error.billdate.cutoffdate",
                    new String[] { fmt.format(cutOffDate) },
                    null);
    }
    
	public List<EgBilldetails> prepairBillDetailsMap(final AbstractEstimate abstractEstimate, final Model model) {
		EgBilldetails egBilldetails = null;
		List<EgBilldetails> billList = new ArrayList<EgBilldetails>();
		for (final AbstractEstimateDeduction deduction : abstractEstimate.getAbsrtractEstimateDeductions()) {
			egBilldetails = new EgBilldetails();
			egBilldetails.setCreditamount(BigDecimal.ZERO);
			egBilldetails.setGlcodeid(new BigDecimal(deduction.getChartOfAccounts().getId()));
			billList.add(egBilldetails);
		}
		return billList;

	}

}
