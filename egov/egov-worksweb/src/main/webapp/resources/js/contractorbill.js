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
$detailsRowCount = $('#detailsSize').val();
$(document).ready(function(){
	
	otherDeductionCreditGlcode_initialize();	
	replaceBillTypeChar();
	calculateNetPayableAmount();
	
	if($("#mbTotalAmount").html()){
		$("#debitamount").val($("#mbTotalAmount").html());
		$("#netPayableAmount").val($("#mbTotalAmount").html());
		$("#debitamount").prop("readonly",true);
	}
	// TODO: remove this condition
	if($('#hiddenbilldate').val() != '') {
		$('#billdate').datepicker('setDate',new Date($('#hiddenbilldate').val()));	
	}
	
	var currentState = $('#currentState').val();
	if(currentState == 'Created') {
		$('#approverDetailHeading').hide();
		
		$('#approvalDepartment').removeAttr('required');
		$('#approvalDesignation').removeAttr('required');
		$('#approvalPosition').removeAttr('required');
	}
		
	function replaceBillTypeChar() { 
		$('#billtype option').each(function() {
		   var $this = $(this);
		   if($this.val() != '') {
			   var billType = $this.text().replace(/_/g, ' ');
			   $this.text(billType);
			   $this.val(billType);
		   }
		});
	}
				
	$('.btn-primary').click(function(){
		var button = $(this).attr('id');
		if (button != null && button == 'Forward') {
			//TODO: remove code till billdate < workOrderDate condition check
			var billDate = $('#billdate').data('datepicker').date;
			var workOrderDate = $('#workOrderDate').data('datepicker').date;
			var currentDate = new Date();
			if(currentDate.getMonth() == 0 || currentDate.getMonth() == 1 || currentDate.getMonth() == 2) {
				currentDate = new Date(currentDate.getFullYear() - 1, 3, 1);
			}
			var currentFinYearDate = new Date(currentDate.getFullYear(), 3, 1);
			if(billDate < currentFinYearDate) {
				bootbox.alert($('#errorBillDateFinYear').val());
				$('#billdate').val(""); 
				return false;
			}
			
			if(billDate < workOrderDate) {
				bootbox.alert($('#errorBillDateWorkOrder').val());
				$('#billdate').val(""); 
				return false;
			}
			
			var debitamount = $('#debitamount').val();
			$('#billamount').val(debitamount);
			if($('#partyBillDate').val() != '') { 
				var workOrderDate = $('#workOrderDate').data('datepicker').date;
				var partyBillDate = $('#partyBillDate').data('datepicker').date;
				if(workOrderDate > partyBillDate) {
					bootbox.alert($('#errorPartyBillDate').val());
					$('#partyBillDate').val(""); 
					return false;
				}
				//TODO: remove this condition
				if(partyBillDate > billDate) {
					bootbox.alert($('#errorPartyBillDateBillDate').val());
					$('#partyBillDate').val(""); 
					return false;
				}
			}	
			if($('#mbDate').val() != '' && $('#mbDate').val()!==undefined) { 
				var workOrderDate = $('#workOrderDate').data('datepicker').date;
				var mbDate = $('#mbDate').data('datepicker').date;
				if(workOrderDate > mbDate) {
					bootbox.alert($('#errorMBDate').val());
					$('#mbDate').val("");
					return false;
				}
			}	
			if(!validateMBPageNumbers())
				return false;
			if(!validateNetPayableAmount())
				return false;
			return validateWorkFlowApprover(button);
		}
		return validateWorkFlowApprover(button);
	});
	
	var netPayableAccountCodeId = $('#netPayableAccountCodeId').val();
	$("#netPayableAccountCode").each(function() {
		if($(this).children('option').length == 2) {
		 	$(this).find('option').eq(1).prop('selected', true);
		} else {
			$(this).val(netPayableAccountCodeId);
		}
	});

	function validateWorkFlowApprover(name) {
		document.getElementById("workFlowAction").value = name;
		var approverPosId = document.getElementById("approvalPosition");
		var button = document.getElementById("workFlowAction").value;
		if (button != null && button == 'Submit') {
			$('#approvalDepartment').attr('required', 'required');
			$('#approvalDesignation').attr('required', 'required');
			$('#approvalPosition').attr('required', 'required');
			$('#approvalComent').removeAttr('required');
		}
		if (button != null && button == 'Reject') {
			$('#approvalDepartment').removeAttr('required');
			$('#approvalDesignation').removeAttr('required');
			$('#approvalPosition').removeAttr('required');
			$('#approvalComent').attr('required', 'required');
		}
		if (button != null && button == 'Cancel') {
			$('#approvalDepartment').removeAttr('required');
			$('#approvalDesignation').removeAttr('required');
			$('#approvalPosition').removeAttr('required');
			$('#approvalComent').attr('required', 'required');
		}
		if (button != null && button == 'Forward') {
			$('#approvalDepartment').attr('required', 'required');
			$('#approvalDesignation').attr('required', 'required');
			$('#approvalPosition').attr('required', 'required');
			$('#approvalComent').removeAttr('required');
		}
		if (button != null && button == 'Approve') {
			$('#approvalComent').removeAttr('required');
		}
		$('.creditGlcode').removeAttr('required');
		$('.creditAmount').removeAttr('required');
		if(validateDeductionGrids()){
			document.forms[0].submit;
		}
		else
			return false;
	}
	
});

function addStatutoryDeductionRow() { 
	
	var rowcount = $("#tblstatutorydeductioncreditdetails tbody tr").length;
	if (rowcount < 30) {
		if (document.getElementById('statutorydeductionrow') != null) {
			addRow('tblstatutorydeductioncreditdetails','statutorydeductionrow');
		}
	} else {
		  bootbox.alert('limit reached!');
	}
}

function addOtherDeductionRow() { 
	
	$('.otherDeductionCreditGlcode').typeahead('destroy');
	var rowcount = $("#tblotherdeductioncreditdetails tbody tr").length;
	if (rowcount < 30) {
		if (document.getElementById('otherdeductionrow') != null) {
			addRow('tblotherdeductioncreditdetails','otherdeductionrow');
			otherDeductionCreditGlcode_initialize();
		}
	} else {
		  bootbox.alert('limit reached!');
	}
}

function addRetentionMoneyDeductionRow() { 
	
	var rowcount = $("#tblretentionmoneydeductioncreditdetails tbody tr").length;
	if (rowcount < 30) {
		if (document.getElementById('retentionmoneydeductionrow') != null) {
			addRow('tblretentionmoneydeductioncreditdetails','retentionmoneydeductionrow');
		}
	} else {
		  bootbox.alert('limit reached!');
	}
}


function getRow(obj) {
	if(!obj)return null;
	tag = obj.nodeName.toUpperCase();
	while(tag != 'BODY'){
		if (tag == 'TR') return obj;
		obj=obj.parentNode ;
		tag = obj.nodeName.toUpperCase();
	}
	return null;
}

function deleteStatutoryDeductionRow(obj) {
	var rowcount=$("#tblstatutorydeductioncreditdetails tbody tr").length;
    if(rowcount<=1) {
		bootbox.alert("This row can not be deleted");
		return false;
	} else {
		deleteRow(obj,'tblstatutorydeductioncreditdetails');
		calculateNetPayableAmount();
		return true;
	}	
}

function deleteOtherDeductionRow(obj) {
	var rowcount=$("#tblotherdeductioncreditdetails tbody tr").length;
    if(rowcount<=1) {
		bootbox.alert("This row can not be deleted");
		return false;
	} else {
		deleteRow(obj,'tblotherdeductioncreditdetails');
		calculateNetPayableAmount();
		return true;
	}	
}

function deleteRetentionMoneyDeductionRow(obj) {
	var rowcount=$("#tblretentionmoneydeductioncreditdetails tbody tr").length;
    if(rowcount<=1) {
		bootbox.alert("This row can not be deleted");
		return false;
	} else {
		deleteRow(obj,'tblretentionmoneydeductioncreditdetails');
		calculateNetPayableAmount();
		return true;
	}	
}

function calculateNetPayableAmount(){
	validateAmount();
	var debitAmount = $('#debitamount').val();
	var totalDeductionAmount = 0;
	$( "input[name$='creditamount']" ).each(function(){
		totalDeductionAmount = totalDeductionAmount + parseFloat(($(this).val()?$(this).val():"0"));
	});
	$('#netPayableAmount').val(roundTo(debitAmount-totalDeductionAmount));
	validateNetPayableAmount();
}	

function validateMBPageNumbers() { 
	if(($('#fromPageNo').val()!==undefined && $('#toPageNo').val()!==undefined && $('#fromPageNo').val() != '' && $('#fromPageNo').val() == 0) || ($('#toPageNo').val() != '' && $('#toPageNo').val() == 0)) {
		bootbox.alert("MB Page Numbers should be greater than Zero!");
		return false;
	}
	if($('#fromPageNo').val()!==undefined && $('#toPageNo').val()!==undefined && $('#fromPageNo').val() != '' && $('#toPageNo').val() != '' && eval($('#fromPageNo').val()) > eval($('#toPageNo').val())) {
		bootbox.alert("MB From Page Number cannot be greater than MB To Page Number!");
		return false;
	}
	return true;
}

function validateNetPayableAmount() {
	if($('#debitamount').val() != '' && $('#netPayableAmount').val() < 0) {
		bootbox.alert("Net Payable amount cannot be less than zero!");
		return false;
	}
	return true;
}

function validateAmount() {
	$( "input[name$='debitamount']" ).on("keyup", function(){
	    var valid = /^[1-9](\d{0,9})(\.\d{0,2})?$/.test(this.value),
	        val = this.value;
	    
	    if(!valid){
	        console.log("Invalid input!");
	        this.value = val.substring(0, val.length - 1);
	    }
	});
	$( "input[name$='creditamount']" ).on("keyup", function(){
	    var valid = /^[1-9](\d{0,9})(\.\d{0,2})?$/.test(this.value),
	        val = this.value;
	    
	    if(!valid){
	        console.log("Invalid input!");
	        this.value = val.substring(0, val.length - 1);
	    }
	});
}

function roundTo(value, decimals, decimal_padding) {
	if (!decimals)
		decimals = 2;
	if (!decimal_padding)
		decimal_padding = '0';
	if (isNaN(value))
		value = 0;
	value = Math.round(value * Math.pow(10, decimals));
	var stringValue = (value / Math.pow(10, decimals)).toString();
	var padding = 0;
	var parts = stringValue.split(".");
	if (parts.length == 1) {
		padding = decimals;
		stringValue += ".";
	} else
		padding = decimals - parts[1].length;
	for (var i = 0; i < padding; i++) {
		stringValue += decimal_padding;
	}
	return stringValue;
}


function otherDeductionCreditGlcode_initialize() {
	 var custom = new Bloodhound({
	    datumTokenizer: function(d) { return d.tokens; },
	    queryTokenizer: Bloodhound.tokenizers.whitespace,
		   remote: {
	            url: '/egworks/contractorbill/ajaxotherdeduction-coa?searchQuery=%QUERY',
	            filter: function (data) {
	                return $.map(data, function (ct) {
	                    return {
	                        id: ct.id,
	                        name: ct.name,
	                        glcode: ct.glcode,
	                        glcodesearch: ct.glcode+' ~ '+ct.name
	                    };
	                });
	            }
	        }
    });

    custom.initialize();

    $('.otherDeductionCreditGlcode').typeahead({
    	hint : true,
		highlight : true,
		minLength : 3
		
	}, {		    
          displayKey: 'glcodesearch',
          source: custom.ttAdapter()
    }).on('typeahead:selected typeahead:autocompleted', function (event, data) {
    	$(this).parents("tr:first").find('.otherdeductionname').val(data.name);
    	$(this).parents("tr:first").find('.otherdeductionid').val(data.id);    
    });
}

function resetCreditAccountDetails(obj){
	if(obj.value=='') {
		$(obj).parents("tr:first").find('.otherdeductionid').val('');
		$(obj).parents("tr:first").find('.otherdeductionname').val('');
	}
}

function renderPDF(){
	var id = $('#id').val();
	window.open("/egworks/contractorbill/contractorbillPDF/" + id, '', 'height=650,width=980,scrollbars=yes,left=0,top=0,status=yes');
}

function viewMB(id) {
	window.open("/egworks/mb/view/" + id, '', 'height=650,width=980,scrollbars=yes,left=0,top=0,status=yes');
}


function addRow(tableName,rowName) { 
	
	var rowcount = $("#"+tableName+" tbody tr").length;
	if (rowcount < 30) {
		if (document.getElementById(rowName) != null) {
			// get Next Row Index to Generate
			var nextIdx = 0;
				nextIdx = $("#"+tableName+" tbody tr").length;
			// validate status variable for exiting function
			var isValid = 1;// for default have success value 0

			// validate existing rows in table
			$("#"+tableName+" tbody tr").find('input,select').each(
					function() {
						if (($(this).data('optional') === 0)
								&& (!$(this).val()) && !($(this).attr('name')===undefined)) { 
							$(this).focus();
							bootbox.alert($(this).data('errormsg'));
							isValid = 0;// set validation failure
							return false;
						}
			});
			if (isValid === 0) {
				return false;
			}
			
			// Generate all textboxes Id and name with new index
			$("#"+rowName+"").clone().find("input,select,errors").each(
			function() {	
				if ($(this).data('server')) {
					$(this).removeAttr('data-server');
				}
				
				$(this).attr(
						{
							'name' : function(_, name) {
								if(!($(this).attr('name')===undefined))
									return name.replace(/\d+/, nextIdx); 
							},
							'id' : function(_, id) {
								if(!($(this).attr('id')===undefined))
									return id.replace(/\d+/, nextIdx); 
							},
							'data-idx' : function(_,dataIdx)
							{
								return nextIdx;
							}
						});
	
					// if element is static attribute hold values for next row, otherwise it will be reset
					if (!$(this).data('static')) {
						$(this).val('');
					}
	
			}).end().appendTo("#"+tableName+" tbody");		
			
		}
	} else {
		  bootbox.alert('limit reached!');
	}
}


function deleteRow(obj,tableName) {
    var rIndex = getRow(obj).rowIndex;
    
    var id = $(getRow(obj)).children('td:first').children('input:first').val();
    //To get all the deleted rows id
    var aIndex = rIndex - 1;
	var tbl=document.getElementById(tableName);	
	var rowcount=$("#"+tableName+" tbody tr").length;
    if(rowcount<=1) {
		bootbox.alert("This row can not be deleted");
		return false;
	} else {
		tbl.deleteRow(rIndex);
		//starting index for table fields
		var idx=parseInt($detailsRowCount);
		//regenerate index existing inputs in table row
		$("#"+tableName+" tbody tr").each(function() {
		
			hiddenElem=$(this).find("input:hidden");
			
			if(!$(hiddenElem).val())
			{
				$(this).find("input, errors").each(function() {
					   $(this).attr({
					      'name': function(_, name) {
					    	  if(!($(this).attr('name')===undefined))
					    		  return name.replace(/\[.\]/g, '['+ idx +']'); 
					      },
					      'id': function(_, id) {
					    	  if(!($(this).attr('id')===undefined))
					    		  return id.replace(/\[.\]/g, '['+ idx +']'); 
					      },
						  'data-idx' : function(_,dataIdx)
						  {
							  return idx;
						  }
					   });
			    });
				idx++;
			}
		});
		return true;
	}	
}

function calculateRetentionMoneyDeductionAmounts()
{
	var retentionMoneyPerForPartBill = $("#retentionMoneyPerForPartBill").val();
	var retentionMoneyPerForFinalBill = $("#retentionMoneyPerForFinalBill").val();
	var debitAmount = $("#debitamount").val();
	var billType = $("#billtype").val();
	var resultLength = $('#tblretentionmoneydeductioncreditdetails tr').length - 1;
	var index;
	for (var i = 1; i <= resultLength; i++) {
		index = i - 1;
		var accountcode = document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditGlcode").value;
		if(accountcode!=""){
			if(debitAmount=="")
				document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditamount").value = "";
			if(debitAmount!="" && billType!="" && billType == 'Part Bill')
				document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditamount").value = parseFloat((parseFloat(debitAmount)*parseFloat(retentionMoneyPerForPartBill))/100);
			else if(debitAmount !="" && billType!="" && billType == 'Final Bill')
				document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditamount").value = parseFloat((parseFloat(debitAmount)*parseFloat(retentionMoneyPerForFinalBill))/100);
		}else
			document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditamount").value = "";
	}
	
}

function calculateRetentionMoneyDeductionAmount(obj)
{
	var retentionMoneyPerForPartBill = $("#retentionMoneyPerForPartBill").val();
	var retentionMoneyPerForFinalBill = $("#retentionMoneyPerForFinalBill").val();
	var debitAmount = $("#debitamount").val();
	var rIndex = getRow(obj).rowIndex;
	rIndex--;
	var billType = $("#billtype").val();
	if(document.getElementById("retentionMoneyDeductionDetailes["+rIndex+"].creditGlcode").value !=""){
		if(debitAmount=="")
			document.getElementById("retentionMoneyDeductionDetailes["+index+"].creditamount").value = "";
		if(debitAmount!="" && billType!="" && billType == 'Part Bill')
			document.getElementById("retentionMoneyDeductionDetailes["+rIndex+"].creditamount").value = parseFloat((parseFloat(debitAmount)*parseFloat(retentionMoneyPerForPartBill))/100);
		else if(debitAmount!="" &&  billType!="" && billType == 'Final Bill')
			document.getElementById("retentionMoneyDeductionDetailes["+rIndex+"].creditamount").value = parseFloat((parseFloat(debitAmount)*parseFloat(retentionMoneyPerForFinalBill))/100);
	}else
		document.getElementById("retentionMoneyDeductionDetailes["+rIndex+"].creditamount").value ="";
	
}

function validateDeductionGrids(){
	var flag = true;
	//Validating statutory deduction grid
	var statutoryDeductionTblLength = $('#tblstatutorydeductioncreditdetails tr').length - 1;
	var index;
	for (var i = 1; i <= statutoryDeductionTblLength; i++) {
		index = i - 1;
		var accountCode = document.getElementById('statutoryDeductionDetailes[' + index + '].creditGlcode').value;

		var amount = document.getElementById('statutoryDeductionDetailes[' + index + '].creditamount').value;
		if(accountCode!="" && amount==""){
			bootbox.alert("Credit Amount is mandatory for statutory deduction row: " +i );
			return false;
		}
		
	}
	
	//Validating other deduction grid
	var statutoryDeductionTblLength = $('#tblotherdeductioncreditdetails tr').length - 1;
	var index;
	for (var i = 1; i <= statutoryDeductionTblLength; i++) {
		index = i - 1;
		var accountCode = document.getElementById('otherDeductionDetailes[' + index + '].glcodeid').value;

		var amount = document.getElementById('otherDeductionDetailes[' + index + '].creditamount').value;
		if(accountCode!="" && amount==""){
			bootbox.alert("Credit Amount is mandatory for other deduction row: " +i );
			return false;
		}
		
	}
	
	//Validating retention deduction grid
	var statutoryDeductionTblLength = $('#tblretentionmoneydeductioncreditdetails tr').length - 1;
	var index;
	for (var i = 1; i <= statutoryDeductionTblLength; i++) {
		index = i - 1;
		var accountCode = document.getElementById('retentionMoneyDeductionDetailes[' + index + '].creditGlcode').value;

		var amount = document.getElementById('retentionMoneyDeductionDetailes[' + index + '].creditamount').value;
		if(accountCode!="" && amount==""){
			bootbox.alert("Credit Amount is mandatory for retention deduction row: " +i );
			return false;
		}
		
	}
	
	return true;
}