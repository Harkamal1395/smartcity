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
var hint='<a href="#" class="hintanchor" title="@fulldescription@"><i class="fa fa-question-circle" aria-hidden="true"></i></a>';
var isDatepickerOpened=false;

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

function validateQuantityInput(object) {
    var valid = /^[0-9](\d{0,9})(\.\d{0,4})?$/.test($(object).val()),
        val = $(object).val();
    
    if(!valid){
        console.log("Invalid input!");
        $(object).val(val.substring(0, val.length - 1));
    }
}

function calculateActivityAmounts(currentObj) {
	rowcount = $(currentObj).attr('id').split('_').pop();
	var currentQuantity = $(currentObj).val() == "" ? 0 : $(currentObj).val();
	var unitRate = parseFloat($('#unitRate_' + rowcount).val().trim());
	var amountCurrentEntry = parseFloat(parseFloat(unitRate) * parseFloat(currentQuantity)).toFixed(2);
	$('#amount_' + rowcount).html(amountCurrentEntry);
	$('#hiddenAmount_' + rowcount).val(amountCurrentEntry);
	mbTotal();
}

function mbTotal() {
	var total = 0.0;
	var tenderedTotal = 0.0;
	var nonTenderedTotal = 0.0;
	var tenderPercentage = $('#tenderFinalizedPercentage').html();
	$('.tendered').each(function() {
		if($(this).html().trim() != "")
			tenderedTotal = parseFloat(parseFloat(tenderedTotal) + parseFloat($(this).html().replace(',', ''))).toFixed(2);
	});
	tenderedTotal = parseFloat(parseFloat(tenderedTotal) + (parseFloat(tenderedTotal) * parseFloat(tenderPercentage) / 100)).toFixed(2);
	
	$('.nontendered').each(function() {
		if($(this).html().trim() != "")
			nonTenderedTotal = parseFloat(parseFloat(nonTenderedTotal) + parseFloat($(this).html().replace(',', ''))).toFixed(2);
	});
	
	total = parseFloat(parseFloat(nonTenderedTotal) + parseFloat(tenderedTotal)).toFixed(2);
	$('#mbTotal').html(total);
	$('#mbAmount').val(total);
}

function getFormData($form) {
	var unindexed_array = $form.serializeArray();
	var indexed_array = {};

	$.map(unindexed_array, function(n, i) {
		indexed_array[n['name']] = n['value'];
	});

	return indexed_array;
}

$('#btncreatemb').click(function() {
	var loaNumber = $('#workOrderNumber').val();
	if (loaNumber != '') {
		window.location.href = "/egworks/contractorportal/mb/create?loaNumber=" + loaNumber;
	} else
		bootbox.alert('Please enter LOA Number');
});

$(document).ready(function() {
	var workOrderNumber = new Bloodhound({
		datumTokenizer : function(datum) {
			return Bloodhound.tokenizers.whitespace(datum.value);
		},
		queryTokenizer : Bloodhound.tokenizers.whitespace,
		remote : {
			url : '/egworks/contractorportal/mb/ajaxworkorder-mbheader?workOrderNo=%QUERY',
			filter : function(data) {
				return $.map(data, function(ct) {
					return {
						name : ct
					};
				});
			}
		}
	});

	workOrderNumber.initialize();
	var workOrderNumber_typeahead = $('#workOrderNumber').typeahead({
		hint : true,
		highlight : true,
		minLength : 3
	}, {
		displayKey : 'name',
		source : workOrderNumber.ttAdapter()
	});
});