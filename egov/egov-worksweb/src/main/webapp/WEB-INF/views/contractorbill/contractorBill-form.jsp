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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>	
		<form:form id="contractorBillForm" class="form-horizontal form-groups-bordered" modelAttribute="contractorBillRegister" role="form" action="contractorbill-save" method="post" enctype="multipart/form-data">

			<input type="hidden" name="workOrderDate" id="workOrderDate" class="form-control datepicker" maxlength="10" data-inputmask="'mask': 'd/m/y'" data-date-end-date="0d" value='<fmt:formatDate value="${workOrderEstimate.workOrder.workOrderDate}" pattern="dd/MM/yyyy"/>' > 
			<form:hidden path="workOrderEstimate.workOrder.id"  name="workOrder" id="workOrderId" value="${workOrderEstimate.workOrder.id}" /> 
			<input type="hidden"  name="woeId" id="woeId" value="${woeId}" />
			
					<div class="panel panel-primary" data-collapsed="0">
						<div class="panel-heading">
							<div class="panel-title"><spring:message code="lbl.header" /></div> 
						</div>
						<div>
							<spring:hasBindErrors name="contractorBillRegister">
								<div class="alert alert-danger col-md-10 col-md-offset-1">
						      			<form:errors path="*" /><br/>
						      	</div>
				        	</spring:hasBindErrors>
				        </div>
						<!-- <div class="panel-body"> -->
							<jsp:include page="contractorBill-header.jsp"/>
							<jsp:include page="contractorBill-mbdetails.jsp"/>
							<jsp:include page="contractorBill-debitaccountdetails.jsp"/>
							<jsp:include page="contractorBill-creditaccountdetails.jsp"/>
							
						<!-- </div> -->
					</div>
					<jsp:include page="../common/uploadDocuments.jsp"/>
			<jsp:include page="../common/commonWorkflowMatrix.jsp"/>
			<div class="buttonbottom" align="center">
				<jsp:include page="../common/commonWorkflowMatrix-button.jsp" />
			</div>
		</form:form>  
<script src="<c:url value='/resources/js/contractorbill.js?rnd=${app_release_no}'/>"></script>
<script src="<c:url value='/resources/global/js/egov/inbox.js' context='/egi'/>"></script>
