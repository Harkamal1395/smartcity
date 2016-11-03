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

package org.egov.tl.web.actions.search;

import static org.egov.infra.web.struts.actions.BaseFormAction.NEW;
import static org.egov.infra.web.utils.WebUtils.toJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.web.struts.actions.BaseFormAction;
import org.egov.tl.entity.License;
import org.egov.tl.entity.TradeLicense;
import org.egov.tl.service.TradeLicenseService;
import org.egov.tl.service.masters.LicenseCategoryService;
import org.egov.tl.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;

import com.opensymphony.xwork2.validator.annotations.Validations;

@ParentPackage("egov")
@Validations
@Results({ @Result(name = NEW, location = "searchTrade-new.jsp") })
public class SearchTradeAction extends BaseFormAction {
    private static final long serialVersionUID = 1L;
    private final SearchForm searchForm = new SearchForm();
    private String roleName;
    private String applicationNumber;
    private String licenseNumber;
    private String oldLicenseNumber;
    private Long categoryId;
    private Long subCategoryId;
    private String tradeTitle;
    private String tradeOwnerName;
    private String propertyAssessmentNo;
    private String mobileNo;
    private Boolean isCancelled;
    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    @Qualifier("licenseCategoryService")
    protected LicenseCategoryService licenseCategoryService;

    @Autowired
    private TradeLicenseService tradeLicenseService;

    @Override
    public Object getModel() {
        return searchForm;
    }

    @Override
    public void prepare() {
        super.prepare();
        addDropdownData("categoryList", licenseCategoryService.findAll());
        addDropdownData("subCategoryList", Collections.emptyList());
        setRoleName(securityUtils.getCurrentUser().getRoles().toString());
    }

    @Action(value = "/search/searchTrade-newForm")
    public String newForm() {
        return BaseFormAction.NEW;
    }

    @SuppressWarnings("unchecked")
    @Action(value = "/search/searchTrade-search")
    public void search() throws IOException {
        List<SearchForm> resultList = new ArrayList<SearchForm>();
        String result = null;
        final List<TradeLicense> licenses = tradeLicenseService
                .searchTradeLicense(applicationNumber, licenseNumber, oldLicenseNumber, categoryId, subCategoryId,
                        tradeTitle, tradeOwnerName, propertyAssessmentNo, mobileNo, isCancelled);
        resultList = prepareOutput(licenses);
        // for converting resultList to JSON objects.
        // Write back the JSON Response.
        result = new StringBuilder("{ \"data\":").append(toJSON(resultList, SearchForm.class,
                SearchTradeResultHelperAdaptor.class)).append("}").toString();
        final HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        IOUtils.write(result, response.getWriter());
    }

    /**
     * @param licenseList
     * @return
     */
    private List<SearchForm> prepareOutput(final List<? extends License> licenseList) {
        final List<SearchForm> finalList = new LinkedList<SearchForm>();
        SearchForm searchFormInfo;
        List<String> licenseActions;
        for (final License license : licenseList) {
            searchFormInfo = new SearchForm();
            searchFormInfo.setLicenseId(license.getId());
            searchFormInfo.setApplicationNumber(license.getApplicationNumber());
            searchFormInfo.setLicenseNumber(license.getLicenseNumber());
            searchFormInfo.setOldLicenseNumber(license.getOldLicenseNumber());
            searchFormInfo.setCategoryName(license.getCategory().getName());
            searchFormInfo.setSubCategoryName(license.getTradeName().getName());
            searchFormInfo.setTradeTitle(license.getNameOfEstablishment());
            searchFormInfo.setTradeOwnerName(license.getLicensee().getApplicantName());
            searchFormInfo.setMobileNo(license.getLicensee().getMobilePhoneNumber());
            searchFormInfo.setPropertyAssessmentNo(license.getAssessmentNo() != null ? license.getAssessmentNo() : "");
            licenseActions = new ArrayList<String>();
            licenseActions.add("View Trade");
            // FIXME EgwStatus usage should be removed from here
            if (license.getStatus() != null) {
                if (roleName.contains(Constants.ROLE_BILLCOLLECTOR) && !license.isPaid() && !license.isStateRejected()
                        && (license.getStatus().getStatusCode().equals(Constants.STATUS_ACKNOLEDGED) || license.getState() != null
                                && license.getState().getValue().equals(Constants.WF_STATE_COMMISSIONER_APPROVED_STR)))
                    licenseActions.add("Collect Fees");
                else if (license.getStatus() != null
                        && license.getStatus().getStatusCode().equalsIgnoreCase(Constants.STATUS_ACTIVE)
                        && (roleName.contains(Constants.TL_CREATOR_ROLENAME)
                                || roleName.contains(Constants.TL_APPROVER_ROLENAME)))
                    licenseActions.add("Print Certificate");
                if (license.getStatus().getStatusCode().equals(Constants.STATUS_UNDERWORKFLOW))
                    licenseActions.add("Print Provisional Certificate");
            } else if (license.isLegacy() && !license.isPaid())
                licenseActions.add("Modify Legacy License");
            if (roleName.contains(Constants.TL_CREATOR_ROLENAME) || roleName.contains(Constants.TL_APPROVER_ROLENAME))
                if (!license.isPaid() && !license.getLicenseAppType().getName().equals(Constants.RENEWAL_LIC_APPTYPE)
                        && license.getStatus() != null
                        && license.getStatus().getStatusCode().equalsIgnoreCase(Constants.STATUS_ACTIVE))
                    licenseActions.add("Renew License");
            if (roleName.contains(Constants.TL_APPROVER_ROLENAME))
                if (license.getDateOfExpiry() != null && checkForRenewalNotice(license.getDateOfExpiry()))
                    licenseActions.add("Renewal Notice");
            searchFormInfo.setActions(licenseActions);
            finalList.add(searchFormInfo);
        }
        return finalList;
    }

    @Override
    public boolean acceptableParameterName(final String paramName) {
        final List<String> nonAcceptable = Arrays.asList(new String[] { "struts.token.name", "struts.token", "token.name" });
        final boolean retValue = super.acceptableParameterName(paramName);
        return retValue ? !nonAcceptable.contains(paramName) : retValue;
    }

    public boolean checkForRenewalNotice(final Date dateOfExpiry) {
        boolean readyForRenewal = false;
        final Calendar currentDate = Calendar.getInstance();
        final Calendar renewalDate = Calendar.getInstance();
        renewalDate.setTime(dateOfExpiry);
        renewalDate.add(Calendar.DATE, Constants.RENEWALTIMEPERIOD);

        if (renewalDate.before(currentDate) || renewalDate.equals(currentDate))
            readyForRenewal = true;
        return readyForRenewal;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(final String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getOldLicenseNumber() {
        return oldLicenseNumber;
    }

    public void setOldLicenseNumber(final String oldLicenseNumber) {
        this.oldLicenseNumber = oldLicenseNumber;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(final Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getTradeTitle() {
        return tradeTitle;
    }

    public void setTradeTitle(final String tradeTitle) {
        this.tradeTitle = tradeTitle;
    }

    public String getTradeOwnerName() {
        return tradeOwnerName;
    }

    public void setTradeOwnerName(final String tradeOwnerName) {
        this.tradeOwnerName = tradeOwnerName;
    }

    public String getPropertyAssessmentNo() {
        return propertyAssessmentNo;
    }

    public void setPropertyAssessmentNo(final String propertyAssessmentNo) {
        this.propertyAssessmentNo = propertyAssessmentNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(final String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(final Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

}
