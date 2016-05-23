BICst.ETL_FILTER_STRING_COMBO = [[{
    text: BI.i18nText("BI-In"),
    value: BICst.TARGET_FILTER_STRING.BELONG_VALUE
},{
    text: BI.i18nText("BI-Not_In"),
    value: BICst.TARGET_FILTER_STRING.NOT_BELONG_VALUE
}],[{
    text: BI.i18nText("BI-Contain"),
    value: BICst.TARGET_FILTER_STRING.CONTAIN
},{
    text: BI.i18nText("BI-Not_Contain"),
    value: BICst.TARGET_FILTER_STRING.NOT_CONTAIN
}],[{
    text: BI.i18nText("BI-Begin_With"),
    value: BICst.TARGET_FILTER_STRING.BEGIN_WITH
},{
    text: BI.i18nText("BI-Not_Begin_With"),
    value: BICst.TARGET_FILTER_STRING.NOT_BEGIN_WITH
}],[{
    text: BI.i18nText("BI-End_With"),
    value: BICst.TARGET_FILTER_STRING.END_WITH
},{
    text: BI.i18nText("BI-Not_End_With"),
    value: BICst.TARGET_FILTER_STRING.NOT_END_WITH
}] ,[{
    text: BI.i18nText("BI-Formula"),
    value: BICst.FILTER_TYPE.FORMULA
}]];

BICst.ETL_FILTER_NUMBER_COMBO = [[{
    text: BI.i18nText("BI-ETL_Filter_Belongs"),
    value: BICst.TARGET_FILTER_NUMBER.CONTAINS
}],[{
    text: BI.i18nText("BI-ETL_Number_IN"),
    value: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE
}, {
    text: BI.i18nText("BI-Not") + BI.i18nText("BI-ETL_Number_IN"),
    value: BICst.TARGET_FILTER_NUMBER.NOT_BELONG_VALUE
}],[{
    text: BI.i18nText("BI-Equal"),
    value: BICst.TARGET_FILTER_NUMBER.EQUAL_TO
}, {
    text: BI.i18nText("BI-Not_Equal_To"),
    value: BICst.TARGET_FILTER_NUMBER.NOT_EQUAL_TO
}],[{
    text: BI.i18nText("BI-More_Than") + '/' + BI.i18nText("BI-More_Than_And_Equal"),
    value: BICst.TARGET_FILTER_NUMBER.LARGE_OR_EQUAL_CAL_LINE
}, {
    text: BI.i18nText("BI-Less_Than") + '/' + BI.i18nText("BI-Less_And_Equal"),
    value: BICst.TARGET_FILTER_NUMBER.SMALL_OR_EQUAL_CAL_LINE
}],[{
    text: BI.i18nText("BI-ETL_Top_N", 'N'),
    value: BICst.TARGET_FILTER_NUMBER.TOP_N
}, {
    text: BI.i18nText("BI-ETL_Bottom_N", 'N'),
    value: BICst.TARGET_FILTER_NUMBER.BOTTOM_N
}],[{
    text: BI.i18nText("BI-Formula"),
    value: BICst.FILTER_TYPE.FORMULA
}]];

BICst.ETL_FILTER_NUMBER_VALUE = {};
BICst.ETL_FILTER_NUMBER_VALUE.SETTED = 1;
BICst.ETL_FILTER_NUMBER_VALUE.AVG = 2;
BICst.ETL_FILTER_NUMBER_SEGMENT = [{
    text: BI.i18nText("BI-Setted_Value"),
    value: BICst.ETL_FILTER_NUMBER_VALUE.SETTED
},{
    text: BI.i18nText("BI-Average_Value"),
    value: BICst.ETL_FILTER_NUMBER_VALUE.AVG
}]

BICst.ETL_FILTER_NUMBER_AVG_TYPE = {};
BICst.ETL_FILTER_NUMBER_AVG_TYPE.ALL = 1;
BICst.ETL_FILTER_NUMBER_AVG_TYPE.INNER_GROUP = 2;
BICst.ETL_FILTER_NUMBER_AVG_ITEMS = [{
    text: BI.i18nText("BI-ETL_Number_Avg_All"),
    value: BICst.ETL_FILTER_NUMBER_AVG_TYPE.ALL
},{
    text: BI.i18nText("BI-ETL_Number_Avg_Inner"),
    value: BICst.ETL_FILTER_NUMBER_AVG_TYPE.INNER_GROUP
}]

BICst.ETL_FILTER_NUMBER_N_TYPE = {};
BICst.ETL_FILTER_NUMBER_N_TYPE.ALL = 1;
BICst.ETL_FILTER_NUMBER_N_TYPE.INNER_GROUP = 2;
BICst.ETL_FILTER_NUMBER_N_ITEMS = [{
    text: BI.i18nText("BI-ETL_Number_N_All"),
    value: BICst.ETL_FILTER_NUMBER_N_TYPE.ALL
},{
    text: BI.i18nText("BI-ETL_Number_N_Inner"),
    value: BICst.ETL_FILTER_NUMBER_N_TYPE.INNER_GROUP
}]
BICst.ETL_FILTER_DATE_COMBO = [[{
    text: BI.i18nText("BI-ETL_Filter_Belongs"),
    value: BICst.FILTER_DATE.CONTAINS_DAY
}],[{
    text: BI.i18nText("BI-ETL_Date_In_Range"),
    value: BICst.FILTER_DATE.BELONG_DATE_RANGE
}], [{
    text: BI.i18nText("BI-More_Than"),
    value: BICst.FILTER_DATE.MORE_THAN
}, {
    text: BI.i18nText("BI-Less_Than"),
    value: BICst.FILTER_DATE.LESS_THAN
}], [{
    text: BI.i18nText("BI-Equal"),
    value: BICst.FILTER_DATE.EQUAL_TO
}, {
    text: BI.i18nText("BI-Not_Equal_To"),
    value: BICst.FILTER_DATE.NOT_EQUAL_TO
}], [{
    text: BI.i18nText("BI-Formula"),
    value: BICst.FILTER_TYPE.FORMULA
}]];
