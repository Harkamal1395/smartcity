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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<br/>
<table width="100%">
	<tr>
		<td colspan="5" class="headingwk">
			<div class="arrowiconwk">
				<img src="${pageContext.request.contextPath}/images/arrow.gif" height="20" />
			</div>
			<div class="headplacer">
				<s:text name='license.title.feedetail' />
			</div>
		</td>
	</tr>
</table>

	
<table width="50%" border="1" cellspacing="0" cellpadding="0" align="center">
<br/>
	<tr>
		<th class="<c:out value="${trclass}"/>">
			<s:text name='license.feename' />
		</th>
		<th class="<c:out value="${trclass}"/>">
			<s:text name="license.fee.amount" />
		</th>
	</tr>
	
		<s:iterator var="demandDetails" value="getCurrentDemand().getEgDemandDetails()">
      <c:choose>
				<c:when test="${trclass=='greybox'}">
					<c:set var="trclass" value="bluebox" />
				</c:when>
				<c:when test="${trclass=='bluebox'}">
					<c:set var="trclass" value="greybox" />
				</c:when>
			</c:choose>
			<tr>
				<td text-align="center" class="<c:out value="${trclass}"/>">
			<s:property value="#demandDetails.egDemandReason.egDemandReasonMaster.reasonMaster" /> - <s:property value="#demandDetails.egDemandReason.getEgInstallmentMaster().getDescription()" />
				</td>
				<td text-align="center" class="<c:out value="${trclass}"/>">
						<s:text name="fee.rupee.symbol"/><s:property value="#demandDetails.amount" />
				</td>
			</tr>
			<s:if test="#demandDetails.amtRebate-=null && #demandDetails.amtRebate-=0">
			<tr>
			<td text-align="center" class="<c:out value="${trclass}"/>">
			<s:text name="Deduction"/> - <s:property value="#demandDetails.egDemandReason.getEgInstallmentMaster().getDescription()" />
				</td>
				<td text-align="center" class="<c:out value="${trclass}"/>">
					<s:text name="fee.rupee.symbol"/><s:property value="#demandDetails.amtRebate" />
				</td>
			</tr>
			</s:if>
		</s:iterator>
		<c:choose>
			<c:when test="${trclass=='greybox'}">
				<c:set var="trclass" value="bluebox" />
			</c:when>
			<c:when test="${trclass=='bluebox'}">
				<c:set var="trclass" value="greybox" />
			</c:when>
		</c:choose>
		<tr>
			<td align="center" class="<c:out value="${trclass}"/>">
				<b> <s:text name="license.total.fee.amount" /> </b>
			</td>
			<td align="center" class="<c:out value="${trclass}"/>">
				<b><s:text name="fee.rupee.symbol"/><s:property value="getAapplicableDemand(getCurrentDemand())" /> </b>
			</td>
		</tr>
	
</table>
