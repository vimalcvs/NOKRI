package com.scriptsbundle.nokri.employeer.jobs.models;

import android.graphics.Bitmap;

/**
 * Created by Glixen Technologies on 05/01/2018.
 */

public class Nokri_InactiveJobsModel {
    private String jobType;
    private String jobTitle;
    private String jobExpire;
    private String jobExpireDate;
    private String jobId;


    private String inavtiveText;
    private String address;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobExpireDate() {
        return jobExpireDate;
    }

    public void setJobExpireDate(String jobExpireDate) {
        this.jobExpireDate = jobExpireDate;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobExpire() {
        return jobExpire;
    }

    public void setJobExpire(String jobExpire) {
        this.jobExpire = jobExpire;
    }


    public String getInavtiveText() {
        return inavtiveText;
    }

    public void setInavtiveText(String inavtiveText) {
        this.inavtiveText = inavtiveText;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
