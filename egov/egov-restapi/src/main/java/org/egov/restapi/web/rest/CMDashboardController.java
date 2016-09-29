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

package org.egov.restapi.web.rest;

import java.util.List;

import org.egov.infra.web.utils.WebUtils;
import org.egov.restapi.model.StateCityInfo;
import org.egov.restapi.model.dashboard.CollectionIndexDetails;
import org.egov.restapi.model.dashboard.ConsolidatedCollDetails;
import org.egov.restapi.service.DashboardService;
import org.egov.restapi.util.JsonConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller used to provide services for CM Dashboard
 */

@RestController
public class CMDashboardController {

	@Autowired
	private DashboardService dashboardService;
	
	/**
	 * Gives the State-City information across all ULBs
	 * @return string
	 */
	@ExceptionHandler(Exception.class)
	@RequestMapping(value = "/statecityinfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getStateCityInformation() {
        final List<StateCityInfo> stateDetails = dashboardService.getStateCityDetails();
        final String result = new StringBuilder("{ \"data\":").append(WebUtils.toJSON(stateDetails, 
        		StateCityInfo.class, StateInfoHelperAdaptor.class)).append("}").toString();
        return result;
    }

	/**
	 * Provides State-wise Collection Statistics
	 * @return response JSON
	 */
	@RequestMapping(value = "/collectionstats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getConsolidatedCollDetails(){
		ConsolidatedCollDetails consolidatedCollData = dashboardService.getConsolidatedCollDetails();
		return JsonConvertor.convert(consolidatedCollData);
	}
	
	/**
	 * Provides Collection Index details across all ULBs 
	 * @return response JSON
	 */
	@RequestMapping(value = "/collectiondashboard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCollectionDetails(@RequestBody String collDetailsRequest){
		CollectionIndexDetails collectionDetails = dashboardService.getCollectionIndexDetails();
        return JsonConvertor.convert(collectionDetails);
	}
	
}