package com.fr.bi.cal.analyze.report.report.widget.chart.excelExport.table.summary.build;

import com.fr.bi.cal.analyze.report.report.widget.chart.excelExport.table.summary.basic.BIExcelTableData;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Kary on 2017/2/27.
 */
public class SummaryCrossTableDataBuilder extends TableAbstractDataBuilder {
    public SummaryCrossTableDataBuilder(Map<Integer, List<JSONObject>> dimAndTar, JSONObject dataJSON) throws Exception {
        super(dimAndTar, dataJSON);
    }

    @Override
    public void initAttrs() throws JSONException {
        initAllAttrs();
        refreshDimsInfo();
        //仅有列表头的时候(有指标) 修正数据
        if (this.dimIds.size() == 0 && this.crossDimIds.size() > 0 && this.targetIds.size() > 0) {
            amendment();
        }
    }

    @Override
    public void createHeadersAndItems() throws Exception {
        //正常交叉表
        if (null != dataJSON && dataJSON.has("t")) {
            getNormalCrossTable();
            return;
        }
        //仅有列表头的时候（无指标）
        if (this.dimIds.size() == 0 && this.crossDimIds.size() > 0 && this.targetIds.size() > 0) {
            getNoneTarCrossTable();
            return;
        }
        //无列表头(普通汇总表)
        tableWithoutDims();
    }

    @Override
    public BIExcelTableData createTableData() throws JSONException {
        BIExcelTableData tableDataForExport = new BIExcelTableData(headers, items, crossHeaders, crossItems);
        return tableDataForExport;

    }
    private void tableWithoutDims() throws Exception {
        createTableHeader();
        createTableItems();
    }
}
