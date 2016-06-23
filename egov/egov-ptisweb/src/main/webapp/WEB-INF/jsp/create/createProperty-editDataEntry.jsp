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

<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/includes/taglibs.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>
		<s:text name='editdataentry.title' />
</title>
<sx:head />
<link rel="stylesheet" href="<c:url value='/resources/global/css/font-icons/font-awesome/css/font-awesome.min.css' context='/egi'/>">
<script
	src="<c:url value='/resources/global/js/bootstrap/bootstrap.js' context='/egi'/>"
	type="text/javascript"></script>
<link
	href="<c:url value='/resources/global/css/bootstrap/bootstrap-datepicker.css' context='/egi'/>"
	rel="stylesheet" type="text/css" />
<script
	src="<c:url value='/resources/global/js/bootstrap/bootstrap-datepicker.js' context='/egi'/>"
	type="text/javascript"></script>
<script
	src="<c:url value='/resources/global/js/bootstrap/typeahead.bundle.js' context='/egi'/>"
	type="text/javascript"></script>
</head>

<body onload="loadOnStartUp();">
	<s:if test="%{hasErrors()}">
		<div class="errorstyle" id="property_error_area">
			<div class="errortext">
				<s:actionerror />
			</div>
		</div>
	</s:if>
	<s:form name="CreatePropertyDataEntryForm" action="createProperty-updateDataEntry"
		theme="simple" enctype="multipart/form-data">
		<s:push value="model">
			<s:token />
			<!-- The mode value is used in floorform.jsp file to stop from removing the rent agreement header icon -->
			<s:hidden name="mode" id="mode" value="%{mode}" />
			<s:hidden name="modelId" id="modelId" value="%{modelId}" />
			<s:hidden id="modifyRsn" name="modifyRsn" value="%{modifyRsn}" />
			<s:hidden id="indexNumber" name="indexNumber" value="%{indexNumber}" />
			<div class="formmainbox">
				<div class="headingbg">
					<s:text name="dataentrypropertyheader" /> 
				</div>

				<%@  include file="createPropertyForm.jsp"%>
			</div>
			<div class="buttonbottom" align="center">
				<table style="width: 100%; text-align: center;">
					<tr>
						<td><input  type="submit" id="Create" class="buttonsubmit" value="Update" /> 
							<input type="button" name="button2" id="button2" value="Close"	class="button" onclick="window.close();" /></td>
					</tr>
				</table>
			</div>
			
		</s:push>
		
	</s:form>
	<script type="text/javascript">
	jQuery.noConflict();

	jQuery("#loadingMask").remove();
	jQuery(function ($) {
		try { 
			$(".datepicker").datepicker({
				format: "dd/mm/yyyy"
			}); 
			}catch(e){
			console.warn("No Date Picker "+ e);
		}

			$('.datepicker').on('changeDate', function(ev){
			    $(this).datepicker('hide');
			});
	});

	function loadOnStartUp() {
		enableCorresAddr();
		populateBoundaries();
		enableAppartnaumtLandDetails();
		makeMandatory();
		document.getElementById("appurtenantRow").style.display = "none";
		enableOrDisableSiteOwnerDetails(jQuery('input[name="propertyDetail.structure"]'));
		//enableOrDisableBPADetails(jQuery('input[name="propertyDetail.buildingPlanDetailsChecked"]'));
		var appartunentLand = jQuery('input[name="propertyDetail.appurtenantLandChecked"]');
		if (jQuery(appartunentLand).is(":checked")) {
			enableAppartnaumtLandDetails();
		} 
		var category = '<s:property value="%{propertyDetail.categoryType}"/>';
		document.forms[0].propTypeCategoryId.options[document.forms[0].propTypeCategoryId.selectedIndex].value = category;
		toggleFloorDetails();
	     var aadhartextboxes = jQuery('.txtaadhar');
	     console.log(aadhartextboxes);
	     aadhartextboxes.each(function() {
		   	if(jQuery(this).val())
		   	{
		   		  getAadharDetails(this);
		   	}
		 });
	     document.getElementById("upicNo").readOnly = true;
	     document.getElementById("upicNo").disabled = true;
	     showHideFirmName();
		 showHideLengthBreadth();
	}

	function showHideFirmName(){
		var tbl=document.getElementById("floorDetails");
	    var tabLength = (tbl.rows.length)-1;
	    for(var i=1;i<=tabLength;i++){
	         enableDisableFirmName(getControlInBranch(tbl.rows[i],'floorUsage'));
	    }
	}

	function showHideLengthBreadth(){
		var tbl=document.getElementById("floorDetails");
	    var tabLength = (tbl.rows.length)-1;
	    for(var i=1;i<=tabLength;i++){
	         enableDisableLengthBreadth(getControlInBranch(tbl.rows[i],'unstructuredLand'));
	    }
	}

	function enableDisableFirmName(obj){ 
		var selIndex = obj.selectedIndex;
		if(selIndex != undefined){
			var selText = obj.options[selIndex].text; 
			var rIndex = getRow(obj).rowIndex;
			var tbl = document.getElementById('floorDetails');
			var firmval=getControlInBranch(tbl.rows[rIndex],'firmName'); 
			if(selText!=null && selText=='<s:property value="%{@org.egov.ptis.constants.PropertyTaxConstants@NATURE_OF_USAGE_RESIDENCE}"/>'){
				if(firmval.value!=null && firmval.value!="") 
					firmval.value="";
				firmval.readOnly = true;      
			} else{
				firmval.readOnly = false; 
			}
		}
	}  

	function enableDisableLengthBreadth(obj){ 
		var selIndex = obj.selectedIndex;
		if(obj.value=='true'){
				obj.value='true';
				obj.options[selIndex].selected = true;
		}
		else{
			obj.value='false';
			obj.options[selIndex].selected = true;
		}
		
		if(selIndex != undefined){
			var selText = obj.options[selIndex].text; 
			var rIndex = getRow(obj).rowIndex;
			var tbl = document.getElementById('floorDetails');
			var buildLength=getControlInBranch(tbl.rows[rIndex],'builtUpArealength');
			var buildbreadth=getControlInBranch(tbl.rows[rIndex],'builtUpAreabreadth');  
			var builtUpArea=getControlInBranch(tbl.rows[rIndex],'builtUpArea');
			if(selText!=null && selText=='No'){
				buildLength.value="";
				buildLength.readOnly = true;      
				buildbreadth.value="";
				buildbreadth.readOnly = true;
				builtUpArea.readOnly = false;
			} else{
				buildLength.readOnly = false; 
				buildbreadth.readOnly = false;
				builtUpArea.readOnly = true;
			}
		}
	}

	function onSubmit() { 
		jQuery('#gender, #guardianRelation').removeAttr('disabled');
		document.forms[0].action = 'createProperty-updateDataEntry.action';
		document.forms[0].submit;
	    return true; 
	}
				
	</script>
	<script
		src="<c:url value='/resources/global/js/egov/inbox.js' context='/egi'/>"></script>
		<script src="<c:url value='/resources/javascript/helper.js' context='/ptis'/>"></script>
</body>
</html>