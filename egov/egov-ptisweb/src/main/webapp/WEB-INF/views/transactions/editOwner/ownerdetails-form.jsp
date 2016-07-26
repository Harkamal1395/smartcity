<%--
  ~ eGov suite of products aim to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) <2015>  eGovernments Foundation
  ~
  ~     The updated version of eGov suite of products as by eGovernments Foundation
  ~     is available at http://www.egovernments.org
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program. If not, see http://www.gnu.org/licenses/ or
  ~     http://www.gnu.org/licenses/gpl.html .
  ~
  ~     In addition to the terms of the GPL license to be adhered to in using this
  ~     program, the following additional terms are to be complied with:
  ~
  ~         1) All versions of this program, verbatim or modified must carry this
  ~            Legal Notice.
  ~
  ~         2) Any misrepresentation of the origin of the material is prohibited. It
  ~            is required that all modified versions of this material be marked in
  ~            reasonable ways as different from the original version.
  ~
  ~         3) This license does not grant any rights to any user of the program
  ~            with regards to rights under trademark law for use of the trade names
  ~            or trademarks of eGovernments Foundation.
  ~
  ~   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  --%>

<%@ page contentType="text/html" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<style>
body {
	font-family: regular !important;
	font-size: 14px;
}
</style>

<c:if test="${errorMsg != ''}">
	<div class="panel-heading">
		<div class="add-margin error-msg" style="text-align: center;">
			<strong><c:out value="${errorMsg}" /></strong>
		</div>
	</div>
</c:if>
<div class="row">
	<div class="col-md-12">
		<form:form class="form-horizontal form-groups-bordered" method="post"
			name="ownerDetailsForm" id="ownerDetailsForm" action=""
			modelAttribute="property">
			<div class="panel panel-primary" data-collapsed="0"
				style="text-align: left">
				<div class="row">
					<div class="col-md-12">
						<div class="panel panel-primary" data-collapsed="0"
							style="text-align: left">
							<div class="panel-heading">
								<div class="panel-title">
									<spring:message code="lbl.hdr.propertydetails" />
								</div>
							</div>
							<div class="panel-body">
								<div class="row add-border">
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.assmtno" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out value="${property.basicProperty.upicNo}"></c:out>
									</div>
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.assmtno.parentproperty" />
									</div>
									<div class="col-xs-3 add-margin view-content">N/A</div>
								</div>
								<div class="row add-border">
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.propertytype" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out value="${property.propertyDetail.categoryType}"></c:out>
									</div>
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.excemption" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out value="${property.taxExemptedReason.name}"
											default="N/A"></c:out>
									</div>
								</div>
								<div class="row add-border">
								<div class="col-xs-3 add-margin">
										<spring:message code="lbl.category.ownership" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out default="N/A"
											value="${property.propertyDetail.propertyTypeMaster.type}"></c:out>
									</div>
								<div class="col-xs-3 add-margin">
										<spring:message code="lbl.effectivedate" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<fmt:formatDate pattern="dd/MM/yyyy"
											value="${property.basicProperty.propOccupationDate}" />
									</div>
								</div>
								<div class="row add-border">
									
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.extent.appurtenant" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out
											value="${property.propertyDetail.extentAppartenauntLand}"
											default="N/A"></c:out>
									</div>
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.superstructure" />
									</div>
									<c:choose>
										<c:when test="${property.propertyDetail.structure == 'true'}">
											<div class="col-xs-3 add-margin view-content">Yes</div>
										</c:when>
										<c:otherwise>
											<div class="col-xs-3 add-margin view-content">No</div>
										</c:otherwise>
									</c:choose>
								</div>
								<div class="row add-border">
									
									</div>
								<div class="row add-border">
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.registrationDoc.no" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out value="${property.basicProperty.regdDocNo}"
											default="N/A"></c:out>
									</div>
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.registrationDoc.date" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<fmt:formatDate pattern="dd/MM/yyyy"
											value="${property.basicProperty.regdDocDate}" />
									</div>
								</div>
								<div class="row add-border">
									<div class="col-xs-3 add-margin">
										<spring:message code="lbl.reason.creation" />
									</div>
									<div class="col-xs-3 add-margin view-content">
										<c:out
											value="${property.propertyDetail.propertyMutationMaster.mutationName}"></c:out>
									</div>
								</div>
							</div>

						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<div class="panel panel-primary" data-collapsed="0"
							style="text-align: left">
							<div class="panel-heading">
								<div class="panel-title">
									<spring:message code="lbl.property.address.details" />
								</div>
							</div>
							<div class="panel-body">
								<div class="row">
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.locality" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out
											value="${property.basicProperty.propertyID.locality.name}" />
									</div>

									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.zone" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${property.basicProperty.propertyID.zone.name}" />
									</div>
								</div>

								<div class="row">
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.property.address" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${property.basicProperty.address}" />
									</div>
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.revwardno" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${property.basicProperty.propertyID.ward.name}" />
									</div>
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.blockno" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${property.basicProperty.propertyID.area.name}" />
									</div>
								</div>
								<div class="row">
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.street" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out
											value="${property.basicProperty.propertyID.street.name}"
											default="N/A" />
									</div>
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.elec.wardno" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out
											value="${property.basicProperty.propertyID.electionBoundary.name}" />
									</div>
								</div>
								<div class="row">
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.doorno" />

									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${doorNumber}" default="N/A" />
									</div>
									<div class="col-sm-3 add-margin">
										<spring:message code="lbl.pincode" />
									</div>
									<div class="col-sm-3 add-margin view-content">
										<c:out value="${pinCode}" default="N/A" />
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>

				<div class="panel panel-primary">
					<div class="panel-body custom-form">
						<div class="form-group">
							<label class="col-sm-3 control-label"><spring:message
									code="lbl.doorNumber" /><span class="mandatory1">*</span> </label>
							<div class="col-sm-3 add-margin">
								<form:input path="" id="doorNumber" name="doorNumber"
									value="${doorNumber}" maxlength="32"
									cssClass="form-control patternvalidation"
									data-pattern="address" />
							</div>
						</div>
					</div>

					<table width="100%" border="0" cellspacing="0" cellpadding="0"
						class="table table-bordered" id="vacantLandTable">

						<tbody>
							<tr>
								<th class="bluebgheadtd"><spring:message code="lbl.adharno" /></th>
								<th class="bluebgheadtd"><spring:message
										code="lbl.MobileNumber" /><span class="mandatory1">*</span></th>
								<th class="bluebgheadtd"><spring:message
										code="lbl.OwnerName" /><span class="mandatory1">*</span></th>
								<th class="bluebgheadtd"><spring:message code="lbl.gender" /><span class="mandatory1">*</span></th>
								<th class="bluebgheadtd"><spring:message
										code="lbl.EmailAddress" /></th>
								<th class="bluebgheadtd"><spring:message
										code="lbl.GuardianRelation" /><span class="mandatory1">*</span></th>
								<th class="bluebgheadtd"><spring:message
										code="lbl.Guardian" /><span class="mandatory1">*</span></th>
							</tr>
							<c:choose>
								<c:when
									test="${!property.basicProperty.propertyOwnerInfo.isEmpty()}">
									<c:forEach var="ownerInfo"
										items="${property.basicProperty.propertyOwnerInfo}"
										varStatus="status">
										<tr id="ownerDetailsRow">
											<form:hidden
												path="basicProperty.propertyOwnerInfo[${status.index}].owner.id" />
											<form:hidden
												path="basicProperty.propertyOwnerInfo[${status.index}].owner.username" />
											<form:hidden
												path="basicProperty.propertyOwnerInfo[${status.index}].owner.password" />
											<td class="blueborderfortd" align="center"><form:input
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.aadhaarNumber"
													id="aadharNumber" maxlength="12" cssClass="form-control"/>
											</td>
											<td class="blueborderfortd" align="center"><form:input
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.mobileNumber"
													id="mobileNumber" maxlength="10" cssClass="form-control" />
											</td>
											<td class="blueborderfortd" align="center"><form:input
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.name"
													id="name" maxlength="74" cssClass="form-control" /></td>

											<td class="blueborderfortd" align="center"><form:select
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.gender"
													id="sgender" name="gender" data-first-option="false"
													cssClass="form-control">
													<option value="">--select--</option>
													<form:options items="${gender}" />
												</form:select></td>
											<td class="blueborderfortd" align="center"><form:input
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.emailId"
													id="emailId" maxlength="32" cssClass="form-control" /></td>
											<td class="blueborderfortd" align="center"><form:select
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.guardianRelation"
													id="guardianRelation" name="guardianRelation"
													data-first-option="false" cssClass="form-control">
													<option value="">--select--</option>
													<form:options items="${guardianRelationMap}" />
												</form:select></td>
											<td class="blueborderfortd" align="center"><form:input
													path="basicProperty.propertyOwnerInfo[${status.index}].owner.guardian"
													id="guardianName" maxlength="32" cssClass="form-control" />
											</td>

										</tr>
									</c:forEach>
								</c:when>
							</c:choose>
						</tbody>
					</table>
				</div>
			</div>
			<div class="row">
				<div class="text-center">
					<button type="submit" class="btn btn-primary add-margin"
						id="submitform">
						<spring:message code="lbl.submit" />
					</button>
					<a href="javascript:void(0)" class="btn btn-default"
						onclick="self.close()"><spring:message code="lbl.close" /></a>
				</div>
			</div>
		</form:form>
	</div>
</div>
<script
	src="<c:url value='/resources/global/js/egov/patternvalidation.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script>
	jQuery('#submitform').click(function(e) {

		
		if (!jQuery('#doorNumber').val()) {
			bootbox.alert('Door number is mandatory');
			return false;
		}
		else if (!jQuery('#mobileNumber').val()) {
			bootbox.alert('Mobile Number is mandatory');
			return false;
		}
		else if (!jQuery('#name').val()) {
			bootbox.alert('Owner Name is mandatory');
			return false;
		}
		else if (!jQuery('#sgender').val()) {
			bootbox.alert('Gender is mandatory');
			return false;
		}
		else if (!jQuery('#guardianRelation').val()) {
			bootbox.alert('Guardian Relation is mandatory');
			return false;
		}
		else if (!jQuery('#guardianName').val()) {
			bootbox.alert('Guardian Name is mandatory');
			return false;
		}		
		

		return true;
		
	})
</script>