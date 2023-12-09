package com.saulpos.model.report;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.Report;
import com.saulpos.model.bean.ReportColumn;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

public class DynamicReportsModel extends AbstractReportModel{

    public DynamicReportsModel(Report report, AbstractDataProvider dataProvider) {
        super(report, dataProvider);
    }

    @Override
    public void run() {
        try {
            JasperReportBuilder report = report();
            report.setPageFormat(PageType.LETTER, Report.Orientation.PORTRAIT.equals(this.report.getOrientation()) ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE);
            report.columns(columns());
            report.highlightDetailOddRows();
            report.title(Templates.createTitleComponent(this.report.getTitle()));
            report.setDataSource(getDataSource());
            JasperViewer jasperViewer = new JasperViewer(report.toJasperPrint(), false);
            jasperViewer.setVisible(true);
        }catch (Exception e){
            DialogBuilder.createExceptionDialog("Error", "Error generating the report", e.getMessage(), e).showAndWait();
        }
    }

    private ColumnBuilder[] columns() throws DRException {
        ArrayList<ColumnBuilder> answer = new ArrayList<>();
        ArrayList<ReportColumn> columns = new ArrayList<>(this.report.getColumns());
        Collections.sort(columns);
        for (ReportColumn column : columns){
            answer.add(col.column(column.getName(), column.getSqlName(), (DRIDataType<? super Object, Object>) type.detectType(column.getColumnType().toString())));
        }

        return answer.toArray(new ColumnBuilder[0]);
    }

    private JRDataSource getDataSource() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

        ArrayList<ReportColumn> columns = new ArrayList<>(this.report.getColumns());
        Collections.sort(columns);

        DRDataSource dataSource = new DRDataSource(columns.stream().map(ReportColumn::getSqlName).toArray(String[]::new));

        List<Object[]> items = this.getDataProvider().getItems(this.report.getQuery());

        for (Object[] row : items){
            dataSource.add(row);
        }

        return dataSource;
    }

}
