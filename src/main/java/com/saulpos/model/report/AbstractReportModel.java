package com.saulpos.model.report;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.bean.Report;

public abstract class AbstractReportModel {
    protected Report report;

    private AbstractDataProvider dataProvider;

    public AbstractReportModel(Report report, AbstractDataProvider dataProvider) {
        this.report = report;
        this.dataProvider = dataProvider;
    }

    public abstract void run();

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public AbstractDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(AbstractDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
