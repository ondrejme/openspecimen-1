package com.krishagni.catissueplus.core.common.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.util.ReflectionUtils;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.util.MessageUtil;
import com.krishagni.catissueplus.core.common.util.Utility;

public abstract class LabelPrintRule {
	public enum CmdFileFmt {
		CSV("csv"),
		KEY_VALUE("key-value");

		private String fmt;

		CmdFileFmt(String fmt) {
			this.fmt = fmt;
		}

		public static CmdFileFmt get(String input) {
			for (CmdFileFmt cfFmt : values()) {
				if (cfFmt.fmt.equals(input)) {
					return cfFmt;
				}
			}

			return null;
		}
	};

	private String labelType;
	
	private IpAddressMatcher ipAddressMatcher;

	private List<User> users = new ArrayList<>();
	
	private String printerName;
	
	private String cmdFilesDir;

	private String labelDesign;

	private List<LabelTmplToken> dataTokens = new ArrayList<>();
	
	private CmdFileFmt cmdFileFmt = CmdFileFmt.KEY_VALUE;

	public String getLabelType() {
		return labelType;
	}

	public void setLabelType(String labelType) {
		this.labelType = labelType;
	}

	public IpAddressMatcher getIpAddressMatcher() {
		return ipAddressMatcher;
	}

	public void setIpAddressMatcher(IpAddressMatcher ipAddressMatcher) {
		this.ipAddressMatcher = ipAddressMatcher;
	}


	public void setUserLogin(User user) {
		users = new ArrayList<>();
		users.add(user);
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getCmdFilesDir() {
		return cmdFilesDir;
	}

	public void setCmdFilesDir(String cmdFilesDir) {
		this.cmdFilesDir = cmdFilesDir;
	}

	public String getLabelDesign() {
		return labelDesign;
	}

	public void setLabelDesign(String labelDesign) {
		this.labelDesign = labelDesign;
	}

	public List<LabelTmplToken> getDataTokens() {
		return dataTokens;
	}

	public void setDataTokens(List<LabelTmplToken> dataTokens) {
		this.dataTokens = dataTokens;
	}

	public CmdFileFmt getCmdFileFmt() {
		return cmdFileFmt;
	}

	public void setCmdFileFmt(CmdFileFmt cmdFileFmt) {
		this.cmdFileFmt = cmdFileFmt;
	}

	public void setCmdFileFmt(String fmt) {
		this.cmdFileFmt = CmdFileFmt.get(fmt);
		if (this.cmdFileFmt == null) {
			throw new IllegalArgumentException("Invalid command file format: " + fmt);
		}
	}

	public boolean isApplicableFor(User user, String ipAddr) {
		if (CollectionUtils.isNotEmpty(users) && !users.stream().anyMatch(u -> u.equals(user))) {
			return false;
		}

		if (ipAddressMatcher != null && !ipAddressMatcher.matches(ipAddr)) {
			return false;
		}
		
		return true;
	}
	
	public Map<String, String> getDataItems(PrintItem<?> printItem) {
		try {
			Map<String, String> dataItems = new LinkedHashMap<>();


			if (!isWildCard(labelDesign)) {
				dataItems.put(getMessageStr("LABELDESIGN"), labelDesign);
			}

			if (!isWildCard(labelType)) {
				dataItems.put(getMessageStr("LABELTYPE"), labelType);
			}

			if (!isWildCard(printerName)) {
				dataItems.put(getMessageStr("PRINTER"), printerName);
			}
			
			for (LabelTmplToken token : dataTokens) {
				dataItems.put(getMessageStr(token.getName()), token.getReplacement(printItem.getObject()));
			}

			return dataItems;
		} catch (Exception e) {
			throw OpenSpecimenException.serverError(e);
		}
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("label design = ").append(getLabelDesign())
			.append(", label type = ").append(getLabelType())
			.append(", user = ").append(getUsersList(true))
			.append(", printer = ").append(getPrinterName());

		String tokens = getDataTokens().stream()
			.map(token -> token.getName())
			.collect(Collectors.joining(";"));
		result.append(", tokens = ").append(tokens);
		return result.toString();
	}

	public Map<String, String> toDefMap() {
		return toDefMap(false);
	}

	public Map<String, String> toDefMap(boolean ufn) {
		try {
			Map<String, String> rule = new HashMap<>();
			rule.put("labelType", getLabelType());
			rule.put("ipAddressMatcher", getIpAddressRange(getIpAddressMatcher()));
			rule.put("users", getUsersList(ufn));
			rule.put("printerName", getPrinterName());
			rule.put("cmdFilesDir", getCmdFilesDir());
			rule.put("labelDesign", getLabelDesign());
			rule.put("dataTokens", getTokenNames());
			rule.put("cmdFileFmt", getCmdFileFmt().fmt);
			rule.putAll(getDefMap(ufn));
			return rule;
		} catch (Exception e) {
			throw new RuntimeException("Error in creating map from print rule ", e);
		}
	}

	protected abstract Map<String, String> getDefMap(boolean ufn);

	protected boolean isWildCard(String str) {
		return StringUtils.isBlank(str) || str.trim().equals("*");
	}

	private String getMessageStr(String name) {
		return MessageUtil.getInstance().getMessage("print_" + name, null);
	}

	private String getTokenNames() {
		return dataTokens.stream().map(LabelTmplToken::getName).collect(Collectors.joining(","));
	}

	private String getIpAddressRange(IpAddressMatcher ipRange) {
		if (ipRange == null) {
			return null;
		}

		String address = getFieldValue(ipAddressMatcher, "requiredAddress").toString();
		address = address.substring(address.indexOf("/") + 1);

		int maskBits = getFieldValue(ipAddressMatcher, "nMaskBits");
		return address + "/" + maskBits;
	}

	private <T> T getFieldValue(Object obj, String fieldName) {
		Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
		field.setAccessible(true);
		return (T)ReflectionUtils.getField(field, obj);
	}

	private String getUsersList(boolean ufn) {
		Function<User, String> mapper = ufn ? (u) -> u.getLoginName() : (u) -> u.getId().toString();
		return Utility.nullSafeStream(getUsers()).map(mapper).collect(Collectors.joining(","));
	}
}
