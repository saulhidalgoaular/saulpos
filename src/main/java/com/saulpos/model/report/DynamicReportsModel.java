package com.saulpos.model.report;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.bean.Report;
import com.saulpos.model.bean.ReportColumn;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

public class DynamicReportsModel extends AbstractReportModel{

    public DynamicReportsModel(Report report, AbstractDataProvider dataProvider) {
        super(report, dataProvider);
    }

    @Override
    public void run() {
        JasperReportBuilder report = report();
        report.setPageFormat(PageType.LETTER, Report.Orientation.PORTRAIT.equals(this.report.getOrientation()) ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE);
        report.columns(columns());
        report.setDataSource(sampleDataSource());
        try {
            JasperViewer jasperViewer = new JasperViewer(report.toJasperPrint(), false);
            jasperViewer.setVisible(true);
        } catch (DRException e) {
            throw new RuntimeException(e);
        }
    }

    private ColumnBuilder[] columns(){
        ArrayList<ColumnBuilder> answer = new ArrayList<>();
        ArrayList<ReportColumn> columns = new ArrayList<>(this.report.getColumns());
        Collections.sort(columns);
        for (ReportColumn column : columns){
            if (ReportColumn.Type.StringType.equals(column.getColumnType())){
                answer.add(col.column(column.getName(), column.getSqlName(), type.stringType()));
            }
            if (ReportColumn.Type.BigDecimalType.equals(column.getColumnType())){
                answer.add(col.column(column.getName(), column.getSqlName(), type.bigDecimalType()));
            }
            if (ReportColumn.Type.IntegerType.equals(column.getColumnType())){
                answer.add(col.column(column.getName(), column.getSqlName(), type.integerType()));
            }
            if (ReportColumn.Type.DateType.equals(column.getColumnType())){
                answer.add(col.column(column.getName(), column.getSqlName(), type.dateType()));
            }
        }

        return answer.toArray(new ColumnBuilder[0]);
    }

    private JRDataSource sampleDataSource(){
        DRDataSource dataSource = new DRDataSource("item", "orderdate", "quantity", "unitprice");
        dataSource.add("Notebook", new Date(), 1, new BigDecimal(500));
        return dataSource;
    }
}
