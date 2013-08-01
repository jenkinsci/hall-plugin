package com.companyline.hall.jenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class HallPublisherDescriptor extends BuildStepDescriptor<Publisher> {
	
	public HallPublisherDescriptor () {
		super (HallPublisher.class);
		load();
	}
	
	@Override
	public String getDisplayName() {
		return "Publish to Hall";
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        save();
        return super.configure(req,formData);
	}

	@Override
	public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> arg0) {
		return true;
	}
	
	@Override
	public HallPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		String apikey = formData.optString("api_key");
		boolean success = formData.optBoolean("publish_success");
		boolean failure = formData.optBoolean("publish_failure");
		boolean unstable = formData.optBoolean("publish_unstable");
		return new HallPublisher(apikey, success, failure, unstable);
	}
}