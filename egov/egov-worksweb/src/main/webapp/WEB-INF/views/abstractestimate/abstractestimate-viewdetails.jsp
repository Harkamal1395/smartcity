<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
	<div class="row add-border">
		<div class="col-xs-3 add-margin">
			<spring:message code="lbl.department" />
		</div> 
	
		<div class="col-xs-3 add-margin view-content">
			<c:out value="${abstractEstimate.executingDepartment.name}"></c:out>
		</div>
		<div class="col-xs-3 add-margin">
			<spring:message code="lbl.abstractestimatedate" />
		</div> 
	
		<div class="col-xs-3 add-margin view-content">
			<fmt:formatDate pattern="dd/MM/yyyy" value="${abstractEstimate.estimateDate}" />
		</div>
	</div>	
	<div class="row add-border">
		<div class="col-xs-3 add-margin">
			<spring:message code="lbl.natureofwork" />
		</div> 
	
		<div class="col-xs-3 add-margin view-content">
			<c:out value="${abstractEstimate.natureOfWork.name}"></c:out>
		</div>
		<div class="col-xs-3 add-margin">
			<spring:message code="lbl.election.ward" />
		</div> 
	
		<div class="col-xs-3 add-margin view-content">
			<c:out value="${abstractEstimate.ward.boundaryNum}"></c:out>
		</div>
	</div>	
	<c:if test="${abstractEstimate.lineEstimateDetails != null}">	
		<div class="row add-border">
			<div class="col-xs-3 add-margin">
				<spring:message code="lbl.locality" />
			</div> 
		
			<div class="col-xs-3 add-margin view-content">
				<c:out value="${abstractEstimate.lineEstimateDetails.lineEstimate.location.name}"></c:out>
			</div>
			<div class="col-xs-3 add-margin">
				<spring:message code="lbl.workcategory" />
			</div> 
			<c:choose>
				<c:when test="${abstractEstimate.lineEstimateDetails.lineEstimate.workCategory == 'SLUM_WORK' }">
					<div class="col-xs-3 add-margin view-content">
						<spring:message code="lbl.slum" />
					</div>
				</c:when>
				<c:otherwise>
					<div class="col-xs-3 add-margin view-content">
						<spring:message code="lbl.nonslum" />
					</div>
				</c:otherwise>
			</c:choose>
		</div>
		<c:if test="${abstractEstimate.lineEstimateDetails.lineEstimate.workCategory == 'SLUM_WORK' }">
			<div class="row add-border">
				<div class="col-xs-3 add-margin">
					<spring:message code="lbl.typeofslum" />
				</div> 
			
				<div class="col-xs-3 add-margin view-content">
					<c:out value="${abstractEstimate.lineEstimateDetails.lineEstimate.typeOfSlum}"></c:out>
				</div>
				<div class="col-xs-3 add-margin">
					<spring:message code="lbl.beneficiary" />
				</div> 
			
				<div class="col-xs-3 add-margin view-content">
					<c:out value="${abstractEstimate.lineEstimateDetails.lineEstimate.beneficiary}"></c:out>
				</div>
			</div>
		</c:if>
	</c:if>	
	<div class="row add-border">
		<div class="col-xs-3 add-margin">
			<spring:message code="lbl.location" />
		</div> 
		<div class="col-xs-3 add-margin view-content">
			<c:choose>
				<c:when test="${abstractEstimate.location != null}">
				<c:if test="${abstractEstimate.latitude != null && abstractEstimate.longitude != null}">
				<span class="map-tool-class btn-secondary" data-toggle="tooltip"
							data-placement="top" title="" data-original-title="Locate on map"
							onclick="jQuery('#view-location').modal('show', {backdrop: 'static'});">
							<i class="fa fa-map-marker"></i></span> </c:if>
					<span id="address_locate"><c:out value="${abstractEstimate.location}"></c:out></span>
				</c:when>
				<c:otherwise>
					<c:out default="N/A" value="N/A"></c:out>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div class="row add-border">
	     <div class="col-xs-3 add-margin">
				<spring:message code="lbl.latitude" />
			</div>  
			<div class="col-xs-3 add-margin view-content">
				<c:choose>
					<c:when test="${abstractEstimate.latitude != null}">
						<c:out value="${abstractEstimate.latitude}"></c:out>
					</c:when>
					<c:otherwise>
						<c:out default="N/A" value="N/A"></c:out>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="col-xs-3 add-margin">
				<spring:message code="lbl.longitude" />
			</div> 
			<div class="col-xs-3 add-margin view-content">
				<c:choose>
					<c:when test="${abstractEstimate.longitude != null}">
						<c:out value="${abstractEstimate.longitude}"></c:out>
					</c:when>
					<c:otherwise>
						<c:out default="N/A" value="N/A"></c:out>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	<div class="row">
				<div class="col-xs-3 add-margin">
					<spring:message code="lbl.nameofwork" />
				</div> 
			
				<div class="col-xs-3 add-margin view-content">
					<c:out value="${abstractEstimate.name}"></c:out>
				</div>
				<div class="col-xs-3 add-margin">
					<spring:message code="lbl.workdescription" />
				</div> 
			
				<div class="col-xs-3 add-margin view-content">
					<c:out value="${abstractEstimate.description}"></c:out>
				</div>
	</div>
<script src="<c:url value='/resources/js/viewlocationmap.js?rnd=${app_release_no}'/>"></script>
<script>
var lat = '${abstractEstimate.latitude}';
var lng = '${abstractEstimate.longitude}';
</script>