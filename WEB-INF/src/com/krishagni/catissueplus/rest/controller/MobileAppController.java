package com.krishagni.catissueplus.rest.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.krishagni.catissueplus.core.biospecimen.events.SpecimenInfo;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenListCriteria;
import com.krishagni.catissueplus.core.biospecimen.services.MobileAppService;
import com.krishagni.catissueplus.core.biospecimen.services.impl.MobileAppServiceImpl;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.common.util.ConfigUtil;
import com.krishagni.catissueplus.core.de.events.FormDataDetail;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.napi.FormData;
import edu.common.dynamicextensions.nutility.ContainerJsonSerializer;
import edu.common.dynamicextensions.nutility.ContainerSerializer;
import edu.common.dynamicextensions.nutility.IoUtil;

@Controller
@RequestMapping("/mobile-app")
public class MobileAppController {

	private MobileAppService mobileAppSvc = new MobileAppServiceImpl();

	@RequestMapping(method = RequestMethod.GET, value = "/collection-protocol")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, Object> getCollectionProtocol(
		@RequestParam(value = "shortTitle")
		String shortTitle) {
		return ResponseEvent.unwrap(mobileAppSvc.getCpDetail(RequestEvent.wrap(shortTitle)));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/form")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void getForm(
		@RequestParam(value = "cpId")
		Long cpId,

		@RequestParam(value = "entity")
		String entity,

		@RequestParam(value = "viewForm")
		String viewForm,

		@RequestParam(value = "maxPvs", required = false, defaultValue = "0")
		int maxPvListSize,

		HttpServletResponse httpResp)
		throws IOException {

		Map<String, Object> params = new HashMap<>();
		params.put("cpId", cpId);
		params.put("entity", entity);
		params.put("viewForm", viewForm);

		ResponseEvent<Container> resp = mobileAppSvc.getForm(RequestEvent.wrap(params));
		Container form = ResponseEvent.unwrap(resp);
		sendForm(httpResp, form, maxPvListSize);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/form-data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, Object> saveFormData(@RequestBody Map<String, Object> valueMap) {
		Map<String, Object> appData = (Map<String, Object>) valueMap.get("appData");
		if (!valueMap.containsKey("id")) {
			valueMap.put("id", appData.get("recordId"));
		}

		String formName = (String) appData.get("formName");
		Container form = Container.getContainer(formName);
		FormData formData = FormData.getFormData(form, valueMap, true, null);

		FormDataDetail detail = new FormDataDetail();
		detail.setFormId(form.getId());
		detail.setFormData(formData);
		detail.setRecordId(formData.getRecordId());
		return ResponseEvent.unwrap(mobileAppSvc.saveFormData(RequestEvent.wrap(detail)));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/form-data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, Object> getFormData(
		@RequestParam(value = "action")
		String action,

		@RequestParam
		Map<String, String> input) {

		input.put("action", action);
		return ResponseEvent.unwrap(mobileAppSvc.getFormData(RequestEvent.wrap(input)));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/specimens")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<? extends SpecimenInfo> getSpecimens(
		@RequestParam(value = "cpId")
		Long cpId,

		@RequestParam(value = "cprId")
		Long cprId,

		@RequestParam(value = "label", required = false)
		String label) {

		SpecimenListCriteria crit = new SpecimenListCriteria()
			.cpId(cpId).cprId(cprId)
			.lineages(new String[] { "New"})
			.startAt(0)
			.maxResults(100)
			.limitItems(true);

		if (label != null) {
			crit.labels(Collections.singletonList(label)).exactMatch(false);
		}

		return ResponseEvent.unwrap(mobileAppSvc.getSpecimens(RequestEvent.wrap(crit)));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/child-specimens")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<? extends SpecimenInfo> getSpecimens(
		@RequestParam(value = "parentSpecimenId")
		Long parentId,

		@RequestParam(value = "label", required = false)
		String label) {

		SpecimenListCriteria crit = new SpecimenListCriteria()
			.ancestorId(parentId)
			.lineages(new String[] { "Aliquot"})
			.startAt(0)
			.maxResults(100)
			.limitItems(true);

		if (label != null) {
			crit.labels(Collections.singletonList(label)).exactMatch(false);
		}

		return ResponseEvent.unwrap(mobileAppSvc.getSpecimens(RequestEvent.wrap(crit)));
	}


	@RequestMapping(method = RequestMethod.POST, value = "/{cpId}/upload-data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, Object> uploadData(
		@PathVariable("cpId")
		Long cpId,

		@PathVariable("file") MultipartFile file)
	throws IOException {
		OutputStream out = null;
		try {
			File mobileAppData = new File(ConfigUtil.getInstance().getDataDir(), "mobile-app");
			if (!mobileAppData.exists()) {
				mobileAppData.mkdirs();
			}

			File uploadedFile = new File(mobileAppData, UUID.randomUUID().toString());
			out = new FileOutputStream(uploadedFile);
			IoUtil.copy(file.getInputStream(), out);

			Map<String, Object> params = new HashMap<>();
			params.put("cpId", cpId);
			params.put("file", uploadedFile);
			return ResponseEvent.unwrap(mobileAppSvc.uploadData(RequestEvent.wrap(params)));
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private void sendForm(HttpServletResponse httpResp, Container form, int maxPvListSize)
	throws IOException {
		httpResp.setCharacterEncoding("UTF-8");
		Writer writer = httpResp.getWriter();

		ContainerSerializer serializer = new ContainerJsonSerializer(form, writer);
		serializer.serialize(maxPvListSize);
		writer.flush();
	}
}
