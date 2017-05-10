package com.fr.bi.field.dimension.dimension;

import com.finebi.cube.api.BICubeManager;
import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.field.dimension.calculator.NumberDimensionCalculator;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.operation.group.BIGroupFactory;
import com.fr.bi.stable.operation.group.group.AutoGroup;
import com.fr.bi.stable.operation.sort.BISortFactory;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.utils.BICollectionUtils;
import com.fr.general.GeneralUtils;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StableUtils;

import java.util.List;

public class BINumberDimension extends BIAbstractDimension {

    private static final long serialVersionUID = 5523315852419800406L;

    public BINumberDimension() {
    }

    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        super.parseJSON(jo, userId);
        JSONObject group = jo.optJSONObject("group");
        if (group == null || !group.has("type")) {
            group = new JSONObject().put("type", BIReportConstant.GROUP.AUTO_GROUP);
        }
        JSONObject sort = jo.optJSONObject("sort");
        if (sort == null || !sort.has("type")) {
            sort = new JSONObject().put("type", BIReportConstant.SORT.CUSTOM);
        }
        boolean needCreateSort = false;
        if(group.getInt("type") == BIReportConstant.GROUP.AUTO_GROUP
                && sort.getInt("type") == BIReportConstant.SORT.CUSTOM){
            if(!sort.has("details")){
                formatAutoGroupJSON(group, userId);
                needCreateSort = true;
            }
        }
        this.group = BIGroupFactory.parseNumberGroup(group);
        if(needCreateSort == true){
            sort.put("details", ((AutoGroup)this.group).getAllAutoGroupNames());
        }
        this.sort = BISortFactory.parseSort(sort);
    }

    private void formatAutoGroupJSON(JSONObject group, long userId) throws JSONException {
        JSONObject groupValue = group.optJSONObject("groupValue");
        if(groupValue == null){
            groupValue = JSONObject.create();
        }
        ICubeDataLoader loader = BICubeManager.getInstance().fetchCubeLoader(userId);
        ICubeTableService ti = loader.getTableIndex(this.getStatisticElement().getTableBelongTo().getTableSource());
        ICubeColumnIndexReader reader = ti.loadGroup(new IndexKey(this.getStatisticElement().getFieldName()));
        groupValue.put(BIJSONConstant.JSON_KEYS.FILED_MAX_VALUE, ti != null ?  GeneralUtils.objectToNumber(BICollectionUtils.lastUnNullKey(reader)) : 0);
        groupValue.put(BIJSONConstant.JSON_KEYS.FIELD_MIN_VALUE, ti != null ? GeneralUtils.objectToNumber(BICollectionUtils.firstUnNullKey(reader)) : 0);
        group.put("groupValue", groupValue);
    }

    @Override
    public String toString(Object v) {
        if (v instanceof Double) {
            return StableUtils.convertNumberStringToString(((Double) v).doubleValue());
        }
        return super.toString(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BINumberDimension)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        return true;
    }

    @Override
    public DimensionCalculator createCalculator(BusinessField column, List<BITableSourceRelation> relations) {
        return new NumberDimensionCalculator(this, column, relations);
    }

    @Override
    public DimensionCalculator createCalculator(BusinessField column, List<BITableSourceRelation> relations, List<BITableSourceRelation> directToDimensionRelations) {
        return new NumberDimensionCalculator(this, column, relations, directToDimensionRelations);
    }


    @Override
    public Object getValueByType(Object data) {
        if (group.getType() == BIReportConstant.GROUP.CUSTOM_NUMBER_GROUP) {
            return data == null ? null : data.toString();
        }
        return data;
    }
}