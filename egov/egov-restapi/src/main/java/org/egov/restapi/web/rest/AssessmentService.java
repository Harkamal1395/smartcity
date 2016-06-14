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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.egov.dcb.bean.ChequePayment;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.utils.StringUtils;
import org.egov.infra.validation.exception.ValidationError;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.entity.property.Document;
import org.egov.ptis.domain.entity.property.PropertyTypeMaster;
import org.egov.ptis.domain.model.AssessmentDetails;
import org.egov.ptis.domain.model.DrainageEnum;
import org.egov.ptis.domain.model.ErrorDetails;
import org.egov.ptis.domain.model.FloorDetails;
import org.egov.ptis.domain.model.LocalityDetails;
import org.egov.ptis.domain.model.MasterCodeNamePairDetails;
import org.egov.ptis.domain.model.NewPropertyDetails;
import org.egov.ptis.domain.model.OwnerDetails;
import org.egov.ptis.domain.model.PayPropertyTaxDetails;
import org.egov.ptis.domain.model.PropertyTaxDetails;
import org.egov.ptis.domain.model.ReceiptDetails;
import org.egov.ptis.domain.model.RestAssessmentDetails;
import org.egov.ptis.domain.model.RestPropertyTaxDetails;
import org.egov.ptis.domain.model.enums.BasicPropertyStatus;
import org.egov.ptis.domain.service.property.PropertyExternalService;
import org.egov.restapi.model.AmenitiesDetails;
import org.egov.restapi.model.AssessmentRequest;
import org.egov.restapi.model.AssessmentsDetails;
import org.egov.restapi.model.BuildingPlanDetails;
import org.egov.restapi.model.ConstructionTypeDetails;
import org.egov.restapi.model.CorrespondenceAddressDetails;
import org.egov.restapi.model.CreatePropertyDetails;
import org.egov.restapi.model.LocalityCodeDetails;
import org.egov.restapi.model.OwnershipCategoryDetails;
import org.egov.restapi.model.PropertyAddressDetails;
import org.egov.restapi.model.PropertyTaxBoundaryDetails;
import org.egov.restapi.model.SurroundingBoundaryDetails;
import org.egov.restapi.model.VacantLandDetails;
import org.egov.restapi.util.JsonConvertor;
import org.egov.restapi.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AssessmentService class is used as the RESTFul service to handle user request and response.
 * 
 * @author ranjit
 *
 */
@RestController
public class AssessmentService {

    @Autowired
    private PropertyExternalService propertyExternalService;
    @Autowired
    private ValidationUtil validationUtil;

    /**
     * This method is used for handling user request for assessment details.
     * 
     * @param assessmentNumber - assessment number i.e. property id
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/assessmentDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getAssessmentDetails(@RequestBody String assessmentRequest)
            throws JsonGenerationException, JsonMappingException, IOException {
        AssessmentRequest assessmentReq = (AssessmentRequest) getObjectFromJSONRequest(assessmentRequest,
                AssessmentRequest.class);
        AssessmentDetails assessmentDetail = propertyExternalService
                .loadAssessmentDetails(assessmentReq.getAssessmentNo(), PropertyExternalService.FLAG_FULL_DETAILS,
                        BasicPropertyStatus.ACTIVE);
        return getJSONResponse(assessmentDetail);
    }

    /**
     * This method is used get the property tax details.
     * 
     * @param assessmentNo - assessment no
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertytaxdetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getPropertyTaxDetails(@RequestBody String assessmentRequest)
            throws JsonGenerationException, JsonMappingException, IOException {

        PropertyTaxDetails propertyTaxDetails = new PropertyTaxDetails();
        AssessmentRequest assessmentReq = (AssessmentRequest) getObjectFromJSONRequest(assessmentRequest,
                AssessmentRequest.class);
        try {
            String assessmentNo = assessmentReq.getAssessmentNo();
            if (null != assessmentNo) {
                propertyTaxDetails = propertyExternalService.getPropertyTaxDetails(assessmentNo);
            } else {
                ErrorDetails errorDetails = getInvalidCredentialsErrorDetails();
                propertyTaxDetails.setErrorDetails(errorDetails);
            }
            if (propertyTaxDetails.getOwnerDetails() == null)
            {
                propertyTaxDetails.setOwnerDetails(new ArrayList<OwnerDetails>(0));
            }
            if (propertyTaxDetails.getLocalityName() == null)
                propertyTaxDetails.setLocalityName("");
            if (propertyTaxDetails.getPropertyAddress() == null)
                propertyTaxDetails.setPropertyAddress("");
            if (propertyTaxDetails.getTaxDetails() == null)
            {
                RestPropertyTaxDetails ar = new RestPropertyTaxDetails();
                List taxDetails = new ArrayList<RestPropertyTaxDetails>(0);
                taxDetails.add(ar);
                propertyTaxDetails.setTaxDetails(taxDetails);
            }
        } catch (Exception e) {
            List<ErrorDetails> errorList = new ArrayList<ErrorDetails>(0);
            ErrorDetails er = new ErrorDetails();
            er.setErrorCode(e.getMessage());
            er.setErrorMessage(e.getMessage());
            errorList.add(er);
            return JsonConvertor.convert(errorList);
        }
        return JsonConvertor.convert(propertyTaxDetails);
    }

    /**
     * This method is used get the property tax details.
     * 
     * @param assessmentNo - assessment no
     * @param ownerName - Owner Name
     * @param mobileNumber - Mobile Number
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    /**
     */
    @RequestMapping(value = "/property/propertytaxdetailsByOwnerDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getPropertyTaxDetailsByOwnerDetails(@RequestBody String assessmentRequest)
            throws JsonGenerationException, JsonMappingException, IOException {

        List<PropertyTaxDetails> propertyTaxDetailsList = new ArrayList<PropertyTaxDetails>();
        AssessmentRequest assessmentReq = (AssessmentRequest) getObjectFromJSONRequest(assessmentRequest,
                AssessmentRequest.class);
        try {
            String assessmentNo = assessmentReq.getAssessmentNo();
            String ownerName = assessmentReq.getOwnerName();
            String mobileNumber = assessmentReq.getMobileNumber();
            if (!StringUtils.isBlank(assessmentNo) || !StringUtils.isBlank(ownerName) || !StringUtils.isBlank(mobileNumber)) {
                propertyTaxDetailsList = propertyExternalService.getPropertyTaxDetails(assessmentNo, ownerName, mobileNumber);
            } else {
                ErrorDetails errorDetails = getInvalidCredentialsErrorDetails();
                PropertyTaxDetails propertyTaxDetails = new PropertyTaxDetails();
                propertyTaxDetails.setErrorDetails(errorDetails);
                propertyTaxDetailsList.add(propertyTaxDetails);
            }

            for (PropertyTaxDetails propertyTaxDetails : propertyTaxDetailsList) {
                if (propertyTaxDetails.getOwnerDetails() == null) {
                    propertyTaxDetails.setOwnerDetails(new ArrayList<OwnerDetails>(0));
                }
                if (propertyTaxDetails.getLocalityName() == null)
                    propertyTaxDetails.setLocalityName("");
                if (propertyTaxDetails.getPropertyAddress() == null)
                    propertyTaxDetails.setPropertyAddress("");
                if (propertyTaxDetails.getTaxDetails() == null) {
                    RestPropertyTaxDetails ar = new RestPropertyTaxDetails();
                    List<RestPropertyTaxDetails> taxDetails = new ArrayList<RestPropertyTaxDetails>(0);
                    taxDetails.add(ar);
                    propertyTaxDetails.setTaxDetails(taxDetails);
                }
            }
        } catch (Exception e) {
            List<ErrorDetails> errorList = new ArrayList<ErrorDetails>(0);
            ErrorDetails er = new ErrorDetails();
            er.setErrorCode(e.getMessage());
            er.setErrorMessage(e.getMessage());
            errorList.add(er);
            return JsonConvertor.convert(errorList);
        }
        return JsonConvertor.convert(propertyTaxDetailsList);
    }

    /**
     * This method is used to search the property based on boundary details.
     * 
     * @param propertyTaxBoundaryDetails - boundary details request
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertyTaxDetailsByBoundary", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getPropertyTaxDetailsByBoundary(@RequestBody String propertyTaxBoundaryDetails)
            throws JsonGenerationException, JsonMappingException, IOException {
        PropertyTaxBoundaryDetails propTaxBoundaryDetails = (PropertyTaxBoundaryDetails) getObjectFromJSONRequest(
                propertyTaxBoundaryDetails, PropertyTaxBoundaryDetails.class);
        String circleName = propTaxBoundaryDetails.getCircleName();
        String zoneName = propTaxBoundaryDetails.getZoneName();
        String wardName = propTaxBoundaryDetails.getWardName();
        String blockName = propTaxBoundaryDetails.getBlockName();
        String ownerName = propTaxBoundaryDetails.getOwnerName();
        String doorNo = propTaxBoundaryDetails.getDoorNo();
        String aadhaarNumber = propTaxBoundaryDetails.getAadhaarNumber();
        String mobileNumber = propTaxBoundaryDetails.getMobileNumber();
        List<PropertyTaxDetails> propertyTaxDetailsList = propertyExternalService.getPropertyTaxDetails(circleName,
                zoneName, wardName, blockName, ownerName, doorNo, aadhaarNumber, mobileNumber);
        return getJSONResponse(propertyTaxDetailsList);
    }

    /**
     * This method is used to pay the property tax.
     * 
     * @param assessmentNo - assessment number
     * @param paymentMode - mode of payment
     * @param totalAmount - total amount paid
     * @param paidBy - payer name
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/paypropertytax", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String payPropertyTax(@RequestBody String payPropertyTaxDetails, final HttpServletRequest request)
            throws JsonGenerationException, JsonMappingException, IOException {
        String responseJson;
        try {
            responseJson = new String();
            PayPropertyTaxDetails payPropTaxDetails = (PayPropertyTaxDetails) getObjectFromJSONRequest(
                    payPropertyTaxDetails, PayPropertyTaxDetails.class);

            ErrorDetails errorDetails = validationUtil.validatePaymentDetails(payPropTaxDetails);
            if (null != errorDetails) {
                responseJson = JsonConvertor.convert(errorDetails);
            } else {
                payPropTaxDetails.setSource(request.getSession().getAttribute("source") != null ? request.getSession()
                        .getAttribute("source").toString()
                        : "");
                ReceiptDetails receiptDetails = propertyExternalService.payPropertyTax(payPropTaxDetails);
                responseJson = JsonConvertor.convert(receiptDetails);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
            List<ErrorDetails> errorList = new ArrayList<ErrorDetails>(0);

            List<ValidationError> errors = e.getErrors();
            for (ValidationError ve : errors)
            {
                ErrorDetails er = new ErrorDetails();
                er.setErrorCode(ve.getKey());
                er.setErrorMessage(ve.getMessage());
                errorList.add(er);
            }
            responseJson = JsonConvertor.convert(errorList);
        } catch (Exception e) {
            e.printStackTrace();
            List<ErrorDetails> errorList = new ArrayList<ErrorDetails>(0);
            ErrorDetails er = new ErrorDetails();
            er.setErrorCode(e.getMessage());
            er.setErrorMessage(e.getMessage());
            errorList.add(er);
            responseJson = JsonConvertor.convert(errorList);
        }
        return responseJson;
    }

    /**
     * This method is used to pay the water tax.
     * 
     * @param consumerNo - consumer number
     * @param paymentMode - mode of payment
     * @param totalAmount - total amount paid
     * @param paidBy - payer's name
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */

    /**
     * This method is used to get the property type master details
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/ownershipCategories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getOwnershipCategories() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> propTypeMasterDetailsList = propertyExternalService
                .getPropertyTypeMasterDetails();
        return getJSONResponse(propTypeMasterDetailsList);
    }

    /**
     * This method returns Ownership Category for the given code.
     * 
     * @param ownershipCategoryCode
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/ownershipCategoryByCode", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getOwnershipCategoryByCode(@RequestBody String ownershipCategoryDetails)
            throws JsonGenerationException, JsonMappingException, IOException {
        OwnershipCategoryDetails ownershipCategory = (OwnershipCategoryDetails) getObjectFromJSONRequest(
                ownershipCategoryDetails, OwnershipCategoryDetails.class);
        PropertyTypeMaster propertyTypeMaster = propertyExternalService
                .getPropertyTypeMasterByCode(ownershipCategory.getOwnershipCategoryCode());
        return getJSONResponse(propertyTypeMaster);
    }

    /**
     * This method is used to get the property type based on ownership category
     * 
     * @param categoryCode - property category code
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertyTypesByOwnership", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getPropertyTypeCategoryDetails(@RequestBody String ownershipCategoryDetails)
            throws JsonGenerationException, JsonMappingException, IOException {
        OwnershipCategoryDetails ownershipCategory = (OwnershipCategoryDetails) getObjectFromJSONRequest(
                ownershipCategoryDetails, OwnershipCategoryDetails.class);
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService
                .getPropertyTypeCategoryDetails(ownershipCategory.getOwnershipCategoryCode());
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get the property type based one category
     * 
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertyTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getPropertyTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getPropertyTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all the apartments and complexes.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/apartments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getApartmentsAndComplexes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService
                .getApartmentsAndComplexes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get reasons for create the property.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/createPropertyReasons", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getCreatePropertyReasons() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService
                .getReasonsForChangeProperty(PropertyTaxConstants.PROP_CREATE_RSN);
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all localities.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/localities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getLocalities() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getLocalities();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all localities.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/boundaryByLocalityCode", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String getBoundaryByLocalityCode(@RequestBody String localityCodeDetails)
            throws JsonGenerationException, JsonMappingException, IOException {
        LocalityCodeDetails locCodeDetails = (LocalityCodeDetails) getObjectFromJSONRequest(localityCodeDetails,
                LocalityCodeDetails.class);
        LocalityDetails localityDetails = propertyExternalService
                .getLocalityDetailsByLocalityCode(locCodeDetails.getLocalityCode());
        return getJSONResponse(localityDetails);
    }

    /**
     * This method is used to get all list of all the election wards.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/electionWards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getElectionWards() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getElectionBoundaries();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all list of all the enumeration blocks.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/enumerationBlocks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getEnumerationBlocks() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getEnumerationBlocks();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all types of floors.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/floorTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getFloorTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getFloorTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all type of roofs.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/roofTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getRoofTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getRoofTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all list of all type of walls.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/wallTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getWallTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getWallTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all list of all type of woods
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/woodTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getWoodTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getWoodTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all list of floor numbers.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/floors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getFloors() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = new ArrayList<MasterCodeNamePairDetails>();
        TreeMap<Integer, String> floorMap = PropertyTaxConstants.FLOOR_MAP;
        Set<Integer> keys = floorMap.keySet();
        for (Integer key : keys) {
            MasterCodeNamePairDetails mstrCodeNamePairDetails = new MasterCodeNamePairDetails();
            mstrCodeNamePairDetails.setCode(key.toString());
            mstrCodeNamePairDetails.setName(floorMap.get(key));
            mstrCodeNamePairDetailsList.add(mstrCodeNamePairDetails);
        }
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all classifications of the property structures.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertyClassifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getPropertyClassifications() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService
                .getBuildingClassifications();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get nature of usages of the property.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/propertyUsages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getPropertUsages() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getNatureOfUsages();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all list of occupancies.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/occupancyTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getOccupancyTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getOccupancies();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all the tax exemption categories.
     * 
     * @param username - usernam credential
     * @param password - password credential
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/exemptionCategories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getTaxExemptionCategories() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getExemptionCategories();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get drainages.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/drainages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getDrainages() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = new ArrayList<MasterCodeNamePairDetails>();
        for (DrainageEnum drngEnum : DrainageEnum.values()) {
            MasterCodeNamePairDetails mstrCodeNamePairDetails = new MasterCodeNamePairDetails();
            mstrCodeNamePairDetails.setCode(drngEnum.getCode());
            mstrCodeNamePairDetails.setName(drngEnum.name());
            mstrCodeNamePairDetailsList.add(mstrCodeNamePairDetails);
        }
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get all approver departments.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/approverDepartments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getApproverDepartments() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getApproverDepartments();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to create property.
     * 
     * @param createPropertyDetails - Property details request
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(value = "/property/createProperty", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String createProperty(@RequestBody String createPropertyDetails)
            throws JsonGenerationException, JsonMappingException, IOException, ParseException {
        ApplicationThreadLocals.setUserId(Long.valueOf("40"));
        CreatePropertyDetails createPropDetails = (CreatePropertyDetails) getObjectFromJSONRequest(
                createPropertyDetails, CreatePropertyDetails.class);

        ErrorDetails errorDetails = ValidationUtil.validateCreateRequest(createPropDetails);
        if (errorDetails != null) {
            return getJSONResponse(errorDetails);
        } else {
            String propertyTypeMasterCode = createPropDetails.getPropertyTypeMasterCode();
            String propertyCategoryCode = createPropDetails.getPropertyCategoryCode();
            String apartmentCmplxCode = createPropDetails.getApartmentCmplxCode();
            List<OwnerDetails> ownerDetailsList = createPropDetails.getOwnerDetails();

            AssessmentsDetails assessmentsDetails = createPropDetails.getAssessmentDetails();
            String mutationReasonCode = assessmentsDetails.getMutationReasonCode();
            // TODO : Need to verify whether this values has to be used or not
            String parentPropertyAssessmentNo = assessmentsDetails.getParentPropertyAssessmentNo();
            String extentOfSite = assessmentsDetails.getExtentOfSite();
            Boolean isExtentAppurtenantLand = assessmentsDetails.getIsExtentAppurtenantLand();
            String occupancyCertificationNo = assessmentsDetails.getOccupancyCertificationNo();
            Boolean isSuperStructure = assessmentsDetails.getIsSuperStructure();
            String siteOwnerName = assessmentsDetails.getSiteOwnerName();
            Boolean isBuildingPlanDetails = assessmentsDetails.getIsBuildingPlanDetails();

            BuildingPlanDetails buildingPlanDetails = assessmentsDetails.getBuildingPlanDetails();
            String buildingPermissionNo = buildingPlanDetails.getBuildingPermissionNo();
            String buildingPermissionDate = buildingPlanDetails.getBuildingPermissionDate();
            String percentageDeviation = buildingPlanDetails.getPercentageDeviation();

            String regdDocNo = assessmentsDetails.getRegdDocNo();
            String regdDocDate = assessmentsDetails.getRegdDocDate();

            PropertyAddressDetails propAddressDetails = createPropDetails.getPropertyAddressDetails();
            String localityCode = propAddressDetails.getLocalityCode();
            String street = propAddressDetails.getStreet();
            String electionWardCode = propAddressDetails.getElectionWardCode();
            String doorNo = propAddressDetails.getDoorNo();
            String enumerationBlockCode = propAddressDetails.getEnumerationBlockCode();
            String pinCode = propAddressDetails.getPinCode();
            Boolean isCorrAddrDiff = propAddressDetails.getIsCorrAddrDiff();
            CorrespondenceAddressDetails corrAddressDetails = propAddressDetails.getCorrAddressDetails();
            String corrAddr1 = corrAddressDetails.getCorrAddr1();
            String corrAddr2 = corrAddressDetails.getCorrAddr2();
            String corrPinCode = corrAddressDetails.getCorrPinCode();

            AmenitiesDetails amenitiesDetails = createPropDetails.getAmenitiesDetails();
            Boolean hasLift = amenitiesDetails.hasLift();
            Boolean hasToilet = amenitiesDetails.hasToilet();
            Boolean hasWaterTap = amenitiesDetails.hasWaterTap();
            Boolean hasElectricity = amenitiesDetails.hasElectricity();
            Boolean hasAttachedBathroom = amenitiesDetails.hasAttachedBathroom();
            Boolean hasWaterHarvesting = amenitiesDetails.hasWaterHarvesting();
            Boolean hasCable = amenitiesDetails.hasCableConnection();

            ConstructionTypeDetails constructionTypeDetails = createPropDetails.getConstructionTypeDetails();
            String floorTypeCode = constructionTypeDetails.getFloorTypeCode();
            String roofTypeCode = constructionTypeDetails.getRoofTypeCode();
            String wallTypeCode = constructionTypeDetails.getWallTypeCode();
            String woodTypeCode = constructionTypeDetails.getWoodTypeCode();

            List<FloorDetails> floorDetailsList = createPropDetails.getFloorDetails();
            String completionDate = floorDetailsList.get(0).getConstructionDate();

            VacantLandDetails vacantLandDetails = createPropDetails.getVacantLandDetails();
            String surveyNumber = vacantLandDetails.getSurveyNumber();
            String pattaNumber = vacantLandDetails.getPattaNumber();
            Float vacantLandArea = vacantLandDetails.getVacantLandArea();
            Double marketValue = vacantLandDetails.getMarketValue();
            Double currentCapitalValue = vacantLandDetails.getCurrentCapitalValue();
            String effectiveDate = vacantLandDetails.getEffectiveDate();

            SurroundingBoundaryDetails surroundingBoundaryDetails = createPropDetails.getSurroundingBoundaryDetails();
            String northBoundary = surroundingBoundaryDetails.getNorthBoundary();
            String southBoundary = surroundingBoundaryDetails.getSouthBoundary();
            String eastBoundary = surroundingBoundaryDetails.getEastBoundary();
            String westBoundary = surroundingBoundaryDetails.getWestBoundary();

            List<Document> documents = null;
            NewPropertyDetails newPropertyDetails = propertyExternalService.createNewProperty(propertyTypeMasterCode,
                    propertyCategoryCode, apartmentCmplxCode, ownerDetailsList, mutationReasonCode, extentOfSite,
                    isExtentAppurtenantLand, occupancyCertificationNo, isSuperStructure, siteOwnerName,
                    isBuildingPlanDetails, buildingPermissionNo, buildingPermissionDate, percentageDeviation, regdDocNo,
                    regdDocDate, localityCode, street, electionWardCode, doorNo, enumerationBlockCode, pinCode,
                    isCorrAddrDiff, corrAddr1, corrAddr2, corrPinCode, hasLift, hasToilet, hasWaterTap, hasElectricity,
                    hasAttachedBathroom, hasWaterHarvesting, hasCable, floorTypeCode, roofTypeCode, wallTypeCode,
                    woodTypeCode, floorDetailsList, surveyNumber, pattaNumber, vacantLandArea, marketValue,
                    currentCapitalValue, effectiveDate, completionDate, northBoundary, southBoundary, eastBoundary,
                    westBoundary, documents);
            return getJSONResponse(newPropertyDetails);
        }
    }

    /**
     * This method is used to prepare jSON response.
     * 
     * @param obj - a POJO object
     * @return jsonResponse - JSON response string
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    private String getJSONResponse(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        String jsonResponse = objectMapper.writeValueAsString(obj);
        return jsonResponse;
    }

    /**
     * This method is used to get the error details for invalid credentials.
     * 
     * @return
     */
    private ErrorDetails getInvalidCredentialsErrorDetails() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(PropertyTaxConstants.THIRD_PARTY_ERR_CODE_INVALIDCREDENTIALS);
        errorDetails.setErrorMessage(PropertyTaxConstants.THIRD_PARTY_ERR_MSG_INVALIDCREDENTIALS);
        return errorDetails;
    }

    /**
     * This method is used to get the error details for communication failure.
     * 
     * @return
     */
    private ErrorDetails getRequestFailedErrorDetails() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(PropertyTaxConstants.THIRD_PARTY_ERR_CODE_COMMUNICATION_FAILURE);
        errorDetails.setErrorMessage(PropertyTaxConstants.THIRD_PARTY_ERR_MSG_COMMUNICATION_FAILURE);
        return errorDetails;
    }

    /**
     * This method is used to get all the different types of documents.
     * 
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/documentTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getDocumentTypes() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService.getDocumentTypes();
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method is used to get reasons for mutation.
     *
     * @return responseJson - server response in JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/mutationReasons", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public String getMutatioReasons() throws JsonGenerationException, JsonMappingException, IOException {
        List<MasterCodeNamePairDetails> mstrCodeNamePairDetailsList = propertyExternalService
                .getReasonsForChangeProperty(PropertyTaxConstants.PROP_MUTATION_RSN);
        return getJSONResponse(mstrCodeNamePairDetailsList);
    }

    /**
     * This method loads the assessment details.
     * 
     * @param assessmentNumber - assessment number i.e. property id
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "/property/assessmentdetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public String fetchAssessmentDetails(@RequestBody String assessmentRequest)
            throws JsonGenerationException, JsonMappingException, IOException {
        AssessmentRequest assessmentReq = (AssessmentRequest) getObjectFromJSONRequest(assessmentRequest,
                AssessmentRequest.class);
        RestAssessmentDetails assessmentDetails = propertyExternalService
                .loadAssessmentDetails(assessmentReq.getAssessmentNo());
        return getJSONResponse(assessmentDetails);
    }
    
    /**
     * This method is used to get POJO object from JSON request.
     * 
     * @param jsonString - request JSON string
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Object getObjectFromJSONRequest(String jsonString, Class cls)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.setDateFormat(ChequePayment.CHEQUE_DATE_FORMAT);
        return mapper.readValue(jsonString, cls);
    }
}