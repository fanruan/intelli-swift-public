package com.fr.bi.etl.analysis;

import com.fr.bi.stable.constant.BIReportConstant;

/**
 * Created by Daniel on 2016/1/21.
 */
public class Constants {

    public final static String PARENTS ="parents";
    public final static String FIELDS ="etl_fields";
    public final static String ITEMS ="items";
    public final static String PACK_ID ="myetlidbuxudonga";
    public final static String GENERATED_PERCENT ="generated_percent";
    public final static String SYSTEM_TIME = BIReportConstant.SYSTEM_TIME;

    public final static class BUSINESS_TABLE_TYPE {
        public final static int ANALYSIS_TYPE = 0x8;
    }

    public static final class TABLE_TYPE {
        public static final int TEMP = 0x6;

        public static final int BASE = 0x7;

        public static final int ETL = 0x8;

        public static final int USER_BASE = 0x9;

        public static final int USER_ETL = 0xa;

    }

    public static final class ETL_TYPE {
        public static final int SELECT_DATA = 0x1;

        public static final int SELECT_NONE_DATA = 0x2;

        public static final int FILTER = 0x3;

        public static final int GROUP_SUMMARY = 0x4;

        public static final int ADD_COLUMN = 0x5;

        public static final int USE_PART_FIELDS = 0x6;

        public static final int MERGE_SHEET = 0x7;

    }

}