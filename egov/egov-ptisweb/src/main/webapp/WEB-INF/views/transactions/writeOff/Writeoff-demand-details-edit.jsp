<%--
  ~    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) 2017  eGovernments Foundation
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
  ~            Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~            derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~            For any further queries on attribution, including queries on brand guidelines,
  ~            please contact contact@egovernments.org
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
  ~
  --%>
<script text='javascript/text'>
$(document).ready(function()
	     {
	getselectedinstallments();
	     });
	  function getselectedinstallments(val){
	  fromVal = "${writeOff.fromInstallment}";
	  
	  toVal = "${writeOff.toInstallment}";
	 
	instString = $("#instString").val();
	  if(fromVal && toVal){
		  var fromValIndex = instString.split(",").indexOf(fromVal);
		  var toValIndex = instString.split(",").indexOf(toVal);
		/*   $("#demandDetailsTable").attr('style', 'display:none;');
		   instString.split(",").forEach((val,index)=> {
			  var queryIdentifier = ".row-"+val;
			  $(queryIdentifier).attr('style', 'display:none;');

		  }); */ 	  
		  /* if(fromValIndex > toValIndex)
			return alert("To Installment cannot be greater than From Installment");
		   $("#demandDetailsTable").removeAttr('style'); */	
		 //  $("#demandDetailsTable").attr('style', 'display:none;');
		  instString.split(",").forEach((val,index)=> {
			  if(fromValIndex <= index && index <= toValIndex){
				//  alert("valeu is "+val);
			  var queryIdentifier = ".row-"+val;
			//  alert("query identifie is "+queryIdentifier);
			  $(queryIdentifier).removeAttr('style', 'display:none;');
			  }
		  });
	  }
	  }
	   
		  </script>

<div class="row">
	<div class="col-md-12">
		<div class="panel panel-primary" data-collapsed="0"
			style="text-align: left">
			<div class="panel-heading">
				<div class="panel-title">

					<spring:message code="lbl.cv.dmndDet" />
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="panel-body">
						<div class="row add-border">
						<c:if test="${writeOff.fromInstallment !=null}">
							<div class="col-xs-2 add-margin">
								<spring:message code="lbl.installmnt.start" />
							</div>
							<form:hidden path="" name="instString" id="instString"
								value="${instString}" />
							<div class="col-xs-2 add-margin view-content" id="frominstall">
								<c:out value="${writeOff.fromInstallment}"></c:out>	
							</div>
							<div class="col-xs-2 add-margin postion">
								<spring:message code="lbl.installmnt.end" />
							</div>
							<div class="col-xs-2 add-margin view-content " id="toinstall">
								<c:out value="${writeOff.toInstallment}"></c:out>	
							</div>
							</c:if>
						</div>
					</div>
				</div>
			</div>
			<div class="panel-body">
				<div align="center"
					class="overflow-x-scroll floors-tbl-freeze-column-div">
					<table class="table table-bordered" width="100%" id="demandDetails" >
						<tr>
							<th class="bluebgheadtd"><spring:message
									code="lbl.writeOff.instalmnt" /></th>
							<th class="bluebgheadtd">Tax Name</th>
							<th class="bluebgheadtd"><spring:message
									code="lbl.dmd.amount" /></th>
							<th class="bluebgheadtd"><spring:message
									code="lbl.writeoff.dmd" /></th>
							<th class="bluebgheadtd"><spring:message
									code="lbl.collection" /></th>
						</tr>
						
							<c:forEach items="${demandDetailList}" var="demandDetails"
							varStatus="status">
							<tr class="row-${demandDetails.installment.description}" 
									value="status" style="display: none;">
								<td class="greybox"><form:hidden
										path="demandDetailBeanList[${status.index }].installment.id" />
									<c:if
										test="${demandDetailList[status.index].installment.id == demandDetailList[status.index-1].installment.id}">
									&nbsp;
									</c:if> <c:if
										test="${demandDetailList[status.index].installment.id != demandDetailList[status.index-1].installment.id}">

										<c:out value="${demandDetails.installment}"></c:out>
					</c:if></td>
								<td class="greybox"><form:hidden
										path="demandDetailBeanList[${status.index }].reasonMaster" />
									<c:out value="${demandDetails.reasonMaster}"></c:out></td>
								<td class="greybox"><c:out
										value="${demandDetails.actualAmount}">
									</c:out></td>
								<td class="greybox"><c:out
										value="${demandDetails.revisedAmount}">
									</c:out></td>
								<td class="greybox"><c:out
										value="${demandDetails.actualCollection}">
									</c:out></td>

							</tr>
						</c:forEach>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>