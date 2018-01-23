package com.fr.swift.conf.business.table;

import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.fr.swift.conf.business.AbstractSwiftParseXml;
import com.fr.swift.conf.business.SwiftBaseXmlDao;
import com.fr.swift.conf.business.ISwiftXmlWriter;

/**
 * This class created on 2018-1-23 16:44:27
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class SwiftXmlBusinessTableDao extends SwiftBaseXmlDao<FineBusinessTable> {

    public SwiftXmlBusinessTableDao(AbstractSwiftParseXml<FineBusinessTable> xmlHandler, String xmlFileName, ISwiftXmlWriter<FineBusinessTable> swiftXmlWriter) {
        super(xmlHandler, xmlFileName, swiftXmlWriter);
    }
}
