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
package org.egov.works.web.controller.offlinestatus;

import java.util.List;

import org.egov.works.letterofacceptance.entity.SearchRequestLetterOfAcceptance;
import org.egov.works.letterofacceptance.service.LetterOfAcceptanceService;
import org.egov.works.offlinestatus.service.OfflineStatusService;
import org.egov.works.web.adaptor.SearchLetterOfAcceptanceJsonAdaptor;
import org.egov.works.workorder.entity.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping(value = "/offlinestatus")
public class AjaxOfflineStatusController {

    @Autowired
    private OfflineStatusService offlineStatusService;

    @Autowired
    private LetterOfAcceptanceService letterOfAcceptanceService;

    @Autowired
    private SearchLetterOfAcceptanceJsonAdaptor searchLetterOfAcceptanceJsonAdaptor;

    @RequestMapping(value = "/ajaxsearch-loa", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String ajaxSearch(final Model model,
            @ModelAttribute final SearchRequestLetterOfAcceptance searchRequestLetterOfAcceptance) {
        final List<WorkOrder> searchLoaList = offlineStatusService
                .searchLetterOfAcceptanceForOfflineStatus(searchRequestLetterOfAcceptance);
        final String result = new StringBuilder("{ \"data\":").append(toSearchLetterOfAcceptanceJson(searchLoaList))
                .append("}").toString();
        return result;
    }

    public Object toSearchLetterOfAcceptanceJson(final Object object) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(WorkOrder.class, searchLetterOfAcceptanceJsonAdaptor).create();
        final String json = gson.toJson(object);
        return json;
    }

    @RequestMapping(value = "/ajaxestimatenumbers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<String> findEstimateNumbersForLOA(@RequestParam final String estimateNumber) {
        return letterOfAcceptanceService.getEstimateNumbersForApprovedLoa(estimateNumber);
    }

}