package org.egov.works.web.controller.mb;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.egov.infra.exception.ApplicationException;
import org.egov.works.mb.entity.MBHeader;
import org.egov.works.mb.service.MBHeaderService;
import org.egov.works.models.tender.OfflineStatus;
import org.egov.works.offlinestatus.service.OfflineStatusService;
import org.egov.works.utils.WorksConstants;
import org.egov.works.utils.WorksUtils;
import org.egov.works.web.adaptor.MeasurementBookJsonAdaptor;
import org.egov.works.workorder.entity.WorkOrderEstimate;
import org.egov.works.workorder.entity.WorkOrder.OfflineStatuses;
import org.egov.works.workorder.service.WorkOrderEstimateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@RestController
@RequestMapping(value = "/measurementbook")
public class CreateMBController {

    @Autowired
    private WorkOrderEstimateService workOrderEstimateService;

    @Autowired
    private MeasurementBookJsonAdaptor measurementBookJsonAdaptor;

    @Autowired
    private MBHeaderService mbHeaderService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private WorksUtils worksUtils;
    
    public WorkOrderEstimate getWorkOrderEstimate(final Long workOrderEstimateId) {
        final WorkOrderEstimate workOrderEstimate = workOrderEstimateService.getWorkOrderEstimateById(workOrderEstimateId);
        return workOrderEstimate;
    }

    @RequestMapping(value = "/create/{workOrderEstimateId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String createMeasurementBook(@PathVariable final Long workOrderEstimateId,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        final WorkOrderEstimate workOrderEstimate = getWorkOrderEstimate(workOrderEstimateId);
        final String result = new StringBuilder().append(toSearchMilestoneTemplateJson(workOrderEstimate)).toString();
        return result;
    }

    public Object toSearchMilestoneTemplateJson(final Object object) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(WorkOrderEstimate.class, measurementBookJsonAdaptor).create();
        final String json = gson.toJson(object);
        return json;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody String create(@ModelAttribute("mbHeader") final MBHeader mbHeader,
            final Model model, final BindingResult errors, final HttpServletRequest request, final BindingResult resultBinder,
            final HttpServletResponse response) throws ApplicationException, IOException {

        Long approvalPosition = 0l;
        String approvalComment = "";
        String workFlowAction = "";
        if (request.getParameter("approvalComment") != null)
            approvalComment = request.getParameter("approvalComent");
        if (request.getParameter("workFlowAction") != null)
            workFlowAction = request.getParameter("workFlowAction");
        if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
            approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));

        final JsonObject jsonObject = new JsonObject();
        mbHeaderService.validateMBInDrafts(mbHeader.getWorkOrderEstimate().getId(), jsonObject, errors);
        mbHeaderService.validateMBInWorkFlow(mbHeader.getWorkOrderEstimate().getId(), jsonObject, errors);
        mbHeaderService.validateMBHeader(mbHeader, jsonObject, resultBinder);

        if (jsonObject.toString().length() > 2) {
            sendAJAXResponse(jsonObject.toString(), response);
            return "";
        }

        final MBHeader savedMBHeader = mbHeaderService.create(mbHeader, approvalPosition, approvalComment, workFlowAction);

        mbHeaderService.fillWorkflowData(jsonObject, request, savedMBHeader);

        jsonObject.addProperty("message", messageSource.getMessage("msg.mbheader.saved",
                new String[] { mbHeader.getMbRefNo() },
                null));

        return jsonObject.toString();
    }

    protected void sendAJAXResponse(final String msg, final HttpServletResponse response) {
        try {
            final Writer httpResponseWriter = response.getWriter();
            IOUtils.write(msg, httpResponseWriter);
            IOUtils.closeQuietly(httpResponseWriter);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/mb-success", method = RequestMethod.GET)
    public ModelAndView successView(@ModelAttribute MBHeader mbHeader,
            @RequestParam("mbHeader") Long id, @RequestParam("approvalPosition") final Long approvalPosition,
            final HttpServletRequest request, final Model model) {

        if (id != null)
            mbHeader = mbHeaderService.getMBHeaderById(id);

        final String pathVars = worksUtils.getPathVars(mbHeader.getEgwStatus(), mbHeader.getState(),
                mbHeader.getId(), approvalPosition);

        final String[] keyNameArray = pathVars.split(",");
        String approverName = "";
        String currentUserDesgn = "";
        String nextDesign = "";
        if (keyNameArray.length != 0 && keyNameArray.length > 0)
            if (keyNameArray.length == 1 && keyNameArray[0] != null)
                id = Long.parseLong(keyNameArray[0]);
            else if (keyNameArray.length == 3) {
                if (keyNameArray[0] != null)
                    id = Long.parseLong(keyNameArray[0]);
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
            } else {
                if (keyNameArray[0] != null)
                    id = Long.parseLong(keyNameArray[0]);
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
                nextDesign = keyNameArray[3];
            }

        model.addAttribute("approverName", approverName);
        model.addAttribute("currentUserDesgn", currentUserDesgn);
        model.addAttribute("nextDesign", nextDesign);

        final String message = getMessageByStatus(mbHeader, approverName, nextDesign);

        model.addAttribute("message", message);

        return new ModelAndView("mb-success", "mbHeader", mbHeader);
    }

    private String getMessageByStatus(final MBHeader mbHeader, final String approverName,
            final String nextDesign) {
        String message = "";

        if (mbHeader.getEgwStatus().getCode().equals(MBHeader.MeasurementBookStatus.NEW.toString()))
            message = messageSource.getMessage("msg.mbheader.saved",
                    new String[] { mbHeader.getMbRefNo() }, null);
        else if (mbHeader.getEgwStatus().getCode().equals(MBHeader.MeasurementBookStatus.CREATED.toString())
                && !mbHeader.getState().getValue().equals(WorksConstants.WF_STATE_REJECTED))
            message = messageSource.getMessage("msg.mbheader.created",
                    new String[] { approverName, nextDesign, mbHeader.getMbRefNo() }, null);
        else if (mbHeader.getEgwStatus().getCode().equals(MBHeader.MeasurementBookStatus.APPROVED.toString()))
            message = messageSource.getMessage("msg.mbheader.approved",
                    new String[] { mbHeader.getMbRefNo() }, null);
        else if (mbHeader.getState() != null
                && mbHeader.getState().getValue().equals(WorksConstants.WF_STATE_REJECTED))
            message = messageSource.getMessage("msg.mbheader.rejected",
                    new String[] { mbHeader.getMbRefNo(), approverName, nextDesign }, null);
        else if (mbHeader.getEgwStatus().getCode().equals(MBHeader.MeasurementBookStatus.CANCELLED.toString()))
            message = messageSource.getMessage("msg.mbheader.cancelled",
                    new String[] { mbHeader.getMbRefNo() }, null);

        return message;
    }
}
