package com.companyline.hall.jenkins;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

public class HallPublisher extends Notifier {
	
	public String api_key;
	public boolean publish_failure;
	public boolean publish_success;
	public boolean publish_unstable;
	
    @DataBoundConstructor
    public HallPublisher(String api_key, boolean publish_success, boolean publish_failure, boolean publish_unstable ) {
        this.api_key = api_key;
        this.publish_failure = publish_failure;
        this.publish_success = publish_success;
        this.publish_unstable = publish_unstable;
    }

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}
	
    @Override
    public HallPublisherDescriptor getDescriptor() {
        return (HallPublisherDescriptor)super.getDescriptor();
    }

	@Override
	public boolean perform(@SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

		String jobName = build.getFullDisplayName();
		String duration = build.getDurationString();
		@SuppressWarnings("deprecation")
		String buildUrl = build.getAbsoluteUrl();
		
		String result = null;
		if (build.getResult() == Result.SUCCESS) {
			if (!publish_success) {
				return true;
			}
			result = "succeeded";
		} else if (build.getResult() == Result.UNSTABLE) {
			if (!publish_unstable) {
				return true;
			}
			result = "was unstable";
		} else if (build.getResult() == Result.FAILURE) {
			if (!publish_failure) {
				return true;
			}
			result = "failed";
		} else {
			return true;
		}
		
		URL url = new URL(String.format("https://hall.com/api/1/services/zapier/%s", api_key));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		
		String message = String.format("<a target=\"_blank\" href=\"%s\">%s</a> %s in %s", buildUrl, jobName, result, duration);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", message);
		params.put("service_title", "Jenkins");

		OutputStream os = connection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write(getQuery(params));
		writer.close();
		os.close();
		
		connection.getResponseCode();

		return true;
	}
	
	private String getQuery(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (String key : params.keySet()) {
			if (first) {
				first = false;
			} else {
				result.append("&");
			}
			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(params.get(key), "UTF-8"));
		}

		return result.toString();
	}

	@Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }
}
